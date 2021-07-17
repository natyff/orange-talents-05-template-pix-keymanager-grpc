package br.com.zup.chavepix.service

import br.com.zup.chavepix.RemoveChavePixRequest
import br.com.zup.chavepix.entities.ChavePix
import br.com.zup.chavepix.handler.ChavePixNaoCadastradaException
import br.com.zup.chavepix.repository.ChavePixRepository
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid


@Validated
@Singleton
class RemoveChavePixService(@Inject val chavePixRepository: ChavePixRepository
) {

    @Transactional
    fun removeChave(@Valid request: RemoveChavePixRequest) {

        val chaveEncontrada: Optional<ChavePix> = chavePixRepository.findByIdAndClienteId(UUID.fromString(request.pixId),
            UUID.fromString(request.clienteId))
        if (chaveEncontrada.isPresent) {
            chavePixRepository.delete(chaveEncontrada.get())
            return
        }
        throw ChavePixNaoCadastradaException("A chave informada não foi encontrada ou não pertence ao cliente")
    }

}