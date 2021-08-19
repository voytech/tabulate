package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.api.builder.dsl.cell
import io.github.voytech.tabulate.data.Product
import io.github.voytech.tabulate.data.Products
import io.github.voytech.tabulate.testsupport.AttributedCellTest
import io.github.voytech.tabulate.testsupport.TestExportOperationsFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class TemplateTest {

    @Test
    fun `should tabulate collection`() {
        TestExportOperationsFactory.test = AttributedCellTest { attributedCell ->
            Assertions.assertNotNull(attributedCell)
            when (attributedCell.rowIndex) {
                0 -> {
                   when (attributedCell.columnIndex) {
                       0 -> assertEquals(attributedCell.value.value,"camera")
                       1 -> assertEquals(attributedCell.value.value,"Sony Film")
                       2 -> assertEquals(attributedCell.value.value,"An excellent camera for non-professional usage")
                       3 -> assertEquals(attributedCell.value.value,"Sony")
                       4 -> assertTrue(attributedCell.value.value is LocalDate)
                       5 -> assertEquals(attributedCell.value.value,BigDecimal(200.00))
                   }
                }
            }
        }

        Products.CAMERAS.tabulate(TabulationFormat("test"),Unit) {
            name = "Products table"
            columns {
                column(Product::code)
                column(Product::name)
                column(Product::description)
                column(Product::manufacturer)
            }
        }
    }

    @Test
    fun `should tabulate collection with trailing custom row`() {
        Products.CAMERAS.tabulate(TabulationFormat("test"),Unit) {
            name = "Products table"
            columns {
                column(Product::code)
                column(Product::name)
                column(Product::description)
                column(Product::manufacturer)
            }
            rows {
                row(5) {
                   cell {
                       value  = "Custom row cell"
                   }
                }
            }
        }
    }

    /*
    @Test
    fun `should tabulate Flux`() {
        Flux.fromIterable(Products.CAMERAS).tabulate(mock<Product>().createTableExportOperation(), Unit) {
            name = "Products table"
            columns {
                column(Product::code)
                column(Product::name)
                column(Product::description)
                column(Product::manufacturer)
            }
        }
    }
    */

}