package api.controller

import api.dto.user.CreateUserRequest
import api.dto.user.UserResponse
import application.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController {

    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val userService: UserService

    //===========================================================//
    //===========================================================//
    // GET

    @GetMapping("/me")
    fun getCurrentUser(authentication: Authentication): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(
            userService.findByKeycloakSub(authentication.name)
        )
    }

    //===========================================================//
    //===========================================================//
    // POST

    @PostMapping
    fun create(authentication: Authentication,@RequestBody request: CreateUserRequest): ResponseEntity<UserResponse> {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                userService.create(
                    keycloakSub = authentication.name,
                    request = request
                )
            )
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(userService: UserService) {
        this.userService = userService
    }
}