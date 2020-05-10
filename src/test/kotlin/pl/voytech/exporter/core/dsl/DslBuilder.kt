package pl.voytech.exporter.core.dsl

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import pl.voytech.exporter.core.api.dsl.table
import pl.voytech.exporter.core.model.Description
import pl.voytech.exporter.core.model.infinite
import pl.voytech.exporter.data.Product
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

object BasicDslTableDefinitionSpek: Spek({
    Feature("Should be able to define table, columns and cells with just a header") {
        val productList by memoized { listOf(
            Product("camera","Sony Film Beauty","An excellent camera for non-professional usage", "Sony"),
            Product("camera","Sony Film Sharp","An excellent camera for professional usage", "Sony")
        ) }

        Scenario("defining simple table model through kotlin DSL") {
            val tableMeta = table<Product> {
                name = "Products table"
                columns {
                    column("code") {
                        columnTitle { title = "Code" }
                        fromField = Product::code
                    }
                    column("name") {
                        columnTitle { title = "Name" }
                        fromField = Product::name
                    }
                    column("description") { fromField = Product::description }
                    column("manufacturer") { fromField = Product::manufacturer}
                }
            }

            Then("it should be not null") {
                assertNotNull(tableMeta)
            }

            Then("it should have two columns") {
                assertTrue { (tableMeta.columns.size == 4) }
            }

            Then("columns should be correctly named") {
                assertEquals("code", tableMeta.columns[0].id,"first column should be 'code'")
                assertEquals(Description(title = "Code",hints = null), tableMeta.columns[0].columnTitle, "first column description should be 'Code'")
                assertEquals("name", tableMeta.columns[1].id,"second column should be 'Name'")
                assertEquals(Description(title = "Name",hints = null), tableMeta.columns[1].columnTitle, "second column description should be 'Name'")
                assertEquals("description", tableMeta.columns[2].id,"third column should be 'Description'")
                assertNull(tableMeta.columns[2].columnTitle, "third column description should be null")
                assertEquals("manufacturer", tableMeta.columns[3].id,"fourth column should be 'Manufacturer'")
                assertNull(tableMeta.columns[3].columnTitle, "fourth column description should be null")
            }

            Then("columns should have correctly set object field getters") {
                assertEquals(Product::code, tableMeta.columns[0].fromField,"first column should get field value using ref 'Product::code'")
                assertEquals(Product::name, tableMeta.columns[1].fromField,"second column should get field value using ref 'Product::name'")
                assertEquals(Product::description, tableMeta.columns[2].fromField,"third column should get field value using ref 'Product::description'")
                assertEquals(Product::manufacturer, tableMeta.columns[3].fromField,"fourth column should get field value using ref 'Product::manufacturer'")
            }

        }
    }
})
