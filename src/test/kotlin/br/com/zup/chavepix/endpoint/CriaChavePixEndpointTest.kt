package br.com.zup.chavepix.endpoint

import br.com.zup.chavepix.ChavePixRequest
import br.com.zup.chavepix.KeyManagerGrpcServiceGrpc
import br.com.zup.chavepix.client.ContasItauClient
import br.com.zup.chavepix.dto.NovaChavePix
import br.com.zup.chavepix.entities.ChavePix
import br.com.zup.chavepix.entities.ContaAssociada
import br.com.zup.chavepix.enums.TipoConta
import br.com.zup.chavepix.enums.TipoDeChave
import br.com.zup.chavepix.handler.ChavePixExistenteException
import br.com.zup.chavepix.repository.ChavePixRepository
import br.com.zup.chavepix.service.NovaChavePixService
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.annotation.TransactionMode
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.lang.IllegalStateException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(
    transactional = false,
    transactionMode = TransactionMode.SINGLE_TRANSACTION
)

class CriaChavePixEndpointTest(
    val grpcClient: KeyManagerGrpcServiceGrpc
    .KeyManagerGrpcServiceBlockingStub, val chavePixRepository: ChavePixRepository
) {
    @Inject
    lateinit var service: NovaChavePixService

    @Inject
    lateinit var itauClient: ContasItauClient

    val CLIENTE_ID: UUID = UUID.randomUUID()

    @BeforeEach
    internal fun setup(){
        chavePixRepository.deleteAll()
    }

    @AfterEach
    internal fun tearDown(){
        chavePixRepository.deleteAll()
    }

    @Test
    fun `deve registrar nova chave pix com sucesso`() {

        `when`(service!!.cria(chavePixMock())).`thenReturn`(chavePixResponseMock())

        val response = grpcClient.criaChavePix(
            ChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setTipoDeChave(ChavePixRequest.TipoDeChave.CPF)
                .setTipoConta(ChavePixRequest.TipoConta.CONTA_CORRENTE)
                .setValorChave("02467781054")
                .build()
        )

        with(response) {
            assertEquals(CLIENTE_ID.toString(), clienteId)
            assertNotNull(pixId)
        }
    }

    @Test
    fun `nao deve cadastrar pois os dados estao incorretos`() {

        `when`(service.cria(chavePixMock())).thenThrow(IllegalStateException("Cliente não encontrado"))

        val request = assertThrows<StatusRuntimeException> {
            grpcClient.criaChavePix(
                ChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoDeChave(ChavePixRequest.TipoDeChave.CPF)
                    .setTipoConta(ChavePixRequest.TipoConta.CONTA_CORRENTE)
                    .setValorChave("02467781054")
                    .build()
            )
        }
        with(request) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Os dados do cliente está invalido", status.description)
        }
    }

    @Test
    fun `nao deve cadastrar pois os dados já estao cadastrados`() {

        `when`(service.cria(chavePixMock())).thenThrow(ChavePixExistenteException("Chave Pix já existe"))

        val dadosDuplicados = dados(
            clienteId = CLIENTE_ID,
            tipoDeChave =  TipoDeChave.CPF,
            tipoConta = TipoConta.CONTA_CORRENTE,
            valorChave = "02467781054"
        )
        chavePixRepository.save(dadosDuplicados)

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.criaChavePix(
                ChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoDeChave(ChavePixRequest.TipoDeChave.CPF)
                    .setTipoConta(ChavePixRequest.TipoConta.CONTA_CORRENTE)
                    .setValorChave("02467781054")
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("A chave PIX já existe", status.description)
        }

    }

    private fun dados(clienteId: UUID,
                      tipoDeChave: TipoDeChave,
                      tipoConta: TipoConta,
                      valorChave: String): ChavePix {
           return ChavePix(
               clienteId =  CLIENTE_ID,
               tipoChave = TipoDeChave.CPF,
               tipoConta =  TipoConta.CONTA_CORRENTE,
               "",
               ContaAssociada("", "", "", "", "")
           )
    }


    @MockBean(NovaChavePixService::class)
    fun service(): NovaChavePixService? {
        return Mockito.mock(NovaChavePixService::class.java)
    }

    @MockBean(ContasItauClient::class)
    fun mockItau(): ContasItauClient? {
        return Mockito.mock(ContasItauClient::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub {
            return KeyManagerGrpcServiceGrpc.newBlockingStub(channel)
        }
    }


    private fun chavePixMock(): NovaChavePix {
        val conta = NovaChavePix(
            CLIENTE_ID.toString(),
            TipoDeChave.CPF,
            "02467781054",
            TipoConta.CONTA_CORRENTE
        )
        return conta
    }

    private fun chavePixResponseMock(): ChavePix {
        val mockKey = ChavePix(
            CLIENTE_ID,
            TipoDeChave.CPF,
            TipoConta.CONTA_CORRENTE,
            "",
            ContaAssociada("", "", "", "", "")
        )
        mockKey.id = CLIENTE_ID
        return mockKey
    }
}




