package pl.voytech.exporter.data

import java.math.BigDecimal

data class Product(
    val code: String,
    val name: String,
    val description: String? = "",
    val manufacturer: String
)

data class Price(
    val code: String,
    val netAmount: BigDecimal,
    val grossAmount: BigDecimal,
    val vatRate: BigDecimal
)

data class PriceList(
    val code: String,
    val pricesByProductCode: Map<String, Price>  = mutableMapOf()
)
