package data.infrastructure.broker.ibkr

import api.config.IbkrConfig
import application.logging.logger
import application.service.trader.ITraderService
import data.infrastructure.broker.ibkr.sql.order.IInteractiveBrokersOrderRepository
import data.infrastructure.broker.ibkr.sql.user.IInteractiveBrokersUserRepository
import data.infrastructure.broker.ibkr.sql.user.InteractiveBrokersUserEntity
import data.repository.user.IUserRepository
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.atomics.*
import kotlin.concurrent.*

//===========================================================//
//===========================================================//

@Component
class InteractiveBrokerSessionManager(
    private val ibkrUserRepository: IInteractiveBrokersUserRepository,
    private val userRepository: IUserRepository,
    private val config: IbkrConfig,

    private val traderService: ITraderService,
    private val ibkrOrderRepository: IInteractiveBrokersOrderRepository,
) {
    // TODO : 'ReentrantReadWriteLock' is not safe to use with Kotlin coroutines, so we need to rework that part.
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val m_Logger = logger<InteractiveBrokerSessionManager>()

    private val m_Sessions = ConcurrentHashMap<UUID, IbkrClientSession>()
    private val m_UserLocks = ConcurrentHashMap<UUID, Mutex>()

    @OptIn(ExperimentalAtomicApi::class)
    private val m_IsShuttingDown = AtomicBoolean(false)
    private val m_SessionMutex = ReentrantReadWriteLock()
    // NOTE : If fairness becomes a problem with 'm_SessionMutex', then set constructor(p0 = true)
    //        For more information read the official documentation of 'ReentrantReadWriteLock'

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    @Transactional
    suspend fun connect(userId: UUID, port: Int) {
        m_SessionMutex.read {
            throwIfShuttingDown()
            val mutex = m_UserLocks.computeIfAbsent(userId) { Mutex() }

            return mutex.withLock {
                // NOTE : This is not the best place for saving a new ibkrUser, but it stays for now.
                m_Sessions[userId] ?: run {
                    try {
                        val ibkrUserEntity = ibkrUserRepository.getById(userId).getOrNull() ?: run {
                            val userEntity = userRepository.getById(userId).getOrThrow()
                            ibkrUserRepository.save(user = InteractiveBrokersUserEntity(
                                user = userEntity,
                                clientId = ibkrUserRepository.fetchNextId().getOrThrow()
                            )).getOrThrow()
                        }

                        val clientSession = IbkrClientSession(
                            userId = userId,
                            clientId = ibkrUserEntity.clientId,
                            host = config.host,
                            port = port,
                            orderHandlerService = IbkrOrderHandlerService(traderService, ibkrOrderRepository)
                        )

                        clientSession.connect()

                        m_Sessions[userId] = clientSession
                    }
                    catch (e: Exception) {
                        m_Logger.info("$e")
                        throw e
                    }
                }
            }
        }
    }

    //===========================================================//

    suspend fun getClient(userId: UUID): IbkrClientSession {
        m_SessionMutex.read {
            throwIfShuttingDown()
            val mutex = m_UserLocks.computeIfAbsent(userId) { Mutex() }

            return mutex.withLock {
                m_Sessions[userId] ?: throw IllegalArgumentException("No active IBKR session for user with ID:{${userId}}")
            }
        }
    }

    //===========================================================//

    suspend fun isConnected(userId: UUID): Boolean {
        m_SessionMutex.read {
            throwIfShuttingDown()
            val mutex = m_UserLocks.computeIfAbsent(userId) { Mutex() }

            return mutex.withLock {
                m_Sessions[userId]?.isConnected() == true
            }
        }
    }

    //===========================================================//

    suspend fun disconnect(userId: UUID) {
        m_SessionMutex.read {
            throwIfShuttingDown()
            val mutex = m_UserLocks.computeIfAbsent(userId) { Mutex() }

            mutex.withLock {
                m_Sessions.remove(userId)?.disconnect()
                m_UserLocks.remove(userId)
            }
        }
    }

    //===========================================================//

    @OptIn(ExperimentalAtomicApi::class)
    @PreDestroy
    fun shutdown() = runBlocking {
        m_SessionMutex.write {
            m_IsShuttingDown.store(true)

            m_Logger.info("IBKR session manager is shutting down.")

            m_Sessions.values.forEach {
                try {
                    it.disconnect()
                }
                catch (e: Exception) {
                    m_Logger.info("$e")
                }
            }

            m_UserLocks.clear()
            m_Sessions.clear()

            m_Logger.info("IBKR session manager is shutdown successfully.")
        }
    }

    //===========================================================//
    //===========================================================//
    // Private Method(s)

    @OptIn(ExperimentalAtomicApi::class)
    private fun throwIfShuttingDown() {
        if (m_IsShuttingDown.load()) throw IllegalStateException("IBKR session manager has already been shutdown.")
    }
}