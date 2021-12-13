package io.github.voytech.tabulate

import io.github.voytech.tabulate.api.builder.RowPredicates.all
import io.github.voytech.tabulate.api.builder.dsl.*
import io.github.voytech.tabulate.data.Product
import io.github.voytech.tabulate.excel.model.attributes.CellExcelDataFormatAttribute
import io.github.voytech.tabulate.excel.model.attributes.dataFormat
import io.github.voytech.tabulate.excel.model.attributes.filterAndSort
import io.github.voytech.tabulate.excel.model.attributes.format
import io.github.voytech.tabulate.model.RowCellExpression
import io.github.voytech.tabulate.model.and
import io.github.voytech.tabulate.model.attributes.cell.*
import io.github.voytech.tabulate.model.attributes.cell.enums.*
import io.github.voytech.tabulate.model.attributes.column.columnWidth
import io.github.voytech.tabulate.model.attributes.column.width
import io.github.voytech.tabulate.model.attributes.row.height
import io.github.voytech.tabulate.model.attributes.table.template
import io.github.voytech.tabulate.template.export
import io.github.voytech.tabulate.template.tabulate
import io.github.voytech.tabulate.test.CellPosition
import io.github.voytech.tabulate.test.CellRange
import io.github.voytech.tabulate.test.cellassertions.*
import io.github.voytech.tabulate.testsupport.PoiTableAssert
import org.apache.poi.openxml4j.util.ZipSecureFile
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileInputStream
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.random.Random
import kotlin.system.measureTimeMillis

@DisplayName("Testing various excel exports")
class ApachePoiTabulateTests {

    @Test
    fun `should export products to excel file with various style attributes`() {
        val productList = createDataSet(1000)
        measureTimeMillis {
            productList.tabulate("test.xlsx") {
                name = "Products table"
                firstRow = 2
                firstColumn = 2
                columns {
                    column("nr") {
                        attributes {
                            text {
                                fontFamily = "Times New Roman"
                                fontColor = Color(10, 100, 100)
                                fontSize = 12
                                italic = true
                                weight = DefaultWeightStyle.BOLD
                                strikeout = true
                                underline = true
                            }
                        }
                    }
                    column(Product::code) {
                        attributes {
                            text {
                                fontFamily = "Times New Roman"
                                fontColor = Colors.BLACK
                                fontSize = 12
                            }
                            background { color = Colors.BLUE }
                        }
                    }
                    column(Product::name)
                    column(Product::description)
                    column(Product::manufacturer)
                    column(Product::price)
                    column(Product::distributionDate) {
                        attributes {
                            dataFormat { value = "dd.mm.YYYY" }
                        }
                    }
                }
                rows {
                    header {
                        columnTitle("nr") { value = "Nr.:" }
                        columnTitles("Code", "Name", "Description", "Manufacturer", "Price", "Distribution")
                        attributes {
                            height { px = 120 }
                            borders {
                                leftBorderStyle = DefaultBorderStyle.SOLID
                                leftBorderColor = Colors.BLACK
                                rightBorderStyle = DefaultBorderStyle.SOLID
                                rightBorderColor = Colors.BLACK
                                bottomBorderStyle = DefaultBorderStyle.SOLID
                                bottomBorderColor = Colors.BLACK
                                topBorderStyle = DefaultBorderStyle.SOLID
                                topBorderColor = Colors.BLACK
                            }
                            alignment {
                                horizontal = DefaultHorizontalAlignment.CENTER
                                vertical = DefaultVerticalAlignment.MIDDLE
                            }
                            text {
                                fontFamily = "Times New Roman"
                                fontColor = Color(90, 100, 100)
                                fontSize = 12
                                italic = true
                                weight = DefaultWeightStyle.BOLD
                            }
                        }
                    }
                    rowNumberingOn("nr")
                    footer {
                        cell { value = "Footer column 1" }
                        cell { value = "Footer column 2" }
                        cell { value = "Footer column 3" }
                        cell { value = "Footer column 4" }
                        attributes { text { } }
                    }
                }
            }
        }.also {
            println("Elapsed time: $it")
        }

        PoiTableAssert<Product>(
            tableName = "Products table",
            file = File("test.xlsx"),
            cellTests = mapOf(
                CellPosition(2, 2) to AssertContainsCellAttributes(
                    CellTextStylesAttribute(
                        fontFamily = "Times New Roman",
                        fontColor = Color(90, 100, 100),
                        fontSize = 12,
                        italic = true,
                        strikeout = true,
                        underline = true,
                        weight = DefaultWeightStyle.BOLD,
                    )
                ),
                CellRange((2..2), (3..8)) to AssertContainsCellAttributes(
                    CellBordersAttribute(
                        leftBorderStyle = DefaultBorderStyle.SOLID,
                        leftBorderColor = Colors.BLACK,
                        rightBorderStyle = DefaultBorderStyle.SOLID,
                        rightBorderColor = Colors.BLACK,
                        bottomBorderStyle = DefaultBorderStyle.SOLID,
                        bottomBorderColor = Colors.BLACK,
                        topBorderStyle = DefaultBorderStyle.SOLID,
                        topBorderColor = Colors.BLACK,
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
                        weight = DefaultWeightStyle.BOLD,
                    )
                ),
                CellPosition(2, 2) to AssertMany(
                    AssertCellValue(expectedValue = "Nr.:"),
                ),
                CellPosition(2, 3) to AssertMany(
                    AssertCellValue(expectedValue = "Code"),
                    AssertContainsCellAttributes(
                        CellTextStylesAttribute(
                            fontFamily = "Times New Roman",
                            fontColor = Color(90, 100, 100),
                            fontSize = 12,
                            italic = true,
                            weight = DefaultWeightStyle.BOLD
                        ),
                        CellBackgroundAttribute(color = Colors.BLUE)
                    )
                ),
                CellPosition(2, 4) to AssertCellValue(expectedValue = "Name"),
                CellPosition(2, 5) to AssertCellValue(
                    expectedValue = "Description"
                ),
                CellPosition(2, 6) to AssertCellValue(
                    expectedValue = "Manufacturer"
                ),
                CellPosition(2, 7) to AssertCellValue(expectedValue = "Price"),
                CellPosition(2, 8) to AssertCellValue(
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
        createDataSet(1000).tabulate("test.xlsx") {
            name = "Products table"
            firstRow = 1
            attributes {
                template {
                    fileName = "src/test/resources/template.xlsx"
                }
            }
            columns {
                column("nr")
                column(Product::code)
                column(Product::name)
                column(Product::description)
                column(Product::manufacturer)
                column(Product::distributionDate) {
                    attributes {
                        dataFormat { value = "dd.mm.YYYY" }
                    }
                }
            }
            rows {
                row(all()) {
                    cells {
                        cell("nr") { expression = RowCellExpression { row -> row.objectIndex?.plus(1) } }
                    }
                }
            }
        }

        PoiTableAssert<Product>(
            tableName = "Products table",
            file = File("test.xlsx"),
            cellTests = mapOf(
                CellPosition(0, 0) to AssertCellValue(expectedValue = "Nr.:"),
                CellPosition(0, 1) to AssertCellValue(expectedValue = "Code"),
                CellPosition(0, 2) to AssertCellValue(expectedValue = "Name"),
                CellPosition(0, 3) to AssertCellValue(
                    expectedValue = "Description"
                ),
                CellPosition(0, 4) to AssertCellValue(
                    expectedValue = "Manufacturer"
                ),
                CellPosition(0, 5) to AssertCellValue(
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
    fun `should export to excel file with "excel table" feature`() {
        val productList = createDataSet(1000)
        productList.tabulate("test.xlsx") {
            name = "Products table"
            attributes { filterAndSort {} }
            columns {
                column(Product::code)
                column(Product::name)
                column(Product::description)
                column(Product::manufacturer)
                column(Product::price)
                column(Product::distributionDate) {
                    attributes {
                        format { "dd.mm.YYYY" }
                    }
                }
            }
        }

        PoiTableAssert<Product>(
            tableName = "Products table",
            file = File("test.xlsx"),
            cellTests = mapOf()
        ).perform().also {
            it.cleanup()
        }
    }

    @Test
    fun `should export table with custom rows and cell and row spans`() {
        CustomTable {
            name = "Test table"
            rows {
                newRow {
                    cell { rowSpan = 2; value = "Row span" }
                    cell { colSpan = 2; value = "This is very long title. 2 columns span. Row 1" }
                    cell { value = "Last column. Row 1" }
                }
                newRow {
                    cell { colSpan = 2; value = "This is very long title. 2 columns span. Row 2" }
                    cell { value = "Last column. Row 2" }
                }
            }
        }.export(File("test.xlsx"))

        PoiTableAssert<Product>(
            tableName = "Test table",
            file = File("test.xlsx"),
            cellTests = mapOf(
                CellPosition(0, 0) to AssertCellValue(
                    expectedValue = "Row span",
                    expectedRowspan = 2
                ),
                CellPosition(0, 1) to AssertCellValue(
                    expectedValue = "This is very long title. 2 columns span. Row 1",
                    expectedColspan = 2
                ),
                CellPosition(0, 3) to AssertCellValue(
                    expectedValue = "Last column. Row 1"
                ),
                CellPosition(1, 1) to AssertCellValue(
                    expectedValue = "This is very long title. 2 columns span. Row 2",
                    expectedColspan = 2
                ),
                CellPosition(1, 3) to AssertCellValue(
                    expectedValue = "Last column. Row 2"
                ),
            )
        ).perform().also {
            it.cleanup()
        }
    }

    @Test
    fun `should export table with custom row with image`() {
        CustomTable {
            name = "Test table"
            columns {
                column("description")
                column("image") {
                    attributes { width { px = 300 } }
                }
            }
            rows {
                newRow {
                    attributes { height { px = 200 } }
                    cell { value = "It is : " }
                    cell {
                        value = "src/test/resources/kotlin.jpeg"
                        typeHint { DefaultTypeHints.IMAGE_URL }
                    }
                }
            }
        }.export(File("test.xlsx"))

        PoiTableAssert<Product>(
            tableName = "Test table",
            file = File("test.xlsx"),
            cellTests = mapOf(
                CellPosition(0, 0) to AssertCellValue(expectedValue = "It is : "),
                CellPosition(0, 1) to AssertCellValue(
                    expectedValue = FileInputStream("src/test/resources/kotlin.jpeg").readBytes()
                )
            )
        ).perform().also {
            it.cleanup()
        }
    }

    @Test
    fun `should export table using shared table template`() {
        val sharedStyleTemplate = CustomTable {
            attributes {
                columnWidth { auto = true }
            }
            rows {
                header {
                    attributes {
                        background {
                            color = Colors.BLACK
                        }
                        text {
                            fontColor = Colors.WHITE
                            weight = DefaultWeightStyle.BOLD
                        }
                    }
                }
                matching { even() and gt(0) } assign {
                    attributes {
                        background {
                            color = Colors.GREEN
                        }
                    }
                }
                matching { footer() } assign {
                    attributes {
                        background {
                            color = Colors.BLACK
                        }
                    }
                }
            }
        }

        createDataSet(4).tabulate("test.xlsx", sharedStyleTemplate + Table {
            name = "Products"
            columns {
                column(Product::code)
                column(Product::name)
                column(Product::description)
                column(Product::price)
            }
            rows {
                header("Id", "Name", "Description", "Price")
                atIndex { footer() } newRow {
                    cell(Product::code) { value = "Footer first cell" }
                }
            }
        })
        val headerAttributes = AssertMany(
            AssertEqualsAttribute(
                expectedAttribute = CellTextStylesAttribute(
                    fontColor = Colors.WHITE,
                    weight = DefaultWeightStyle.BOLD
                ),
                onlyProperties = setOf(
                    CellTextStylesAttribute::fontColor,
                    CellTextStylesAttribute::weight
                )
            ),
            AssertEqualsAttribute(CellBackgroundAttribute(color = Colors.BLACK))
        )
        PoiTableAssert<Product>(
            tableName = "Products",
            file = File("test.xlsx"),
            cellTests = mapOf(
                CellPosition(0, 0) to AssertMany(
                    AssertCellValue(expectedValue = "Id"), headerAttributes
                ),
                CellPosition(0, 1) to AssertMany(
                    AssertCellValue(expectedValue = "Name"), headerAttributes
                ),
                CellPosition(0, 2) to AssertMany(
                    AssertCellValue(expectedValue = "Description"), headerAttributes
                ),
                CellPosition(0, 3) to AssertMany(
                    AssertCellValue(expectedValue = "Price"), headerAttributes
                ),
                CellRange((1..1), (0..3)) to AssertNoAttribute(CellBackgroundAttribute(color = Colors.GREEN)),
                CellRange((2..2), (0..3)) to AssertEqualsAttribute(CellBackgroundAttribute(color = Colors.GREEN)),
                CellRange((3..3), (0..3)) to AssertNoAttribute(CellBackgroundAttribute(color = Colors.GREEN)),
                CellRange((4..4), (0..3)) to AssertEqualsAttribute(CellBackgroundAttribute(color = Colors.GREEN)),
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
