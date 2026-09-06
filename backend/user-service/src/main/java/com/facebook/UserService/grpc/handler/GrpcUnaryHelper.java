package com.facebook.UserService.grpc.handler;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class GrpcUnaryHelper {

    private static final Logger log = LoggerFactory.getLogger(GrpcUnaryHelper.class);

    public <T> void handleUnary(Supplier<T> action,
                                StreamObserver<T> responseObserver,
                                String errorMsg) {
        try {
            responseObserver.onNext(action.get());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("User not found")) {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                        .withDescription(e.getMessage())
                        .asRuntimeException());
                return;
            }
            log.error(errorMsg, e);
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            log.error(errorMsg, e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}
