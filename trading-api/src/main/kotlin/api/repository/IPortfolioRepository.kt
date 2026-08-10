package api.repository

import api.entity.PortfolioEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface IPortfolioRepository : JpaRepository<PortfolioEntity, UUID> {
    /**
     * Find Portfolio by User keycloak subject code
     * @param keycloakSub Keycloak subject code
     */
    fun findByUserKeycloakSub(keycloakSub: String): PortfolioEntity?
}