package infrastructure.broker

import api.config.IbkrConfig
import jakarta.annotation.PreDestroy
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
class InteractiveBrokerSessionManager(
    private val eventPublisher: ApplicationEventPublisher,
    private val config: IbkrConfig
) {

    private val sessions = ConcurrentHashMap<UUID, InteractiveBrokersSession>()

    suspend fun connect(userId: UUID, port: Int): InteractiveBrokersSession {

        val existing = sessions[userId]

        if (existing?.isConnected() == true) {
            return existing
        }

        existing?.disconnect()

        val client = IbkrClient(eventPublisher)

        val session = InteractiveBrokersSession(
            userId = userId,
            client = client,
            host = config.host,
            port = port,
            clientId = 1
        )

        session.connect()

        sessions[userId] = session

        return session
    }

    fun getClient(userId: UUID): IbkrClient {
        return sessions[userId]?.getClient()
            ?: throw IllegalStateException(
                "User has no active IBKR connection"
            )
    }

    fun isConnected(userId: UUID): Boolean {
        return sessions[userId]?.isConnected() == true
    }

    fun disconnect(userId: UUID) {
        sessions.remove(userId)?.disconnect()
    }

    @PreDestroy
    fun shutdown() {
        sessions.values.forEach {
            it.disconnect()
        }

        sessions.clear()
    }
}