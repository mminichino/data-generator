package com.codelry.util.generator;

import com.codelry.util.generator.dto.ConnectionParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConnectionParametersTest {

  @Test
  void deserializesHostnameFromJson() throws Exception {
    String json = """
        {"type":"couchbase","hostname":"18.191.171.10","port":8091,"username":"Administrator","password":"secret","database":"fares"}
        """;

    ConnectionParameters parameters = new ObjectMapper().readValue(json, ConnectionParameters.class);

    assertEquals("18.191.171.10", parameters.getHost());
    assertEquals(8091, parameters.getPort());
    assertEquals("fares", parameters.getDatabase());
  }
}
