package domain.tax;

//===========================================================//
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