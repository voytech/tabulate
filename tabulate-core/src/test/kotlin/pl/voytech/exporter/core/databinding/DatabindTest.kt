package pl.voytech.exporter.core.databinding

import org.junit.jupiter.api.Test
import pl.voytech.exporter.core.api.builder.dsl.table
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
            table {
                name = "Products table"
                columns {
                    column(Product::code)
                    column(Product::name)
                    column(Product::description)
                    column(Product::manufacturer)
                }
            },
            mock<Product>().createOperations(),
            ByteArrayOutputStream()
        )
    }

    private fun <T> mock() : ExportOperationConfiguringFactory<T> {
        return object: ExportOperationConfiguringFactory<T>() {
            override fun getExportOperationsFactory(): ExportOperationsFactory<T> = object : ExportOperationsFactory<T> {
                override fun createLifecycleOperations(): LifecycleOperations<T> = object : LifecycleOperations<T> {
                    override fun finish(stream: OutputStream) {
                        println("finish!")
                    }
                }

                override fun createTableRenderOperations(): TableRenderOperations<T> = object : TableRenderOperations<T> {
                    override fun renderRowCell(context: AttributedCell) {
                        println("cell context: $context")
                    }
                }
            }

            override fun getAttributeOperationsFactory(): AttributeRenderOperationsFactory<T>? = null
        }
    }
}