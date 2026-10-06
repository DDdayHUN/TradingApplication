package data.repository.trader.sql

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ITraderJpaRepository : JpaRepository<TraderEntity, UUID> {
    @EntityGraph(attributePaths = ["holdings"])
    fun findByIdWithHoldings(
        @Param("id") id: UUID
    ): TraderEntity?

    @EntityGraph(attributePaths = ["holdings"])
    fun findAllByIdWithHoldings(
        @Param("ids") ids: Iterable<UUID>
    ): List<TraderEntity>
}