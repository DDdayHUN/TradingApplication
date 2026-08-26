package application.service.broker

import domain.market.security.SecurityIdentifier
import infrastructure.broker.IbkrHistoricalBar
import kotlin.time.Instant

interface IBrokerService {
    suspend fun placeOrder(request: BrokerOrderRequest): Int
    suspend fun requestOrderStatus()
    suspend fun getAvailableCapital(): Double
    suspend fun getHistoricalData(securityIdentifier: SecurityIdentifier, from: Instant, to: Instant): List<IbkrHistoricalBar>
}