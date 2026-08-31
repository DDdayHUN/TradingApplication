package data.repository.user

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface IUserJpaRepository : JpaRepository<UserEntity, UUID>