package br.com.zup.chavepix.chavepix

import br.com.zup.chavepix.entities.ContaAssociada

data class DadosDaContaResponse(
    val tipo: String,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
) {

    fun toModel(): ContaAssociada {
        return ContaAssociada(
            instituicao = this.instituicao.nome,
            nomeTitular = this.titular.nome,
            cpf = this.titular.cpf,
            agencia = this.agencia,
            numeroConta = this.numero
        )
    }

}

data class TitularResponse(val nome: String, val cpf: String)
data class InstituicaoResponse(val nome: String, val ispb: String)
