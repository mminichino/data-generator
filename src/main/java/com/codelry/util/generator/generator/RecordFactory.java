package com.codelry.util.generator.generator;

import com.codelry.util.generator.dto.Table;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class RecordFactory {
  private static final Logger LOGGER = LogManager.getLogger(RecordFactory.class);
  public BlockingQueue<Record> recordQueue;
  public BlockingQueue<Throwable> errorQueue = new LinkedBlockingQueue<>();
  private final List<Future<Record>> loadTasks = new ArrayList<>();
  private ExecutorService loadExecutor;
  private static final AtomicLong counter = new AtomicLong(0);
  private final Table table;
  private int batchSize = 32;
  private final long count;
  private Thread runThread;

  public RecordFactory(Table table, long start, long count) {
    this.loadExecutor = Executors.newFixedThreadPool(64);
    this.recordQueue = new LinkedBlockingQueue<>(32);
    this.table = table;
    this.table.getIndex().set(start);
    this.count = count;
    this.batchSize = (this.batchSize > count) ? (int) count : this.batchSize;
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

  public void loadTaskAdd(Callable<Record> task) {
    loadTasks.add(loadExecutor.submit(task));
  }

  public void loadTaskGet() {
    for (Future<Record> future : loadTasks) {
      try {
        Record record = future.get();
        LOGGER.debug("Record added to queue {}", record.getId());
        recordQueue.put(record);
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
        Generator generator = new Generator(table);
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

  public List<Record> collect(int quantity) {
    List<Record> records = new ArrayList<>();
    for (int i = 0; i < quantity; i++) {
      try {
        records.add(getNext());
      } catch (InterruptedException e) {
        LOGGER.error(e.getMessage(), e);
      }
    }
    return records;
  }

  public Record getNext() throws InterruptedException {
    return recordQueue.poll(5, TimeUnit.SECONDS);
  }
}
