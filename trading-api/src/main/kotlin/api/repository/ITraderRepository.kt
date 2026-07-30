package api.repository

import api.entity.TraderEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ITraderRepository : JpaRepository<TraderEntity, UUID> {
    fun findAllByPortfolioUserId(userId: UUID): List<TraderEntity>
    fun findAllByPortfolioUserKeycloakSub(keycloakSub: String): List<TraderEntity>
    fun findAllByPortfolioId(portfolioId: UUID): List<TraderEntity>
    fun findByIdAndPortfolioUserKeycloakSub(id: UUID, keycloakSub: String): TraderEntity?
}