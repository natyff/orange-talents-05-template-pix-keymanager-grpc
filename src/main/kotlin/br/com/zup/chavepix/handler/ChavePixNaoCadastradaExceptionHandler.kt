package br.com.zup.chavepix.handler


import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ChavePixNaoCadastradaExceptionHandler: ExceptionHandler<ChavePixNaoCadastradaException>{

    override fun handle(exception: ChavePixNaoCadastradaException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(
            Status.NOT_FOUND.withDescription(exception.message).withCause(exception)
        )
    }

    override fun supports(exp: Exception): Boolean {
        return exp is ChavePixNaoCadastradaException
    }
}