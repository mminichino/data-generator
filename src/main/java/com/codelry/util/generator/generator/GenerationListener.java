package com.codelry.util.generator.generator;

public interface GenerationListener {
  void onProgress(long completedRecords, long totalRecords);

  boolean isCancelled();
}
