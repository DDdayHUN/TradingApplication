package data.repository.trader.sql

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ITraderRepository : JpaRepository<TraderEntity, UUID> {
    /**
     * Find all Trader by User ID
     * @param userId Portfolios User ID
     */
    fun findAllByPortfolioUserId(userId: UUID): List<TraderEntity>

    /**
     * Find all Trader by User Keycloak Sub.
     * @param keycloakSub Keycloak subject code for User
     */
    fun findAllByPortfolioUserKeycloakSub(keycloakSub: String): List<TraderEntity>

    /**
     * Find all Trader by Portfolio ID
     * @param portfolioId Unique Portfolio ID
     */
    fun findAllByPortfolioId(portfolioId: UUID): List<TraderEntity>

    /**
     * Find a Trader by trader ID and User Keycloak Sub
     * @param id Trader ID
     * @param keycloakSub Keycloak subject code for user
     */
    fun findByIdAndPortfolioUserKeycloakSub(id: UUID, keycloakSub: String): TraderEntity?
}