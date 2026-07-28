package api.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "app_users")
class User (
    @Column(name = "keycloak_sub", nullable = false, unique = true)
    var keycloakSub: String
){
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, orphanRemoval = true, cascade = [CascadeType.ALL])
    var traders: MutableList<Trader> = mutableListOf()
}