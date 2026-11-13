package data.infrastructure.broker.ibkr

import api.config.IbkrConfig
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.read
import kotlin.concurrent.write

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

    @OptIn(ExperimentalAtomicApi::class)
    private val isShuttingDown = AtomicBoolean(false)
    private val sessionMutex = ReentrantReadWriteLock()
    // NOTE : If fairness becomes a problem with 'sessionMutex', then set constructor(p0 = true)
    // NOTE : For more information read the official documentation of 'ReentrantReadWriteLock'

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    suspend fun connect(userId: UUID, port: Int): InteractiveBrokersSession {
        sessionMutex.read {
            throwIfShuttingDown()
            val mutex = userLocks.computeIfAbsent(userId) { Mutex() }

            return mutex.withLock {
                sessions[userId] ?: run {
                    val client = IbkrClient(userId, eventPublisher)

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
    }

    //===========================================================//

    suspend fun getClient(userId: UUID): IbkrClient {
        sessionMutex.read {
            throwIfShuttingDown()
            val mutex = userLocks.computeIfAbsent(userId) { Mutex() }

            return mutex.withLock {
                sessions[userId]?.getClient()
                    ?: throw IllegalStateException("User has no active IBKR connection with ID:{${userId}}")
            }
        }
    }

    //===========================================================//

    suspend fun isConnected(userId: UUID): Boolean {
        sessionMutex.read {
            throwIfShuttingDown()
            val mutex = userLocks.computeIfAbsent(userId) { Mutex() }

            return mutex.withLock {
                sessions[userId]?.isConnected() == true
            }
        }
    }

    //===========================================================//

    suspend fun disconnect(userId: UUID) {
        sessionMutex.read {
            throwIfShuttingDown()
            val mutex = userLocks.computeIfAbsent(userId) { Mutex() }
            mutex.withLock { sessions.remove(userId)?.disconnect() }
        }
    }

    //===========================================================//

    @OptIn(ExperimentalAtomicApi::class)
    @PreDestroy
    fun shutdown() {
        runBlocking {
            sessionMutex.write {
                isShuttingDown.store(true)
                sessions.values.forEach {
                    it.disconnect()
                }

                userLocks.clear()
                sessions.clear()
            }
        }
    }

    //===========================================================//
    //===========================================================//
    // Private Method(s)

    @OptIn(ExperimentalAtomicApi::class)
    private fun throwIfShuttingDown() {
        if (isShuttingDown.load()) throw IllegalStateException("IBKR session manager has already been shut down")
    }
}