package pl.voytech.exporter.core.template

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import pl.voytech.exporter.core.api.dsl.table
import pl.voytech.exporter.core.model.CellType
import pl.voytech.exporter.data.Product
import pl.voytech.exporter.impl.template.excel.SXSSFWorkbookExportOperation
import java.io.File
import java.io.FileOutputStream
import kotlin.test.assertNotNull

object BasicDslTableExportSpek: Spek({
    Feature("Should be able to define table, columns and export data to excel file") {
        Scenario("defining simple table model and exporting to excel file.") {
            val productList =  listOf(
                Product("camera","Sony Film Beauty","An excellent camera for non-professional usage", "Sony"),
                Product("camera","Sony Film Sharp","An excellent camera for professional usage", "Sony"),
                Product("tv","Sony TV","An excellent tv", "Sony")
            )
            val file = File("test.xls")
            FileOutputStream(file).use {
                DataExportTemplate<Product>(SXSSFWorkbookExportOperation()).exportToStream(
                    table {
                        name = "Products table"
                        columns {
                            column("nr") { columnTitle { title = "Nr.:" }}
                            column("code") { columnTitle { title = "Code" }; fromField = Product::code }
                            column("name") { columnTitle { title = "Name" }; fromField = Product::name }
                            column("description") { columnTitle { title = "Description" }; fromField = Product::description }
                            column("manufacturer") { columnTitle { title = "Manufacturer" }; fromField = Product::manufacturer }
                        }
                        rows {
                            row {
                                selector = { true }
                                cells {
                                    forColumn("nr") { eval = { row -> row.index } }
                                }
                            }
                        }
                    },
                    productList,
                    it
                )
            }
            Then("file should be written successfully") {
                assertNotNull(file)
            }
        }
    }
})
