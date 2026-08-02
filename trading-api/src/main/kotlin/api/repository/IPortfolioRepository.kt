package api.repository

import api.entity.PortfolioEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface IPortfolioRepository : JpaRepository<PortfolioEntity, UUID> {
    fun findByUserKeycloakSub(keycloakSub: String): PortfolioEntity?
}