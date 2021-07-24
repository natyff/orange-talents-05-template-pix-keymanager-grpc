package br.com.zup.chavepix.handler


import io.grpc.Status
import java.lang.AssertionError

class DefaultExceptionHandler : ExceptionHandler<Exception> {

    override fun handle(exp: Exception): ExceptionHandler.StatusWithDetails {
        val status = when (exp) {
            is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(exp.message)
            is IllegalStateException -> Status.FAILED_PRECONDITION.withDescription(exp.message)
            is AssertionError -> Status.ALREADY_EXISTS.withDescription(exp.message)
            else -> Status.UNKNOWN
        }
        return ExceptionHandler.StatusWithDetails(status.withCause(exp))
    }

    override fun supports(exp: Exception): Boolean {
        return true
    }

}