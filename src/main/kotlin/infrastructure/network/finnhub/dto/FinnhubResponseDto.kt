package infrastructure.network.finnhub.dto

import infrastructure.network.Quote

//===========================================================//
/**
 * Data Transfer Object for Finnhub quote response.
 *
 *
 * This record represents the raw response format returned by Finnhub.
 *
 * @param c current price
 * @param d change
 * @param dp percent change
 * @param h high price of the day
 * @param l low price of the day
 * @param o open price of the day
 * @param pc previous close price
 */
//===========================================================//

data class FinnhubQuoteResponseDto(
    val c: Double,
    val d: Double,
    val dp: Double,
    val h: Double,
    val l: Double,
    val o: Double,
    val pc: Double
) {
    fun toDomain(): Quote {
        return Quote(
            c,
        )
    }
}

//===========================================================//
/**
 * Data Transfer Object for Finnhub symbol response.
 * This record represents the raw response format returned by Finnhub.
 *
 * @property count number of returned result items
 * @property result list of result items.
 *
 */
//===========================================================//

data class FinnhubSymbolResponseDto(
    val count: Int,
    val result: List<ResultItem>
) {

    /**
     * DTO for a single symbol search response
     *
     * @property description The full description of the security
     * @property displaySymbol human-readable symbol
     * @property symbol ticker symbol for the security
     * @property type type of the security: ETF, STOCK, FUND
     */
    data class ResultItem(
        val description: String,
        val displaySymbol: String,
        val symbol: String,
        val type: String
    )
}