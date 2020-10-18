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
import pl.voytech.exporter.impl.template.excel.dataFormat
import pl.voytech.exporter.impl.template.excel.xlsxExport
import pl.voytech.exporter.testutils.CellPosition
import pl.voytech.exporter.testutils.CellRange
import pl.voytech.exporter.testutils.cellassertions.AssertCellValue
import pl.voytech.exporter.testutils.cellassertions.AssertContainsCellExtensions
import pl.voytech.exporter.testutils.cellassertions.AssertMany
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.random.Random
import kotlin.test.assertNotNull

object BasicDslTableExportSpek : Spek({

    Feature("Regular tabular data export to excel") {
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

        Scenario("defining simple table model and exporting to excel file.") {
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
                            width { auto = true },
                            font {
                                fontFamily = "Times New Roman"
                                fontColor = Colors.BLACK
                                fontSize = 12
                            },
                            background { color = Colors.BLUE }
                        )
                    }
                    column(Product::name) {
                        extensions(width { auto = true })
                    }
                    column(Product::description) {
                        extensions(width { auto = true })
                    }
                    column(Product::manufacturer) {
                        extensions(width { auto = true })
                    }
                    column(Product::price) {
                        extensions(width { auto = true })
                    }
                    column(Product::distributionDate) {
                        extensions(
                            width { auto = true },
                            dataFormat { value = "dd.mm.YYYY" }
                        )
                    }
                }
                rows {
                    row {
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
                                leftBorderColor = Colors.BLACK,
                                rightBorderStyle = BorderStyle.SOLID,
                                rightBorderColor = Colors.BLACK,
                                bottomBorderStyle = BorderStyle.SOLID,
                                bottomBorderColor = Colors.BLACK,
                                topBorderStyle = BorderStyle.SOLID,
                                topBorderColor = Colors.BLACK
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
                        selector = RowSelectors.all()
                        cells {
                            forColumn("nr") { eval = { row -> row.objectIndex?.plus(1) } }
                        }
                    }
                }
            }
            FileOutputStream(file).use { productList.exportTable(table, xlsxExport(), it) }
            Then("file should exists and be valid xlsx readable by POI API") {
                PoiTableAssert<Product>(
                    tableName = "Products table",
                    file = File("test0.xlsx"),
                    cellTests = mapOf(
                        CellRange((2..2), (2..8)) to AssertContainsCellExtensions(
                            CellBordersExtension(
                                leftBorderStyle = BorderStyle.SOLID,
                                leftBorderColor = Colors.BLACK,
                                rightBorderStyle = BorderStyle.SOLID,
                                rightBorderColor = Colors.BLACK,
                                bottomBorderStyle = BorderStyle.SOLID,
                                bottomBorderColor = Colors.BLACK,
                                topBorderStyle = BorderStyle.SOLID,
                                topBorderColor = Colors.BLACK
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
                        ),
                        CellPosition(2, 2) to AssertMany(
                            AssertCellValue(expectedType = CellType.STRING, expectedValue = "Nr.:"),
                        ),
                        CellPosition(2, 3) to AssertMany(
                            AssertCellValue(expectedType = CellType.STRING, expectedValue = "Code"),
                            AssertContainsCellExtensions(
                                font {
                                    fontFamily = "Times New Roman"
                                    fontColor = Color(90, 100, 100)
                                    fontSize = 12
                                    italic = true
                                    weight = WeightStyle.BOLD
                                },
                                CellBackgroundExtension(color = Colors.BLUE)
                            )
                        ),
                        CellPosition(2, 4) to AssertCellValue(expectedType = CellType.STRING, expectedValue = "Name"),
                        CellPosition(2, 5) to AssertCellValue(
                            expectedType = CellType.STRING,
                            expectedValue = "Description"
                        ),
                        CellPosition(2, 6) to AssertCellValue(
                            expectedType = CellType.STRING,
                            expectedValue = "Manufacturer"
                        ),
                        CellPosition(2, 7) to AssertCellValue(expectedType = CellType.STRING, expectedValue = "Price"),
                        CellPosition(2, 8) to AssertCellValue(
                            expectedType = CellType.STRING,
                            expectedValue = "Distribution"
                        ),
                        CellPosition(3, 8) to AssertContainsCellExtensions(CellExcelDataFormatExtension("dd.mm.YYYY"))
                    )
                ).perform().also {
                    it.cleanup()
                }
            }
        }
    }

    Feature("Excel file interpolation with dynamic tabular data") {
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
                            column(Product::manufacturer)
                            column(Product::distributionDate) {
                                extensions(
                                    dataFormat {  value = "dd.mm.YYYY" }
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
                    file = File("test2.xlsx"),
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
                        CellPosition(0, 5) to AssertCellValue(
                            expectedType = CellType.STRING,
                            expectedValue = "Distribution"
                        ),
                        CellPosition(1, 5) to AssertContainsCellExtensions(
                            CellExcelDataFormatExtension("dd.mm.YYYY")
                        )
                    )
                ).perform().also {
                    it.cleanup()
                }
            }
        }
    }

    Feature("Excel specific table filtering and sorting") {
        Scenario("defining simple table and exporting to excel file into filterable, sortable 'excel table'.") {
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
            val file = File("test3.xlsx")
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
                            dataFormat {  value = "dd.mm.YYYY" }
                        )
                    }
                }
            }
            FileOutputStream(file).use {
                productList.exportTable(table, xlsxExport(), it)
            }
            Then("file should be written successfully") {
                assertNotNull(file)
                PoiTableAssert<Product>(
                    tableName = "Products table",
                    file = File("test3.xlsx"),
                    cellTests = mapOf()
                ).perform().also {
                    it.cleanup()
                }
            }
        }
    }

    Feature("Tabular data consisting only from custom cells - no dynamic data set included") {
        Scenario("Defining simple table with static cells (different column spans and row spans) and reusable styles.") {
            val file = File("test1.xlsx")
            val styles = arrayOf(
                alignment { horizontal = HorizontalAlignment.CENTER },
                background { color = Colors.WHITE },
                borders {
                    leftBorderColor = Colors.BLACK
                    rightBorderColor = Colors.BLACK
                    topBorderColor = Colors.BLACK
                    bottomBorderColor = Colors.BLACK
                    leftBorderStyle = BorderStyle.SOLID
                    rightBorderStyle = BorderStyle.SOLID
                    topBorderStyle = BorderStyle.SOLID
                    bottomBorderStyle = BorderStyle.SOLID
                },
                font {
                    weight = WeightStyle.BOLD
                    strikeout = false
                    underline = false
                    italic = false
                    fontColor = Colors.BLACK
                }
            )
            FileOutputStream(file).use {
                table<Any> {
                    columns { count = 4 }
                    rows {
                        row {
                            cells {
                                cell {
                                    rowSpan = 2
                                    value = "row span"
                                    extensions(*styles)
                                }
                                cell {
                                    colSpan = 2
                                    value = "This is very long title spanning entire column space."
                                    extensions(*styles)
                                }
                                cell {
                                    value = "Last column."
                                    extensions(*styles)
                                }
                            }
                        }
                        row {
                            cells {
                                cell {
                                    colSpan = 2
                                    value = "This is very long title spanning entire column space. Row 2"
                                    extensions(*styles)
                                }
                                cell {
                                    value = "Last column. Row 2"
                                    extensions(*styles)
                                }
                            }
                        }
                    }
                }.exportWith(xlsxExport(), it)
            }
            Then("file should exists and be valid xlsx readable by POI API") {
                PoiTableAssert<Product>(
                    tableName = "Products table",
                    file = File("test1.xlsx"),
                    cellTests = mapOf()
                ).perform().also {
                    it.cleanup()
                }
            }
        }
    }

})
