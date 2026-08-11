package domain

import java.util.UUID

class User {
    val uuid: UUID

    constructor(uuid: UUID = UUID.randomUUID()) {
        this.uuid = uuid
    }
}