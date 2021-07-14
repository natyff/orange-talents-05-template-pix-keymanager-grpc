package br.com.zup.chavepix.handler

import br.com.zup.chavepix.ChavePixRequest
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import javax.inject.Singleton
import javax.validation.ConstraintViolationException
import javax.validation.ValidationException

@Singleton
@InterceptorBean(ErrorHandler::class)
class ErrorHandlerInterceptor : MethodInterceptor<ChavePixRequest, Any> {

    override fun intercept(context: MethodInvocationContext<ChavePixRequest, Any>): Any? {

        try {
            return context.proceed()
        } catch (ex: Exception) {
            val responseObserver = context.parameterValues[1] as StreamObserver<*>

            val status = when (ex) {
                is ConstraintViolationException -> Status.INVALID_ARGUMENT
                    .withCause(ex)
                    .withDescription("O tipo de chave informado não existe")
                is ChavePixExistenteException -> Status.ALREADY_EXISTS
                    .withCause(ex)
                    .withDescription("A chave informada já está cadastrada")
                is ValidationException -> Status.INVALID_ARGUMENT
                    .withCause(ex)
                    .withDescription("O tipo de chave informado não corresponde a chave cadastrada")
                else -> Status.UNKNOWN.withCause(ex).withDescription("Erro Desconhecido")
            }

            responseObserver.onError(status.asRuntimeException())
        }
        return null
    }
}