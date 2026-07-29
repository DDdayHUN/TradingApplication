package api.repository

import api.entity.TraderEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ITraderRepository : JpaRepository<TraderEntity, UUID> {

    /**
     * Find all Traders by users Keycloak subject code.
     * @param keycloakSub Subject code provided by keycloak
     */
    fun findAllByUserKeycloakSub(keycloakSub: String): List<TraderEntity>

    /**
     * Find all Traders by users ID.
     * @param userId users unique ID.
     */
    fun findAllByUserId(userId: UUID): List<TraderEntity>

    /**
     * Find Trader by trader ID and users keycloak subject code.
     * @param id trader unique ID.
     * @param keycloakSub user keycloak subject code.
     */
    fun findByIdAndUserKeycloakSub(id: UUID, keycloakSub: String): TraderEntity?
}