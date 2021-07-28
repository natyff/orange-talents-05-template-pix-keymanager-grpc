package br.com.zup.chavepix.dto

import br.com.zup.chavepix.client.BcbClient
import br.com.zup.chavepix.handler.ChavePixNaoCadastradaException
import br.com.zup.chavepix.repository.ChavePixRepository
import br.com.zup.chavepix.validation.ValidUUID
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class ValidaFiltro {

    abstract fun busca(chavePixRepository: ChavePixRepository, bcbClient: BcbClient): ChavePixDetalhe

        @Introspected
        data class ComPixId(
            @field: NotBlank @field:ValidUUID
            val pixId: String,

            @field:NotBlank @field:ValidUUID
            val clientId: String
        ): ValidaFiltro(){
            override fun busca(chavePixRepository: ChavePixRepository, bcbClient: BcbClient): ChavePixDetalhe {
                return chavePixRepository.findById(UUID.fromString(pixId))
                    .filter { it.pertenceAo(UUID.fromString(clientId))}
                    .map(ChavePixDetalhe::of)
                    .orElseThrow {ChavePixNaoCadastradaException("Chave pix não encontrada")}
            }
        }

    @Introspected
    data class PorChave(
        @field:NotBlank @field:Size(max=77) val chave:String): ValidaFiltro(){

            private val LOGGER = LoggerFactory.getLogger(this::class.java)

        override fun busca(chavePixRepository: ChavePixRepository, bcbClient: BcbClient): ChavePixDetalhe {
            return chavePixRepository.findByChave(chave)
                .map(ChavePixDetalhe::of)
                .orElseGet{
                    LOGGER.info("Consultando chave pix '$chave' no Bacen")

                    val response = bcbClient.findByChave(chave)
                    when(response.status){
                        HttpStatus.OK -> response.body()?.converter()
                        else -> throw ChavePixNaoCadastradaException("chave não encotrada")
                    }
                }
        }
    }

    @Introspected
    class Invalido(): ValidaFiltro(){
        override fun busca(chavePixRepository: ChavePixRepository, bcbClient: BcbClient): ChavePixDetalhe {
            throw IllegalArgumentException("Chave pix invalida ou não informada")
        }
    }
}