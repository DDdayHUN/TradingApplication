package data.infrastructure.broker.ibkr.sql.user

import data.repository.user.sql.UserEntity
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "app_infrastructure_ibkr_user")
class InteractiveBrokersUserEntity(

    @Id
    @Column(name = "id")
    var id: UUID? = null,

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "id", nullable = false, updatable = false)
    var user: UserEntity,

    @Column(name = "client_id", nullable = false, updatable = false)
    var clientId: Int
)