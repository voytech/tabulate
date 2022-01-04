package io.github.voytech.tabulate.csv

import io.github.voytech.tabulate.api.builder.dsl.header
import io.github.voytech.tabulate.csv.attributes.separator
import io.github.voytech.tabulate.template.tabulate
import io.github.voytech.tabulate.test.sampledata.SampleProduct
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

@DisplayName("Testing various csv exports")
class CsvTabulateTest {
    @Test
    fun `should export products to csv file with custom separator attribute`() {
        val productList = SampleProduct.create(3)
        measureTimeMillis {
            productList.tabulate("test.csv") {
                attributes { separator { value = "," } }
                columns(
                    SampleProduct::code, SampleProduct::name, SampleProduct::description,
                    SampleProduct::manufacturer, SampleProduct::price, SampleProduct::distributionDate
                )
                rows {
                    header("Code", "Name", "Description", "Manufacturer", "Price", "Distribution")
                }
            }
        }.also {
            println("Elapsed time: $it")
        }
    }
}