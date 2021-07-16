package br.com.zup.chavepix.endpoint

import br.com.zup.chavepix.ChavePixRequest
import br.com.zup.chavepix.ChavePixResponse
import br.com.zup.chavepix.KeyManagerGrpcServiceGrpc
import br.com.zup.chavepix.service.NovaChavePixService
import br.com.zup.chavepix.handler.ErrorHandler
import br.com.zup.chavepix.toModel

import io.grpc.stub.StreamObserver

import javax.inject.Inject
import javax.inject.Singleton


@Singleton
@ErrorHandler
class CriaChavePixEndpoint(@Inject private val service: NovaChavePixService) :
    KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceImplBase() {

    override fun criaChavePix(
        request: ChavePixRequest,
        responseObserver: StreamObserver<ChavePixResponse>
    ) {

        val novaChave = request.toModel()
        val chaveCriada = service.cria(novaChave)

        responseObserver.onNext(
            ChavePixResponse.newBuilder()
                .setClienteId(chaveCriada.clienteId.toString())
                .setPixId(chaveCriada.id.toString())
                .build()
        )
        responseObserver.onCompleted()
    }

}