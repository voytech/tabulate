package io.github.voytech.tabulate

import io.github.voytech.tabulate.components.document.api.builder.dsl.document
import io.github.voytech.tabulate.components.document.template.export
import io.github.voytech.tabulate.components.page.api.builder.dsl.page
import io.github.voytech.tabulate.components.table.api.builder.RowPredicates.all
import io.github.voytech.tabulate.components.table.api.builder.dsl.*
import io.github.voytech.tabulate.components.table.model.RowCellExpression
import io.github.voytech.tabulate.components.table.model.and
import io.github.voytech.tabulate.components.table.model.attributes.cell.enums.DefaultTypeHints
import io.github.voytech.tabulate.components.table.model.attributes.table.template
import io.github.voytech.tabulate.components.table.template.export
import io.github.voytech.tabulate.components.table.template.tabulate
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.Height
import io.github.voytech.tabulate.core.model.UnitsOfMeasure
import io.github.voytech.tabulate.core.model.Width
import io.github.voytech.tabulate.core.model.alignment.DefaultHorizontalAlignment
import io.github.voytech.tabulate.core.model.alignment.DefaultVerticalAlignment
import io.github.voytech.tabulate.core.model.attributes.*
import io.github.voytech.tabulate.core.model.border.DefaultBorderStyle
import io.github.voytech.tabulate.core.model.color.Color
import io.github.voytech.tabulate.core.model.color.Colors
import io.github.voytech.tabulate.core.model.text.DefaultFonts
import io.github.voytech.tabulate.core.model.text.DefaultWeightStyle
import io.github.voytech.tabulate.excel.components.table.model.ExcelBorderStyle
import io.github.voytech.tabulate.excel.components.table.model.attributes.*
import io.github.voytech.tabulate.test.*
import io.github.voytech.tabulate.test.assertions.*
import io.github.voytech.tabulate.test.sampledata.SampleProduct
import io.github.voytech.tabulate.testsupport.PoiTableAssert
import org.apache.poi.openxml4j.util.ZipSecureFile
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileInputStream
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.reflect.KProperty1
import kotlin.system.measureTimeMillis

@DisplayName("Testing various excel exports")
class ApachePoiTabulateTests {
    @Test
    fun `should correctly override cell borders defined on various levels`() {
        val productList = SampleProduct.create(1)
        productList.tabulate("test.xlsx") {
            name = "Products table"
            attributes {
                columnWidth { auto = true }
                borders {
                    leftBorderStyle = ExcelBorderStyle.HAIR
                    leftBorderColor = Colors.BLACK
                    rightBorderStyle = ExcelBorderStyle.HAIR
                    rightBorderColor = Colors.BLACK
                    topBorderStyle = ExcelBorderStyle.HAIR
                    topBorderColor = Colors.BLACK
                    bottomBorderStyle = ExcelBorderStyle.HAIR
                    bottomBorderColor = Colors.BLACK
                }
            }
            columns {
                column(SampleProduct::code) { attributes { width { 222.px() } }}
                column(SampleProduct::name)
                column(SampleProduct::description)
                column(SampleProduct::manufacturer)
                column(SampleProduct::price)
            }
            rows {
                newRow {
                    cell {
                        value = "Header column 1"
                        attributes {
                            borders {
                                leftBorderStyle = ExcelBorderStyle.THICK
                                leftBorderColor = Colors.BLACK
                            }
                        }
                    }
                    cell { value = "Header column 2" }
                    cell { value = "Header column 3" }
                    cell { value = "Header column 4" }
                    cell {
                        value = "Header column 5"
                        attributes {
                            borders {
                                rightBorderStyle = ExcelBorderStyle.THICK
                                rightBorderColor = Colors.BLACK
                            }
                        }
                    }
                    attributes {
                        borders {
                            bottomBorderStyle = ExcelBorderStyle.THICK
                            bottomBorderColor = Colors.BLACK
                            topBorderStyle = ExcelBorderStyle.THICK
                            topBorderColor = Colors.BLACK
                        }
                    }
                }
                footer {
                    attributes {
                        borders {
                            bottomBorderStyle = ExcelBorderStyle.THICK
                            bottomBorderColor = Colors.BLACK
                            topBorderStyle = ExcelBorderStyle.THICK
                            topBorderColor = Colors.BLACK
                        }
                    }
                    cell {
                        value = "Footer column 1"
                        attributes {
                            borders {
                                leftBorderStyle = ExcelBorderStyle.THICK
                                leftBorderColor = Colors.BLACK
                            }
                        }
                    }
                    cell { value = "Footer column 2" }
                    cell { value = "Footer column 3" }
                    cell { value = "Footer column 4" }
                    cell {
                        value = "Footer column 5"
                        attributes {
                            borders {
                                rightBorderStyle = ExcelBorderStyle.THICK
                                rightBorderColor = Colors.BLACK
                            }
                        }
                    }
                }
            }
        }
        PoiTableAssert<SampleProduct>(
            tableName = "Products table",
            file = File("test.xlsx"),
            attributeTests = mapOf(
                ColumnPosition(0) to AssertEqualsAttribute(WidthAttribute(value = Width(222F,UnitsOfMeasure.PX))),
            )
        ).perform().also {
            it.cleanup()
        }
    }

    @Test
    fun `should export products to excel file with various style attributes`() {
        val productList = SampleProduct.create(1000)
        measureTimeMillis {
            productList.tabulate("test.xlsx") {
                name = "Products table"
                firstRow = 2
                firstColumn = 2
                columns {
                    column("nr") {
                        attributes {
                            text {
                                fontFamily = DefaultFonts.TIMES_NEW_ROMAN
                                fontColor = Color(10, 100, 100)
                                fontSize = 12
                                italic = true
                                weight = DefaultWeightStyle.BOLD
                                strikeout = true
                                underline = true
                            }
                        }
                    }
                    column(SampleProduct::code) {
                        attributes {
                            text {
                                fontFamily = DefaultFonts.TIMES_NEW_ROMAN
                                fontColor = Colors.BLACK
                                fontSize = 12
                            }
                            background { color = Colors.BLUE }
                        }
                    }
                    column(SampleProduct::name)
                    column(SampleProduct::description)
                    column(SampleProduct::manufacturer)
                    column(SampleProduct::price)
                    column(SampleProduct::distributionDate) {
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
                            height { 120.px() }
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
                                fontFamily = DefaultFonts.TIMES_NEW_ROMAN
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

        PoiTableAssert<SampleProduct>(
            tableName = "Products table",
            file = File("test.xlsx"),
            valueTests = mapOf(
                CellPosition(2, 2) to AssertCellValue(expectedValue = "Nr.:"),
                CellPosition(2, 3) to AssertCellValue(expectedValue = "Code"),
                CellPosition(2, 4) to AssertCellValue(expectedValue = "Name"),
                CellPosition(2, 5) to AssertCellValue(expectedValue = "Description"),
                CellPosition(2, 6) to AssertCellValue(expectedValue = "Manufacturer"),
                CellPosition(2, 7) to AssertCellValue(expectedValue = "Price"),
                CellPosition(2, 8) to AssertCellValue(expectedValue = "Distribution"),
            ),
            attributeTests = mapOf(
                CellPosition(2, 2) to AssertContainsAttributes(
                    TextStylesAttribute(
                        fontFamily = DefaultFonts.TIMES_NEW_ROMAN,
                        fontColor = Color(90, 100, 100),
                        fontSize = 12,
                        italic = true,
                        strikeout = true,
                        underline = true,
                        weight = DefaultWeightStyle.BOLD,
                    )
                ),
                CellRange((2..2), (3..8)) to AssertContainsAttributes(
                    BordersAttribute(
                        leftBorderStyle = DefaultBorderStyle.SOLID,
                        leftBorderColor = Colors.BLACK,
                        rightBorderStyle = DefaultBorderStyle.SOLID,
                        rightBorderColor = Colors.BLACK,
                        bottomBorderStyle = DefaultBorderStyle.SOLID,
                        bottomBorderColor = Colors.BLACK,
                        topBorderStyle = DefaultBorderStyle.SOLID,
                        topBorderColor = Colors.BLACK,
                    ),
                    AlignmentAttribute(
                        horizontal = DefaultHorizontalAlignment.CENTER,
                        vertical = DefaultVerticalAlignment.MIDDLE
                    ),
                    TextStylesAttribute(
                        fontFamily = DefaultFonts.TIMES_NEW_ROMAN,
                        fontColor = Color(90, 100, 100),
                        fontSize = 12,
                        italic = true,
                        weight = DefaultWeightStyle.BOLD,
                    )
                ),
                CellPosition(2, 3) to AssertContainsAttributes(
                        TextStylesAttribute(
                            fontFamily = DefaultFonts.TIMES_NEW_ROMAN,
                            fontColor = Color(90, 100, 100),
                            fontSize = 12,
                            italic = true,
                            weight = DefaultWeightStyle.BOLD
                        ),
                        BackgroundAttribute(color = Colors.BLUE)
                ),
                CellPosition(3, 8) to AssertEqualsAttribute(
                    CellExcelDataFormatAttribute("dd.mm.YYYY")
                ),
                RowPosition(2) to AssertEqualsAttribute(
                    HeightAttribute(value = Height(120F,UnitsOfMeasure.PX))
                )
            )
        ).perform().also {
            it.cleanup()
        }
    }

    @Test
    fun `should interpolate dataset on excel template file`() {
        ZipSecureFile.setMinInflateRatio(0.001)
        SampleProduct.create(1000).tabulate("test.xlsx") {
            name = "Products table"
            firstRow = 1
            attributes {
                template {
                    fileName = "src/test/resources/template.xlsx"
                }
            }
            columns {
                column("nr")
                column(SampleProduct::code)
                column(SampleProduct::name)
                column(SampleProduct::description)
                column(SampleProduct::manufacturer)
                column(SampleProduct::distributionDate) {
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

        PoiTableAssert<SampleProduct>(
            tableName = "Products table",
            file = File("test.xlsx"),
            valueTests = mapOf(
                CellPosition(0, 0) to AssertCellValue(expectedValue = "Nr.:"),
                CellPosition(0, 1) to AssertCellValue(expectedValue = "Code"),
                CellPosition(0, 2) to AssertCellValue(expectedValue = "Name"),
                CellPosition(0, 3) to AssertCellValue(expectedValue = "Description"),
                CellPosition(0, 4) to AssertCellValue(expectedValue = "Manufacturer"),
                CellPosition(0, 5) to AssertCellValue(expectedValue = "Distribution")
            ),
            attributeTests = mapOf(
                CellPosition(1, 5) to AssertContainsAttributes(
                    CellExcelDataFormatAttribute("dd.mm.YYYY")
                )
            )
        ).perform().also {
            it.cleanup()
        }
    }

    @Test
    fun `should export to excel file with excel table feature`() {
        val productList = SampleProduct.create(1000)
        productList.tabulate("test.xlsx") {
            name = "Products table"
            attributes { filterAndSort {} }
            columns {
                column(SampleProduct::code)
                column(SampleProduct::name)
                column(SampleProduct::description)
                column(SampleProduct::manufacturer)
                column(SampleProduct::price)
                column(SampleProduct::distributionDate) {
                    attributes {
                        format { "dd.mm.YYYY" }
                    }
                }
            }
        }

        PoiTableAssert<SampleProduct>(
            tableName = "Products table",
            file = File("test.xlsx"),
            attributeTests = mapOf()
        ).perform().also {
            it.cleanup()
        }
    }

    @Test
    fun `should export table with cell comment`() {
        table {
            name = "Test table"
            rows {
                newRow {
                    cell {
                        value = "Has comment"
                        attributes {
                            comment {
                                author = "Voytech"
                                comment = "A Comment"
                            }
                        }
                    }
                }
            }
        }.export(File("test.xlsx"))
        PoiTableAssert<SampleProduct>(
            tableName = "Test table",
            file = File("test.xlsx"),
            valueTests = mapOf(
                CellPosition(0, 0) to AssertCellValue(
                    expectedValue = "Has comment"
                ),
            ),
            attributeTests = mapOf(
                CellPosition(0, 0) to AssertContainsAttributes(
                    CellCommentAttribute("Voytech", "A Comment")
                ),
            )
        ).perform().also {
            it.cleanup()
        }
    }

    @Test
    fun `should setup printing attributes`() {
        table {
            name = "Test table"
            attributes {
                printing {
                    blackAndWhite = true
                    firstPageNumber = 2
                    isDraft = true
                    leftToRight = true
                    landscape = true
                    printPageNumber = true
                    numberOfCopies = 10
                    firstPrintableColumn = 0
                    lastPrintableColumn = 10
                    footerCenter = "Footer Center"
                    footerLeft = "Footer Left"
                    footerRight = "Footer Right"
                    headerCenter = "Header Center"
                    headerLeft = "Header Left"
                    headerRight = "Header Right"
                }
            }
            rows {
                repeat((0..100).count()) {
                    newRow {
                        repeat((0..100).count()) { cell { value = "Value" } }
                    }
                }
            }
        }.export(File("test.xlsx"))
        PoiTableAssert<SampleProduct>(
            tableName = "Test table",
            file = File("test.xlsx"),
            attributeTests = mapOf(
                EntireTable to AssertContainsAttributes<Attribute<*>>(
                PrintingAttribute(
                    numberOfCopies = 10,
                    isDraft = true,
                    blackAndWhite = true,
                    noOrientation = false,
                    leftToRight = true,
                    printPageNumber = true,
                    firstPageNumber = 2,
                    paperSize = 1,
                    landscape = true,
                    headerMargin = 1.0,
                    footerMargin = 1.0,
                    fitHeight = 1,
                    fitWidth = 1,
                    firstPrintableColumn = 0,
                    lastPrintableColumn = 10,
                    firstPrintableRow = 0,
                    lastPrintableRow = 0,
                    footerCenter = "Footer Center",
                    footerLeft = "Footer Left",
                    footerRight = "Footer Right",
                    headerCenter = "Header Center",
                    headerLeft = "Header Left",
                    headerRight = "Header Right",
                )))
        ).perform().also {
            it.cleanup()
        }
    }

    @Test
    fun `should define borders for entire row using row attribute`() {
        table {
            firstRow = 1
            firstColumn = 1
            name = "Test table"
            rows {
                newRow {
                    repeat((0..5).count()) { cell { value = "Value" } }
                    attributes {
                        rowBorders {
                            leftBorderColor = Colors.BLACK
                            leftBorderStyle = ExcelBorderStyle.THICK

                            rightBorderColor = Colors.BLACK
                            rightBorderStyle = ExcelBorderStyle.THICK

                            topBorderColor = Colors.BLACK
                            topBorderStyle = ExcelBorderStyle.THICK

                            bottomBorderColor = Colors.BLACK
                            bottomBorderStyle = ExcelBorderStyle.THICK
                        }
                    }
                }
            }
        }.export(File("test.xlsx"))
        PoiTableAssert<SampleProduct>(
            tableName = "Test table",
            file = File("test.xlsx"),
            attributeTests = mapOf()
        ).perform().also {
            it.cleanup()
        }
    }

    @Test
    fun `should export table with custom rows and cell and row spans`() {
        table {
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

        PoiTableAssert<SampleProduct>(
            tableName = "Test table",
            file = File("test.xlsx"),
            valueTests = mapOf(
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
        table {
            name = "Test table"
            columns {
                column("description")
                column("image") {
                    attributes { width { 300.px() } }
                }
            }
            rows {
                newRow {
                    attributes { height { 200.px() } }
                    cell { value = "It is : " }
                    cell {
                        value = "src/test/resources/kotlin.jpeg"
                        typeHint { DefaultTypeHints.IMAGE_URI }
                    }
                }
            }
        }.export(File("test.xlsx"))

        PoiTableAssert<SampleProduct>(
            tableName = "Test table",
            file = File("test.xlsx"),
            valueTests = mapOf(
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
        val sharedStyleTemplate = table {
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

        SampleProduct.create(4).tabulate("test.xlsx", sharedStyleTemplate + typedTable {
            name = "Products"
            columns {
                column(SampleProduct::code)
                column(SampleProduct::name)
                column(SampleProduct::description)
                column(SampleProduct::price)
            }
            rows {
                header("Id", "Name", "Description", "Price")
                atIndex { footer() } newRow {
                    cell(SampleProduct::code) { value = "Footer first cell" }
                }
            }
        })
        val headerAttributes = AssertMany(
            AssertEqualsAttribute(
                expectedAttribute = TextStylesAttribute(
                    fontColor = Colors.WHITE,
                    weight = DefaultWeightStyle.BOLD
                ),
                onlyProperties = setOf(
                    TextStylesAttribute::fontColor,
                    TextStylesAttribute::weight
                )
            ),
            AssertEqualsAttribute(BackgroundAttribute(color = Colors.BLACK))
        )
        PoiTableAssert<SampleProduct>(
            tableName = "Products",
            file = File("test.xlsx"),
            valueTests = mapOf(
                CellPosition(0, 0) to AssertCellValue(expectedValue = "Id"),
                CellPosition(0, 1) to AssertCellValue(expectedValue = "Name"),
                CellPosition(0, 2) to AssertCellValue(expectedValue = "Description"),
                CellPosition(0, 3) to AssertCellValue(expectedValue = "Price"),
            ),
            attributeTests = mapOf(
                CellPosition(0, 0) to headerAttributes,
                CellPosition(0, 1) to headerAttributes,
                CellPosition(0, 2) to headerAttributes,
                CellPosition(0, 3) to headerAttributes,
                CellRange(1..1, 0..3) to AssertNoAttribute(BackgroundAttribute(color = Colors.GREEN)),
                CellRange(2..2, 0..3) to AssertEqualsAttribute(BackgroundAttribute(color = Colors.GREEN)),
                CellRange(3..3, 0..3) to AssertNoAttribute(BackgroundAttribute(color = Colors.GREEN)),
                CellRange(4..4, 0..3) to AssertEqualsAttribute(BackgroundAttribute(color = Colors.GREEN)),
            )
        ).perform().also {
            it.cleanup()
        }
    }

    private fun <T: Any> RowBuilderApi<T>.dollarColumn(prop: KProperty1<T, Any?>) =
        cell(prop) {
            expression = RowCellExpression {
                "${(it.record?.let { obj -> (prop.get(obj) as BigDecimal).setScale(2, RoundingMode.HALF_UP) } ?: 0)} $"
            }
        }

    @Test
    fun `should correctly export tables on separate excel sheets`() {
        val tableStyle = table {
            attributes {
                columnWidth { 110.px() }
                rowHeight { 20.px() }
                text { fontSize = 8 }
                alignment {
                    vertical = DefaultVerticalAlignment.MIDDLE
                    horizontal = DefaultHorizontalAlignment.CENTER
                }
                borders {
                    leftBorderColor = Colors.LIGHT_GRAY
                    leftBorderStyle = DefaultBorderStyle.DASHED
                    rightBorderColor = Colors.LIGHT_GRAY
                    rightBorderStyle = DefaultBorderStyle.DASHED
                    topBorderColor = Colors.LIGHT_GRAY
                    topBorderStyle = DefaultBorderStyle.DOTTED
                    bottomBorderColor = Colors.LIGHT_GRAY
                    bottomBorderStyle = DefaultBorderStyle.DOTTED
                }
            }
        }
        val headerStyle = table {
            rows {
                header {
                    attributes {
                        text {
                            fontColor = Colors.WHITE
                            weight = DefaultWeightStyle.BOLD
                        }
                        background {
                            color = Colors.BLACK
                        }
                    }
                }
            }
        }
        document {
            page {
                name = "first"
                table(tableStyle + headerStyle + typedTable<SampleProduct> {
                    columns {
                        column(SampleProduct::code) { attributes { text { fontColor = Colors.RED } } }
                        column(SampleProduct::name)
                        column(SampleProduct::description)
                        column(SampleProduct::price)
                    }
                    rows {
                        header("Id", "Name", "Description", "Price")
                        matching { gt(0) } assign { dollarColumn(SampleProduct::price) }
                        matching { odd() } assign { attributes { background { color = Colors.YELLOW } }}
                    }
                    dataSource(SampleProduct.create(25))
                })
                table<Unit> {
                    rows {
                        newRow {
                            cell { value = "This is next table below.." ; colSpan = 4 }
                        }
                        newRow {
                            cell { value = "1" }
                            cell { value = "2" }
                            cell { value = "3" }
                            cell { value = "4" }
                        }
                    }
                }
            }
            page {
                name = "second"
                table<SampleProduct>(headerStyle + typedTable {
                    attributes {
                        columnWidth { 100.px() }
                        text { fontColor = Colors.BLACK }
                    }
                    dataSource(SampleProduct.create(10))
                    columns {
                        column(SampleProduct::code)
                        column(SampleProduct::name)
                    }
                    rows { header("Id 2", "Name 2", "Description 2", "Price 2") }
                })
            }
        }.export("test.xlsx")
    }
}
