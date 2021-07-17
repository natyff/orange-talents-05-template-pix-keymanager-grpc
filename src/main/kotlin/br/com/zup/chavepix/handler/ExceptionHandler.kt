package br.com.zup.chavepix.handler

import io.grpc.Metadata
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto

interface ExceptionHandler <in E: Exception>  {
    // trata exceção e a mapeia para StatusWithDetails
    fun handle(e: E): StatusWithDetails

    // Indica se a implementação do Handler sabe lidar com a exceção
    fun supports(e: Exception) : Boolean

    //  Wrapper para Status e Metadata
    data class StatusWithDetails(val status: Status, val metadata: Metadata = Metadata()){
        constructor(se: StatusRuntimeException): this(se.status, se.trailers ?: Metadata())
        constructor(sp: com.google.rpc.Status): this(StatusProto.toStatusRuntimeException(sp))

        fun asRuntimeException(): StatusRuntimeException {
            return status.asRuntimeException(metadata)
        }
    }
}