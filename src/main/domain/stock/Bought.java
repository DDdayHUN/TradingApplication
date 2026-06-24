package domain.stock;

public final class Bought {
    public final double price;
    public long         amount;

    public Bought(final double price, final long amount) {
        if(price <= 0d) throw new IllegalArgumentException("Price");
        if(amount == 0L) throw new IllegalArgumentException("Amount");

        this.price = price;
        this.amount = amount;
    }
}
