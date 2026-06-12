package com.codelry.util.generator.generator;

public class GenerationCancelledException extends RuntimeException {
  public GenerationCancelledException() {
    super("Generation cancelled");
  }
}
