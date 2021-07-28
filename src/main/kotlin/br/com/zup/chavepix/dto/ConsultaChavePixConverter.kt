package br.com.zup.chavepix.dto

import br.com.zup.chavepix.ConsultaChavePixResponse
import br.com.zup.chavepix.TipoConta
import br.com.zup.chavepix.TipoDeChave
import com.google.protobuf.Timestamp
import java.time.ZoneId


class ConsultaChavePixConverter {

    fun converter(chavePixDetalhe: ChavePixDetalhe): ConsultaChavePixResponse{
        return ConsultaChavePixResponse.newBuilder()
        .setClienteId(chavePixDetalhe.clienteId?.toString() ?:"")
            .setPixId(chavePixDetalhe.pixId?.toString() ?: "")
            .setChave(ConsultaChavePixResponse.ChavePix.newBuilder()
                .setTipo(TipoDeChave.valueOf(chavePixDetalhe.tipoChave.name))
                .setChave(chavePixDetalhe.chave)
                .setConta(ConsultaChavePixResponse.ChavePix.ContaInfo.newBuilder()
                    .setTipo(TipoConta.valueOf(chavePixDetalhe.tipoConta.name))
                    .setInstituicao(chavePixDetalhe.conta.instituicao)
                    .setNomeDoTitular(chavePixDetalhe.conta.nomeTitular)
                    .setCpfDoTitular(chavePixDetalhe.conta.cpf)
                    .setAgencia(chavePixDetalhe.conta.agencia)
                    .setNumeroDaConta(chavePixDetalhe.conta.numeroConta)
                    .build()
                )
                .setCriadaEm(chavePixDetalhe.criadoEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                }))
            .build()
    }
}