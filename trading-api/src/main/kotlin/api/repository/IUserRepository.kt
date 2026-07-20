package api.repository

import api.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface IUserRepository : JpaRepository<UserEntity, UUID> {
    fun findByKeycloakSub(keycloakSub: String): UserEntity?
}