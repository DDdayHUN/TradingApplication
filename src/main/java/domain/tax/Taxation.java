package domain.tax;

//===========================================================//
/**
 * Represents a fixed set of taxation configurations.
 *
 * <p>Taxation instances are singleton-style objects representing specific tax systems.
 * All taxation rules are exposed as static constants.</p>
 */
//===========================================================//

public abstract class Taxation {
    //===========================================================//
    //===========================================================//
    // Public Field(s)

    static public final Taxation HUNGARY = new HungaryTaxation();

    //===========================================================//
    //===========================================================//
    // Public Interface(s)

    public abstract double taxCapitalGain(final double value);
    public abstract double taxInterestIncome(final double value);
    public abstract double taxDividendIncome(final double value);
    public abstract double taxCryptoGain(final double value);

    //===========================================================//
    //===========================================================//
    // Constructor(s)

    Taxation() {}
}
