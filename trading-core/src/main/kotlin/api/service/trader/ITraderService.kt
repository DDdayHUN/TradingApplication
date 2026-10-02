package api.service.trader

import api.dto.ChangeTraderAlgorithmRequest
import api.dto.CreateTraderRequest
import domain.trader.Trader
import java.util.UUID

interface ITraderService {
    suspend fun createTrader(request: CreateTraderRequest): Trader
    suspend fun deleteTrader(traderId: UUID)
    suspend fun getAll(): Set<Trader>
    suspend fun getById(traderId: UUID): Trader
    suspend fun changeAlgorithm(traderId: UUID, request: ChangeTraderAlgorithmRequest): Trader
}