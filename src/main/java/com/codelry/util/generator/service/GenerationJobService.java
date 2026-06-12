package com.codelry.util.generator.service;

import com.codelry.util.generator.driver.Couchbase;
import com.codelry.util.generator.driver.Redis;
import com.codelry.util.generator.dto.EntityCollection;
import com.codelry.util.generator.dto.GenerationJobStatus;
import com.codelry.util.generator.dto.GenerationStatus;
import com.codelry.util.generator.dto.StartGenerationResponse;
import com.codelry.util.generator.generator.EntityLoad;
import com.codelry.util.generator.generator.GenerationCancelledException;
import com.codelry.util.generator.generator.GenerationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class GenerationJobService {
  private static final Logger logger = LoggerFactory.getLogger(GenerationJobService.class);
  private static final long JOB_RETENTION_MS = 60 * 60 * 1000L;

  private final CouchbaseConnectionManager couchbaseConnectionManager;
  private final RedisConnectionManager redisConnectionManager;
  private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
    Thread thread = new Thread(r, "generation-job");
    thread.setDaemon(true);
    return thread;
  });
  private final Map<String, JobState> jobs = new ConcurrentHashMap<>();

  public GenerationJobService(
      CouchbaseConnectionManager couchbaseConnectionManager,
      RedisConnectionManager redisConnectionManager) {
    this.couchbaseConnectionManager = couchbaseConnectionManager;
    this.redisConnectionManager = redisConnectionManager;
  }

  public StartGenerationResponse startCouchbaseJob(String userId, EntityCollection schema) {
    return startJob(userId, schema, () -> {
      Couchbase driver = new Couchbase();
      driver.init(schema, 1);
      driver.connect(couchbaseConnectionManager.getCollection(userId));
      return driver;
    });
  }

  public StartGenerationResponse startRedisJob(String userId, EntityCollection schema) {
    return startJob(userId, schema, () -> {
      ReactiveRedisTemplate<String, String> reactiveTemplate = redisConnectionManager.reactiveRedisTemplate(userId);
      ReactiveRedisJsonTemplate<String, String> reactiveJsonTemplate =
          redisConnectionManager.reactiveRedisJsonTemplate(userId);
      Redis driver = new Redis();
      driver.init(schema, 1);
      driver.connect(reactiveTemplate, reactiveJsonTemplate);
      if (redisConnectionManager.isUseJson(userId)) {
        driver.setUseJson(true);
      }
      return driver;
    });
  }

  private StartGenerationResponse startJob(
      String userId,
      EntityCollection schema,
      DriverFactory driverFactory) {
    purgeExpiredJobs();
    String jobId = UUID.randomUUID().toString();
    long totalRecords = computeTotalRecords(schema);
    JobState job = new JobState(jobId, userId, totalRecords);
    jobs.put(jobId, job);

    executor.submit(() -> runJob(job, driverFactory));
    return new StartGenerationResponse(jobId, GenerationStatus.RUNNING, totalRecords);
  }

  public GenerationJobStatus getJobStatus(String userId, String jobId) {
    JobState job = requireJob(userId, jobId);
    return job.toStatus();
  }

  public GenerationJobStatus cancelJob(String userId, String jobId) {
    JobState job = requireJob(userId, jobId);
    if (job.status == GenerationStatus.RUNNING) {
      job.cancelRequested.set(true);
    }
    return job.toStatus();
  }

  private void runJob(JobState job, DriverFactory driverFactory) {
    EntityLoad driver = null;
    try {
      driver = driverFactory.create();
      driver.generate(job);
      if (job.cancelRequested.get()) {
        job.finish(GenerationStatus.CANCELLED, "Generation cancelled");
      } else {
        job.finish(GenerationStatus.COMPLETED, "Generation completed successfully");
      }
    } catch (GenerationCancelledException e) {
      job.finish(GenerationStatus.CANCELLED, "Generation cancelled");
    } catch (Exception e) {
      logger.error("Generation job {} failed", job.jobId, e);
      job.finish(GenerationStatus.FAILED, e.getMessage() != null ? e.getMessage() : "Generation failed");
    } finally {
      if (driver != null) {
        try {
          driver.cleanup();
        } catch (Exception e) {
          logger.warn("Failed to cleanup driver for job {}", job.jobId, e);
        }
      }
    }
  }

  private JobState requireJob(String userId, String jobId) {
    purgeExpiredJobs();
    JobState job = jobs.get(jobId);
    if (job == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Generation job not found");
    }
    if (!job.userId.equals(userId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Generation job not found");
    }
    return job;
  }

  private long computeTotalRecords(EntityCollection schema) {
    if (schema.getEntities() == null || schema.getEntities().isEmpty()) {
      return 0;
    }
    return schema.getEntities().stream().mapToLong(entity -> entity.getCount()).sum();
  }

  private void purgeExpiredJobs() {
    long cutoff = System.currentTimeMillis() - JOB_RETENTION_MS;
    Iterator<Map.Entry<String, JobState>> iterator = jobs.entrySet().iterator();
    while (iterator.hasNext()) {
      JobState job = iterator.next().getValue();
      if (job.updatedAt < cutoff && job.status != GenerationStatus.RUNNING) {
        iterator.remove();
      }
    }
  }

  @FunctionalInterface
  private interface DriverFactory {
    EntityLoad create() throws Exception;
  }

  private static final class JobState implements GenerationListener {
    private final String jobId;
    private final String userId;
    private final long totalRecords;
    private final long startedAt;
    private final AtomicBoolean cancelRequested = new AtomicBoolean(false);

    private volatile GenerationStatus status = GenerationStatus.RUNNING;
    private volatile long completedRecords;
    private volatile String message;
    private volatile long updatedAt;

    private JobState(String jobId, String userId, long totalRecords) {
      this.jobId = jobId;
      this.userId = userId;
      this.totalRecords = totalRecords;
      this.startedAt = System.currentTimeMillis();
      this.updatedAt = startedAt;
    }

    @Override
    public void onProgress(long completedRecords, long totalRecords) {
      this.completedRecords = completedRecords;
      this.updatedAt = System.currentTimeMillis();
    }

    @Override
    public boolean isCancelled() {
      return cancelRequested.get();
    }

    private synchronized void finish(GenerationStatus status, String message) {
      if (this.status != GenerationStatus.RUNNING) {
        return;
      }
      this.status = status;
      this.message = message;
      this.updatedAt = System.currentTimeMillis();
    }

    private GenerationJobStatus toStatus() {
      GenerationJobStatus statusDto = new GenerationJobStatus();
      statusDto.setJobId(jobId);
      statusDto.setStatus(status);
      statusDto.setTotalRecords(totalRecords);
      statusDto.setCompletedRecords(completedRecords);
      statusDto.setPercentComplete(percentComplete());
      statusDto.setRecordsPerSecond(recordsPerSecond());
      statusDto.setMessage(message);
      statusDto.setStartedAt(startedAt);
      statusDto.setUpdatedAt(updatedAt);
      return statusDto;
    }

    private int percentComplete() {
      if (totalRecords <= 0) {
        return status == GenerationStatus.COMPLETED ? 100 : 0;
      }
      return (int) Math.min(100, Math.round((completedRecords * 100.0) / totalRecords));
    }

    private double recordsPerSecond() {
      long elapsedMs = updatedAt - startedAt;
      if (elapsedMs <= 0 || completedRecords <= 0) {
        return 0;
      }
      return completedRecords / (elapsedMs / 1000.0);
    }
  }
}
