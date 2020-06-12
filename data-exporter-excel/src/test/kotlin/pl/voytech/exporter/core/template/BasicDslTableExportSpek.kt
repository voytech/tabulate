package pl.voytech.exporter.core.template

import org.apache.poi.openxml4j.util.ZipSecureFile
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import pl.voytech.exporter.core.api.dsl.export
import pl.voytech.exporter.core.api.dsl.table
import pl.voytech.exporter.core.model.CellType
import pl.voytech.exporter.core.model.RowSelectors
import pl.voytech.exporter.core.model.extension.style.*
import pl.voytech.exporter.core.model.extension.style.enums.BorderStyle
import pl.voytech.exporter.core.model.extension.style.enums.HorizontalAlignment
import pl.voytech.exporter.core.model.extension.style.enums.VerticalAlignment
import pl.voytech.exporter.core.model.extension.style.enums.WeightStyle
import pl.voytech.exporter.core.utils.PoiTableAssert
import pl.voytech.exporter.data.Product
import pl.voytech.exporter.impl.template.excel.CellExcelDataFormatExtension
import pl.voytech.exporter.impl.template.excel.xlsxExport
import pl.voytech.exporter.testutils.CellPosition
import pl.voytech.exporter.testutils.cellassertions.AssertCellExtensions
import pl.voytech.exporter.testutils.cellassertions.AssertCellValue
import pl.voytech.exporter.testutils.cellassertions.AssertEqualExtension
import pl.voytech.exporter.testutils.cellassertions.AssertMany
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.random.Random
import kotlin.test.assertNotNull

object BasicDslTableExportSpek : Spek({
    Feature("Regular tabular data export to excel") {
        Scenario("defining simple table model and exporting to excel file.") {
            val random = Random(1000)
            val productList = (0..999).map {
                Product(
                    "prod_nr_$it",
                    "Name $it",
                    "This is description $it",
                    "manufacturer $it",
                    LocalDate.now(),
                    BigDecimal(random.nextDouble(200.00, 1000.00))
                )
            }
            val file = File("test.xlsx")
            val table = table<Product> {
                name = "Products table"
                firstRow = 2
                firstColumn = 2
                columns {
                    column("nr") {
                        columnTitle { title = "Nr.:" }
                        columnExtensions(ColumnWidthExtension(width = 50))
                        cellExtensions(
                            CellFontExtension(
                                fontFamily = "Times New Roman",
                                fontColor = Color(10, 100, 100),
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
                                fontColor = Color(0, 0, 0),
                                fontSize = 12
                            ),
                            CellBackgroundExtension(color = Color(10, 100, 100))
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
                    column(Product::price) { columnTitle { title = "Price" }}
                    column(Product::distributionDate) {
                        columnTitle { title = "Distribution" }
                        cellExtensions(
                            CellExcelDataFormatExtension("dd.mm.YYYY")
                        )
                    }
                }
                rows {
                    row {
                        createAt = productList.size + 1
                        cells {
                            forColumn("nr") {
                                eval = { row -> row.dataset.size}
                            }
                            forColumn(Product::distributionDate) {
                                value = "This is a date of distribution"
                                cellExtensions(CellFontExtension(weight = WeightStyle.BOLD))
                            }
                            forColumn(Product::manufacturer) {
                                value = "A name of manufacturer"
                            }
                            forColumn(Product::description) {
                                value = "A product description"
                            }
                            forColumn(Product::price) {
                                value = "=SUM(K12:K111)"
                                type = CellType.NATIVE_FORMULA
                            }
                        }
                    }
                    row {
                        selector = RowSelectors.atRowIndex(0)
                        rowExtensions(RowHeightExtension(height = 220))
                        cellExtensions(
                            CellBordersExtension(
                                leftBorderStyle = BorderStyle.SOLID,
                                leftBorderColor = Color(0, 0, 0),
                                rightBorderStyle = BorderStyle.SOLID,
                                rightBorderColor = Color(0, 0, 0),
                                bottomBorderStyle = BorderStyle.SOLID,
                                bottomBorderColor = Color(0, 0, 0)
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
                            forColumn("nr") { eval = { row -> row.objectIndex } }
                        }
                    }
                }
            }
            FileOutputStream(file).use {
                productList.exportTo(table, xlsxExport(), it)
            }
            Then("file should exists and be valid xlsx readable by POI API") {
                PoiTableAssert<Product>(
                    tableName = "Products table",
                    file =  File("test.xlsx"),
                    cellTests = mapOf(
                        CellPosition(2, 2) to AssertMany(
                            AssertCellValue(expectedType = CellType.STRING, expectedValue = "Nr.:"),
                            AssertCellExtensions(
                                AssertEqualExtension(
                                    CellFontExtension(
                                        fontFamily = "Times New Roman",
                                        fontColor = Color(10, 100, 100),
                                        fontSize = 12,
                                        italic = true,
                                        weight = WeightStyle.BOLD,
                                        strikeout = true,
                                        underline = true
                                    )
                                )
                            )
                        ),
                        CellPosition(2, 3) to AssertMany(
                            AssertCellValue(expectedType = CellType.STRING, expectedValue = "Code"),
                            AssertCellExtensions(
                                AssertEqualExtension(
                                    CellBackgroundExtension(color = Color(10, 100, 100))
                                ),
                                AssertEqualExtension(
                                    CellFontExtension(
                                        fontFamily = "Times New Roman",
                                        fontColor = Color(0, 0, 0),
                                        fontSize = 12
                                    )
                                )
                            )
                        ),
                        CellPosition(2, 4) to AssertCellValue(expectedType = CellType.STRING, expectedValue = "Name"),
                        CellPosition(2, 5) to AssertCellValue(expectedType = CellType.STRING, expectedValue = "Description"),
                        CellPosition(2, 6) to AssertCellValue(expectedType = CellType.STRING, expectedValue = "Manufacturer"),
                        CellPosition(2, 7) to AssertCellValue(expectedType = CellType.STRING, expectedValue = "Price"),
                        CellPosition(2, 8) to AssertCellValue(expectedType = CellType.STRING, expectedValue = "Distribution")
                    )
                )
                .perform().also {
                    it.cleanup()
                }
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
                    LocalDate.now(),
                    BigDecimal(Random(1000).nextDouble(200.00, 1000.00))
                )
            }
            val file = File("test2.xlsx")
            ZipSecureFile.setMinInflateRatio(0.001)
            FileOutputStream(file).use {
                productList.export<Product, SXSSFWorkbook>(it) {
                    table {
                        name = "Products table"
                        firstRow = 1
                        columns {
                            column("nr")
                            column(Product::code)
                            column(Product::name)
                            column(Product::description)
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
                                    forColumn("nr") { eval = { row -> row.objectIndex } }
                                }
                            }
                        }
                    }
                    operations = xlsxExport(ClassLoader.getSystemResourceAsStream("template.xlsx"))
                }
            }
            Then("file should be written successfully") {
                assertNotNull(file)
            }
        }
    }

})
