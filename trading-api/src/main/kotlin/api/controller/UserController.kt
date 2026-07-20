package api.controller

import api.dto.user.CreateUserRequest
import api.dto.user.UserResponse
import api.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController {
    private val userService: UserService

    @PostMapping
    fun create(@RequestBody request: CreateUserRequest): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.create(request))
    }

    @GetMapping
    fun getAll(): ResponseEntity<List<UserResponse>>{
        return ResponseEntity.ok(userService.findAll())
    }

    constructor(userService: UserService) {
        this.userService = userService
    }
}