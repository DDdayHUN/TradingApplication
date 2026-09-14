package data.repository.trader.sql

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ITraderJpaRepository : JpaRepository<TraderEntity, UUID> {
    @EntityGraph(attributePaths = ["holdings"])
    @Query(
        """
        SELECT t
        FROM TraderEntity t
        WHERE t.id = :id
        """
    )
    fun findByIdWithHoldings(
        @Param("id") id: UUID
    ): TraderEntity?
}