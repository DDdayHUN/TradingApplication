package domain.tax

//===========================================================//

/**
 * Represents a fixed set of taxation configurations.
 * 
 * 
 * Taxation instances are singleton-style objects representing specific tax systems.
 * All taxation rules are exposed as static constants.
 */
//===========================================================//
abstract class Taxation internal constructor() {
    //===========================================================//
    //===========================================================//
    // Public Field(s)

    companion object {
        val HUNGARY: Taxation = HungaryTaxation()
    }

    //===========================================================//
    //===========================================================//
    // Public Method(es)

    abstract fun calculateRevenueAfterTax(revenue: Double, costBasis: Double): Double
    //abstract fun taxInterestIncome(value: Double): Double
    //abstract fun taxDividendIncome(value: Double): Double
    //abstract fun taxCryptoGain(value: Double): Double
}
