package br.com.zup.chavepix.endpoint

import br.com.zup.chavepix.ConsultaChavePixGrpcServiceGrpc
import br.com.zup.chavepix.ConsultaChavePixRequest
import br.com.zup.chavepix.ConsultaChavePixResponse
import br.com.zup.chavepix.client.BcbClient
import br.com.zup.chavepix.dto.ChavePixDetalhe
import br.com.zup.chavepix.dto.ConsultaChavePixConverter
import br.com.zup.chavepix.dto.ValidaFiltro
import br.com.zup.chavepix.handler.ErrorHandler
import br.com.zup.chavepix.repository.ChavePixRepository

import io.grpc.stub.StreamObserver
import javax.validation.Validator
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException


@Singleton
@ErrorHandler
class ConsultaChavePixEndpoint(@Inject private val chavePixRepository: ChavePixRepository,
                                @Inject private val bcbClient: BcbClient,
                                @Inject private val validator: Validator):
    ConsultaChavePixGrpcServiceGrpc.ConsultaChavePixGrpcServiceImplBase() {

    override fun consulta(
        request: ConsultaChavePixRequest,
        responseObserver: StreamObserver<ConsultaChavePixResponse>
    ) {

        val validaFiltro = request.toModel(validator)
        val chavePixDetalhe = validaFiltro.busca(chavePixRepository, bcbClient)

        responseObserver.onNext(
            ConsultaChavePixConverter().converter(chavePixDetalhe))
        responseObserver.onCompleted()
    }
}


fun ConsultaChavePixRequest.toModel(validator: Validator): ValidaFiltro {

    val validaFiltro = when (filtroCase!!) {
        ConsultaChavePixRequest.FiltroCase.ID -> id.let {
            ValidaFiltro.ComPixId(clientId = it.clienteId, pixId = it.pixId)
        }
        ConsultaChavePixRequest.FiltroCase.CHAVE -> ValidaFiltro.PorChave(chave)
        ConsultaChavePixRequest.FiltroCase.FILTRO_NOT_SET -> ValidaFiltro.Invalido()
    }

    val violations = validator.validate(validaFiltro)
    if(violations.isNotEmpty()){
        throw ConstraintViolationException(violations)
    }
    return validaFiltro
}
