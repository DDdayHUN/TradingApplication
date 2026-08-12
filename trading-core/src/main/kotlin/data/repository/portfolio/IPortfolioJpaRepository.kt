package data.repository.portfolio

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface IPortfolioJpaRepository : JpaRepository<PortfolioEntity, UUID> {
    /**
     * Find Portfolio by User keycloak subject code
     * @param keycloakSub Keycloak subject code
     */
    fun findByUserKeycloakSub(keycloakSub: String): PortfolioEntity?
}