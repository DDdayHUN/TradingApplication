package data

import domain.stock.History
import domain.stock.Holding

data class SerializationData(
    val stockHistory: Map<String, List<History>>,
    val holdings: Map<String, List<Holding>>
)
