package com.codelry.util.generator.service;

import com.codelry.util.generator.data.*;
import com.codelry.util.generator.dto.CouchbaseConnectionConfig;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@GRpcService
public class CouchbaseConnectService extends CouchbaseConnectGrpc.CouchbaseConnectImplBase {
  private static final Logger logger = LoggerFactory.getLogger(CouchbaseConnectService.class);
  private final CouchbaseConnectionManager couchbaseConnectionManager;

  @Autowired
  public CouchbaseConnectService(CouchbaseConnectionManager couchbaseConnectionManager) {
    this.couchbaseConnectionManager = couchbaseConnectionManager;
  }

  @Override
  public void connect(CouchbaseConnectRequest request, StreamObserver<CouchbaseConnectResponse> responseObserver) {
    try {
      CouchbaseConnectionConfig config = new CouchbaseConnectionConfig();
      config.setHost(request.getHostname());
      config.setUsername(request.getUsername());
      config.setPassword(request.getPassword());
      config.setUseTls(request.getTls());
      config.setTlsSkipVerify(request.getTlsSkipVerify());
      config.setBucket(request.getBucket());
      config.setScope(request.getScope());
      config.setCollection(request.getCollection());
      couchbaseConnectionManager.connect(request.getUserId(), config);
      logger.info("Successfully connected to Couchbase");
      responseObserver.onNext(CouchbaseConnectResponse.newBuilder().setSuccess(true).build());
      responseObserver.onCompleted();
    } catch (Exception e) {
      logger.error("Failed to connect to Couchbase", e);
      responseObserver.onError(e);
    }
  }

  @Override
  public void disconnect(CouchbaseDisconnectRequest request, StreamObserver<CouchbaseDisconnectResponse> responseObserver) {
    try {
      if (!couchbaseConnectionManager.isNotConnected(request.getUserId())) {
        couchbaseConnectionManager.disconnect(request.getUserId());
      }
      logger.info("Successfully disconnected from Couchbase");
      responseObserver.onNext(CouchbaseDisconnectResponse.newBuilder().setSuccess(true).build());
      responseObserver.onCompleted();
    } catch (Exception e) {
      logger.error("Failed to disconnect from Couchbase", e);
      responseObserver.onError(e);
    }
  }

  @Override
  public void status(CouchbaseStatusRequest request, StreamObserver<CouchbaseStatusResponse> responseObserver) {
    boolean connected = !couchbaseConnectionManager.isNotConnected(request.getUserId());
    responseObserver.onNext(CouchbaseStatusResponse.newBuilder().setConnected(connected).build());
    responseObserver.onCompleted();
  }
}
