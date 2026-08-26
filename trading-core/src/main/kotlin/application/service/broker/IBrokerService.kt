package application.service.broker

interface IBrokerService {
    suspend fun placeOrder(request: BrokerOrderRequest): Int
    suspend fun requestOrderStatus()
    suspend fun getAvailableCapital(): Double
}