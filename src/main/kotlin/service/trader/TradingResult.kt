package service.trader

data class TradingResult(
    val success: Boolean,
    val message: String,
){
    companion object {
        fun success(message: String = "Order was successfully"): TradingResult {
            return TradingResult(true, message)
        }

        fun failure(message: String): TradingResult{
            return TradingResult(false, message)
        }
    }
}