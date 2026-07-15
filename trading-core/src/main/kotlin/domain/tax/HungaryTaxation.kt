package domain.tax

import kotlin.math.abs

//===========================================================//
/**
 * An implementation of [ITaxation].
 */
//===========================================================//
// TODO : Ellenőrizni mégegyszer, hogy legálisan ezek tényleg így vannak-e, meg logikailag is.
// NOTE : Ez nem teljesen igaz a magyar rendszerre, de asszem ha jól olvastam az elmúlt 3 évben deducable a stock loss, de az accumulated losses-hez kéne egy timer.
//        Egy szó mint száz: Ez egy szar és majd újra kell gondolni, megbeszélni, de most egyenlőre jó lesz, cuz I fucking don't care XDD.
internal class HungaryTaxation : ITaxation {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private var m_AccumulatedLosses: Double = 0.0
    private val m_SZJA: Double = 0.15
    private val m_SZOCHO: Double = 0.13

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
            m_AccumulatedLosses += abs(profitOrLoss)
            return revenue
        }

        var taxableProfit = profitOrLoss

        if (m_AccumulatedLosses > 0) {
            if (taxableProfit >= m_AccumulatedLosses) {
                taxableProfit -= m_AccumulatedLosses
                m_AccumulatedLosses = 0.0
            } else {
                m_AccumulatedLosses -= taxableProfit
                taxableProfit = 0.0
            }
        }

        return revenue - taxableProfit * m_SZJA
    }
}