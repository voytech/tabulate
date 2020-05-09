package pl.voytech.exporter.core.dsl

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import pl.voytech.exporter.core.api.dsl.table
import pl.voytech.exporter.core.model.infinite
import pl.voytech.exporter.data.Product
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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
                showHeader = true
                columns {
                    column("Code") { fromField = Product::code }
                    column("Name") { fromField = Product::name }
                    column("Description") { fromField = Product::description }
                    column("Manufacturer") { fromField = Product::manufacturer}
                }
            }

            Then("it should be not null") {
                assertNotNull(tableMeta)
            }

            Then("it should have two columns") {
                assertTrue { (tableMeta.columns.size == 4) }
            }

            Then("columns should be correctly named") {
                assertEquals("Code", tableMeta.columns[0].columnTitle,"first column should be 'Code'")
                assertEquals("Name", tableMeta.columns[1].columnTitle,"second column should be 'Name'")
                assertEquals("Description", tableMeta.columns[2].columnTitle,"third column should be 'Description'")
                assertEquals("Manufacturer", tableMeta.columns[3].columnTitle,"fourth column should be 'Manufacturer'")
            }

            Then("columns should have correctly set object field getters") {
                assertEquals(Product::code, tableMeta.columns[0].fromField,"first column should get field value using ref 'Product::code'")
                assertEquals(Product::name, tableMeta.columns[1].fromField,"second column should get field value using ref 'Product::name'")
                assertEquals(Product::description, tableMeta.columns[2].fromField,"third column should get field value using ref 'Product::description'")
                assertEquals(Product::manufacturer, tableMeta.columns[3].fromField,"fourth column should get field value using ref 'Product::manufacturer'")
            }

            Then("table should have single row range") {
                assertTrue{ tableMeta.rowRanges.size == 1 }
            }

            Then("table should have INFINITE row range") {
                assertEquals(infinite(), tableMeta.rowRanges[0],"table should have single infinite row range")
            }
        }
    }
})
