package br.com.zup.chavepix.dto


import br.com.zup.chavepix.entities.ChavePix
import br.com.zup.chavepix.entities.ContaAssociada
import br.com.zup.chavepix.enums.TipoConta
import br.com.zup.chavepix.enums.TipoDeChave
import br.com.zup.chavepix.validation.ValidUUID
import br.com.zup.chavepix.validation.ValidaChavesPix
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidaChavesPix
@Introspected
data class NovaChavePix(
    @ValidUUID
    @field:NotBlank val clienteId: String?,
    @field:NotNull val tipo: TipoDeChave?,
    @field:Size(max = 77) val chave: String,
    @field:NotNull val tipoConta: TipoConta?
) {

    fun toModel(conta: ContaAssociada): ChavePix {
        return ChavePix(
            clienteId = UUID.fromString(this.clienteId),
            tipoChave = TipoDeChave.valueOf(this.tipo!!.name),
            chave = if (this.tipo == TipoDeChave.ALEATORIA) UUID.randomUUID().toString() else this.chave,
            tipoConta = TipoConta.valueOf(this.tipoConta!!.name),
            conta = conta
        )
    }

}



