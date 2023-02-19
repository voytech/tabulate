package io.github.voytech.tabulate.test.sampledata

import java.math.BigDecimal
import java.time.LocalDate
import kotlin.random.Random

data class SampleCustomer(
    val firstName: String,
    val lastName: String,
    val country: String,
    val city: String,
    val street: String,
    val houseNumber: String,
    val flat: String? = null
) {

    companion object {
        fun create(count: Int = 1): List<SampleCustomer> {
            Random(count)
            return (0 .. count).map {
                SampleCustomer(
                    firstNameDictionary.shuffled().first(),
                    lastNameDictionary.shuffled().first(),
                    countryDictionary.shuffled().first(),
                    "City",
                    streetDictionary.shuffled().first(),
                    "10","1"
                )
            }
        }
    }
}