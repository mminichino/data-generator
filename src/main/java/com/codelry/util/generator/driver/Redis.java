package com.codelry.util.generator.driver;

import com.codelry.util.generator.generator.DataLoad;
import com.codelry.util.generator.generator.Record;
import io.lettuce.core.RedisException;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

public class Redis extends DataLoad {
  private ReactiveHashOperations<String, String, String> hashOps;
  public final PriorityBlockingQueue<Throwable> errorQueue = new PriorityBlockingQueue<>();

  public void connect(ReactiveRedisTemplate<String, String> template) {
    this.hashOps = template.opsForHash();
  }

  @Override
  public void prepare() {}

  @Override
  public void insertBatch(List<Record> batch) {
    Flux.fromIterable(batch)
        .flatMap(record -> hashOps.putAll(record.documentId, record.toMap()))
        .retryWhen(Retry.backoff(10, Duration.ofMillis(10)).filter(t -> t instanceof RedisException))
        .doOnError(errorQueue::put)
        .blockLast();
  }

  @Override
  public void cleanup() {}
}
