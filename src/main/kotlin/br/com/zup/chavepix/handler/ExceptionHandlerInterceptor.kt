package br.com.zup.chavepix.handler

import io.grpc.BindableService
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import org.slf4j.LoggerFactory
import javax.inject.Singleton


@Singleton
@InterceptorBean(ErrorHandler::class)
class ExceptionHandlerInterceptor (private val solve: ExceptionHandlerSolve) :
    MethodInterceptor<BindableService, Any?> {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun intercept(context: MethodInvocationContext<BindableService, Any?>): Any? {

        return try {
            context.proceed()
        } catch (ex: Exception) {
            logger.error("Handling the exception '${ex.javaClass.name}' while processing the call: ${context.targetMethod}", ex)

            val handler = solve.resolve(ex) as ExceptionHandler<Exception>
            val status = handler.handle(ex)

            GrpcEndpointArguments(context).response().onError(status.asRuntimeException())

            return null
        }
    }
}

class GrpcEndpointArguments(val context: MethodInvocationContext<BindableService, Any?>) {
    fun response(): StreamObserver<*> {
        return context.parameterValues[1] as StreamObserver<*>
    }
}