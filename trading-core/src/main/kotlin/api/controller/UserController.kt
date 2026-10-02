package api.controller

import api.dto.UserResponse
import api.dto.toResponse
import api.service.auth.IAuthenticationService
import application.service.user.IUserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

//===========================================================//
//===========================================================//

@RestController
@RequestMapping("/api/users/")
class UserController(
    private val auth: IAuthenticationService,
    private val userService: IUserService
) {
    //===========================================================//
    //===========================================================//
    // GET

    @GetMapping
    suspend fun getCurrentUser(): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(
            userService.getById(auth.currentUser()).toResponse()
        )
    }

    //===========================================================//
    //===========================================================//
    // POST

    @PostMapping
    suspend fun create(): ResponseEntity<UserResponse> {
        val response = auth.createUser().toResponse()

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response)
    }
}