package pl.voytech.exporter.core.template

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import pl.voytech.exporter.core.api.dsl.table
import pl.voytech.exporter.core.model.hints.style.CellFontHint
import pl.voytech.exporter.core.model.hints.style.Color
import pl.voytech.exporter.core.model.hints.style.ColumnWidthHint
import pl.voytech.exporter.data.Product
import pl.voytech.exporter.impl.template.excel.SXSSFWorkbookExport
import java.io.File
import java.io.FileOutputStream
import kotlin.test.assertNotNull

object BasicDslTableExportSpek: Spek({
    Feature("Should be able to define table, columns and export data to excel file") {
        Scenario("defining simple table model and exporting to excel file.") {
            val productList = (0..1000).map { Product("prod_nr_$it","Name $it", "This is description $it", "manufacturer $it")}
            val file = File("test.xlsx")
            FileOutputStream(file).use {
                DataExportTemplate<Product>(SXSSFWorkbookExport()).exportToStream(
                    table {
                        name = "Products table"
                        columns {
                            column("nr") {
                                columnTitle { title = "Nr.:" }
                                columnHints(ColumnWidthHint(width = 1000))
                                cellHints(
                                    CellFontHint(
                                        fontFamily = "Times New Roman",
                                        fontColor = Color(10,100,100),
                                        fontSize = 12
                                    )
                                )
                            }
                            column("code") {
                                columnTitle { title = "Code" }
                                fromField = Product::code;
                                columnHints(ColumnWidthHint(width = 3000))
                                cellHints(
                                    CellFontHint(
                                        fontFamily = "Times New Roman",
                                        fontColor = Color(0,0,0),
                                        fontSize = 12
                                    )
                                )
                            }
                            column("name") {
                                columnTitle { title = "Name" }
                                fromField = Product::name
                                columnHints(ColumnWidthHint(width = 4000))
                            }
                            column("description") {
                                columnTitle { title = "Description" }
                                fromField = Product::description
                                columnHints(ColumnWidthHint(width = 8000))
                            }
                            column("manufacturer") {
                                columnTitle { title = "Manufacturer" }
                                fromField = Product::manufacturer
                                columnHints(ColumnWidthHint(width = 4000))
                            }
                        }
                        rows {
                            row {
                                selector = {true}
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
