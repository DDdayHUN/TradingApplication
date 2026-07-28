package api.repository

import api.dto.user.UserResponse
import api.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

///**
// * Jpa Repository for user.
// * CRUD Methods all default defined.
// * Method names follow JPA rules.
// */
interface IUserRepository : JpaRepository<UserEntity, UUID> {
    //===========================================================//
    /**
     * Find user by keycloak subject code
     * @param keycloakSub sub code in the JWT Token.
     */
    fun findByKeycloakSub(keycloakSub: String): UserEntity?
}