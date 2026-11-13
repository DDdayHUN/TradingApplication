package data.infrastructure.broker.ibkr.sql.user

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface IInteractiveBrokersUserJpaRepository : JpaRepository<InteractiveBrokersUserEntity, UUID> {

}