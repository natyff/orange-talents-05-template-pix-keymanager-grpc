package br.com.zup.chavepix.entities

import javax.persistence.Embeddable

@Embeddable
data class ContaAssociada(
    val instituicao: String,
    val nomeTitular: String,
    val cpf: String,
    val agencia: String,
    val numeroConta: String,


) {
    companion object {
        public val ITAU_UNIBANCO_ISPB: String = "60701190"
    }
}
