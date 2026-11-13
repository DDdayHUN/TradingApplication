package data.infrastructure.broker.ibkr.sql.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface IInteractiveBrokersUserJpaRepository : JpaRepository<InteractiveBrokersUserEntity, UUID> {
    @Query(
        """
        SELECT COALESCE(MAX(ibkrUser.clientId), 0)
        FROM InteractiveBrokersUserEntity ibkrUser
        """
    )
    fun findLargestClientId(): Int
}