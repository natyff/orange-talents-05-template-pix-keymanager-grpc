package br.com.zup.chavepix.client

import br.com.zup.chavepix.chavepix.DadosDaContaResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client


@Client(value = "http://localhost:9091")
interface ContasItauClient {

    @Get(uri = "/api/v1/clientes/{clienteId}/contas")
    fun buscaContaPorTipo(@PathVariable clienteId: String, @QueryValue tipo: String)
            : HttpResponse<DadosDaContaResponse>

}
