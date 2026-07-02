package infrastructure.network.finnhub.dto

//===========================================================//
/**
 * Data Transfer Object for Finnhub symbol response.
 *
 *
 * This record represents the raw response format returned by Finnhub.
 *
 * @property count number of returned result items
 * @property result list of result items.
 *
 */
//===========================================================//

data class FinnhubSymbolDto(
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