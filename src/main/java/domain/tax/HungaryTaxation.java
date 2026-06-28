package domain.tax;

//===========================================================//
/**
 * An implementation of {@link Taxation}.
 */
//===========================================================//
// TODO : Ellenőrizni mégegyszer, hogy legálisan ezek tényleg így vannak-e, meg logikailag is.

final class HungaryTaxation extends Taxation {
    //===========================================================//
    //===========================================================//
    // Private Field(s)

    private final double SZJA = 0.15d;
    private final double SZOCHO = 0.13d;

    //===========================================================//
    //===========================================================//
    // Public Interface(s)

    /**
     * SOURCE : https://bankmonitor.hu/mediatar/cikk/befekteteseid-adozasa-2025-ben-10-fontos-tudnivalo/
     * Ellenőrzött tőkepiaci ügylet (pl. tőzsdei részvény eladásából származó nyereség): 15% személyi jövedelemadót (SZJA) kell fizetni, és nincs szociális hozzájárulási adó (SZOCHO).
     */
    @Override
    public double taxCapitalGain(final double value) {
        return value * (1.0d - SZJA);
    }

    /**
     * SOURCE : https://bankmonitor.hu/mediatar/cikk/befekteteseid-adozasa-2025-ben-10-fontos-tudnivalo/
     * Kamatjövedelem (pl. bankbetét, vállalati kötvény kamata, pénzpiaci alap hozama): Itt már magasabb a teher: a 15% SZJA mellett 13% SZOCHO-t is kell fizetni, ami összesen 28%-os adóterhet jelent.
     */
    @Override
    public double taxInterestIncome(final double value) {
        return value * (1.0d - (SZJA + SZOCHO));
    }

    /**
     * SOURCE : https://bankmonitor.hu/mediatar/cikk/befekteteseid-adozasa-2025-ben-10-fontos-tudnivalo/
     * Alapesetben 15% SZJA terheli. SZOCHO-t csak bizonyos összeghatárig (az éves minimálbér 24-szereséig terjedő összevont jövedelemig) kell fizetni, 
     * de fontos könnyítés, hogy a tőzsdén jegyzett társaságoktól kapott osztalék után jellemzően nincs SZOCHO fizetési kötelezettség.
     */
    @Override
    public double taxDividendIncome(final double value) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    /**
     * SOURCE : https://bankmonitor.hu/mediatar/cikk/befekteteseid-adozasa-2025-ben-10-fontos-tudnivalo/
     * Kriptovalutából származó jövedelem: Erre is 15% SZJA vonatkozik, de csak a realizált nyereség után.
     */
    @Override
    public double taxCryptoGain(final double value) {
        return value * (1.0d - SZJA);
    }

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    HungaryTaxation() {}
}