package br.com.zup.chavepix.validation

import br.com.zup.chavepix.dto.NovaChavePix
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton
import javax.validation.Constraint


import javax.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidaChavesPixValidator::class])
annotation class ValidaChavesPix(
    val message: String = "Chave Pix invalida",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = [],
)

@Singleton
class ValidaChavesPixValidator : ConstraintValidator<ValidaChavesPix, NovaChavePix> {

    override fun isValid(
        value: NovaChavePix,
        annotationMetadata: AnnotationValue<ValidaChavesPix>,
        context: ConstraintValidatorContext
    ): Boolean {
        if (value.tipo == null) {
            return false
        }
        return value.tipo.valida(value.chave)
    }
}