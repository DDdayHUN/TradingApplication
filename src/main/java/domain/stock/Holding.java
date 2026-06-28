package domain.stock;

//===========================================================//
/**
 * Represents a stock holding in a portfolio.
 *
 * @param entryPrice - the average purchase price per share.
 * @param amount - the number of shares held.
 */
//===========================================================//

public record Holding(
    double entryPrice,
    long amount
) {
    public Holding {
        if(entryPrice <= 0d) throw new IllegalArgumentException("Price");
        if(amount == 0L) throw new IllegalArgumentException("Amount");
    }
}
