package infrastructure.network.finnhub.dto

import domain.stock.Quote

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

data class FinnhubQuoteDto(
    val c: Double,
    val d: Double,
    val dp: Double,
    val h: Double,
    val l: Double,
    val o: Double,
    val pc: Double
) {
    fun toDomain(symbol: String): Quote {
        return Quote(
            symbol,
            c,
            d,
            dp,
            h,
            l,
            o,
            pc,
            System.currentTimeMillis()
        )
    }
}