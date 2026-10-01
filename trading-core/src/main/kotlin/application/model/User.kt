package application.model

import java.util.UUID

data class User(
    val id: UUID,
    val userName: String
)