package data.infrastructure.broker.ibkr

import api.config.IbkrConfig
import data.infrastructure.broker.ibkr.sql.user.IInteractiveBrokersUserRepository
import data.infrastructure.broker.ibkr.sql.user.InteractiveBrokersUserEntity
import data.repository.user.IUserRepository
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.read
import kotlin.concurrent.write

@Component
class InteractiveBrokerSessionManager(
    private val ibkrUserRepository: IInteractiveBrokersUserRepository,
    private val userRepository: IUserRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val config: IbkrConfig
) {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val sessions = ConcurrentHashMap<UUID, IbkrClientSession>()
    private val userLocks = ConcurrentHashMap<UUID, Mutex>()

    @OptIn(ExperimentalAtomicApi::class)
    private val isShuttingDown = AtomicBoolean(false)
    private val sessionMutex = ReentrantReadWriteLock()
    // NOTE : If fairness becomes a problem with 'sessionMutex', then set constructor(p0 = true)
    // NOTE : For more information read the official documentation of 'ReentrantReadWriteLock'

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    @Transactional
    suspend fun connect(userId: UUID, port: Int) {
        sessionMutex.read {
            throwIfShuttingDown()
            val mutex = userLocks.computeIfAbsent(userId) { Mutex() }

            return mutex.withLock {
                // NOTE : Ez itt csunya itt, de nem baj marad egyelore :)
                sessions[userId] ?: run {
                    val ibkrUserEntity = ibkrUserRepository.getById(userId).getOrNull() ?: run {
                        val userEntity = userRepository.getById(userId).getOrThrow()
                        ibkrUserRepository.save(user = InteractiveBrokersUserEntity(
                            user = userEntity,
                            clientId = ibkrUserRepository.fetchNextId().getOrThrow()
                        )).getOrThrow()
                    }

                    val clientSession = IbkrClientSession(
                        userId,
                        ibkrUserEntity.clientId,
                        config.host,
                        port,
                        eventPublisher
                    )

                    clientSession.connect()

                    sessions[userId] = clientSession
                }
            }
        }
    }

    //===========================================================//

    suspend fun getClient(userId: UUID): IbkrClientSession {
        sessionMutex.read {
            throwIfShuttingDown()
            val mutex = userLocks.computeIfAbsent(userId) { Mutex() }

            return mutex.withLock {
                sessions[userId] ?: throw IllegalStateException("User has no active IBKR connection with ID:{${userId}}")
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