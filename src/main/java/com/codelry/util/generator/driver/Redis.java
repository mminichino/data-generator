package com.codelry.util.generator.driver;

import com.codelry.util.generator.dto.Entity;
import com.codelry.util.generator.generator.EntityLoad;
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

public class Redis extends EntityLoad {
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

  private void insertBatchHash(List<Entity> batch) {
    Flux.fromIterable(batch)
        .flatMap(record -> hashOps.putAll(record.getId(), record.asMap()))
        .retryWhen(Retry.backoff(10, Duration.ofMillis(10)).filter(t -> t instanceof RedisException))
        .doOnError(errorQueue::put)
        .blockLast();
  }

  private void insertBatchJson(List<Entity> batch) {
    Flux.fromIterable(batch)
        .flatMap(record -> jsonOps.jsonSet(record.getId(), "$", record.asJson()))
        .retryWhen(Retry.backoff(10, Duration.ofMillis(10)).filter(t -> t instanceof RedisException))
        .doOnError(errorQueue::put)
        .blockLast();
  }

  @Override
  public void insertBatch(List<Entity> batch) {
    if (useJson) {
      insertBatchJson(batch);
    } else {
      insertBatchHash(batch);
    }
  }

  @Override
  public void cleanup() {}
}
