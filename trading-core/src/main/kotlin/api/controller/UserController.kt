package api.controller

import api.dto.UserResponse
import application.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

//===========================================================//
//===========================================================//

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    //===========================================================//
    //===========================================================//
    // GET

    @GetMapping
    suspend fun getCurrentUser(
        authentication: Authentication
    ): ResponseEntity<UserResponse> {
        val userId = UUID.fromString(authentication.name)

        return ResponseEntity.ok(
            userService.getById(userId)
        )
    }

    //===========================================================//
    //===========================================================//
    // POST

    @PostMapping
    suspend fun create(
        authentication: Authentication
    ): ResponseEntity<UserResponse> {
        val userId = UUID.fromString(authentication.name)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(userService.create(userId))
    }
}