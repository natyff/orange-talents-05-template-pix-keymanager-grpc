package br.com.zup.chavepix.client

import br.com.zup.chavepix.dto.ChavePixDetalhe
import br.com.zup.chavepix.entities.ChavePix
import br.com.zup.chavepix.entities.ContaAssociada
import br.com.zup.chavepix.enums.TipoConta
import br.com.zup.chavepix.enums.TipoDeChave
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import java.time.LocalDateTime

@Client(value = "http://localhost:8082/")
interface BcbClient {

    @Post("/api/v1/pix/keys",
        produces = [MediaType.APPLICATION_XML],
        consumes = [MediaType.APPLICATION_XML]
    )
    fun create(@Body request: CriaChavePixRequest): HttpResponse<CriaChavePixResponse>

    @Delete("/api/v1/pix/keys/{key}",
        produces = [MediaType.APPLICATION_XML],
        consumes = [MediaType.APPLICATION_XML]
    )
    fun delete(@PathVariable chave: String, @Body request: DeleteChavePixRequest): HttpResponse<DeleteChavePixResponse>

    @Get("/api/v1/pix/keys/{key}",
    consumes = [MediaType.APPLICATION_XML]
    )
    fun findByChave(@PathVariable key: String): HttpResponse<DetalhePixResponse>

data class DetalhePixResponse(
    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
) {
    fun converter(): ChavePixDetalhe {
        return ChavePixDetalhe(
            chave = key,
            tipoChave = keyType.domainType!!,
            tipoConta = when(this.bankAccount.accountType){
                BankAccount.AccountType.CACC -> TipoConta.CONTA_CORRENTE
                BankAccount.AccountType.SVGS -> TipoConta.CONTA_POUPANCA
            },
            conta = ContaAssociada(
                instituicao = bankAccount.participant,
                nomeTitular = owner.name,
                cpf = owner.taxIdNumber,
                agencia = bankAccount.branch,
                numeroConta = bankAccount.accountNumber
            ),
            criadoEm = createdAt
        )
    }
}



data class CriaChavePixRequest(
    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner
){

    companion object {

        fun of(chave: ChavePix): CriaChavePixRequest {
            return CriaChavePixRequest(
                keyType = PixKeyType.by(chave.tipoChave),
                key = chave.chave,
                bankAccount = BankAccount(
                    participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
                    branch = chave.conta.agencia,
                    accountNumber = chave.conta.numeroConta,
                    accountType = BankAccount.AccountType.by(chave.tipoConta),
                ),
                owner = Owner(
                    type = Owner.OwnerType.NATURAL_PERSON,
                    name = chave.conta.nomeTitular,
                    taxIdNumber = chave.conta.cpf
                )
            )
        }
    }
}

data class CriaChavePixResponse (
    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
)


data class DeleteChavePixRequest(
    val key: String,
    val participant: String = ContaAssociada.ITAU_UNIBANCO_ISPB,
)

data class DeleteChavePixResponse(
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
)


data class Owner(
    val type: OwnerType,
    val name: String,
    val taxIdNumber: String
) {
    enum class OwnerType {
    NATURAL_PERSON,
    LEGAL_PERSON
    }
}

data class BankAccount(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
) {

    enum class AccountType() {

        CACC, // conta Corrente
        SVGS; // conta poupanca

        companion object {
            fun by(domainType: TipoConta): AccountType {
                return when (domainType) {
                    TipoConta.CONTA_CORRENTE -> CACC
                    TipoConta.CONTA_POUPANCA -> SVGS
                }
            }
        }
    }
}

enum class PixKeyType(val domainType: TipoDeChave?) {

    CPF(TipoDeChave.CPF),
    CNPJ(null),
    PHONE(TipoDeChave.CELULAR),
    EMAIL(TipoDeChave.EMAIL),
    RANDOM(TipoDeChave.ALEATORIA);

    companion object {

        private val mapping = PixKeyType.values().associateBy(PixKeyType::domainType)
        fun by(domainType: TipoDeChave): PixKeyType {
            return  mapping[domainType] ?: throw IllegalArgumentException("Tipo de chave PIX é invalido ou não foi encontrado $domainType")
        }
    }
}
}