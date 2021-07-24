package br.com.zup.chavepix.service

import br.com.zup.chavepix.client.BcbClient
import br.com.zup.chavepix.entities.ChavePix
import br.com.zup.chavepix.client.ContasItauClient

import br.com.zup.chavepix.dto.NovaChavePix
import br.com.zup.chavepix.handler.ChavePixExistenteException
import br.com.zup.chavepix.repository.ChavePixRepository
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val itauClient: ContasItauClient,
    @Inject val bcbClient: BcbClient
) {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun cria(@Valid novaChavePix: NovaChavePix): ChavePix {


        if (chavePixRepository.existsByChave(novaChavePix.chave))
            throw ChavePixExistenteException("Chave Pix '${novaChavePix.chave}' já existe")

        val response = itauClient.buscaContaPorTipo(novaChavePix.clienteId!!, novaChavePix.tipoConta!!.name)
        val conta = response.body()?.toModel() ?: throw IllegalStateException("Cliente não encontrado")

        val chave = novaChavePix.toModel(conta)
        chavePixRepository.save(chave)

        //regirstra a chave no BCB
        val bcbRequest = BcbClient.CriaChavePixRequest.of(chave).also {
            LOGGER.info("Registrando chave PIX no Bacen: $it")
        }


        val bcbResponse = bcbClient.create(bcbRequest)
        if(bcbResponse.status != HttpStatus.CREATED)
            throw IllegalStateException("Erro ao registrar a chave PIX no Bacen")

        //atualiza chave do dominio com chave gerada pelo BCB
        chave.atualiza(bcbResponse.body()!!.key)

        return chave
    }
}