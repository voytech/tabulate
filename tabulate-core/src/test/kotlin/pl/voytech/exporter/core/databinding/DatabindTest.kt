package pl.voytech.exporter.core.databinding

import org.junit.jupiter.api.Test
import pl.voytech.exporter.core.template.ResultHandler
import pl.voytech.exporter.core.template.Source
import pl.voytech.exporter.core.template.context.AttributedCell
import pl.voytech.exporter.core.template.operations.*
import pl.voytech.exporter.core.template.tabulate
import pl.voytech.exporter.data.Product
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.math.BigDecimal
import java.time.LocalDate

class DatabindTest {
    @Test
    fun `should describe simple model based table`() {
        listOf(
            Product(
                "camera",
                "Sony Film Beauty",
                "An excellent camera for non-professional usage",
                "Sony",
                LocalDate.now(),
                BigDecimal(200.00)
            ),
            Product(
                "camera",
                "Sony Film Sharp",
                "An excellent camera for professional usage",
                "Sony",
                LocalDate.now(),
                BigDecimal(1000)
            )
        ).tabulate(
            mock<Product>().createOperations(),
            ByteArrayOutputStream()
        ) {
            name = "Products table"
            columns {
                column(Product::code)
                column(Product::name)
                column(Product::description)
                column(Product::manufacturer)
            }
        }
    }

    private fun <T> mock() : ExportOperationsConfiguringFactory<T, OutputStream> {
        return object: ExportOperationsConfiguringFactory<T, OutputStream>() {
            override fun getExportOperationsFactory(): ExportOperationsFactory<T, OutputStream> = object : ExportOperationsFactory<T, OutputStream> {
                override fun createLifecycleOperations(): LifecycleOperations<T, OutputStream> = object : LifecycleOperations<T, OutputStream> {
                    override fun initialize(source: Source<T>, resultHandler: ResultHandler<T, OutputStream>) {
                        println("Do nothing. Mock implementation")
                    }

                    override fun finish() {
                        println("Do nothing. Mock implementation")
                    }

                }

                override fun createTableRenderOperations(): TableRenderOperations<T> = object : TableRenderOperations<T> {
                    override fun renderRowCell(context: AttributedCell) {
                        println("cell context: $context")
                    }
                }

                override fun createTableOperation(): TableOperation<T> {
                    return object :TableOperation<T> { }
                }
            }

            override fun getAttributeOperationsFactory(): AttributeRenderOperationsFactory<T>? = null
        }
    }
}