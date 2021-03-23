package pl.voytech.exporter.core.template

import org.apache.poi.openxml4j.util.ZipSecureFile
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import pl.voytech.exporter.core.api.builder.dsl.export
import pl.voytech.exporter.core.api.builder.dsl.table
import pl.voytech.exporter.core.model.CellType
import pl.voytech.exporter.core.model.RowSelectors
import pl.voytech.exporter.core.model.attributes.functional.FilterAndSortTableAttribute
import pl.voytech.exporter.core.model.attributes.style.*
import pl.voytech.exporter.core.model.attributes.style.enums.BorderStyle
import pl.voytech.exporter.core.model.attributes.style.enums.HorizontalAlignment
import pl.voytech.exporter.core.model.attributes.style.enums.VerticalAlignment
import pl.voytech.exporter.core.model.attributes.style.enums.WeightStyle
import pl.voytech.exporter.core.utils.PoiTableAssert
import pl.voytech.exporter.data.Product
import pl.voytech.exporter.impl.template.excel.CellExcelDataFormatAttribute
import pl.voytech.exporter.impl.template.excel.dataFormat
import pl.voytech.exporter.impl.template.excel.poiExcelExport
import pl.voytech.exporter.testutils.CellPosition
import pl.voytech.exporter.testutils.CellRange
import pl.voytech.exporter.testutils.cellassertions.AssertCellValue
import pl.voytech.exporter.testutils.cellassertions.AssertContainsCellAttributes
import pl.voytech.exporter.testutils.cellassertions.AssertMany
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.random.Random
import kotlin.system.measureTimeMillis

@DisplayName("Testing export to excel")
class ApachePoiTabulateTests {

    @Test
    fun `should export product data set to excel file`() {
        val productList = createDataSet(1000)
        val file = File("test0.xlsx")
        val table = table<Product> {
            name = "Products table"
            firstRow = 2
            firstColumn = 2
            columns {
                column("nr") {
                    attributes(
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
                    attributes(
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
                    attributes(width { auto = true })
                }
                column(Product::description) {
                    attributes(width { auto = true })
                }
                column(Product::manufacturer) {
                    attributes(width { auto = true })
                }
                column(Product::price) {
                    attributes(width { auto = true })
                }
                column(Product::distributionDate) {
                    attributes(
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
                    attributes(
                        RowHeightAttribute(height = 120),
                        CellBordersAttribute(
                            leftBorderStyle = BorderStyle.SOLID,
                            leftBorderColor = Colors.BLACK,
                            rightBorderStyle = BorderStyle.SOLID,
                            rightBorderColor = Colors.BLACK,
                            bottomBorderStyle = BorderStyle.SOLID,
                            bottomBorderColor = Colors.BLACK,
                            topBorderStyle = BorderStyle.SOLID,
                            topBorderColor = Colors.BLACK
                        ),
                        CellAlignmentAttribute(
                            horizontal = HorizontalAlignment.CENTER,
                            vertical = VerticalAlignment.MIDDLE
                        ),
                        CellFontAttribute(
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

        val elapsedTime = FileOutputStream(file).use { outputStream ->
            measureTimeMillis { productList.exportTable(table, poiExcelExport(), outputStream) }
        }
        println("Elapsed time: $elapsedTime")

        PoiTableAssert<Product>(
            tableName = "Products table",
            file = File("test0.xlsx"),
            cellTests = mapOf(
                CellRange((2..2), (2..8)) to AssertContainsCellAttributes(
                    borders {
                        leftBorderStyle = BorderStyle.SOLID
                        leftBorderColor = Colors.BLACK
                        rightBorderStyle = BorderStyle.SOLID
                        rightBorderColor = Colors.BLACK
                        bottomBorderStyle = BorderStyle.SOLID
                        bottomBorderColor = Colors.BLACK
                        topBorderStyle = BorderStyle.SOLID
                        topBorderColor = Colors.BLACK
                    },
                    alignment {
                        horizontal = HorizontalAlignment.CENTER
                        vertical = VerticalAlignment.MIDDLE
                    },
                    font {
                        fontFamily = "Times New Roman"
                        fontColor = Color(90, 100, 100)
                        fontSize = 12
                        italic = true
                        weight = WeightStyle.BOLD
                    }
                ),
                CellPosition(2, 2) to AssertMany(
                    AssertCellValue(expectedType = CellType.STRING, expectedValue = "Nr.:"),
                ),
                CellPosition(2, 3) to AssertMany(
                    AssertCellValue(expectedType = CellType.STRING, expectedValue = "Code"),
                    AssertContainsCellAttributes(
                        font {
                            fontFamily = "Times New Roman"
                            fontColor = Color(90, 100, 100)
                            fontSize = 12
                            italic = true
                            weight = WeightStyle.BOLD
                        },
                        background { color = Colors.BLUE }
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
                CellPosition(3, 8) to AssertContainsCellAttributes(CellExcelDataFormatAttribute("dd.mm.YYYY"))
            )
        ).perform().also {
            it.cleanup()
        }
    }

    @Test
    fun `should interpolate dataset on excel template file`() {
        val file = File("test2.xlsx")
        ZipSecureFile.setMinInflateRatio(0.001)
        val productList = createDataSet(1000)
        FileOutputStream(file).use {
            productList.export(it) {
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
                            attributes(
                                dataFormat { value = "dd.mm.YYYY" }
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
                operations = poiExcelExport(ClassLoader.getSystemResourceAsStream("template.xlsx"))
            }
        }

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
                CellPosition(1, 5) to AssertContainsCellAttributes(
                    CellExcelDataFormatAttribute("dd.mm.YYYY")
                )
            )
        ).perform().also {
            it.cleanup()
        }
    }

    @Test
    fun `should export to excel file with excel table feature`() {
        val productList = createDataSet(1000)
        val file = File("test3.xlsx")
        val table = table<Product> {
            name = "Products table"
            attributes(FilterAndSortTableAttribute(rowRange = (0..999), columnRange = (0..5)))
            columns {
                column(Product::code)
                column(Product::name)
                column(Product::description)
                column(Product::manufacturer)
                column(Product::price)
                column(Product::distributionDate) {
                    attributes(
                        dataFormat { value = "dd.mm.YYYY" }
                    )
                }
            }
        }
        FileOutputStream(file).use {
            productList.exportTable(table, poiExcelExport(), it)
        }
        assertNotNull(file)
        PoiTableAssert<Product>(
            tableName = "Products table",
            file = File("test3.xlsx"),
            cellTests = mapOf()
        ).perform().also {
            it.cleanup()
        }
    }

    @Test
    fun `should export table with custom rows and cell and row spans`() {
        val file = File("test1.xlsx")
        val cellStyle = listOf(
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
                name = "Test table"
                columns { count = 4 }
                attributes(cellStyle)
                rows {
                    row {
                        cells {
                            cell {
                                rowSpan = 2
                                value = "Row span"
                            }
                            cell {
                                colSpan = 2
                                value = "This is very long title. 2 columns span. Row 1"
                            }
                            cell {
                                value = "Last column. Row 1"
                            }
                        }
                    }
                    row {
                        cells {
                            cell {
                                colSpan = 2
                                value = "This is very long title. 2 columns span. Row 2"
                            }
                            cell {
                                value = "Last column. Row 2"
                            }
                        }
                    }
                }
            }.export(poiExcelExport(), it)
        }
        PoiTableAssert<Product>(
            tableName = "Test table",
            file = File("test1.xlsx"),
            cellTests = mapOf(
                CellPosition(0, 0) to AssertCellValue(
                    expectedType = CellType.STRING,
                    expectedValue = "Row span",
                    expectedRowspan = 2
                ),
                CellPosition(0, 1) to AssertCellValue(
                    expectedType = CellType.STRING,
                    expectedValue = "This is very long title. 2 columns span. Row 1",
                    expectedColspan = 2
                ),
                CellPosition(0, 3) to AssertCellValue(
                    expectedType = CellType.STRING,
                    expectedValue = "Last column. Row 1"
                ),
                CellPosition(1, 1) to AssertCellValue(
                    expectedType = CellType.STRING,
                    expectedValue = "This is very long title. 2 columns span. Row 2",
                    expectedColspan = 2
                ),
                CellPosition(1, 3) to AssertCellValue(
                    expectedType = CellType.STRING,
                    expectedValue = "Last column. Row 2"
                ),
            )
        ).perform().also {
            it.cleanup()
        }
    }

    @Test
    fun `should export table with custom row with image`() {
        val file = File("test_img.xlsx")
        FileOutputStream(file).use {
            table<Any> {
                name = "Test table"
                columns {
                    column("description")
                    column("image") {
                        attributes(width { width = 300 })
                    }
                }
                rows {
                    row {
                        attributes(height { height = 200 })
                        cells {
                            cell { value = "It is : " }
                            cell {
                                value = "src/test/resources/kotlin.jpeg"
                                type = CellType.IMAGE_URL
                            }
                        }
                    }
                }
            }.export(poiExcelExport(), it)
        }
        PoiTableAssert<Product>(
            tableName = "Test table",
            file = File("test_img.xlsx"),
            cellTests = mapOf(
                CellPosition(0, 0) to AssertCellValue(expectedType = CellType.STRING, expectedValue = "It is : "),
                CellPosition(0, 1) to AssertCellValue(
                    expectedType = CellType.IMAGE_DATA,
                    expectedValue = FileInputStream("src/test/resources/kotlin.jpeg").readBytes()
                )
            )
        ).perform().also {
            it.cleanup()
        }
    }

    private fun createDataSet(count: Int? = 1): List<Product> {
        val random = Random(count!!)
        return (0..count).map {
            Product(
                if (it % 2 == 0) "prod_nr_${it}${it % 2}" else "prod_nr_$it",
                "Name $it",
                "This is description $it",
                "manufacturer $it",
                LocalDate.now(),
                BigDecimal(random.nextDouble(200.00, 1000.00))
            )
        }
    }
}
