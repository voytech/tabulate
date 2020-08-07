package pl.voytech.exporter.core.dsl

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import pl.voytech.exporter.core.api.dsl.table
import pl.voytech.exporter.core.model.Description
import pl.voytech.exporter.data.Product
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

object BasicDslTableDefinitionSpek : Spek({
    Feature("Should be able to define table, columns and cells with just a header") {
        val productList by memoized {
            listOf(
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
        }

        Scenario("defining simple table model through kotlin DSL") {
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

            Then("it should be not null") {
                assertNotNull(tableMeta)
            }

            Then("it should have two columns") {
                assertTrue { (tableMeta.columns.size == 4) }
            }

            Then("columns should have correctly set object field getters") {
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
    }
})
