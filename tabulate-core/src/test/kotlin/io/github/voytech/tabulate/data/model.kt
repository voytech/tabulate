package io.github.voytech.tabulate.data

import java.math.BigDecimal
import java.time.LocalDate

data class Product(
    val code: String,
    val name: String,
    val description: String? = "",
    val manufacturer: String,
    val distributionDate: LocalDate,
    val price: BigDecimal,
)

data class Price(
    val code: String,
    val netAmount: BigDecimal,
    val grossAmount: BigDecimal,
    val vatRate: BigDecimal,
)

object Products {
    val ITEMS = listOf(
        nextItem(1),
        nextItem(2),
    )

    fun items(num: Int) = (1..num).map { nextItem(it) }

    private fun nextItem(i: Int): Product =
        Product(
            "code$i",
            "name$i",
            "description$i",
            "manufacturer$i",
            LocalDate.now(),
            BigDecimal(1000)
        )
}
