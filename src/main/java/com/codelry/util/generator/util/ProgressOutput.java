package com.codelry.util.generator.util;

public class ProgressOutput {
  long total;
  long errors = 0;
  long counter = 0;

  public ProgressOutput(long total) {
    this.total = total;
  }

  public void init() {
    System.out.printf("Progress: 0 of %d - 0%%\r", total);
    System.out.flush();
  }

  public void writeLine(long count) {
    counter += count;
    double percentage = (double) counter / total;
    int percent = (int) Math.round(percentage * 100);
    String suffix = errors > 0 ? String.format(" %sErrors: %d%s", Color.YELLOW, errors, Color.RESET) : "";
    System.out.printf("Progress: %d of %d - %d%%%s\r", counter, total, percent, suffix);
    System.out.flush();
  }

  public void newLine() {
    System.out.println("\n");
    System.out.flush();
  }

  public void incrementErrorCount() {
    errors += 1;
  }

  public void incrementErrorCount(long count) {
    errors += count;
  }
}
