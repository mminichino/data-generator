package com.codelry.util.generator.driver;

import com.codelry.util.generator.dto.Entity;
import com.codelry.util.generator.generator.EntityLoad;
import com.couchbase.client.core.error.CouchbaseException;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.ReactiveCollection;
import reactor.core.publisher.Flux;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

public class Couchbase extends EntityLoad {
  private static final Logger logger = LoggerFactory.getLogger(Couchbase.class);
  private ReactiveCollection collection;
  public final PriorityBlockingQueue<Throwable> errorQueue = new PriorityBlockingQueue<>();

  public void connect(Collection collection) {
    this.collection = collection.reactive();
  }

  @Override
  public void prepare() {}

  @Override
  public void insertBatch(List<Entity> batch) {
    Flux.fromIterable(batch)
        .flatMap(record -> collection.upsert(record.getId(), record.asJson()), 256)
        .retryWhen(Retry.backoff(10, Duration.ofMillis(10)).filter(t -> t instanceof CouchbaseException))
        .doOnError(errorQueue::put)
        .blockLast();
  }

  @Override
  public void cleanup() {}
}
