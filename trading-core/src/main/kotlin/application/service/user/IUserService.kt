package application.service.user

import data.repository.user.sql.UserEntity
import java.util.*

interface IUserService {
    // TODO [v2.0]: "This must be replaced in future releases, cuz this doesn't support large amounts of users"
    suspend fun getAllUser(): List<UserEntity>
}