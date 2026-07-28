package api.repository

import api.entity.TraderEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ITraderRepository : JpaRepository<TraderEntity, UUID> {
    fun findAllByUser_KeycloakSub(keycloakSub: String): List<TraderEntity>
    fun findAllByUser_Id(userId: UUID): List<TraderEntity>
    fun findByIdAndUser_KeycloakSub(id: UUID, keycloakSub: String): TraderEntity?
}