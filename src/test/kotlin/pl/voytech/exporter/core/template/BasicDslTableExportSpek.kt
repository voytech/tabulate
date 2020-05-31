package pl.voytech.exporter.core.template

import com.google.common.reflect.ClassPath
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import pl.voytech.exporter.core.api.dsl.export
import pl.voytech.exporter.core.api.dsl.table
import pl.voytech.exporter.core.model.RowSelectors
import pl.voytech.exporter.core.model.extension.style.*
import pl.voytech.exporter.core.model.extension.style.enums.BorderStyle
import pl.voytech.exporter.core.model.extension.style.enums.HorizontalAlignment
import pl.voytech.exporter.core.model.extension.style.enums.VerticalAlignment
import pl.voytech.exporter.core.model.extension.style.enums.WeightStyle
import pl.voytech.exporter.data.Product
import pl.voytech.exporter.impl.template.excel.CellExcelDataFormatExtension
import pl.voytech.exporter.impl.template.excel.excelExport
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import kotlin.test.assertNotNull

object BasicDslTableExportSpek: Spek({
    Feature("Regular tabular data export to excel") {
        Scenario("defining simple table model and exporting to excel file.") {
            val productList = (0..1000).map { Product("prod_nr_$it","Name $it", "This is description $it", "manufacturer $it", LocalDate.now())}
            val file = File("test.xlsx")
            FileOutputStream(file).use {
                productList.exportTo(
                    table {
                        name = "Products table"
                        columns {
                            column("nr") {
                                columnTitle { title = "Nr.:" }
                                columnExtensions(ColumnWidthExtension(width = 50))
                                cellExtensions(
                                    CellFontExtension(
                                        fontFamily = "Times New Roman",
                                        fontColor = Color(10,100,100),
                                        fontSize = 12,
                                        italic = true,
                                        weight = WeightStyle.BOLD,
                                        strikeout = true,
                                        underline = true
                                    )
                                )
                            }
                            column(Product::code) {
                                columnTitle { title = "Code" }
                                columnExtensions(ColumnWidthExtension(width = 100))
                                cellExtensions(
                                    CellFontExtension(
                                        fontFamily = "Times New Roman",
                                        fontColor = Color(0,0,0),
                                        fontSize = 12
                                    ),
                                    CellBackgroundExtension(color = Color(10,100,100))
                                )
                            }
                            column(Product::name) {
                                columnTitle { title = "Name" }
                                columnExtensions(ColumnWidthExtension(width = 100))
                            }
                            column(Product::description) {
                                columnTitle { title = "Description" }
                                columnExtensions(ColumnWidthExtension(width = 300))
                            }
                            column(Product::manufacturer) {
                                columnTitle { title = "Manufacturer" }
                                columnExtensions(ColumnWidthExtension(width = 100))
                                dataFormatter = { field -> (field as String).toUpperCase() }
                            }
                            column(Product::distributionDate) {
                                columnTitle { title = "Distribution" }
                                cellExtensions(
                                    CellExcelDataFormatExtension("dd.mm.YYYY")
                                )
                            }
                        }
                        rows {
                            row {
                                selector = RowSelectors.at(0)
                                rowExtensions(RowHeightExtension(height = 220))
                                cellExtensions(
                                    CellBordersExtension(
                                        leftBorderStyle = BorderStyle.SOLID,
                                        leftBorderColor = Color(0,0,0),
                                        rightBorderStyle = BorderStyle.SOLID,
                                        rightBorderColor = Color(0,0,0),
                                        bottomBorderStyle = BorderStyle.SOLID,
                                        bottomBorderColor = Color(0,0,0)
                                    ),
                                    CellAlignmentExtension(
                                        horizontal = HorizontalAlignment.CENTER,
                                        vertical = VerticalAlignment.MIDDLE
                                    )
                                )
                            }
                            row {
                                selector = RowSelectors.all()
                                cells {
                                    forColumn("nr") { eval = { row -> row.index } }
                                }
                            }
                        }
                    },
                    excelExport(),
                    it
                )
            }
            Then("file should be written successfully") {
                assertNotNull(file)
            }
        }
    }
    Feature("Tabular data export to excel using excel template as input.") {
        Scenario("defining simple table model and exporting to excel file.") {
            val productList = (0..1000).map {
                Product(
                    "prod_nr_$it",
                    "Name $it",
                    "This is description $it",
                    "manufacturer $it",
                    LocalDate.now()
                )
            }
            val file = File("test2.xlsx")
            FileOutputStream(file).use {
                productList.export<Product,SXSSFWorkbook>(it) {
                    table {
                        name = "Products table"
                        firstRow = 1
                        columns {
                            column("nr") {}
                            column(Product::code) {}
                            column(Product::name) {}
                            column(Product::description) {}
                            column(Product::manufacturer) {
                                dataFormatter = { field -> (field as String).toUpperCase() }
                            }
                            column(Product::distributionDate) {
                                cellExtensions(
                                    CellExcelDataFormatExtension("dd.mm.YYYY")
                                )
                            }
                        }
                        rows {
                            row {
                                selector = RowSelectors.all()
                                cells {
                                    forColumn("nr") { eval = { row -> row.index } }
                                }
                            }
                        }
                    }
                    operations = excelExport(ClassLoader.getSystemResourceAsStream("template.xlsx"))
                }
            }
            Then("file should be written successfully") {
                assertNotNull(file)
            }
        }
    }

})
