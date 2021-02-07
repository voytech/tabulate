package pl.voytech.exporter.core.dsl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import pl.voytech.exporter.core.api.builder.dsl.table
import pl.voytech.exporter.data.Product
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.random.Random

class DslBuilder {

    @Test
    fun `should define table columns and cells with just a header`() {
        val productList = listOf(
                Product(
                    "camera",
                    "Sony Film Beauty",
                    "An excellent camera for non-professional usage",
                    "Sony",
                    LocalDate.now(),
                    BigDecimal(
                        Random(1000).nextDouble(200.00, 1000.00)
                    )
                ),
                Product(
                    "camera",
                    "Sony Film Sharp",
                    "An excellent camera for professional usage",
                    "Sony",
                    LocalDate.now(),
                    BigDecimal(Random(1000).nextDouble(200.00, 1000.00))
                )
            )

        val tableMeta = table<Product> {
            name = "Products table"
            columns {
                column(Product::code)
                column(Product::name)
                column(Product::description) {}
                column(Product::manufacturer) {}
            }
            rows {

            }
        }
        assertNotNull(tableMeta)
        assertEquals(tableMeta.columns.size, 4)
        assertEquals(
            Product::code,
            tableMeta.columns[0].id.ref,
            "first column should get field value using ref 'Product::code'"
        )
        assertEquals(
            Product::name,
            tableMeta.columns[1].id.ref,
            "second column should get field value using ref 'Product::name'"
        )
        assertEquals(
            Product::description,
            tableMeta.columns[2].id.ref,
            "third column should get field value using ref 'Product::description'"
        )
        assertEquals(
            Product::manufacturer,
            tableMeta.columns[3].id.ref,
            "fourth column should get field value using ref 'Product::manufacturer'"
        )

    }
}

