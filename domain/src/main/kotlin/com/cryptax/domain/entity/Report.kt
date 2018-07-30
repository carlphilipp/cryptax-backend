package com.cryptax.domain.entity

import java.time.ZonedDateTime

data class Report(
    val totalGainsLosses: Double,
    val breakdown: Map<Currency, Details>
)

data class Details(var gainsLosses: Double = 0.0, val lines: List<Line> = mutableListOf()) {
    fun add(line: Line): Details {
        (lines as MutableList).add(line)
        return this
    }

    fun sortByDate() {
        (lines as MutableList).sortWith(compareBy { it.date })
    }
}

data class Line(
    val transactionId: String,
    val date: ZonedDateTime,
    val currency1: Currency,
    val currency2: Currency,
    val type: Transaction.Type,
    val price: Double,
    val quantity: Double,
    private val currency1UsdValue: Double,
    private val currency2UsdValue: Double) {

    val metadata: Metadata = Metadata(
        currency1UsdValue = currency1UsdValue,
        currency2UsdValue = currency2UsdValue,
        quantityCurrency2 = quantity * price)

    fun currencies(): List<Currency> {
        return listOf(currency1, currency2)
    }
}

data class Metadata(
    var ignored: Boolean = true,
    val currency1UsdValue: Double,
    val currency2UsdValue: Double,
    var currentPrice: Double? = null,
    var originalPrice: Double? = null,
    var gainsLosses: Double? = null,
    val quantityCurrency2: Double
)
