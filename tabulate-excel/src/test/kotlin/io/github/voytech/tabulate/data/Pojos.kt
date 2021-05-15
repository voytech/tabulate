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