package data.repository.user.sql

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface IUserJpaRepository : JpaRepository<UserEntity, UUID>{
 @EntityGraph(attributePaths = ["portfolio", "portfolio.traders", "portfolio.traders.holdings"])
 override fun findById(id: UUID): Optional<UserEntity>
}