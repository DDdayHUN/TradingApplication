package domain

import java.util.UUID

class User {
    val id: UUID

    constructor(id: UUID = UUID.randomUUID()) {
        this.id = id
    }
}