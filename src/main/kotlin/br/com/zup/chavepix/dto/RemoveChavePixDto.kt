package br.com.zup.chavepix.dto

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
data class RemoveChavePixDto(
    @field:NotBlank val clienteId: String?,
    @field:NotBlank val chavePix: String,
) {
}