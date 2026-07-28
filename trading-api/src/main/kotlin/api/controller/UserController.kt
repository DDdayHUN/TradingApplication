package api.controller

import api.dto.user.CreateUserRequest
import api.dto.user.UserResponse
import api.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

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
    fun getCurrentUser(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<UserResponse> {
        val keycloakSub = jwt.subject

        return ResponseEntity.ok(
            userService.findByKeycloakSub(keycloakSub!!)
        )
    }

    @GetMapping
    fun getAll(): ResponseEntity<List<UserResponse>>{
        return ResponseEntity.ok(userService.findAll())
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.findById(id))
    }

    //===========================================================//
    //===========================================================//
    // POST

    @PostMapping
    fun create(@RequestBody request: CreateUserRequest): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.create(request))
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(userService: UserService) {
        this.userService = userService
    }
}