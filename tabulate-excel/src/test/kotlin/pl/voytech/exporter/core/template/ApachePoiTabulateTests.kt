package pl.voytech.exporter.core.template

import org.apache.poi.openxml4j.util.ZipSecureFile
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import pl.voytech.exporter.core.api.builder.dsl.table
import pl.voytech.exporter.core.model.CellType
import pl.voytech.exporter.core.model.RowSelectors
import pl.voytech.exporter.core.model.attributes.cell.*
import pl.voytech.exporter.core.model.attributes.cell.enums.DefaultBorderStyle
import pl.voytech.exporter.core.model.attributes.cell.enums.DefaultHorizontalAlignment
import pl.voytech.exporter.core.model.attributes.cell.enums.DefaultVerticalAlignment
import pl.voytech.exporter.core.model.attributes.cell.enums.DefaultWeightStyle
import pl.voytech.exporter.core.model.attributes.column.width
import pl.voytech.exporter.core.model.attributes.row.RowHeightAttribute
import pl.voytech.exporter.core.model.attributes.row.height
import pl.voytech.exporter.core.model.attributes.table.FilterAndSortTableAttribute
import pl.voytech.exporter.core.model.attributes.table.template
import pl.voytech.exporter.core.utils.PoiTableAssert
import pl.voytech.exporter.data.Product
import pl.voytech.exporter.impl.template.excel.CellExcelDataFormatAttribute
import pl.voytech.exporter.impl.template.excel.dataFormat
import pl.voytech.exporter.impl.template.excel.xlsx
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
                        width { px = 50 },
                        text {
                            fontFamily = "Times New Roman"
                            fontColor = Color(10, 100, 100)
                            fontSize = 12
                            italic = true
                            weight = DefaultWeightStyle.BOLD
                            strikeout = true
                            underline = true
                        }
                    )
                }
                column(Product::code) {
                    attributes(
                        width { auto = true },
                        text {
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
                        RowHeightAttribute(px = 120),
                        CellBordersAttribute(
                            leftBorderStyle = DefaultBorderStyle.SOLID,
                            leftBorderColor = Colors.BLACK,
                            rightBorderStyle = DefaultBorderStyle.SOLID,
                            rightBorderColor = Colors.BLACK,
                            bottomBorderStyle = DefaultBorderStyle.SOLID,
                            bottomBorderColor = Colors.BLACK,
                            topBorderStyle = DefaultBorderStyle.SOLID,
                            topBorderColor = Colors.BLACK
                        ),
                        CellAlignmentAttribute(
                            horizontal = DefaultHorizontalAlignment.CENTER,
                            vertical = DefaultVerticalAlignment.MIDDLE
                        ),
                        CellTextStylesAttribute(
                            fontFamily = "Times New Roman",
                            fontColor = Color(90, 100, 100),
                            fontSize = 12,
                            italic = true,
                            weight = DefaultWeightStyle.BOLD
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
            measureTimeMillis { productList.tabulate(table, xlsx(), outputStream) }
        }
        println("Elapsed time: $elapsedTime")

        PoiTableAssert<Product>(
            tableName = "Products table",
            file = File("test0.xlsx"),
            cellTests = mapOf(
                CellRange((2..2), (2..8)) to AssertContainsCellAttributes(
                    borders {
                        leftBorderStyle = DefaultBorderStyle.SOLID
                        leftBorderColor = Colors.BLACK
                        rightBorderStyle = DefaultBorderStyle.SOLID
                        rightBorderColor = Colors.BLACK
                        bottomBorderStyle = DefaultBorderStyle.SOLID
                        bottomBorderColor = Colors.BLACK
                        topBorderStyle = DefaultBorderStyle.SOLID
                        topBorderColor = Colors.BLACK
                    },
                    alignment {
                        horizontal = DefaultHorizontalAlignment.CENTER
                        vertical = DefaultVerticalAlignment.MIDDLE
                    },
                    text {
                        fontFamily = "Times New Roman"
                        fontColor = Color(90, 100, 100)
                        fontSize = 12
                        italic = true
                        weight = DefaultWeightStyle.BOLD
                    }
                ),
                CellPosition(2, 2) to AssertMany(
                    AssertCellValue(expectedType = CellType.STRING, expectedValue = "Nr.:"),
                ),
                CellPosition(2, 3) to AssertMany(
                    AssertCellValue(expectedType = CellType.STRING, expectedValue = "Code"),
                    AssertContainsCellAttributes(
                        text {
                            fontFamily = "Times New Roman"
                            fontColor = Color(90, 100, 100)
                            fontSize = 12
                            italic = true
                            weight = DefaultWeightStyle.BOLD
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
        ZipSecureFile.setMinInflateRatio(0.001)
        createDataSet(1000).tabulate(
            table {
                name = "Products table"
                firstRow = 1
                attributes( template { fileName = "src/test/resources/template.xlsx" } )
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
            },
            File("test2.xlsx")
        )

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
        productList.tabulate(
            table {
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
            }, File("test3.xlsx")
        )
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
            alignment { horizontal = DefaultHorizontalAlignment.CENTER },
            background { color = Colors.WHITE },
            borders {
                leftBorderColor = Colors.BLACK
                rightBorderColor = Colors.BLACK
                topBorderColor = Colors.BLACK
                bottomBorderColor = Colors.BLACK
                leftBorderStyle = DefaultBorderStyle.SOLID
                rightBorderStyle = DefaultBorderStyle.SOLID
                topBorderStyle = DefaultBorderStyle.SOLID
                bottomBorderStyle = DefaultBorderStyle.SOLID
            },
            text {
                weight = DefaultWeightStyle.BOLD
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
            }.export(xlsx(), it)
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
                        attributes(width { px = 300 })
                    }
                }
                rows {
                    row {
                        attributes(height { px = 200 })
                        cells {
                            cell { value = "It is : " }
                            cell {
                                value = "src/test/resources/kotlin.jpeg"
                                type = CellType.IMAGE_URL
                            }
                        }
                    }
                }
            }.export(xlsx(), it)
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
