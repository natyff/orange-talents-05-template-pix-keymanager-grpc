package br.com.zup.chavepix.endpoint

import br.com.zup.chavepix.ChavePixRequest
import br.com.zup.chavepix.KeyManagerGrpcServiceGrpc
import br.com.zup.chavepix.chavepix.DadosDaContaResponse
import br.com.zup.chavepix.chavepix.InstituicaoResponse
import br.com.zup.chavepix.chavepix.TitularResponse
import br.com.zup.chavepix.client.BcbClient
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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(
    transactional = false,
)

class CriaChavePixEndpointTest(
    val grpcClient: KeyManagerGrpcServiceGrpc
    .KeyManagerGrpcServiceBlockingStub, val chavePixRepository: ChavePixRepository
) {


    @Inject
    lateinit var itauClient: ContasItauClient

    @Inject
    lateinit var bcbClient: BcbClient

    companion object {
        val CLIENTE_ID: UUID = UUID.randomUUID()
    }


    @BeforeEach
    fun setup(){
        chavePixRepository.deleteAll()
    }

//    @AfterEach
//    internal fun tearDown(){
//        chavePixRepository.deleteAll()
//    }

    @Test
    fun `deve registrar nova chave pix com sucesso`() {
        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(chavePixResponseMock()))

        `when`(bcbClient.create(chavePixRequest())).thenReturn(HttpResponse.created(chavePixResponse()))

        val response = grpcClient.criaChavePix(
            ChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setTipoDeChave(br.com.zup.chavepix.TipoDeChave.CPF)
                .setTipoConta(br.com.zup.chavepix.TipoConta.CONTA_CORRENTE)
                .setValorChave("02467781054")
                .build()
        )


        with(response) {
            assertEquals(CLIENTE_ID.toString(), clienteId)
            assertNotNull(id)
        }
    }


    @Test
    fun `nao deve cadastrar pois os dados estao incorretos`() {

        val request = assertThrows<StatusRuntimeException> {
            grpcClient.criaChavePix(
                ChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoDeChave(br.com.zup.chavepix.TipoDeChave.CPF)
                    .setTipoConta(br.com.zup.chavepix.TipoConta.CONTA_CORRENTE)
                    .setValorChave("02467781054_")
                    .build()
            )
        }
        with(request) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Os dados do cliente está invalido", status.description)
        }
    }

    @Test
    fun `nao deve cadastrar pois os dados ja estao cadastrados`() {

        chavePixRepository.save(chaveDuplicada(
            tipoChave =TipoDeChave.CPF,
            chave ="02467781054",
            clienteId = CLIENTE_ID
        ))

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.criaChavePix(
                ChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoDeChave(br.com.zup.chavepix.TipoDeChave.CPF)
                    .setTipoConta(br.com.zup.chavepix.TipoConta.CONTA_CORRENTE)
                    .setValorChave("02467781054")
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("A chave PIX já existe", status.description)
        }

    }

    @Test
    fun `nao deve registar se nao for registrado no BCB`(){
        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(chavePixResponseMock()))

        `when`(bcbClient.create(chavePixRequest()))
            .thenReturn(HttpResponse.badRequest())

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.criaChavePix(ChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setTipoDeChave(br.com.zup.chavepix.TipoDeChave.CPF)
                .setTipoConta(br.com.zup.chavepix.TipoConta.CONTA_CORRENTE)
                .setValorChave("02467781054")
                .build())
        }

        with(thrown){
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Erro ao registrar a chave PIX no Bacen", status.description)
        }
    }


    private fun dados(clienteId: UUID,
                      tipoDeChave: TipoDeChave,
                      tipoConta: TipoConta,
                      valorChave: String,
    ): ChavePix {
           return ChavePix(
               clienteId =  CLIENTE_ID,
               tipoChave = TipoDeChave.CPF,
               tipoConta =  TipoConta.CONTA_CORRENTE,
               "",
               ContaAssociada("", "", "", "", "")
           )
    }

    private fun chavePixRequest(): BcbClient.CriaChavePixRequest {
        return BcbClient.CriaChavePixRequest(
            keyType = BcbClient.PixKeyType.CPF,
            key = "02467781054",
            bankAccount = bankAccount(),
            owner = owner()
        )
    }

    private fun chavePixResponse(): BcbClient.CriaChavePixResponse {
        return BcbClient.CriaChavePixResponse(
            keyType = BcbClient.PixKeyType.CPF,
            key = "02467781054",
            bankAccount = bankAccount(),
            createdAt = LocalDateTime.now(),
            owner = owner()
        )
    }
    private fun bankAccount(): BcbClient.BankAccount {
        return BcbClient.BankAccount(
            participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
            accountNumber = "",
            accountType = BcbClient.BankAccount.AccountType.CACC,
            branch = ""
        )
    }

    private fun owner(): BcbClient.Owner {
        return BcbClient.Owner(
            type = BcbClient.Owner.OwnerType.NATURAL_PERSON,
            name = "",
            taxIdNumber = "02467781054"
        )
    }


    @MockBean(ContasItauClient::class)
    fun mockItau(): ContasItauClient? {
        return Mockito.mock(ContasItauClient::class.java)
    }

    @MockBean(BcbClient::class)
    fun mockBcb(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub {
            return KeyManagerGrpcServiceGrpc.newBlockingStub(channel)
        }
    }



    private fun chavePixResponseMock(): DadosDaContaResponse {
        return DadosDaContaResponse(
            agencia = "0001",
            instituicao = InstituicaoResponse("ITAÚ UNIBANCO S.A.", "60701190"),
            numero = "291900",
            tipo = "CONTA_CORRENTE",
            titular = TitularResponse("Rafael M C Ponte","02467781054")
        )
    }


    private fun chaveDuplicada(
        tipoChave: TipoDeChave,
        chave: String = UUID.randomUUID().toString(),
        clienteId: UUID = UUID.randomUUID(),
    ): ChavePix {
        return ChavePix(
            clienteId = clienteId,
            tipoChave = tipoChave,
            chave = chave,
            tipoConta = TipoConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "ITAÚ UNIBANCO S.A.",
                nomeTitular = "Rafael Ponte",
                cpf = "02467781054",
                agencia = "1218",
                numeroConta = "291900"
            )
        )
    }
}




