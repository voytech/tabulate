package io.github.voytech.tabulate.csv

import io.github.voytech.tabulate.components.table.api.builder.dsl.header
import io.github.voytech.tabulate.csv.components.table.model.attributes.separator
import io.github.voytech.tabulate.csv.testsupport.CsvTableAssert
import io.github.voytech.tabulate.components.table.template.tabulate
import io.github.voytech.tabulate.test.CellPosition
import io.github.voytech.tabulate.test.assertions.AssertCellValue
import io.github.voytech.tabulate.test.sampledata.SampleProduct
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File

@DisplayName("Testing csv exports")
class CsvTabulateTest {

    @Test
    fun `should export product to csv file - minimalistic example`() {
        SampleProduct.create(1).tabulate("test.csv") {
            columns(SampleProduct::code, SampleProduct::name, SampleProduct::description, SampleProduct::manufacturer)
        }

        CsvTableAssert<SampleProduct>(
            cellTests = mapOf(
                CellPosition(0, 0) to AssertCellValue(expectedValue = "prod_nr_00"),
                CellPosition(0, 1) to AssertCellValue(expectedValue = "Name 0"),
                CellPosition(0, 2) to AssertCellValue(expectedValue = "This is description 0"),
                CellPosition(0, 3) to AssertCellValue(expectedValue = "manufacturer 0"),
            ),
            file = File("test.csv")
        ).perform().also { it.cleanup() }
    }

    @Test
    fun `should export products to csv file with custom separator attribute`() {
        SampleProduct.create(1).tabulate("test.csv") {
            attributes { separator { value = ";" } }
            columns(
                SampleProduct::code, SampleProduct::name, SampleProduct::description,
                SampleProduct::manufacturer, SampleProduct::price, SampleProduct::distributionDate
            )
            rows {
                header("Code", "Name", "Description", "Manufacturer", "Price", "Distribution")
            }
        }

        CsvTableAssert<SampleProduct>(
            cellTests = mapOf(
                CellPosition(0, 0) to AssertCellValue(expectedValue = "Code"),
                CellPosition(0, 1) to AssertCellValue(expectedValue = "Name"),
                CellPosition(0, 2) to AssertCellValue(expectedValue = "Description"),
                CellPosition(0, 3) to AssertCellValue(expectedValue = "Manufacturer"),
                CellPosition(0, 4) to AssertCellValue(expectedValue = "Price"),
                CellPosition(0, 5) to AssertCellValue(expectedValue = "Distribution")
            ),
            file = File("test.csv"),
            separator = ";"
        ).perform().also { it.cleanup() }
    }
}