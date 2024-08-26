package io.github.voytech.tabulate.test.sampledata

import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Serializable
data class SampleProduct(
    val code: String,
    val name: String,
    val description: String? = "",
    val manufacturer: String,
    @Contextual
    val distributionDate: LocalDate,
    @Contextual
    val price: BigDecimal
) {

    companion object {
        private val json = Json {
            serializersModule = SerializersModule {
                contextual(LocalDateSerializer)
                contextual(BigDecimalSerializer)
            }
        }

        private val data: List<SampleProduct> by lazy {
            val jsonString = this::class.java.getResource("/products.json")!!.readText()
            json.decodeFromString(ListSerializer(serializer()), jsonString)
        }

        fun create(count: Int = 1): List<SampleProduct> {
            return data.take(count)
        }
    }
}

object LocalDateSerializer : KSerializer<LocalDate> {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.format(formatter))
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString(), formatter)
    }
}

object BigDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.DOUBLE)

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeDouble(value.toDouble())
    }

    override fun deserialize(decoder: Decoder): BigDecimal {
        return BigDecimal.valueOf(decoder.decodeDouble())
    }
}