package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.data.Product
import io.github.voytech.tabulate.data.Products
import io.github.voytech.tabulate.utils.Mocks.mock
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux

class TemplateTest {

    @Test
    fun `should tabulate collection`() {
        Products.CAMERAS.tabulate(mock<Product>().createTableExportOperation(), { }) {
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
    fun `should tabulate Flux`() {
        Flux.fromIterable(Products.CAMERAS).tabulate(mock<Product>().createTableExportOperation(), { }) {
            name = "Products table"
            columns {
                column(Product::code)
                column(Product::name)
                column(Product::description)
                column(Product::manufacturer)
            }
        }
    }
}