package br.com.zup.chavepix.endpoint

import br.com.zup.chavepix.*

import br.com.zup.chavepix.client.BcbClient
import br.com.zup.chavepix.entities.ChavePix
import br.com.zup.chavepix.entities.ContaAssociada
import br.com.zup.chavepix.enums.TipoConta
import br.com.zup.chavepix.enums.TipoDeChave
import br.com.zup.chavepix.extention.violations
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
import org.testcontainers.shaded.org.bouncycastle.asn1.x500.style.RFC4519Style.owner
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


@MicronautTest(transactional = false)
internal class ConsultaChavePixEndpointTest(
    private val chavePixRepository: ChavePixRepository,
    private val grpcClient: ConsultaChavePixGrpcServiceGrpc.ConsultaChavePixGrpcServiceBlockingStub
){
    @Inject
    lateinit var bcbClient: BcbClient


    @BeforeEach
    fun setUp(){
        chavePixRepository.save(chaveFake(tipoDeChave = TipoDeChave.CPF, chave = "86135457004", clientId = UUID.randomUUID()))
        chavePixRepository.save(chaveFake(tipoDeChave = TipoDeChave.EMAIL, chave = "yuri.matheus@zup.com.br", clientId = UUID.randomUUID()))
        chavePixRepository.save(chaveFake(tipoDeChave = TipoDeChave.ALEATORIA, chave = "18cdca9e-1bc5-4924-a2f7-5981238767b3", clientId = UUID.randomUUID()))
        chavePixRepository.save(chaveFake(tipoDeChave = TipoDeChave.CELULAR, chave = "+5511999999999", clientId = UUID.randomUUID()))
    }

    @AfterEach
    fun cleanUp() {
        chavePixRepository.deleteAll()
    }


    @Test
    fun `deve consultar chave por id e clienteId com sucesso`(){
        val chaveExistente = chavePixRepository.findByChave("86135457004").get()

        val response  = grpcClient.consulta(ConsultaChavePixRequest.newBuilder()
            .setId(ConsultaChavePixRequest.FiltroPorPixId.newBuilder()
                .setPixId(chaveExistente.id.toString())
                .setClienteId(chaveExistente.clienteId.toString())
                .build())
            .build())

        with(response){
            assertEquals(chaveExistente.id.toString(), pixId)
            assertEquals(chaveExistente.clienteId.toString(), clienteId.toString())
            assertEquals(chaveExistente.tipoChave.name, chave.tipo.name)
            assertEquals(chaveExistente.chave, chave.chave)
        }
    }

    @Test
    fun `deve consultar chave por valor quando existir no banco local`(){
        val chaveExistente = chavePixRepository.findByChave("86135457004").get()
        val responseValorChave = grpcClient.consulta(
            ConsultaChavePixRequest.newBuilder()
                .setChave("86135457004")
                .build())

        with(responseValorChave){
            assertEquals(chaveExistente.id.toString(), pixId)
            assertEquals(chaveExistente.clienteId.toString(), clienteId.toString())
            assertEquals(chaveExistente.tipoChave.name, chave.tipo.name)
            assertEquals(chaveExistente.chave, chave.chave)
        }
    }

    @Test
    fun `nao deve consultar chave por pixId e clienteId quando a validacao do filtro for invalido`(){
         val thrown  = assertThrows<StatusRuntimeException>{
             grpcClient.consulta(ConsultaChavePixRequest.newBuilder()
                       .setId(
                           ConsultaChavePixRequest.FiltroPorPixId.newBuilder()
                            .setClienteId("")
                            .setPixId("")
                            .build())
                 .build())
         }

        with(thrown){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertThat(violations(), containsInAnyOrder(
                Pair("pixId", "não deve estar em branco"),
                Pair("clientId", "não deve estar em branco"),
                Pair("pixId", "não é um formato válido"),
                Pair("clientId", "não é um formato válido")
            ))

        }
    }

    @Test
    fun `nao deve consultar chave quando o registro nao existir`(){
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(ConsultaChavePixRequest.newBuilder()
                .setId(ConsultaChavePixRequest.FiltroPorPixId.newBuilder()
                    .setPixId(UUID.randomUUID().toString())
                    .setClienteId(UUID.randomUUID().toString())
                    .build())
                .build())
        }
        with(thrown){
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada", status.description)
        }
    }



    @Test
    fun`deve consultar a chave quando nao existir no banco mas existir no Bacen`(){

        val bcbResponse = detalhePixResponse()
        `when`(bcbClient.findByChave("outrousuario@zup.com.br")).thenReturn(HttpResponse.ok(detalhePixResponse()))

        val response = grpcClient.consulta(ConsultaChavePixRequest.newBuilder()
            .setChave("outrousuario@zup.com.br")
            .build())

        with(response){
                assertEquals("", pixId)
                assertEquals("", clienteId)
                assertEquals(bcbResponse.keyType.name, chave.tipo.name)
                assertEquals(bcbResponse.key, chave.chave)
        }
    }

    @Test
    fun`nao deve consultar chave por valor quando nao existir registro no banco ou no bacen`(){
        `when`(bcbClient.findByChave("naoexiste@zup.com.br")).thenReturn(HttpResponse.notFound())

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(ConsultaChavePixRequest.newBuilder()
                .setChave("naoexiste@zup.com.br")
                .build())
        }

        with(thrown){
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("chave não encotrada", status.description)
        }
    }


    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                ConsultaChavePixGrpcServiceGrpc.ConsultaChavePixGrpcServiceBlockingStub {
            return ConsultaChavePixGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
    }

    private fun chaveFake(
        tipoDeChave: TipoDeChave,
        chave: String = UUID.randomUUID().toString(),
        clientId: UUID = UUID.randomUUID()
    ): ChavePix {
        return ChavePix(
            clienteId = clientId,
            tipoChave = tipoDeChave,
            chave = chave,
            tipoConta = TipoConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "ITAÚ UNIBANCO S.A.",
                nomeTitular = "Yuri Matheus",
                cpf = "86135457004",
                agencia = "0001",
                numeroConta = "123455"
            )
        )
    }

    private fun detalhePixResponse(): BcbClient.DetalhePixResponse {
        return BcbClient.DetalhePixResponse(
            keyType = BcbClient.PixKeyType.EMAIL,
            key = "outrousuario@zup.com.br",
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )
    }

    private fun bankAccount(): BcbClient.BankAccount {
        return BcbClient.BankAccount(
            participant = "90400888",
            branch = "0001",
            accountNumber = "987654",
            accountType = BcbClient.BankAccount.AccountType.SVGS
        )
    }

    private fun owner(): BcbClient.Owner {
        return BcbClient.Owner(
            type = BcbClient.Owner.OwnerType.NATURAL_PERSON,
            name = "qualquer usuario",
            taxIdNumber = "111111111111"
        )
    }
}