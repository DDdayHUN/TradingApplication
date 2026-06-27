package data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import domain.stock.Holding;
import domain.stock.History;

public class SerializationData {
    public final Map<String, List<History>> stockHistory;
    public final Map<String, List<Holding>>  holdings;

    public SerializationData(final Map<String, List<History>> stockHistory, final Map<String, List<Holding>> holdings) {
        this.stockHistory   = new HashMap<>(stockHistory);
        this.holdings       = new HashMap<>(holdings);
    }
}
