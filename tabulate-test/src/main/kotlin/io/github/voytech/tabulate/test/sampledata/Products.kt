package io.github.voytech.tabulate.test.sampledata

import java.math.BigDecimal
import java.time.LocalDate
import kotlin.random.Random

data class SampleProduct(
    val code: String,
    val name: String,
    val description: String? = "",
    val manufacturer: String,
    val distributionDate: LocalDate,
    val price: BigDecimal
) {

    companion object {
        fun create(count: Int = 1): List<SampleProduct> {
            val random = Random(count)
            return (0 .. count).map {
                SampleProduct(
                    if (it % 2 == 0) "prod_nr_${it}${it % 2}" else "prod_nr_$it",
                    "Name $it",
                    "This is description $it",
                    "manufacturer $it",
                    LocalDate.now(),
                    BigDecimal(random.nextDouble(200.00, 1000.00))
                )
            }
        }
    }
}