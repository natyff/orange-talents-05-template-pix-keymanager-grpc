package br.com.zup.chavepix.entities


import br.com.zup.chavepix.enums.TipoConta
import br.com.zup.chavepix.enums.TipoDeChave
import org.hibernate.annotations.Type
import java.util.*
import javax.persistence.*

import javax.validation.Valid
import javax.validation.constraints.NotNull

@Entity
class ChavePix(

    @field:NotNull @Type(type="uuid-char")  val clienteId: UUID,
    @field:NotNull @field:Enumerated(EnumType.STRING) val tipoChave: TipoDeChave,
    @field:NotNull @field:Enumerated(EnumType.STRING) val tipoConta: TipoConta,
    @field:NotNull var chave: String,
    @field:Valid @Embedded val conta: ContaAssociada
) {
    @Id
    @GeneratedValue
    @Type(type = "uuid-char")
    lateinit var id: UUID


    fun isAleatoria(): Boolean {
        return tipoChave == TipoDeChave.ALEATORIA
    }

    fun atualiza(chave: String): Boolean {
        if (isAleatoria()) {
            this.chave = chave
            return true
        }
        return false
    }
}