package data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import domain.stock.Bought;
import domain.stock.History;

public class SerializationData {
    public final Map<String, List<History>> stockHistory;
    public final Map<String, List<Bought>>  holdings;

    public SerializationData(final Map<String, List<History>> stockHistory,
                             final Map<String, List<Bought>> holdings) {
        this.stockHistory   = new HashMap<>(stockHistory);
        this.holdings       = new HashMap<>(holdings);
    }
}
