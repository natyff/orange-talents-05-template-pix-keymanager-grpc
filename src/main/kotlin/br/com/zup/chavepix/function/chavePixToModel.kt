package br.com.zup.chavepix


import br.com.zup.chavepix.dto.NovaChavePix
import br.com.zup.chavepix.enums.TipoConta
import br.com.zup.chavepix.enums.TipoDeChave


fun ChavePixRequest.toModel(): NovaChavePix {
    return NovaChavePix(
        clienteId = clienteId,
        tipo = when (tipoDeChave) {
            br.com.zup.chavepix.TipoDeChave.DESCONHECIDO -> null
            else -> TipoDeChave.valueOf(tipoDeChave.name)
        },
        chave = valorChave,
        tipoConta = when (tipoConta) {
            br.com.zup.chavepix.TipoConta.CONTA_DESCONHECIDO -> null
            else -> TipoConta.valueOf(tipoConta.name)
        }
    )
}