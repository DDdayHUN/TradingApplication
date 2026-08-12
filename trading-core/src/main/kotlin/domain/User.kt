package domain

import java.util.UUID

class User {
    val id: UUID

    constructor(id: UUID) {
        this.id = id
    }
}