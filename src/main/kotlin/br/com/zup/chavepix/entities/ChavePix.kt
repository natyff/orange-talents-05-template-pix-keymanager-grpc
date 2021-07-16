package br.com.zup.chavepix.entities


import br.com.zup.chavepix.enums.TipoConta
import br.com.zup.chavepix.enums.TipoDeChave
import java.util.*
import javax.persistence.*

import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@Entity
class ChavePix(

    @field:NotNull val clienteId: UUID,
    @field:NotNull @field:Enumerated(EnumType.STRING) val tipoDeChave: TipoDeChave,
    @field:NotNull @field:Enumerated(EnumType.STRING) val tipoConta: TipoConta,
    @field:NotNull var chave: String,
    @field:Valid @Embedded val conta: ContaAssociada
) {
    @Id
    @GeneratedValue
    lateinit var id: UUID
}