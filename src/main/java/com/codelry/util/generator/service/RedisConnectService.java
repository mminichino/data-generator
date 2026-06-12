package com.codelry.util.generator.service;

import com.codelry.util.generator.data.*;
import com.codelry.util.generator.dto.RedisConnectionConfig;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@GRpcService
public class RedisConnectService extends RedisConnectGrpc.RedisConnectImplBase {
  private static final Logger logger = LoggerFactory.getLogger(RedisConnectService.class);
  private final RedisConnectionManager redisConnectionManager;

  @Autowired
  public RedisConnectService(RedisConnectionManager redisConnectionManager) {
    this.redisConnectionManager = redisConnectionManager;
  }

  @Override
  public void connect(ConnectRequest request, StreamObserver<ConnectResponse> responseObserver) {
    try {
      RedisConnectionConfig config = new RedisConnectionConfig();
      config.setHost(request.getHostname());
      config.setPort(request.getPort());
      config.setPassword(request.getPassword());
      config.setUseJson(request.getJson());
      redisConnectionManager.connect(request.getUserId(), config);
      logger.info("Successfully connected to Redis");
      responseObserver.onNext(ConnectResponse.newBuilder().setSuccess(true).build());
      responseObserver.onCompleted();
    } catch (Exception e) {
      logger.error("Failed to connect", e);
      responseObserver.onError(e);
    }
  }

  @Override
  public void disconnect(DisconnectRequest request, StreamObserver<DisconnectResponse> responseObserver) {
    try {
      if (!redisConnectionManager.isNotConnected(request.getUserId())) {
        redisConnectionManager.disconnect(request.getUserId());
      }
      logger.info("Successfully disconnected from Redis");
      responseObserver.onNext(DisconnectResponse.newBuilder().setSuccess(true).build());
      responseObserver.onCompleted();
    } catch (Exception e) {
      logger.error("Failed to disconnect", e);
      responseObserver.onError(e);
    }
  }

  @Override
  public void status(StatusRequest request, StreamObserver<StatusResponse> responseObserver) {
    boolean connected = !redisConnectionManager.isNotConnected(request.getUserId());
    responseObserver.onNext(StatusResponse.newBuilder().setConnected(connected).build());
    responseObserver.onCompleted();
  }
}
