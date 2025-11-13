package com.codelry.util.generator.config;

import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

  private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

  @Bean(destroyMethod = "shutdown")
  public ClientResources clientResources() {
    logger.info("Creating Redis ClientResources bean");
    return DefaultClientResources.create();
  }
}
