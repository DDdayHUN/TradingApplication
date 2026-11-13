package api.controller

import api.dto.UserResponse
import api.dto.toResponse
import api.service.IControllerService
import api.service.auth.IAuthenticationService
import application.service.user.IUserService
import data.repository.user.sql.toDomain
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
    private val controllerService: IControllerService
) {
    //===========================================================//
    //===========================================================//
    // GET

    @GetMapping
    suspend fun getCurrentUser(): ResponseEntity<UserResponse> {
        val userId = auth.currentUser()

        return ResponseEntity.ok(
            controllerService.getUser(userId).toResponse()
        )
    }

    //===========================================================//
    //===========================================================//
    // POST

    @PostMapping
    suspend fun create(): ResponseEntity<UserResponse> {
        val userId = auth.createUser()

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(controllerService.getUser(userId).toResponse())
    }
}