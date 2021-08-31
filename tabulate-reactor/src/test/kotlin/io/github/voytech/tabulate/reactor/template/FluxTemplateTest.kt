package io.github.voytech.tabulate.reactor.template

import io.github.voytech.tabulate.data.Product
import io.github.voytech.tabulate.data.Products
import io.github.voytech.tabulate.template.TabulationFormat
import io.github.voytech.tabulate.testsupport.AttributedCellTest
import io.github.voytech.tabulate.testsupport.TestExportOperationsFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import reactor.core.scheduler.Schedulers
import java.math.BigDecimal
import java.time.LocalDate
import java.util.concurrent.Executors

class FluxTemplateTest {

    @Test
    fun `should tabulate records emitted from Flux publisher`() {
        TestExportOperationsFactory.test = AttributedCellTest { attributedCell ->
            Assertions.assertNotNull(attributedCell)
            when (attributedCell.rowIndex) {
                0 -> {
                    when (attributedCell.columnIndex) {
                        0 -> assertEquals(attributedCell.value.value, "camera")
                        1 -> assertEquals(attributedCell.value.value, "Sony Film")
                        2 -> assertEquals(attributedCell.value.value, "An excellent camera for non-professional usage")
                        3 -> assertEquals(attributedCell.value.value, "Sony")
                        4 -> assertTrue(attributedCell.value.value is LocalDate)
                        5 -> assertEquals(attributedCell.value.value, BigDecimal(200.00))
                    }
                }
            }
        }

        val scheduler = Schedulers.fromExecutor(Executors.newFixedThreadPool(1))
        Products.CAMERAS
            .log()
            .tabulate(TabulationFormat("test"), Unit) {
                name = "Products table"
                columns {
                    column(Product::code)
                    column(Product::name)
                    column(Product::description)
                    column(Product::manufacturer)
                }
            }.publishOn(scheduler)
            .map {
                println("${Thread.currentThread().name} - Do something with $it")
            }
            .blockLast()
    }
}