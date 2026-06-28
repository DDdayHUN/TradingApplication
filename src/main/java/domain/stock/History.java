package domain.stock;

//===========================================================//
/**
 * Represents the historical data of a stock for a single trading day.
 *
 * @param closingPrice - the closing price of the asset.
 */
//===========================================================//

public record History(
    double closingPrice
) {
    public History {
        if(closingPrice <= 0d) throw new IllegalArgumentException("ClosingPrice");
    }
}
