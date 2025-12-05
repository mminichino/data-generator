package com.codelry.util.generator.service;

import com.codelry.util.generator.dto.RedisConnectionConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.lettucemod.RedisModulesClient;
import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SslOptions;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.support.ConnectionPoolSupport;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RedisConnectionManager {

  private static final Logger logger = LoggerFactory.getLogger(RedisConnectionManager.class);

  private final Map<String, LettuceConnectionFactory> connectionFactories = new ConcurrentHashMap<>();
  private final Map<String, ReactiveRedisConnectionFactory> reactiveFactories = new ConcurrentHashMap<>();
  private final Map<String, RedisModulesClient> modulesClients = new ConcurrentHashMap<>();
  private final Map<String, GenericObjectPool<StatefulRedisModulesConnection<String, String>>> connectionPools = new ConcurrentHashMap<>();
  private final ClientResources clientResources;
  private final Map<String, Boolean> connectedByUser = new ConcurrentHashMap<>();
  private final Map<String, Boolean> useJsonByUser = new ConcurrentHashMap<>();

  public RedisConnectionManager(ClientResources clientResources) {
    this.clientResources = clientResources;
  }

  public synchronized void connect(String userId, RedisConnectionConfig config) throws Exception {
    disconnect(userId);

    logger.info("[userId={}] Connecting to Redis: {}:{}", userId, config.getHost(), config.getPort());

    LettuceConnectionFactory factory = createConnectionFactory(config);
    factory.afterPropertiesSet();
    factory.start();
    connectionFactories.put(userId, factory);

    RedisModulesClient client = createModulesClient(config);
    modulesClients.put(userId, client);

    GenericObjectPool<StatefulRedisModulesConnection<String, String>> pool =
        createConnectionPool(client, config);
    connectionPools.put(userId, pool);

    if (config.isUseJson()) {
      useJsonByUser.put(userId, true);
      logger.info("[userId={}] Enabling JSON mode for Redis", userId);
    } else {
      useJsonByUser.put(userId, false);
    }

    connectedByUser.put(userId, true);
    logger.info("[userId={}] Successfully connected to Redis", userId);
  }

  public synchronized void disconnect(String userId) {
    connectedByUser.put(userId, false);

    GenericObjectPool<StatefulRedisModulesConnection<String, String>> pool = connectionPools.remove(userId);
    if (pool != null) {
      pool.close();
    }

    RedisModulesClient client = modulesClients.remove(userId);
    if (client != null) {
      client.shutdown();
    }

    LettuceConnectionFactory factory = connectionFactories.remove(userId);
    if (factory != null) {
      factory.destroy();
    }

    useJsonByUser.remove(userId);
    reactiveFactories.remove(userId);

    logger.info("[userId={}] Disconnected from Redis", userId);
  }

  public boolean isNotConnected(String userId) {
    Boolean connected = connectedByUser.get(userId);
    return connected == null || !connected || !connectionFactories.containsKey(userId);
  }

  public boolean isUseJson(String userId) {
    return Boolean.TRUE.equals(useJsonByUser.get(userId));
  }

  public LettuceConnectionFactory getConnectionFactory(String userId) {
    if (isNotConnected(userId)) {
      throw new IllegalStateException("Redis is not connected for userId=" + userId);
    }
    return connectionFactories.get(userId);
  }

  public RedisModulesClient getModulesClient(String userId) {
    if (isNotConnected(userId)) {
      throw new IllegalStateException("Redis is not connected for userId=" + userId);
    }
    return modulesClients.get(userId);
  }

  public GenericObjectPool<StatefulRedisModulesConnection<String, String>> getConnectionPool(String userId) {
    if (isNotConnected(userId)) {
      throw new IllegalStateException("Redis is not connected for userId=" + userId);
    }
    return connectionPools.get(userId);
  }

  public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(String userId) {
    StringRedisSerializer serializer = new StringRedisSerializer();

    RedisSerializationContext<String, String> context = RedisSerializationContext
        .<String, String>newSerializationContext(serializer)
        .key(serializer)
        .value(serializer)
        .hashKey(serializer)
        .hashValue(serializer)
        .build();

    return new ReactiveRedisTemplate<>(getConnectionFactory(userId), context);
  }

  public ReactiveRedisJsonTemplate<String, String> reactiveRedisJsonTemplate(String userId) {
    StringRedisSerializer serializer = new StringRedisSerializer();
    ObjectMapper mapper = new ObjectMapper();

    RedisSerializationContext<String, String> context = RedisSerializationContext
        .<String, String>newSerializationContext(serializer)
        .key(serializer)
        .value(serializer)
        .hashKey(serializer)
        .hashValue(serializer)
        .build();

    return new ReactiveRedisJsonTemplate<>(getConnectionFactory(userId), context, mapper);
  }

  private LettuceConnectionFactory createConnectionFactory(RedisConnectionConfig config) throws Exception {
    RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
    redisConfig.setHostName(config.getHost());
    redisConfig.setPort(config.getPort());
    redisConfig.setDatabase(config.getDatabase());

    if (StringUtils.hasText(config.getPassword())) {
      redisConfig.setPassword(config.getPassword());
    }

    GenericObjectPoolConfig<StatefulConnection<?, ?>> poolConfig = createPoolConfig(config);

    LettuceClientConfiguration.LettuceClientConfigurationBuilder clientConfigBuilder =
        LettucePoolingClientConfiguration.builder()
            .commandTimeout(config.getTimeout())
            .poolConfig(poolConfig)
            .clientResources(clientResources);

    ClientOptions.Builder options = ClientOptions.builder()
        .autoReconnect(true)
        .pingBeforeActivateConnection(true);

    if (config.isUseSsl()) {
      SslOptions sslOptions = createSslOptions(config);
      options.sslOptions(sslOptions);
    }

    clientConfigBuilder.clientOptions(options.build());
    LettuceClientConfiguration clientConfig = clientConfigBuilder.build();

    return new LettuceConnectionFactory(redisConfig, clientConfig);
  }

  private RedisModulesClient createModulesClient(RedisConnectionConfig config) throws Exception {
    RedisURI.Builder builder = RedisURI.builder()
        .withHost(config.getHost())
        .withPort(config.getPort())
        .withDatabase(config.getDatabase())
        .withSsl(config.isUseSsl())
        .withVerifyPeer(config.isSslVerify())
        .withTimeout(Duration.ofSeconds(10));

    if (StringUtils.hasText(config.getPassword())) {
      builder.withPassword(config.getPassword().toCharArray());
    }

    ClientOptions.Builder options = ClientOptions.builder()
        .autoReconnect(true)
        .pingBeforeActivateConnection(true);

    if (config.isUseSsl()) {
      SslOptions sslOptions = createSslOptions(config);
      options.sslOptions(sslOptions);
    }

    RedisModulesClient client = RedisModulesClient.create(builder.build());
    client.setOptions(options.build());
    return client;
  }

  private GenericObjectPool<StatefulRedisModulesConnection<String, String>> createConnectionPool(
      RedisModulesClient client, RedisConnectionConfig config) {
    GenericObjectPoolConfig<StatefulRedisModulesConnection<String, String>> poolConfig =
        new GenericObjectPoolConfig<>();
    poolConfig.setMaxTotal(config.getMaxActive());
    poolConfig.setMaxIdle(config.getMaxIdle());
    poolConfig.setMinIdle(config.getMinIdle());
    poolConfig.setMaxWait(config.getMaxWait());
    poolConfig.setTestOnBorrow(true);
    poolConfig.setTestOnReturn(true);
    poolConfig.setTestWhileIdle(true);

    return ConnectionPoolSupport.createGenericObjectPool(client::connect, poolConfig);
  }

  private GenericObjectPoolConfig<StatefulConnection<?, ?>> createPoolConfig(RedisConnectionConfig config) {
    GenericObjectPoolConfig<StatefulConnection<?, ?>> poolConfig = new GenericObjectPoolConfig<>();
    poolConfig.setMaxTotal(config.getMaxActive());
    poolConfig.setMaxIdle(config.getMaxIdle());
    poolConfig.setMinIdle(config.getMinIdle());
    poolConfig.setMaxWait(config.getMaxWait());
    poolConfig.setTestOnBorrow(true);
    poolConfig.setTestOnReturn(true);
    poolConfig.setTestWhileIdle(true);
    return poolConfig;
  }

  private SslOptions createSslOptions(RedisConnectionConfig config) throws Exception {
    SslOptions.Builder sslOptionsBuilder = SslOptions.builder();

    if (StringUtils.hasText(config.getKeystorePath())) {
      KeyStore keyStore = KeyStore.getInstance(config.getKeystoreType());
      try (FileInputStream fis = new FileInputStream(config.getKeystorePath())) {
        keyStore.load(fis, config.getKeystorePassword().toCharArray());
      }

      KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      keyManagerFactory.init(keyStore, config.getKeystorePassword().toCharArray());

      sslOptionsBuilder.keyManager(keyManagerFactory);
    }

    if (!config.isSslVerify()) {
      sslOptionsBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE);
    } else if (StringUtils.hasText(config.getTruststorePath())) {
      KeyStore trustStore = KeyStore.getInstance(config.getTruststoreType());
      try (FileInputStream fis = new FileInputStream(config.getTruststorePath())) {
        trustStore.load(fis, config.getTruststorePassword().toCharArray());
      }

      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(trustStore);

      sslOptionsBuilder.trustManager(trustManagerFactory);
    }

    sslOptionsBuilder.jdkSslProvider();
    return sslOptionsBuilder.build();
  }
}
