package infrastructure.network.finnhub.dto

data class FinnhubSymbolDto(
    val count: Int,
    val result: List<ResultItem>
) {
    data class ResultItem(
        val description: String,
        val displaySymbol: String,
        val symbol: String,
        val type: String
    )
}