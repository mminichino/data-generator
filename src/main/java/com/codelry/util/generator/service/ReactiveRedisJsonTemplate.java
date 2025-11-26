package com.codelry.util.generator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.output.ArrayOutput;
import io.lettuce.core.output.IntegerOutput;
import io.lettuce.core.output.StatusOutput;
import io.lettuce.core.output.ValueOutput;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

public class ReactiveRedisJsonTemplate<K, V> extends ReactiveRedisTemplate<K, V> {

  private final ObjectMapper objectMapper;
  private final RedisReactiveCommands<String, String> commands;

  public ReactiveRedisJsonTemplate(
      LettuceConnectionFactory connectionFactory,
      RedisSerializationContext<K, V> serializationContext,
      ObjectMapper objectMapper) {
    super(connectionFactory, serializationContext);
    this.objectMapper = objectMapper;

    StatefulRedisConnection<String, String> connection =
        ((RedisClient) Objects.requireNonNull(connectionFactory.getNativeClient()))
            .connect(StringCodec.UTF8);
    this.commands = connection.reactive();
  }

  public <T> Mono<Boolean> jsonSet(String key, String path, T value) {
    return Mono.fromCallable(() -> objectMapper.writeValueAsString(value))
        .flatMap(jsonString ->
            commands.<String>dispatch(
                    JsonCommand.JSON_SET,
                    new StatusOutput<>(StringCodec.UTF8),
                    new CommandArgs<>(StringCodec.UTF8)
                        .addKey(key)
                        .add(path)
                        .add(jsonString)
                )
                .next()
                .map("OK"::equals)
        )
        .onErrorMap(e ->
            new RuntimeException(
                "Failed to execute JSON.SET for key: " + key, e
            )
        );
  }

  public <T> Mono<T> jsonGet(String key, String path, Class<T> valueType) {
    return commands.<String>dispatch(
            JsonCommand.JSON_GET,
            new ValueOutput<>(StringCodec.UTF8),
            new CommandArgs<>(StringCodec.UTF8)
                .addKey(key)
                .add(path)
        )
        .next()
        .map(jsonString -> {
          try {
            return objectMapper.readValue(jsonString, valueType);
          } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize JSON for key: " + key, e);
          }
        })
        .onErrorMap(e ->
            new RuntimeException("Failed to execute JSON.GET for key: " + key, e)
        );
  }

  public Mono<String> jsonGetRaw(String key, String path) {
    return commands.<String>dispatch(
            JsonCommand.JSON_GET,
            new ValueOutput<>(StringCodec.UTF8),
            new CommandArgs<>(StringCodec.UTF8)
                .addKey(key)
                .add(path)
        )
        .next()
        .onErrorMap(e ->
            new RuntimeException("Failed to execute JSON.GET for key: " + key, e)
        );
  }

  public Mono<Long> jsonDel(String key, String path) {
    return commands.<Long>dispatch(
            JsonCommand.JSON_DEL,
            new IntegerOutput<>(StringCodec.UTF8),
            new CommandArgs<>(StringCodec.UTF8)
                .addKey(key)
                .add(path)
        )
        .next()
        .onErrorMap(e ->
            new RuntimeException("Failed to execute JSON.DEL for key: " + key, e)
        );
  }

  public Mono<String> jsonType(String key, String path) {
    return commands.<String>dispatch(
            JsonCommand.JSON_TYPE,
            new ValueOutput<>(StringCodec.UTF8),
            new CommandArgs<>(StringCodec.UTF8)
                .addKey(key)
                .add(path)
        )
        .next()
        .onErrorMap(e ->
            new RuntimeException("Failed to execute JSON.TYPE for key: " + key, e)
        );
  }

  @SafeVarargs
  public final <T> Mono<Long> jsonArrAppend(String key, String path, T... values) {
    return Mono.defer(() -> {
      try {
        CommandArgs<String, String> args = new CommandArgs<>(StringCodec.UTF8)
            .addKey(key)
            .add(path);

        for (T value : values) {
          String jsonValue = objectMapper.writeValueAsString(value);
          args.add(jsonValue);
        }

        return commands.<Long>dispatch(
                JsonCommand.JSON_ARRAPPEND,
                new IntegerOutput<>(StringCodec.UTF8),
                args
            )
            .next();
      } catch (Exception e) {
        return Mono.error(new RuntimeException("Failed to serialize values for JSON.ARRAPPEND", e));
      }
    }).onErrorMap(e ->
        new RuntimeException("Failed to execute JSON.ARRAPPEND for key: " + key, e)
    );
  }

  public Mono<Long> jsonArrLen(String key, String path) {
    return commands.<Long>dispatch(
            JsonCommand.JSON_ARRLEN,
            new IntegerOutput<>(StringCodec.UTF8),
            new CommandArgs<>(StringCodec.UTF8)
                .addKey(key)
                .add(path)
        )
        .next()
        .onErrorMap(e ->
            new RuntimeException("Failed to execute JSON.ARRLEN for key: " + key, e)
        );
  }

  public <T> Mono<Long> jsonArrIndex(String key, String path, T value) {
    return Mono.fromCallable(() -> objectMapper.writeValueAsString(value))
        .flatMap(jsonValue ->
            commands.<Long>dispatch(
                    JsonCommand.JSON_ARRINDEX,
                    new IntegerOutput<>(StringCodec.UTF8),
                    new CommandArgs<>(StringCodec.UTF8)
                        .addKey(key)
                        .add(path)
                        .add(jsonValue)
                )
                .next()
        )
        .onErrorMap(e ->
            new RuntimeException("Failed to execute JSON.ARRINDEX for key: " + key, e)
        );
  }

  @SafeVarargs
  public final <T> Mono<Long> jsonArrInsert(String key, String path, int index, T... values) {
    return Mono.defer(() -> {
      try {
        CommandArgs<String, String> args = new CommandArgs<>(StringCodec.UTF8)
            .addKey(key)
            .add(path)
            .add(String.valueOf(index));

        for (T value : values) {
          String jsonValue = objectMapper.writeValueAsString(value);
          args.add(jsonValue);
        }

        return commands.<Long>dispatch(
                JsonCommand.JSON_ARRINSERT,
                new IntegerOutput<>(StringCodec.UTF8),
                args
            )
            .next();
      } catch (Exception e) {
        return Mono.error(new RuntimeException("Failed to serialize values for JSON.ARRINSERT", e));
      }
    }).onErrorMap(e ->
        new RuntimeException("Failed to execute JSON.ARRINSERT for key: " + key, e)
    );
  }

  public <T> Mono<T> jsonArrPop(String key, String path, int index, Class<T> valueType) {
    return commands.<String>dispatch(
            JsonCommand.JSON_ARRPOP,
            new ValueOutput<>(StringCodec.UTF8),
            new CommandArgs<>(StringCodec.UTF8)
                .addKey(key)
                .add(path)
                .add(String.valueOf(index))
        )
        .next()
        .map(jsonString -> {
          try {
            return objectMapper.readValue(jsonString, valueType);
          } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize popped value", e);
          }
        })
        .onErrorMap(e ->
            new RuntimeException("Failed to execute JSON.ARRPOP for key: " + key, e)
        );
  }

  public Mono<Long> jsonArrTrim(String key, String path, int start, int stop) {
    return commands.<Long>dispatch(
            JsonCommand.JSON_ARRTRIM,
            new IntegerOutput<>(StringCodec.UTF8),
            new CommandArgs<>(StringCodec.UTF8)
                .addKey(key)
                .add(path)
                .add(String.valueOf(start))
                .add(String.valueOf(stop))
        )
        .next()
        .onErrorMap(e ->
            new RuntimeException("Failed to execute JSON.ARRTRIM for key: " + key, e)
        );
  }

  public Flux<String> jsonObjKeys(String key, String path) {
    return commands.<List<String>>dispatch(
            JsonCommand.JSON_OBJKEYS,
            new ArrayOutput<>(StringCodec.UTF8),
            new CommandArgs<>(StringCodec.UTF8)
                .addKey(key)
                .add(path)
        )
        .next()
        .flatMapMany(Flux::fromIterable)
        .onErrorMap(e ->
            new RuntimeException("Failed to execute JSON.OBJKEYS for key: " + key, e)
        );
  }

  public Mono<Long> jsonObjLen(String key, String path) {
    return commands.<Long>dispatch(
            JsonCommand.JSON_OBJLEN,
            new IntegerOutput<>(StringCodec.UTF8),
            new CommandArgs<>(StringCodec.UTF8)
                .addKey(key)
                .add(path)
        )
        .next()
        .onErrorMap(e ->
            new RuntimeException("Failed to execute JSON.OBJLEN for key: " + key, e)
        );
  }

  public Mono<Double> jsonNumIncrBy(String key, String path, double value) {
    return commands.<String>dispatch(
            JsonCommand.JSON_NUMINCRBY,
            new ValueOutput<>(StringCodec.UTF8),
            new CommandArgs<>(StringCodec.UTF8)
                .addKey(key)
                .add(path)
                .add(String.valueOf(value))
        )
        .next()
        .map(Double::parseDouble)
        .onErrorMap(e ->
            new RuntimeException("Failed to execute JSON.NUMINCRBY for key: " + key, e)
        );
  }

  public Mono<Double> jsonNumMultBy(String key, String path, double value) {
    return commands.<String>dispatch(
            JsonCommand.JSON_NUMMULTBY,
            new ValueOutput<>(StringCodec.UTF8),
            new CommandArgs<>(StringCodec.UTF8)
                .addKey(key)
                .add(path)
                .add(String.valueOf(value))
        )
        .next()
        .map(Double::parseDouble)
        .onErrorMap(e ->
            new RuntimeException("Failed to execute JSON.NUMMULTBY for key: " + key, e)
        );
  }

  public Mono<Long> jsonStrAppend(String key, String path, String value) {
    return commands.<Long>dispatch(
            JsonCommand.JSON_STRAPPEND,
            new IntegerOutput<>(StringCodec.UTF8),
            new CommandArgs<>(StringCodec.UTF8)
                .addKey(key)
                .add(path)
                .add("\"" + value + "\"")
        )
        .next()
        .onErrorMap(e ->
            new RuntimeException("Failed to execute JSON.STRAPPEND for key: " + key, e)
        );
  }

  public Mono<Long> jsonStrLen(String key, String path) {
    return commands.<Long>dispatch(
            JsonCommand.JSON_STRLEN,
            new IntegerOutput<>(StringCodec.UTF8),
            new CommandArgs<>(StringCodec.UTF8)
                .addKey(key)
                .add(path)
        )
        .next()
        .onErrorMap(e ->
            new RuntimeException("Failed to execute JSON.STRLEN for key: " + key, e)
        );
  }

  public Mono<Long> jsonToggle(String key, String path) {
    return commands.<Long>dispatch(
            JsonCommand.JSON_TOGGLE,
            new IntegerOutput<>(StringCodec.UTF8),
            new CommandArgs<>(StringCodec.UTF8)
                .addKey(key)
                .add(path)
        )
        .next()
        .onErrorMap(e ->
            new RuntimeException("Failed to execute JSON.TOGGLE for key: " + key, e)
        );
  }

  public Mono<Long> jsonClear(String key, String path) {
    return commands.<Long>dispatch(
            JsonCommand.JSON_CLEAR,
            new IntegerOutput<>(StringCodec.UTF8),
            new CommandArgs<>(StringCodec.UTF8)
                .addKey(key)
                .add(path)
        )
        .next()
        .onErrorMap(e ->
            new RuntimeException("Failed to execute JSON.CLEAR for key: " + key, e)
        );
  }

  public void destroy() {
    if (commands != null) {
      commands.getStatefulConnection().close();
    }
  }
}
