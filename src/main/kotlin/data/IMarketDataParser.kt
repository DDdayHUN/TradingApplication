package data

import domain.assets.security.SecurityIdentifier

interface IMarketDataParser {
    companion object {
        fun get(type: Type): IMarketDataParser {
            return when (type) {
                Type.YahooMarketDataParser -> {
                    YahooMarketDataParser
                }
            }
        }
    }

    fun parse(securityIdentifier: SecurityIdentifier): SecuritySerializationData

    //===========================================================//
    //===========================================================//
    // Helper Class(es)

    sealed interface Type {
        data object YahooMarketDataParser : Type
    }
}