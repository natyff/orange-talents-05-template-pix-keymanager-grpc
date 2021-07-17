package br.com.zup.chavepix.service

import br.com.zup.chavepix.entities.ChavePix
import br.com.zup.chavepix.client.ContasItauClient
import br.com.zup.chavepix.dto.NovaChavePix
import br.com.zup.chavepix.handler.ChavePixExistenteException
import br.com.zup.chavepix.repository.ChavePixRepository
import io.micronaut.validation.Validated
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val itauClient: ContasItauClient
) {


    @Transactional
    fun cria(@Valid novaChavePix: NovaChavePix): ChavePix {


        if (chavePixRepository.existsByChave(novaChavePix.chave))
            throw ChavePixExistenteException("Chave Pix '${novaChavePix.chave}' já existe")

        val response = itauClient.buscaContaPorTipo(novaChavePix.clienteId!!, novaChavePix.tipoConta!!.name)
        val conta = response.body()?.toModel() ?: throw IllegalStateException("Cliente não encontrado")

        val chave = novaChavePix.toModel(conta)
        chavePixRepository.save(chave)

        return chave
    }
}