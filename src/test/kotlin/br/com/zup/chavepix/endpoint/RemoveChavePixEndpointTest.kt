package br.com.zup.chavepix.endpoint


import br.com.zup.chavepix.ChavePixRequest
import br.com.zup.chavepix.RemoveChavePixGrpcServiceGrpc
import br.com.zup.chavepix.RemoveChavePixRequest
import br.com.zup.chavepix.RemoveChavePixResponse
import br.com.zup.chavepix.client.ContasItauClient
import br.com.zup.chavepix.entities.ChavePix
import br.com.zup.chavepix.entities.ContaAssociada
import br.com.zup.chavepix.enums.TipoConta
import br.com.zup.chavepix.enums.TipoDeChave
import br.com.zup.chavepix.repository.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RemoveChavePixEndpointTest(
    val grpcClient: RemoveChavePixGrpcServiceGrpc.RemoveChavePixGrpcServiceBlockingStub,
    val chavePixRepository: ChavePixRepository
) {

    var CLIENTE_ID = UUID.randomUUID()
    val mock = chavePixResponseMock()

    @BeforeEach
    internal fun setUp() {
        chavePixRepository.save(mock)
    }

    @AfterEach
    fun cleanUp() {
        chavePixRepository.deleteAll()
    }

    @Inject
    lateinit var itauClient: ContasItauClient

    @Test
    fun `deve excluir chave com sucesso`() {

        val response = grpcClient.remove(
            RemoveChavePixRequest
                .newBuilder()
                .setClienteId(mock.clienteId.toString())
                .setId(mock.id.toString())
                .build()
        )

        with(response) {
            assertEquals(chavePixResponseMock().clienteId.toString(), clienteId)

        }
    }

    @Test
    fun `nao deve remover chave pix quando a chave nao for cadastrado`() {

        val pixNaoCadastrado = UUID.randomUUID().toString()

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.remove(
                RemoveChavePixRequest.newBuilder()
                    .setClienteId(mock.clienteId.toString())
                    .setId(pixNaoCadastrado)
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("A chave informada não foi encontrada ou não pertence ao cliente", status.description)
        }
    }

    @Test
    fun `nao deve remover chave quando ocorrer erro de conexao ao itau`() {
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.remove(RemoveChavePixRequest.newBuilder().build())
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("DadosInvalidos", status.description)
        }
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                RemoveChavePixGrpcServiceGrpc.RemoveChavePixGrpcServiceBlockingStub {
            return RemoveChavePixGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(ContasItauClient::class)
    fun mockItau(): ContasItauClient? {
        return Mockito.mock(ContasItauClient::class.java)
    }

    private fun chavePixResponseMock(): ChavePix {
        val mockKey = ChavePix(
            CLIENTE_ID,
            TipoDeChave.CPF,
            TipoConta.CONTA_CORRENTE,
            "",
            ContaAssociada("", "", "", "", "")
        )
        return mockKey
    }
}
