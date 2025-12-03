package com.codelry.util.generator.generator;

import com.codelry.util.generator.dto.Entity;
import com.codelry.util.generator.dto.EntityDefinition;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class EntityFactory {
  private static final Logger LOGGER = LogManager.getLogger(EntityFactory.class);
  public BlockingQueue<Entity> recordQueue;
  public BlockingQueue<Throwable> errorQueue = new LinkedBlockingQueue<>();
  private final List<Future<Entity>> loadTasks = new ArrayList<>();
  private ExecutorService loadExecutor;
  private static final AtomicLong counter = new AtomicLong(0);
  private final EntityDefinition definition;
  private int batchSize = 32;
  private final long count;
  private Thread runThread;
  private MeterRegistry registry;

  public EntityFactory(EntityDefinition definition, long start, long count, MeterRegistry registry) {
    this.loadExecutor = Executors.newFixedThreadPool(64);
    this.recordQueue = new LinkedBlockingQueue<>(32);
    this.definition = definition;
    counter.set(start);
    this.count = count;
    this.batchSize = (this.batchSize > count) ? (int) count : this.batchSize;
    this.registry = registry;
  }

  public void setThreads(int threads) {
    loadExecutor = Executors.newFixedThreadPool(threads);
  }

  public void setThreshold(int threshold) {
    recordQueue = new LinkedBlockingQueue<>(threshold);
    batchSize = threshold;
  }

  public void setIndex(long index) {
    counter.set(index);
  }

  public void loadTaskAdd(Callable<Entity> task) {
    loadTasks.add(loadExecutor.submit(task));
  }

  public void loadTaskGet() {
    for (Future<Entity> future : loadTasks) {
      try {
        Entity entity = future.get();
        LOGGER.debug("Record added to queue {}", entity.getId());
        recordQueue.put(entity);
      } catch (ExecutionException e) {
        errorQueue.add(e);
      } catch (InterruptedException e) {
        LOGGER.debug("Record queue wait interrupted");
      }
    }
    loadTasks.clear();
  }

  public void start() {
    runThread = new Thread(() -> {
      while (!Thread.currentThread().isInterrupted() && counter.get() < count) {
        counter.getAndIncrement();
        EntityGenerator generator = new EntityGenerator(definition, counter.get(), registry);
        LOGGER.debug("Generating record {}", counter.get());
        loadTaskAdd(generator::generate);
        if (counter.get() % batchSize == 0 || counter.get() == count) {
          LOGGER.debug("Load batch from queue");
          loadTaskGet();
        }
      }
    });
    LOGGER.debug("Starting record factory");
    runThread.start();
  }

  public void stop() {
    LOGGER.debug("Stopping record factory");
    runThread.interrupt();
  }

  public List<Entity> collect(int quantity) {
    List<Entity> records = new ArrayList<>();
    for (int i = 0; i < quantity; i++) {
      try {
        records.add(getNext());
        registry.counter("entity.create.success", Tags.of("entity", "create")).increment();
      } catch (InterruptedException e) {
        LOGGER.error(e.getMessage(), e);
      }
    }
    return records;
  }

  public Entity getNext() throws InterruptedException {
    return recordQueue.poll(5, TimeUnit.SECONDS);
  }
}
