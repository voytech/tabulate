package io.github.voytech.tabulate.test.sampledata

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json.Default.decodeFromString
import java.io.File


@Serializable
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

        private val data: List<SampleCustomer> by lazy {
            val jsonString = this::class.java.getResource("/sample_customers.json")!!.readText()
            decodeFromString(ListSerializer(serializer()), jsonString)
        }

        fun create(count: Int = 1): List<SampleCustomer> {
            return data.take(count)
        }
    }
}