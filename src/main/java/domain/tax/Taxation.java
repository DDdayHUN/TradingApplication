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
    // Constructor(s)

    protected Taxation() {}  // To prevent instantiation, but allow subclasses.
}