package io.github.voytech.tabulate.data

import java.math.BigDecimal
import java.time.LocalDate

data class Product(
    val code: String,
    val name: String,
    val description: String? = "",
    val manufacturer: String,
    val distributionDate: LocalDate,
    val price: BigDecimal
)

data class Price(
    val code: String,
    val netAmount: BigDecimal,
    val grossAmount: BigDecimal,
    val vatRate: BigDecimal
)

object Products {
    val CAMERAS = listOf(
        Product(
            "camera",
            "Sony Film Beauty",
            "An excellent camera for non-professional usage",
            "Sony",
            LocalDate.now(),
            BigDecimal(200.00)
        ),
        Product(
            "camera",
            "Sony Film Sharp",
            "An excellent camera for professional usage",
            "Sony",
            LocalDate.now(),
            BigDecimal(1000)
        )
    )
}
