package com.codelry.util.generator.internal;

import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TelemetryListener implements ChatModelListener {
  private static final Logger LOGGER = LogManager.getLogger(TelemetryListener.class);

  @Override
  public void onRequest(ChatModelRequestContext requestContext) {
    ChatRequest request = requestContext.chatRequest();
    int messages = request.messages().size();
    int characterCount = request.messages().stream()
        .mapToInt(m -> m.toString().length())
        .sum();
    int estimatedTokens = characterCount / 4;

    LOGGER.debug("LLM request: messages={} size={} tokens={}", messages, characterCount, estimatedTokens);
  }

  @Override
  public void onResponse(ChatModelResponseContext responseContext) {
    ChatResponse response = responseContext.chatResponse();

    if (response != null && response.metadata() != null) {
      TokenUsage usage = response.metadata().tokenUsage();
      if (usage != null) {
        int input  = usage.inputTokenCount();
        int output = usage.outputTokenCount();
        int total  = usage.totalTokenCount();

        LOGGER.debug("LLM response: input={}, output={}, total={}", input, output, total);
      }
    }
  }

  @Override
  public void onError(ChatModelErrorContext errorContext) {
    LOGGER.error("LLM error: {}", errorContext.error().getMessage());
  }
}
