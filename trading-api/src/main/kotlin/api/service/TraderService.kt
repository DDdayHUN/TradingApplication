package api.service

import api.dto.trader.CreateTraderRequest
import api.dto.trader.TraderResponse
import api.exception.trader.TraderNotFoundException
import api.exception.user.UserNotFoundException
import api.mapper.TraderMapper
import api.repository.ITraderRepository
import api.repository.IUserRepository
import domain.algorithm.TradingAlgorithm
import domain.market.security.SecurityIdentifier
import domain.trader.Trader
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TraderService {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private val traderRepository: ITraderRepository
    private val userRepository: IUserRepository
    private val traderMapper: TraderMapper

    //===========================================================//
    //===========================================================//
    // Public Method(s)

    @Transactional
    fun createTrader(keycloakSub: String, request: CreateTraderRequest): TraderResponse {
        val securityIdentifier = SecurityIdentifier(
            isin = request.securityIdentifier.isin,
            tickerSymbol = request.securityIdentifier.tickerSymbol,
            currency = request.securityIdentifier.currency,
        )

        val algorithmType = parseAlgorithmType(
            request.algorithmType,
        )

        val algorithm = TradingAlgorithm.create(
            type = algorithmType,
            securityIdentifier = securityIdentifier,
        )

        val domainTrader = Trader(
            securityIdentifier = securityIdentifier,
            holdings = mutableListOf(),
            allocatedCapital = request.capital,
            algorithm = algorithm,
        )

        val user = userRepository.findByKeycloakSub(keycloakSub)
            ?: throw UserNotFoundException(keycloakSub)

        val entity = traderMapper.toEntity(
            trader = domainTrader,
            user = user,
            algorithmType = algorithmTypeName(algorithmType)
        )

        val savedEntity = traderRepository.save(entity)

        return traderMapper.toResponse(savedEntity)


    }

    //===========================================================//

    @Transactional
    fun findAllForUser(
        keycloakSub: String
    ): List<TraderResponse>{
        return traderRepository.findAllByUser_KeycloakSub(keycloakSub)
            .map(traderMapper::toResponse)
    }

    //===========================================================//

    @Transactional
    fun findByIdForUser(id: UUID, keycloakSub: String): TraderResponse {
        val result = traderRepository.findByIdAndUser_KeycloakSub(id, keycloakSub)
            ?: throw TraderNotFoundException(id, keycloakSub)

        return traderMapper.toResponse(result)
    }


    //===========================================================//
    //===========================================================//
    // Helper Method(s)

    private fun parseAlgorithmType(value: String): TradingAlgorithm.Type {
        return when (value.trim().uppercase()) {
            "TACPP46" -> TradingAlgorithm.Type.TACPP46
            "ALGDES2" -> TradingAlgorithm.Type.ALGDES2
            "ALGDES3" -> TradingAlgorithm.Type.ALGDES3
            "ALGDES31" -> TradingAlgorithm.Type.ALGDES31
            "ALGDES4" -> TradingAlgorithm.Type.ALGDES4

            else -> throw IllegalArgumentException(
                "Unsupported algorithm type: $value"
            )
        }
    }

    //===========================================================//

    private fun algorithmTypeName(
        type: TradingAlgorithm.Type
    ): String {
        return when (type) {
            TradingAlgorithm.Type.TACPP46 -> "TACPP46"
            TradingAlgorithm.Type.ALGDES2 -> "ALGDES2"
            TradingAlgorithm.Type.ALGDES3 -> "ALGDES3"
            TradingAlgorithm.Type.ALGDES31 -> "ALGDES31"
            TradingAlgorithm.Type.ALGDES4 -> "ALGDES4"
        }
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    constructor(traderRepository: ITraderRepository, userRepository: IUserRepository, traderMapper: TraderMapper) {
        this.traderRepository = traderRepository
        this.userRepository = userRepository
        this.traderMapper = traderMapper
    }

}