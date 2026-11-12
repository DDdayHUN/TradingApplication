package data.infrastructure.broker.ibkr

import api.config.IbkrConfig
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
class InteractiveBrokerSessionManager(
    private val eventPublisher: ApplicationEventPublisher,
    private val config: IbkrConfig
) {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val sessions = ConcurrentHashMap<UUID, InteractiveBrokersSession>()
    private val userLocks = ConcurrentHashMap<UUID, Mutex>()
    private val sessionMutex = Mutex()

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    suspend fun connect(userId: UUID, port: Int): InteractiveBrokersSession {
        val mutex = userLocks.computeIfAbsent(userId) { Mutex() }

        return mutex.withLock {
            sessions[userId] ?: run {
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
                session
            }
        }
    }

    //===========================================================//

    suspend fun getClient(userId: UUID): IbkrClient {
        val mutex = userLocks.computeIfAbsent(userId) { Mutex() }

        return mutex.withLock {
            sessions[userId]?.getClient()
                ?: throw IllegalStateException("User has no active IBKR connection with ID:{${userId}}")
        }
    }

    //===========================================================//

    suspend fun isConnected(userId: UUID): Boolean {
        val mutex = userLocks.computeIfAbsent(userId) { Mutex() }

        return mutex.withLock {
            sessions[userId]?.isConnected() == true
        }
    }

    //===========================================================//

    suspend fun disconnect(userId: UUID) {
        val mutex = userLocks.computeIfAbsent(userId) { Mutex() }
        mutex.withLock { sessions.remove(userId)?.disconnect() }
    }

    //===========================================================//

    // NOTE : This is still not perfect, because lingering threads might still be using other methods.
    @PreDestroy
    suspend fun shutdown() {
        sessionMutex.withLock {
            sessions.values.forEach {
                it.disconnect()
            }

            userLocks.clear()
            sessions.clear()
        }
    }
}