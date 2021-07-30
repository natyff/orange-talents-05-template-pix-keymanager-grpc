package br.com.zup.chavepix.endpoint

import br.com.zup.chavepix.*
import br.com.zup.chavepix.handler.ErrorHandler
import br.com.zup.chavepix.repository.ChavePixRepository
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.lang.IllegalArgumentException
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class ListaChavePixEndpoint(@Inject private val chavePixRepository: ChavePixRepository):
ListaChavePixGrpcServiceGrpc.ListaChavePixGrpcServiceImplBase(){

    override fun lista(request: ListaChavePixRequest, responseObserver: StreamObserver<ListaChavePixResponse>) {

        if(request.clienteId.isNullOrEmpty())
            throw IllegalArgumentException("Favor, preencher o identificador do cliente")

        val clienteId = UUID.fromString(request.clienteId)
        val listaChaves = chavePixRepository.findAllByClienteId(clienteId).map {
            ListaChavePixResponse.ChavePix.newBuilder()
                .setPixId(it.id.toString())
                .setTipoDeChave(TipoDeChave.valueOf(it.tipoChave.name))
                .setValorChave(it.chave)
                .setTipoConta(TipoConta.valueOf(it.tipoConta.name))
                .setRegistradaEm(it.registradaEm.let { reg ->
                    val createdAt = reg.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
                .build()
        }
        responseObserver.onNext(ListaChavePixResponse.newBuilder()
            .setClienteId(clienteId.toString())
            .addAllChaves(listaChaves)
            .build())
        responseObserver.onCompleted()
    }
}