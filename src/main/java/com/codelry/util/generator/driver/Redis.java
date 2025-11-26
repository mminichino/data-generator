package com.codelry.util.generator.driver;

import com.codelry.util.generator.generator.DataLoad;
import com.codelry.util.generator.generator.Record;
import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import io.lettuce.core.RedisException;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

public class Redis extends DataLoad {
  private LettuceConnectionFactory factory;
  private ReactiveRedisConnection connection;
  public final PriorityBlockingQueue<Throwable> errorQueue = new PriorityBlockingQueue<>();

  public void connect(LettuceConnectionFactory factory) {
    this.factory = factory;
    this.connection = factory.getReactiveConnection();
  }

  @Override
  public void prepare() {}

  @Override
  public void insertBatch(List<Record> batch) {
//    Flux.fromIterable(batch)
//        .flatMap(record -> insertMap(record.documentId, record.toMap()))
//        .retryWhen(Retry.backoff(10, Duration.ofMillis(10)).filter(t -> t instanceof RedisException))
//        .doOnError(errorQueue::put)
//        .blockLast();
  }

  @Override
  public void cleanup() {}
}
