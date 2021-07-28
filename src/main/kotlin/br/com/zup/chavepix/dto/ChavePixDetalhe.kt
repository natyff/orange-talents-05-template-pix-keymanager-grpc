package br.com.zup.chavepix.dto

import br.com.zup.chavepix.entities.ChavePix
import br.com.zup.chavepix.entities.ContaAssociada
import br.com.zup.chavepix.enums.TipoConta
import br.com.zup.chavepix.enums.TipoDeChave
import java.time.LocalDateTime
import java.util.*

data class ChavePixDetalhe(
    val pixId: UUID? = null,
    val clienteId: UUID? = null,
    val tipoChave: TipoDeChave,
    val chave: String,
    val tipoConta: TipoConta,
    val conta: ContaAssociada,
    val criadoEm: LocalDateTime = LocalDateTime.now()
    ){

    companion object {
       fun of(chave: ChavePix): ChavePixDetalhe {
           return ChavePixDetalhe(
              pixId = chave.id,
              clienteId = chave.clienteId,
              tipoChave = chave.tipoChave,
              chave = chave.chave,
              tipoConta = chave.tipoConta,
              conta = chave.conta,
              criadoEm = LocalDateTime.now()
           )
       }
    }
}
