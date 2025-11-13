package com.codelry.util.generator.config;

import com.redis.lettucemod.api.sync.RedisModulesCommands;
import io.lettuce.core.json.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class RedisConfigTest {

  @Container
  static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis/redis-stack:latest"))
      .withExposedPorts(6379);

  @DynamicPropertySource
  static void redisProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", redis::getFirstMappedPort);
  }

  @Autowired
  private RedisTemplate<String, String> redisTemplate;

  @Autowired
  private RedisModulesCommands<String, String> redisModulesCommands;

  @Autowired
  private RedisModulesTemplate redisModulesTemplate;

  @BeforeEach
  void setUp() {
    Set<String> keys = redisTemplate.keys("test:*");
    if (!keys.isEmpty()) {
      redisTemplate.delete(keys);
    }
  }

  @AfterEach
  void tearDown() {
    Set<String> keys = redisTemplate.keys("test:*");
    if (!keys.isEmpty()) {
      redisTemplate.delete(keys);
    }
  }

  @Test
  void testRedisTemplate_basicStringOperations() {
    String key = "test:simple:key";
    String value = "test-value";

    redisTemplate.opsForValue().set(key, value);
    String retrievedValue = redisTemplate.opsForValue().get(key);

    assertThat(retrievedValue).isEqualTo(value);
  }

  @Test
  void testRedisTemplate_stringOperationsWithExpiry() {
    String key = "test:expiry:key";
    String value = "expiring-value";

    redisTemplate.opsForValue().set(key, value, 10, TimeUnit.SECONDS);
    Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);

    assertThat(redisTemplate.opsForValue().get(key)).isEqualTo(value);
    assertThat(ttl).isGreaterThan(0).isLessThanOrEqualTo(10);
  }

  @Test
  void testRedisTemplate_hashOperations() {
    String key = "test:hash:user";
    String hashKey1 = "name";
    String hashValue1 = "John Doe";
    String hashKey2 = "email";
    String hashValue2 = "john@example.com";

    redisTemplate.opsForHash().put(key, hashKey1, hashValue1);
    redisTemplate.opsForHash().put(key, hashKey2, hashValue2);

    assertThat(redisTemplate.opsForHash().get(key, hashKey1)).isEqualTo(hashValue1);
    assertThat(redisTemplate.opsForHash().get(key, hashKey2)).isEqualTo(hashValue2);
    assertThat(redisTemplate.opsForHash().size(key)).isEqualTo(2);
  }

  @Test
  void testRedisTemplate_listOperations() {
    String key = "test:list:items";

    redisTemplate.opsForList().rightPush(key, "item1");
    redisTemplate.opsForList().rightPush(key, "item2");
    redisTemplate.opsForList().rightPush(key, "item3");

    assertThat(redisTemplate.opsForList().size(key)).isEqualTo(3);
    assertThat(redisTemplate.opsForList().index(key, 0)).isEqualTo("item1");
    assertThat(redisTemplate.opsForList().leftPop(key)).isEqualTo("item1");
    assertThat(redisTemplate.opsForList().size(key)).isEqualTo(2);
  }

  @Test
  void testRedisTemplate_setOperations() {
    String key = "test:set:tags";

    redisTemplate.opsForSet().add(key, "tag1", "tag2", "tag3");

    assertThat(redisTemplate.opsForSet().size(key)).isEqualTo(3);
    assertThat(redisTemplate.opsForSet().isMember(key, "tag1")).isTrue();
    assertThat(redisTemplate.opsForSet().isMember(key, "tag4")).isFalse();
  }

  @Test
  void testRedisTemplate_deleteOperations() {
    String key = "test:delete:key";
    redisTemplate.opsForValue().set(key, "value");

    Boolean deleted = redisTemplate.delete(key);

    assertThat(deleted).isTrue();
    assertThat(redisTemplate.hasKey(key)).isFalse();
  }

  @Test
  void testRedisModulesCommands_basicOperations() {
    String key = "test:modules:key";
    String value = "modules-value";

    redisModulesCommands.set(key, value);
    String retrievedValue = redisModulesCommands.get(key);

    assertThat(retrievedValue).isEqualTo(value);
  }

  @Test
  void testRedisModulesCommands_jsonOperations() {
    String key = "test:json:object";
    String jsonValue = "{\"name\":\"Alice\",\"age\":30}";

    redisModulesTemplate.jsonSet(key, JsonPath.of("$"), jsonValue);
    String retrievedJson = redisModulesTemplate.jsonGet(key).getFirst().asJsonObject().toString();

    assertThat(retrievedJson).contains("Alice").contains("30");
  }

  @Test
  void testRedisModulesCommands_setOperations() {
    String key = "test:modules:set";

    redisModulesCommands.sadd(key, "member1", "member2", "member3");
    Long size = redisModulesCommands.scard(key);

    assertThat(size).isEqualTo(3);
    assertThat(redisModulesCommands.sismember(key, "member1")).isTrue();
  }

  @Test
  void testRedisModulesCommands_hashOperations() {
    String key = "test:modules:hash";

    redisModulesCommands.hset(key, "field1", "value1");
    redisModulesCommands.hset(key, "field2", "value2");
    String value = redisModulesCommands.hget(key, "field1");

    assertThat(value).isEqualTo("value1");
    assertThat(redisModulesCommands.hlen(key)).isEqualTo(2);
  }

  @Test
  void testBothRedisTemplateAndModulesCommands_interoperability() {
    String key = "test:interop:key";
    String value = "shared-value";

    redisTemplate.opsForValue().set(key, value);

    String retrievedByModules = redisModulesCommands.get(key);
    assertThat(retrievedByModules).isEqualTo(value);

    String newValue = "updated-value";
    redisModulesCommands.set(key, newValue);

    String retrievedByTemplate = redisTemplate.opsForValue().get(key);
    assertThat(retrievedByTemplate).isEqualTo(newValue);
  }

  @Test
  void testRedisTemplate_incrementOperations() {
    String key = "test:counter:views";

    Long count1 = redisTemplate.opsForValue().increment(key);
    Long count2 = redisTemplate.opsForValue().increment(key, 5);

    assertThat(count1).isEqualTo(1);
    assertThat(count2).isEqualTo(6);
    assertThat(redisTemplate.opsForValue().get(key)).isEqualTo("6");
  }

  @Test
  void testRedisModulesCommands_expiryOperations() {
    String key = "test:modules:expiry";
    String value = "expiring-value";

    redisModulesCommands.setex(key, 10, value);
    Long ttl = redisModulesCommands.ttl(key);

    assertThat(redisModulesCommands.get(key)).isEqualTo(value);
    assertThat(ttl).isGreaterThan(0).isLessThanOrEqualTo(10);
  }
}
