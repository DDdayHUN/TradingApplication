package domain.tax

import domain.tax.Taxation
import kotlin.math.abs

//===========================================================//
/**
 * An implementation of [Taxation].
 */
//===========================================================//
// TODO : Ellenőrizni mégegyszer, hogy legálisan ezek tényleg így vannak-e, meg logikailag is.
// NOTE : Ez nem teljesen igaz a magyar rendszerre, de asszem ha jól olvastam az elmúlt 3 évben deducable a stock loss, de az accumulated losses-hez kéne egy timer.
//        Egy szó mint száz: Ez egy szar és majd újra kell gondolni, megbeszélni, de most egyenlőre jó lesz, cuz I fucking don't care XDD.
internal class HungaryTaxation : Taxation() {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private var accumulatedLosses: Double = 0.0
    private val SZJA: Double = 0.15
    private val SZOCHO: Double = 0.13

    //===========================================================//
    //===========================================================//
    // Public Method(es)
    /**
     * SOURCE : https://bankmonitor.hu/mediatar/cikk/befekteteseid-adozasa-2025-ben-10-fontos-tudnivalo/
     * Ellenőrzött tőkepiaci ügylet (pl. tőzsdei részvény eladásából származó nyereség): 15% személyi jövedelemadót (SZJA) kell fizetni, és nincs szociális hozzájárulási adó (SZOCHO).
     */
    override fun calculateRevenueAfterTax(revenue: Double, costBasis: Double): Double {
        val profitOrLoss = revenue - costBasis

        if (profitOrLoss < 0) {
            accumulatedLosses += abs(profitOrLoss)
            return revenue
        }

        var taxableProfit = profitOrLoss

        if (accumulatedLosses > 0) {
            if (taxableProfit >= accumulatedLosses) {
                taxableProfit -= accumulatedLosses
                accumulatedLosses = 0.0
            } else {
                accumulatedLosses -= taxableProfit
                taxableProfit = 0.0
            }
        }

        return revenue - taxableProfit * SZJA
    }
}