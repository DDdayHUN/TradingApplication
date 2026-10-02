package api.controller

import api.dto.ConnectIbkrRequest
import api.service.auth.IAuthenticationService
import infrastructure.broker.InteractiveBrokerSessionManager
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/ibkr/")
class IbkrController(
    private val authService: IAuthenticationService,
    private val ibkrSessionManager: InteractiveBrokerSessionManager
) {

    @GetMapping("connected/")
    suspend fun isConnected(): ResponseEntity<Boolean>{
        val user = authService.currentUser()

        return ResponseEntity.ok(
            ibkrSessionManager.isConnected(user.id)
        )
    }


    @PostMapping("connect/")
    suspend fun ibkrConnect(@RequestBody request: ConnectIbkrRequest): ResponseEntity<Boolean> {
        val user = authService.currentUser()

        ibkrSessionManager.connect(
            userId = user.id,
            port = request.port
        )

        val response = ibkrSessionManager.isConnected(user.id)

        return ResponseEntity.ok(response)
    }

    @PostMapping("disconnect/")
    suspend fun ibkrDisconnect(): ResponseEntity<Boolean>{
        val user = authService.currentUser()

        ibkrSessionManager.disconnect(user.id)

        val response = ibkrSessionManager.isConnected(user.id)
        return ResponseEntity.ok(response)
    }
}