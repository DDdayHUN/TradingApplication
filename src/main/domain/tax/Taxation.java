package domain.tax;

import java.util.function.Supplier;

public abstract class Taxation {
    static public Taxation create(final Supplier<Taxation> supplier) {
        return supplier.get();
    }
}