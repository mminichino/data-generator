package com.codelry.util.generator.driver;

import com.codelry.util.generator.generator.DataLoad;
import com.codelry.util.generator.generator.Record;
import com.codelry.util.generator.service.ReactiveRedisJsonTemplate;
import io.lettuce.core.RedisException;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Redis extends DataLoad {
  private static final Logger logger = LoggerFactory.getLogger(Redis.class);
  private ReactiveHashOperations<String, String, String> hashOps;
  private ReactiveRedisJsonTemplate<String, String> jsonOps;
  private boolean useJson;
  public final PriorityBlockingQueue<Throwable> errorQueue = new PriorityBlockingQueue<>();

  public void connect(ReactiveRedisTemplate<String, String> template, ReactiveRedisJsonTemplate<String, String> json) {
    this.hashOps = template.opsForHash();
    this.jsonOps = json;
  }

  public void setUseJson(boolean useJson) {
    this.useJson = useJson;
    logger.info("Setting JSON generation mode to {}", useJson);
  }

  @Override
  public void prepare() {}

  private void insertBatchHash(List<Record> batch) {
    Flux.fromIterable(batch)
        .flatMap(record -> hashOps.putAll(record.documentId, record.toMap()))
        .retryWhen(Retry.backoff(10, Duration.ofMillis(10)).filter(t -> t instanceof RedisException))
        .doOnError(errorQueue::put)
        .blockLast();
  }

  private void insertBatchJson(List<Record> batch) {
    Flux.fromIterable(batch)
        .flatMap(record -> jsonOps.jsonSet(record.documentId, "$", record.document))
        .retryWhen(Retry.backoff(10, Duration.ofMillis(10)).filter(t -> t instanceof RedisException))
        .doOnError(errorQueue::put)
        .blockLast();
  }

  @Override
  public void insertBatch(List<Record> batch) {
    if (useJson) {
      insertBatchJson(batch);
    } else {
      insertBatchHash(batch);
    }
  }

  @Override
  public void cleanup() {}
}
