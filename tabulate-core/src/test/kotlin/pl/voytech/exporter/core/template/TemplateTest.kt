package pl.voytech.exporter.core.template

import org.junit.jupiter.api.Test
import pl.voytech.exporter.data.Product
import pl.voytech.exporter.data.Products
import pl.voytech.exporter.utils.Mocks.mock
import reactor.core.publisher.Flux

class TemplateTest {

    @Test
    fun `should tabulate collection`() {
        Products.CAMERAS.tabulate(mock<Product>().createOperations(), { }) {
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
        Flux.fromIterable(Products.CAMERAS).tabulate(mock<Product>().createOperations(), { }) {
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