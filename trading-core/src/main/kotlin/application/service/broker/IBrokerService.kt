package application.service.broker

interface IBrokerService {
    suspend fun connect()
    fun disconnect()
    fun isConnected(): Boolean
    fun placeOrder(request: BrokerOrderRequest): Int
}