package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.data.Product
import io.github.voytech.tabulate.data.Products
import org.junit.jupiter.api.Test

class TemplateTest {

    @Test
    fun `should tabulate collection`() {
        Products.CAMERAS.tabulate(TabulationFormat("mock"),Unit) {
            name = "Products table"
            columns {
                column(Product::code)
                column(Product::name)
                column(Product::description)
                column(Product::manufacturer)
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