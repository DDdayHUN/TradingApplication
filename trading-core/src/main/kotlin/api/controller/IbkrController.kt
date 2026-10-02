package api.controller

import api.dto.ConnectIbkrRequest
import api.service.auth.IAuthenticationService
import infrastructure.broker.InteractiveBrokerSessionManager
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/ibkr/")
class IbkrController(
    private val authService: IAuthenticationService,
    private val ibkrSessionManager: InteractiveBrokerSessionManager
) {

    @GetMapping("connected/")
    suspend fun isConnected(): ResponseEntity<Boolean>{
        val userId = authService.currentUser()

        return ResponseEntity.ok(
            ibkrSessionManager.isConnected(userId)
        )
    }


    @PostMapping("connect/")
    suspend fun ibkrConnect(@RequestBody request: ConnectIbkrRequest): ResponseEntity<Boolean> {
        val userId = authService.currentUser()

        ibkrSessionManager.connect(
            userId = userId,
            port = request.port
        )

        val response = ibkrSessionManager.isConnected(userId)

        return ResponseEntity.ok(response)
    }

    @PostMapping("disconnect/")
    suspend fun ibkrDisconnect(): ResponseEntity<Boolean>{
        val userId = authService.currentUser()

        ibkrSessionManager.disconnect(userId)

        val response = ibkrSessionManager.isConnected(userId)
        return ResponseEntity.ok(response)
    }
}