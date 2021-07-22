package br.com.zup.chavepix.service

import br.com.zup.chavepix.RemoveChavePixRequest
import br.com.zup.chavepix.client.BcbClient
import br.com.zup.chavepix.entities.ChavePix
import br.com.zup.chavepix.handler.ChavePixNaoCadastradaException
import br.com.zup.chavepix.repository.ChavePixRepository
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import java.lang.IllegalStateException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid


@Validated
@Singleton
class RemoveChavePixService(@Inject val chavePixRepository: ChavePixRepository,
                            @Inject val bcbClient: BcbClient
) {

    @Transactional
    fun removeChave(@Valid request: RemoveChavePixRequest) {

        val pixId = UUID.fromString(request.id)
        val clienteId = UUID.fromString(request.clienteId)

        val chaveEncontrada= chavePixRepository.findByIdAndClienteId(pixId, clienteId)
        .orElseThrow{
            ChavePixNaoCadastradaException("A chave informada não foi encontrada ou não pertence ao cliente")}

        chavePixRepository.delete(chaveEncontrada)

        val deletaChave = BcbClient.DeleteChavePixRequest(chaveEncontrada.chave)
        val bcbResponse = bcbClient.delete(chaveEncontrada.chave, deletaChave)
        if(bcbResponse.status != HttpStatus.OK){
            throw IllegalStateException("Erro ao remover chave Pix do BACEN")
        }
    }
}