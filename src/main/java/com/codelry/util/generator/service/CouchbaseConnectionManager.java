package com.codelry.util.generator.service;

import com.codelry.util.generator.dto.CouchbaseConnectionConfig;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.core.env.SecurityConfig;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CouchbaseConnectionManager {

  private static final Logger logger = LoggerFactory.getLogger(CouchbaseConnectionManager.class);

  private final Map<String, Cluster> clusters = new ConcurrentHashMap<>();
  private final Map<String, ClusterEnvironment> environments = new ConcurrentHashMap<>();
  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
  private final Map<String, Boolean> connectedByUser = new ConcurrentHashMap<>();

  public synchronized void connect(String userId, CouchbaseConnectionConfig config) throws Exception {
    disconnect(userId);

    logger.info("[userId={}] Connecting to Couchbase: {} bucket={} tls={}",
        userId, config.getHost(), config.getBucket(), config.isUseTls());

    String connectionString = buildConnectionString(config);
    ClusterEnvironment environment = createEnvironment(config);
    Cluster cluster = Cluster.connect(
        connectionString,
        com.couchbase.client.java.ClusterOptions.clusterOptions(config.getUsername(), config.getPassword())
            .environment(environment));

    String bucketName = StringUtils.hasText(config.getBucket()) ? config.getBucket() : "default";
    Bucket bucket = cluster.bucket(bucketName);
    bucket.waitUntilReady(java.time.Duration.ofSeconds(30));

    clusters.put(userId, cluster);
    environments.put(userId, environment);
    buckets.put(userId, bucket);
    connectedByUser.put(userId, true);

    logger.info("[userId={}] Successfully connected to Couchbase bucket {}", userId, bucketName);
  }

  public synchronized void disconnect(String userId) {
    connectedByUser.put(userId, false);

    Cluster cluster = clusters.remove(userId);
    if (cluster != null) {
      cluster.disconnect();
    }

    ClusterEnvironment environment = environments.remove(userId);
    if (environment != null) {
      environment.shutdown();
    }

    buckets.remove(userId);

    logger.info("[userId={}] Disconnected from Couchbase", userId);
  }

  public boolean isNotConnected(String userId) {
    Boolean connected = connectedByUser.get(userId);
    return connected == null || !connected || !clusters.containsKey(userId);
  }

  public Collection getCollection(String userId) {
    if (isNotConnected(userId)) {
      throw new IllegalStateException("Couchbase is not connected for userId=" + userId);
    }
    return buckets.get(userId).defaultCollection();
  }

  private String buildConnectionString(CouchbaseConnectionConfig config) {
    String scheme = config.isUseTls() ? "couchbases" : "couchbase";
    return scheme + "://" + config.getHost();
  }

  private ClusterEnvironment createEnvironment(CouchbaseConnectionConfig config) {
    ClusterEnvironment.Builder builder = ClusterEnvironment.builder();

    if (config.isUseTls()) {
      SecurityConfig.Builder securityBuilder = SecurityConfig.enableTls(true);
      if (config.isTlsSkipVerify()) {
        securityBuilder.trustManagerFactory(InsecureTrustManagerFactory.INSTANCE);
      }
      builder.securityConfig(securityBuilder);
    }

    return builder.build();
  }
}
