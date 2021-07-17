package br.com.zup.chavepix.repository

import br.com.zup.chavepix.entities.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import io.micronaut.data.repository.CrudRepository
import java.util.*

@Repository
interface ChavePixRepository: CrudRepository<ChavePix, UUID> {
    fun existsByChave(chave: String) : Boolean

    fun findByClienteId(clienteId: UUID): ChavePix


    fun findByIdAndClienteId(pixId: UUID, clienteId: UUID): Optional<ChavePix>

}