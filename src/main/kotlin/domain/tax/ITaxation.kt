package domain.tax

//===========================================================//
/**
 * Represents a fixed set of taxation configurations.
 * 
 * Taxation instances are singleton-style objects representing specific tax systems.
 * All taxation rules are exposed as static constants.
 *
 * NOTE : Márkoé never touch this.
 */
//===========================================================//

interface ITaxation {
    //===========================================================//
    //===========================================================//
    // Public Field(s)

    // TODO : Maybe move this out to a Taxation object.
    companion object {
        val HUNGARY: ITaxation = HungaryTaxation()
    }

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    fun calculateRevenueAfterTax(revenue: Double, costBasis: Double): Double
    //abstract fun taxInterestIncome(value: Double): Double
    //abstract fun taxDividendIncome(value: Double): Double
    //abstract fun taxCryptoGain(value: Double): Double
}
