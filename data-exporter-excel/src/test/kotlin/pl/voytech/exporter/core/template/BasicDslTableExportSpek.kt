package pl.voytech.exporter.core.template

import org.apache.poi.openxml4j.util.ZipSecureFile
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import pl.voytech.exporter.core.api.builder.dsl.export
import pl.voytech.exporter.core.api.builder.dsl.table
import pl.voytech.exporter.core.model.CellType
import pl.voytech.exporter.core.model.RowSelectors
import pl.voytech.exporter.core.model.extension.functional.FilterAndSortTableExtension
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
import pl.voytech.exporter.testutils.CellRange
import pl.voytech.exporter.testutils.cellassertions.AssertCellValue
import pl.voytech.exporter.testutils.cellassertions.AssertCellValueExpr
import pl.voytech.exporter.testutils.cellassertions.AssertContainsCellExtensions
import pl.voytech.exporter.testutils.cellassertions.AssertMany
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.random.Random
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

object BasicDslTableExportSpek : Spek({

     Feature("Regular tabular data export to excel") {
        Scenario("defining simple table model and exporting to excel file.") {
            val random = Random(1000)
            val productList = (0..1000).map {
                Product(
                    if (it % 2 == 0) "prod_nr_${it}${it % 2}" else "prod_nr_$it",
                    "Name $it",
                    "This is description $it",
                    "manufacturer $it",
                    LocalDate.now(),
                    BigDecimal(random.nextDouble(200.00, 1000.00))
                )
            }
            val file = File("test0.xlsx")
            val table = table<Product> {
                name = "Products table"
                firstRow = 2
                firstColumn = 2
                columns {
                    column("nr") {
                        extensions(
                            width { width = 50 },
                            font {
                                fontFamily = "Times New Roman"
                                fontColor = Color(10, 100, 100)
                                fontSize = 12
                                italic = true
                                weight = WeightStyle.BOLD
                                strikeout = true
                                underline = true
                            }
                        )
                    }
                    column(Product::code) {
                        extensions(
                            ColumnWidthExtension(auto = true),
                            CellFontExtension(
                                fontFamily = "Times New Roman",
                                fontColor = Colors.BLACK,
                                fontSize = 12
                            ),
                            CellBackgroundExtension(color = Color(10, 100, 100))
                        )
                    }
                    column(Product::name) {
                        extensions(ColumnWidthExtension(auto = true))
                    }
                    column(Product::description) {
                        extensions(ColumnWidthExtension(auto = true))
                    }
                    column(Product::manufacturer) {
                        extensions(ColumnWidthExtension(auto = true))
                    }
                    column(Product::price) {
                        extensions(ColumnWidthExtension(auto = true))
                    }
                    column(Product::distributionDate) {
                        extensions(
                            ColumnWidthExtension(auto = true),
                            CellExcelDataFormatExtension("dd.mm.YYYY")
                        )
                    }
                }
                rows {
                    row {
                        createAt = 0
                        cells {
                            cell {
                                colSpan = 7
                                value = "This is very long title spanning entire column space."
                                extensions(
                                    alignment {
                                        horizontal = HorizontalAlignment.CENTER
                                    },
                                    font {
                                        strikeout = false
                                        weight = WeightStyle.BOLD
                                    }
                                )
                            }
                        }
                    }
                    row {
                        createAt = 1
                        cells {
                            forColumn("nr") { value = "Nr.:" }
                            forColumn(Product::code) { value = "Code" }
                            forColumn(Product::name) { value = "Name" }
                            forColumn(Product::description) { value = "Description" }
                            forColumn(Product::manufacturer) { value = "Manufacturer" }
                            forColumn(Product::price) { value = "Price" }
                            forColumn(Product::distributionDate) { value = "Distribution" }
                        }
                        extensions(
                            RowHeightExtension(height = 120),
                            CellBordersExtension(
                                leftBorderStyle = BorderStyle.SOLID,
                                leftBorderColor = Color(0, 0, 0),
                                rightBorderStyle = BorderStyle.SOLID,
                                rightBorderColor = Color(0, 0, 0),
                                bottomBorderStyle = BorderStyle.SOLID,
                                bottomBorderColor = Color(0, 0, 0),
                                topBorderStyle = BorderStyle.SOLID,
                                topBorderColor = Color(0, 0, 0)
                            ),
                            CellAlignmentExtension(
                                horizontal = HorizontalAlignment.CENTER,
                                vertical = VerticalAlignment.MIDDLE
                            ),
                            CellFontExtension(
                                fontFamily = "Times New Roman",
                                fontColor = Color(90, 100, 100),
                                fontSize = 12,
                                italic = true,
                                weight = WeightStyle.BOLD
                            )
                        )
                    }
                    row {
                        createAt = productList.size + 1
                        cells {
                            forColumn("nr") {
                                value = "."
                            }
                            forColumn(Product::price) {
                                value = "=SUM(K12:K111)"
                                type = CellType.NATIVE_FORMULA
                            }
                        }
                    }
                    row {
                        selector = RowSelectors.all()
                        cells {
                            forColumn("nr") { eval = { row -> row.objectIndex?.plus(1) } }
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
                    file = File("test0.xlsx"),
                    cellTests = mapOf(
                        CellRange((2..2),(2..8)) to AssertContainsCellExtensions(
                            CellBordersExtension(
                                leftBorderStyle = BorderStyle.SOLID,
                                leftBorderColor = Color(0, 0, 0),
                                rightBorderStyle = BorderStyle.SOLID,
                                rightBorderColor = Color(0, 0, 0),
                                bottomBorderStyle = BorderStyle.SOLID,
                                bottomBorderColor = Color(0, 0, 0),
                                topBorderStyle = BorderStyle.SOLID,
                                topBorderColor = Color(0, 0, 0)
                            ),
                            CellAlignmentExtension(
                                horizontal = HorizontalAlignment.CENTER,
                                vertical = VerticalAlignment.MIDDLE
                            )
                        ),
                        CellPosition(2, 2) to AssertMany(
                            AssertCellValue(expectedType = CellType.STRING, expectedValue = "Nr.:"),
                            AssertContainsCellExtensions(
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
                        ),
                        CellPosition(2, 3) to AssertMany(
                            AssertCellValue(expectedType = CellType.STRING, expectedValue = "Code"),
                            AssertContainsCellExtensions(
                                CellBackgroundExtension(color = Color(10, 100, 100)),
                                CellFontExtension(
                                    fontFamily = "Times New Roman",
                                    fontColor = Color(0, 0, 0),
                                    fontSize = 12
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
                   // it.cleanup()
                }
            }
        }
    }

    Feature("Tabular data export to excel using excel template as input.") {
        Scenario("loading from template file and filling it up.") {
            val productList = (0..2).map {
                Product(
                    "prod_nr_$it",
                    "Name $it",
                    "This is description $it",
                    "manufacturer $it",
                    LocalDate.now(),
                    BigDecimal(Random(1000).nextDouble(200.00, 1000.00))
                )
            }
            val file = File("test1.xlsx")
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
                                extensions(
                                    CellExcelDataFormatExtension("dd.mm.YYYY")
                                )
                            }
                        }
                        rows {
                            row {
                                selector = RowSelectors.all()
                                cells {
                                    forColumn("nr") { eval = { row -> row.objectIndex?.plus(1) } }
                                }
                            }
                        }
                    }
                    operations = xlsxExport(ClassLoader.getSystemResourceAsStream("template.xlsx"))
                }
            }
            Then("file should be written successfully") {
                assertNotNull(file)
                PoiTableAssert<Product>(
                    tableName = "Products table",
                    file = File("test1.xlsx"),
                    cellTests = mapOf(
                        CellPosition(0, 0) to AssertCellValue(expectedType = CellType.STRING, expectedValue = "Nr.:"),
                        CellPosition(0, 1) to AssertCellValue(expectedType = CellType.STRING, expectedValue = "Code"),
                        CellPosition(0, 2) to AssertCellValue(expectedType = CellType.STRING, expectedValue = "Name"),
                        CellPosition(0, 3) to AssertCellValue(
                            expectedType = CellType.STRING,
                            expectedValue = "Description"
                        ),
                        CellPosition(0, 4) to AssertCellValue(
                            expectedType = CellType.STRING,
                            expectedValue = "Manufacturer"
                        ),
                        CellPosition(1, 4) to AssertCellValueExpr(invoke = { (value, _) ->
                            assertTrue(
                                (value as String).filter { c -> c.isLetter() }.all { c -> c.isUpperCase() },
                                "expected only upper case characters!"
                            )
                        }),
                        CellPosition(0, 5) to AssertCellValue(
                            expectedType = CellType.STRING,
                            expectedValue = "Distribution"
                        ),
                        CellPosition(1, 5) to AssertContainsCellExtensions(
                            CellExcelDataFormatExtension("dd.mm.YYYY")
                        )
                    )
                )
                .perform().also {
                    //it.cleanup()
                }
            }
        }
    }


    Feature("tabular data export to 'xlsx excel table'") {
        Scenario("defining simple table and exporting to excel file into so called excel table.") {
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
            val file = File("test2.xlsx")
            val table = table<Product> {
                name = "Products table"
                extensions(FilterAndSortTableExtension(rowRange = (0..999), columnRange = (0..5)))
                columns {
                    column(Product::code)
                    column(Product::name)
                    column(Product::description)
                    column(Product::manufacturer)
                    column(Product::price)
                    column(Product::distributionDate) {
                        extensions(
                            CellExcelDataFormatExtension("dd.mm.YYYY")
                        )
                    }
                }
            }
            FileOutputStream(file).use {
                productList.exportTo(table, xlsxExport(), it)
            }
            Then("file should be written successfully") {
                assertNotNull(file)
            }
        }
    }

})
