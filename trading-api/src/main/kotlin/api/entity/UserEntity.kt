package api.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "app_user")
class UserEntity (
    @Column(name = "keycloak_sub", nullable = false, unique = true)
    var keycloakSub: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    lateinit var portfolio: PortfolioEntity

    fun attachPortfolio(portfolio: PortfolioEntity) {
        this.portfolio = portfolio
        portfolio.user = this
    }
}