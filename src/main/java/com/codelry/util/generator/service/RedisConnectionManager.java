package com.codelry.util.generator.service;

import com.codelry.util.generator.dto.RedisConnectionConfig;
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
import java.util.concurrent.atomic.AtomicReference;

@Service
public class RedisConnectionManager {

  private static final Logger logger = LoggerFactory.getLogger(RedisConnectionManager.class);

  private final AtomicReference<LettuceConnectionFactory> connectionFactory = new AtomicReference<>();
  private final AtomicReference<ReactiveRedisConnectionFactory> reactiveFactory = new AtomicReference<>();
  private final AtomicReference<RedisModulesClient> modulesClient = new AtomicReference<>();
  private final AtomicReference<GenericObjectPool<StatefulRedisModulesConnection<String, String>>> connectionPool = new AtomicReference<>();
  private final ClientResources clientResources;
  private volatile boolean connected = false;

  public RedisConnectionManager(ClientResources clientResources) {
    this.clientResources = clientResources;
  }

  public synchronized void connect(RedisConnectionConfig config) throws Exception {
    disconnect();

    logger.info("Connecting to Redis: {}:{}", config.getHost(), config.getPort());

    LettuceConnectionFactory factory = createConnectionFactory(config);
    factory.afterPropertiesSet();
    factory.start();
    connectionFactory.set(factory);

    RedisModulesClient client = createModulesClient(config);
    modulesClient.set(client);

    GenericObjectPool<StatefulRedisModulesConnection<String, String>> pool =
        createConnectionPool(client, config);
    connectionPool.set(pool);

    connected = true;
    logger.info("Successfully connected to Redis");
  }

  public synchronized void disconnect() {
    connected = false;

    if (connectionPool.get() != null) {
      connectionPool.get().close();
      connectionPool.set(null);
    }

    if (modulesClient.get() != null) {
      modulesClient.get().shutdown();
      modulesClient.set(null);
    }

    if (connectionFactory.get() != null) {
      connectionFactory.get().destroy();
      connectionFactory.set(null);
    }

    logger.info("Disconnected from Redis");
  }

  public boolean isNotConnected() {
    return !connected || connectionFactory.get() == null;
  }

  public LettuceConnectionFactory getConnectionFactory() {
    if (isNotConnected()) {
      throw new IllegalStateException("Redis is not connected");
    }
    return connectionFactory.get();
  }

  public RedisModulesClient getModulesClient() {
    if (isNotConnected()) {
      throw new IllegalStateException("Redis is not connected");
    }
    return modulesClient.get();
  }

  public GenericObjectPool<StatefulRedisModulesConnection<String, String>> getConnectionPool() {
    if (isNotConnected()) {
      throw new IllegalStateException("Redis is not connected");
    }
    return connectionPool.get();
  }

  public ReactiveRedisTemplate<String, String> reactiveRedisTemplate() {

    StringRedisSerializer serializer = new StringRedisSerializer();

    RedisSerializationContext<String, String> context = RedisSerializationContext
        .<String, String>newSerializationContext(serializer)
        .key(serializer)
        .value(serializer)
        .hashKey(serializer)
        .hashValue(serializer)
        .build();

    return new ReactiveRedisTemplate<>(getConnectionFactory(), context);
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
