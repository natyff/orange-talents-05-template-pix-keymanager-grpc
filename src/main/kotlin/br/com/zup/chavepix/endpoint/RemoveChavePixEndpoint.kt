package br.com.zup.chavepix.endpoint

import br.com.zup.chavepix.RemoveChavePixGrpcServiceGrpc
import br.com.zup.chavepix.RemoveChavePixRequest
import br.com.zup.chavepix.RemoveChavePixResponse
import br.com.zup.chavepix.handler.ErrorHandler
import br.com.zup.chavepix.service.RemoveChavePixService
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class RemoveChavePixEndpoint(@Inject private val service: RemoveChavePixService):
    RemoveChavePixGrpcServiceGrpc.RemoveChavePixGrpcServiceImplBase() {

    override fun remove(request: RemoveChavePixRequest,
                        responseObserver: StreamObserver<RemoveChavePixResponse>) {

        val removeChave = service.removeChave(request)

        responseObserver.onNext(RemoveChavePixResponse.newBuilder()
            .setClienteId(request.clienteId)
            .build())
        responseObserver.onCompleted()
    }
}