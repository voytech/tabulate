package io.github.voytech.tabulate

import io.github.voytech.tabulate.components.document.api.builder.dsl.document
import io.github.voytech.tabulate.components.document.template.export
import io.github.voytech.tabulate.components.container.api.builder.dsl.container
import io.github.voytech.tabulate.components.image.api.builder.dsl.*
import io.github.voytech.tabulate.components.page.api.builder.dsl.PageBuilderApi
import io.github.voytech.tabulate.components.page.api.builder.dsl.page
import io.github.voytech.tabulate.components.page.model.PageExecutionContext
import io.github.voytech.tabulate.components.table.api.builder.dsl.*
import io.github.voytech.tabulate.components.table.model.RowCellExpression
import io.github.voytech.tabulate.components.table.model.attributes.cell.enums.DefaultTypeHints
import io.github.voytech.tabulate.components.table.template.AdditionalSteps
import io.github.voytech.tabulate.components.text.api.builder.dsl.*
import io.github.voytech.tabulate.components.text.api.builder.dsl.alignment
import io.github.voytech.tabulate.components.text.api.builder.dsl.background
import io.github.voytech.tabulate.components.text.api.builder.dsl.borders
import io.github.voytech.tabulate.core.model.Orientation
import io.github.voytech.tabulate.core.model.alignment.DefaultHorizontalAlignment
import io.github.voytech.tabulate.core.model.alignment.DefaultVerticalAlignment
import io.github.voytech.tabulate.core.model.background.DefaultFillType
import io.github.voytech.tabulate.core.model.border.DefaultBorderStyle
import io.github.voytech.tabulate.core.model.color.Colors
import io.github.voytech.tabulate.core.model.text.DefaultFonts
import io.github.voytech.tabulate.core.model.text.DefaultWeightStyle
import io.github.voytech.tabulate.test.sampledata.SampleCustomer
import io.github.voytech.tabulate.test.sampledata.SampleProduct
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.reflect.KProperty1

@DisplayName("Testing various pdf exports")
class PdfBoxTabulateTests {

    private fun <T : Any> RowBuilderApi<T>.dollarColumn(prop: KProperty1<T, Any?>) =
        cell(prop) {
            expression = RowCellExpression {
                "${(it.record?.let { obj -> (prop.get(obj) as BigDecimal).setScale(2, RoundingMode.HALF_UP) } ?: 0)} $"
            }
        }

    val tableHeaderStyle = table {
        rows {
            header {
                attributes {
                    text {
                        fontColor = Colors.WHITE
                        italic = true
                        weight = DefaultWeightStyle.BOLD
                    }
                    background {
                        color = Colors.BLACK
                    }
                    rowBorders {// TODO - simplify border API. Add builder methods like 'horizontalBorders', 'verticalBorders', 'allBorders'
                        leftBorderColor = Colors.RED
                        leftBorderStyle = DefaultBorderStyle.SOLID
                        leftBorderWidth = 1f.pt()
                        rightBorderColor = Colors.RED
                        rightBorderStyle = DefaultBorderStyle.SOLID
                        rightBorderWidth = 1f.pt()
                        topBorderColor = Colors.RED
                        topBorderWidth = 1f.pt()
                        topBorderStyle = DefaultBorderStyle.SOLID
                        bottomBorderColor = Colors.RED
                        bottomBorderWidth = 1f.pt()
                        bottomBorderStyle = DefaultBorderStyle.SOLID
                    }
                }
            }
        }
    }


    val textBoxStyle: TextAttributesBuilderApi.() -> Unit = {
        height { 20.pt() }
        text {
            fontFamily = DefaultFonts.COURIER_NEW
        }
        alignment {
            horizontal = DefaultHorizontalAlignment.CENTER
            vertical = DefaultVerticalAlignment.MIDDLE
        }
        background {
            color = Colors.LIGHT_GRAY
            fill = DefaultFillType.SOLID
        }
        borders {
            all {
                style = DefaultBorderStyle.DOUBLE
                width = 3.pt()
                color = Colors.BLACK
            }
        }
    }

    val imageStyles: ImageAttributesBuilderApi.() -> Unit = {
        background {
            color = Colors.LIGHT_GRAY
            fill = DefaultFillType.SOLID
        }
        borders {
            all {
                style = DefaultBorderStyle.SOLID
                width = 2.pt()
                color = Colors.LIGHT_GRAY
            }
        }
    }

    @Test
    fun `should correctly export single table with different cell types`() {
        document {
            page {
                table<Unit> {
                    attributes {
                        margins { left { 5.pt() }; top { 5.pt() } }
                        borders {
                            all {
                                width = 0.5.pt()
                                style = DefaultBorderStyle.SOLID
                                color = Colors.LIGHT_GRAY
                            }
                        }
                    }
                    columns {
                        column(0) { attributes { width { 100.pt() } } }
                        (1..9).forEach { column(it) {} } //TODO fix this.
                        column(10) { attributes { width { 50.pt() } } }
                    }
                    rows {
                        newRow {
                            attributes {
                                height { 50.pt() }
                                alignment { vertical = DefaultVerticalAlignment.MIDDLE }
                            }
                            cell { value = BigDecimal.valueOf(10.345) }
                            cell { value = true }
                            cell { value = 1000 }
                            cell { value = "text" }
                            cell { value = 100.34F }
                            cell { value = 'c' }
                            cell { value = 34.toUByte() }
                            cell { value = (-34).toByte() }
                            cell { value = 34.toUShort() }
                            cell { value = (-34).toShort() }
                            cell {
                                value = "https://cdn.pixabay.com/photo/2013/07/12/14/07/basketball-147794_960_720.png"
                                typeHint { DefaultTypeHints.IMAGE_URI }
                            }
                        }
                        newRow {
                            attributes {
                                alignment { vertical = DefaultVerticalAlignment.MIDDLE }
                            }
                            cell {
                                value = "src/test/resources/kotlin.jpeg"
                                typeHint { DefaultTypeHints.IMAGE_URI }
                            }
                            cell { value = 'k' }
                            cell { value = 'o' }
                            cell { value = 't' }
                            cell { value = 'l' }
                            cell { value = 'i' }
                            cell { value = 'n' }
                        }
                    }
                }
            }
        }.export("table_cell_types.pdf")
    }

    @Test
    fun `should correctly export single table on multiple pages`() {
        val firstPage: (PageBuilderApi.() -> Unit) = {
            header {
                text {
                    value = "Some heading."
                    attributes {
                        textBoxStyle()
                    }
                }
            }
            table(tableHeaderStyle + typedTable<SampleProduct> {
                attributes {
                    margins {
                        top { 10.pt() }
                        left { 10.pt() }
                    }
                    borders {
                        all {
                            width = 1.pt()
                            color = Colors.LIGHT_GRAY
                            style = DefaultBorderStyle.SOLID
                        }
                    }
                    text {
                        fontSize = 10
                    }
                }
                columns {
                    column(SampleProduct::code) {
                        attributes {
                            text {
                                weight = DefaultWeightStyle.BOLD
                                fontFamily =
                                    DefaultFonts.COURIER_NEW // TODO make accessing model enumerations easier by providing them into scope of the builder.
                                fontSize = 10
                            }
                            alignment {
                                horizontal = DefaultHorizontalAlignment.LEFT
                                vertical = DefaultVerticalAlignment.MIDDLE
                            }
                            borders { leftBorderWidth = 2f.pt() }
                        }
                    }
                    column(SampleProduct::name)
                    column(SampleProduct::description)
                    column(SampleProduct::price) {
                        attributes {
                            alignment {
                                horizontal = DefaultHorizontalAlignment.RIGHT
                                vertical = DefaultVerticalAlignment.MIDDLE
                            }
                            borders { rightBorderWidth = 10f.pt() }
                        }
                    }
                }
                rows {
                    header("Id", "Name", "Description", "Price")
                    matching { gt(0) } assign { dollarColumn(SampleProduct::price) }
                    matching { odd() } assign { attributes { background { color = Colors.YELLOW } } }
                    //newRow(20)  { cell(SampleProduct::code) { rowSpan = 6} }
                    newRow(25) {
                        attributes {
                            borders {
                                all {
                                    style = DefaultBorderStyle.NONE
                                }
                            } //TODO BUG FIX. borders on cell level dont apply here.
                            rowBorders {
                                all {
                                    color = Colors.RED
                                    style = DefaultBorderStyle.DOUBLE
                                }
                            }
                        }
                        cell(SampleProduct::code) {
                            colSpan = 2
                            value = "Mid row col span = 2"
                            attributes {
                                alignment {
                                    horizontal = DefaultHorizontalAlignment.CENTER
                                }
                            }
                        }
                        cell(SampleProduct::description) { value = "Mid row Description" }
                        cell(SampleProduct::price) { }
                    }
                    footer {
                        cell(SampleProduct::code) { value = "." }
                        cell(SampleProduct::name) { value = "." }
                        cell(SampleProduct::description) { value = "." }
                        cell(SampleProduct::price) { value = "." }
                    }
                    newRow(AdditionalSteps.TRAILING_ROWS) {
                        attributes {
                            background { color = Colors.RED }; borders {
                            all {
                                style = DefaultBorderStyle.NONE
                            }
                        }
                        }
                        cell(SampleProduct::code) { value = "" }
                        cell(SampleProduct::name) { value = "" }
                        cell(SampleProduct::description) { value = "" }
                        cell(SampleProduct::price) { }
                    }
                }
                dataSource(SampleProduct.create(154))
            })
            footer {
                text {
                    value<PageExecutionContext> { ctx -> "Page number: ${ctx.pageNumber}" }
                    attributes {
                        textBoxStyle()
                        height { 30.pt() }
                    }
                }
            }
        }
        document { page(firstPage) }.export("single_table.pdf")
    }

    @Test
    fun `should correctly export two on same sheet, first above second`() {
        document {
            page {
                table(tableHeaderStyle + typedTable<SampleProduct> {
                    attributes {
                        margins {
                            top { 10.pt() }
                            left { 10.pt() }
                        }
                        borders {
                            all {
                                width = 1.pt()
                                color = Colors.LIGHT_GRAY
                                style = DefaultBorderStyle.SOLID
                            }
                        }
                        text {
                            fontSize = 10
                        }
                    }
                    columns {
                        column(SampleProduct::code)
                        column(SampleProduct::name)
                        column(SampleProduct::description)
                        column(SampleProduct::price)
                    }
                    rows {
                        header("Id", "Name", "Description", "Price")
                        matching { gt(0) } assign { dollarColumn(SampleProduct::price) }
                        matching { odd() } assign { attributes { background { color = Colors.YELLOW } } }
                        footer {
                            cell(SampleProduct::code) { value = "." }
                            cell(SampleProduct::name) { value = "." }
                            cell(SampleProduct::description) { value = "." }
                            cell(SampleProduct::price) { value = "." }
                        }
                    }
                    dataSource(SampleProduct.create(55))
                })
                table(tableHeaderStyle + typedTable<SampleCustomer> {
                    attributes {
                        margins {
                            top { 10.pt() }
                            left { 10.pt() }
                        }
                        borders {
                            all {
                                width = 1.pt()
                                color = Colors.LIGHT_GRAY
                                style = DefaultBorderStyle.SOLID
                            }
                        }
                        text {
                            fontSize = 10
                        }
                    }
                    columns {
                        column(SampleCustomer::firstName)
                        column(SampleCustomer::lastName)
                        column(SampleCustomer::country)
                        column(SampleCustomer::city)
                        column(SampleCustomer::street)
                        column(SampleCustomer::houseNumber)
                        column(SampleCustomer::flat)
                    }
                    rows {
                        header("First Name", "Last Name", "Country", "City", "Street", "House Number", "Flat")
                    }
                    dataSource(SampleCustomer.create(55))
                })
            }
        }.export("two_tables.pdf")
    }


    @Test
    fun `should correctly export overflowing table with footer and headers`() {
        document {
            page {
                header {
                    text {
                        value = "Some heading."
                        attributes {
                            textBoxStyle()
                        }
                    }
                }
                footer {
                    text {
                        value<PageExecutionContext> { ctx -> "Page number: ${ctx.pageNumber}" }
                        attributes {
                            textBoxStyle()
                            height { 30.pt() }
                        }
                    }
                }
                table(tableHeaderStyle + typedTable<SampleProduct> {
                    attributes {
                        margins {
                            top { 10.pt() }
                            left { 10.pt() }
                        }
                    }
                    columns {
                        column(SampleProduct::code) {
                            attributes {
                                text {
                                    fontColor = Colors.RED
                                    weight = DefaultWeightStyle.BOLD
                                    fontFamily = DefaultFonts.COURIER_NEW
                                }
                                alignment {
                                    horizontal = DefaultHorizontalAlignment.LEFT
                                    vertical = DefaultVerticalAlignment.MIDDLE
                                }
                                borders { leftBorderWidth = 2f.pt() }
                            }
                        }
                        column(SampleProduct::name) {
                            attributes {
                                width { 100.pt() }
                                alignment {
                                    horizontal = DefaultHorizontalAlignment.CENTER
                                }
                            }
                        }
                        column(SampleProduct::description)
                        column(SampleProduct::price) {
                            attributes {

                            }
                        }
                    }
                    rows {
                        header("Id", "Name", "Description", "Price")
                        matching { gt(0) } assign { dollarColumn(SampleProduct::price) }
                        matching { odd() } assign { attributes { background { color = Colors.YELLOW } } }
                        footer {
                            cell(SampleProduct::code) { value = "." }
                            cell(SampleProduct::name) { value = "." }
                            cell(SampleProduct::description) { value = "." }
                            cell(SampleProduct::price) { value = "." }
                        }
                        newRow(AdditionalSteps.TRAILING_ROWS) {
                            attributes {
                                background { color = Colors.RED }
                                borders {
                                    all {
                                        style = DefaultBorderStyle.NONE
                                    }
                                }
                            }
                            cell(SampleProduct::code) { value = "" }
                            cell(SampleProduct::name) { value = "" }
                            cell(SampleProduct::description) { value = "" }
                            cell(SampleProduct::price) { }
                        }
                    }
                    dataSource(SampleProduct.create(154))
                })
                table(tableHeaderStyle + typedTable<SampleProduct> {
                    attributes {
                        margins {
                            left { 15.pt() }
                            top { 25.pt() }
                        }
                        alignment {
                            horizontal = DefaultHorizontalAlignment.LEFT
                            vertical = DefaultVerticalAlignment.MIDDLE
                        }
                        borders {
                            topBorderWidth = 0.5f.pt()
                            topBorderColor = Colors.LIGHT_GRAY
                            topBorderStyle = DefaultBorderStyle.SOLID
                        }
                        text {
                            fontColor = Colors.BLACK
                            fontFamily = DefaultFonts.COURIER_NEW
                            fontSize = 7
                        }
                    }
                    columns {
                        column(SampleProduct::code) { attributes { width { 50.pt() } } }
                        column(SampleProduct::price)
                        column(SampleProduct::name)
                        column(SampleProduct::description) { attributes { width { 100.pt() } } }
                    }
                    rows {
                        header("Id", "Name", "Description", "price")
                        matching { gt(0) } assign { dollarColumn(SampleProduct::price) }
                    }
                    dataSource(SampleProduct.create(125))
                })
            }
        }.export("multiple_pages_plus_header_and_footer.pdf")
    }

    @Test
    fun `should correctly export border configurations`() {
        document {
            page {
                table {
                    attributes {
                        margins {
                            left { 5.pt() }
                            top { 5.pt() }
                        }
                        columnWidth { 80.pt() }
                        rowHeight { 20.pt() }
                    }
                    columns {
                        column(0) {}
                        column(1) {}
                        column(2) {}
                        column(3) {}
                    }
                    rows {
                        newRow {
                            attributes {
                                rowBorders {
                                    all {
                                        style = DefaultBorderStyle.DOUBLE
                                        width = 2.pt()
                                        color = Colors.LIGHT_GRAY
                                    }
                                }
                                alignment {
                                    horizontal = DefaultHorizontalAlignment.LEFT
                                }
                            }
                        }
                        newRow(2) {
                            attributes {
                                rowBorders {
                                    leftBorderStyle = DefaultBorderStyle.DOUBLE
                                    leftBorderWidth = 2.pt()
                                    leftBorderColor = Colors.LIGHT_GRAY
                                    rightBorderStyle = DefaultBorderStyle.DOUBLE
                                    rightBorderWidth = 3.pt()
                                    rightBorderColor = Colors.LIGHT_GRAY
                                    topBorderStyle = DefaultBorderStyle.DOUBLE
                                    topBorderWidth = 4.pt()
                                    topBorderColor = Colors.LIGHT_GRAY
                                    bottomBorderStyle = DefaultBorderStyle.DOUBLE
                                    bottomBorderWidth = 1.pt()
                                    bottomBorderColor = Colors.LIGHT_GRAY
                                }
                                alignment {
                                    horizontal = DefaultHorizontalAlignment.LEFT
                                }
                            }
                        }
                        newRow(4) {
                            attributes {
                                rowBorders {
                                    leftBorderStyle = DefaultBorderStyle.DOUBLE
                                    leftBorderWidth = 2.pt()
                                    leftBorderColor = Colors.LIGHT_GRAY
                                    rightBorderStyle = DefaultBorderStyle.DOUBLE
                                    rightBorderWidth = 4.pt()
                                    rightBorderColor = Colors.LIGHT_GRAY
                                    topBorderStyle = DefaultBorderStyle.DOUBLE
                                    topBorderWidth = 2.pt()
                                    topBorderColor = Colors.LIGHT_GRAY
                                    bottomBorderStyle = DefaultBorderStyle.DOUBLE
                                    bottomBorderWidth = 4.pt()
                                    bottomBorderColor = Colors.LIGHT_GRAY
                                }
                                alignment {
                                    horizontal = DefaultHorizontalAlignment.LEFT
                                }
                            }
                        }
                        newRow(6) {
                            attributes {
                                rowBorders {
                                    topBorderStyle = DefaultBorderStyle.DOUBLE
                                    topBorderWidth = 2.pt()
                                    topBorderColor = Colors.LIGHT_GRAY
                                    bottomBorderStyle = DefaultBorderStyle.DOUBLE
                                    bottomBorderWidth = 4.pt()
                                    bottomBorderColor = Colors.LIGHT_GRAY
                                }
                                alignment {
                                    horizontal = DefaultHorizontalAlignment.LEFT
                                }
                            }
                        }
                        newRow(8) {
                            attributes {
                                rowBorders {
                                    leftBorderStyle = DefaultBorderStyle.DOUBLE
                                    leftBorderWidth = 2.pt()
                                    leftBorderColor = Colors.LIGHT_GRAY
                                    rightBorderStyle = DefaultBorderStyle.DOUBLE
                                    rightBorderWidth = 4.pt()
                                    rightBorderColor = Colors.LIGHT_GRAY
                                    bottomBorderStyle = DefaultBorderStyle.SOLID
                                    bottomBorderWidth = 0.5.pt()
                                    bottomBorderColor = Colors.LIGHT_GRAY
                                }
                                alignment {
                                    horizontal = DefaultHorizontalAlignment.LEFT
                                }
                            }
                        }
                        newRow(10) {
                            attributes {
                                rowBorders {
                                    leftBorderStyle = DefaultBorderStyle.DOUBLE
                                    leftBorderWidth = 2.pt()
                                    leftBorderColor = Colors.LIGHT_GRAY
                                    rightBorderStyle = DefaultBorderStyle.DOUBLE
                                    rightBorderWidth = 4.pt()
                                    rightBorderColor = Colors.LIGHT_GRAY
                                    topBorderStyle = DefaultBorderStyle.DOUBLE
                                    topBorderWidth = 4.pt()
                                    topBorderColor = Colors.LIGHT_GRAY
                                }
                                alignment {
                                    horizontal = DefaultHorizontalAlignment.LEFT
                                }
                            }
                        }
                        newRow(12) {
                            attributes {
                                rowBorders {
                                    all {
                                        style = DefaultBorderStyle.INSET
                                        width = 2.pt()
                                        color = Colors.LIGHT_GRAY
                                    }
                                }
                                alignment {
                                    horizontal = DefaultHorizontalAlignment.LEFT
                                }
                            }
                        }
                        newRow(14) {
                            attributes {
                                rowBorders {
                                    all {
                                        style = DefaultBorderStyle.OUTSET
                                        width = 2.pt()
                                        color = Colors.LIGHT_GRAY
                                    }
                                }
                                alignment {
                                    horizontal = DefaultHorizontalAlignment.LEFT
                                }
                            }
                        }
                        newRow(16) {
                            attributes {
                                rowBorders {
                                    all {
                                        style = DefaultBorderStyle.GROOVE
                                        width = 2.pt()
                                        color = Colors.LIGHT_GRAY
                                    }
                                }
                                alignment {
                                    horizontal = DefaultHorizontalAlignment.LEFT
                                }
                            }
                        }
                        newRow(18) {
                            attributes {
                                rowBorders {
                                    leftBorderStyle = DefaultBorderStyle.GROOVE
                                    leftBorderWidth = 2.pt()
                                    leftBorderColor = Colors.LIGHT_GRAY
                                    rightBorderStyle = DefaultBorderStyle.GROOVE
                                    rightBorderWidth = 6.pt()
                                    rightBorderColor = Colors.LIGHT_GRAY
                                    topBorderStyle = DefaultBorderStyle.GROOVE
                                    topBorderWidth = 2.pt()
                                    topBorderColor = Colors.LIGHT_GRAY
                                    bottomBorderStyle = DefaultBorderStyle.GROOVE
                                    bottomBorderWidth = 6.pt()
                                    bottomBorderColor = Colors.LIGHT_GRAY
                                }
                                alignment {
                                    horizontal = DefaultHorizontalAlignment.LEFT
                                }
                            }
                        }
                        newRow(20) {
                            attributes {
                                rowBorders {
                                    all {
                                        style = DefaultBorderStyle.DOTTED
                                        width = 2.pt()
                                        color = Colors.LIGHT_GRAY
                                    }
                                }
                                alignment {
                                    horizontal = DefaultHorizontalAlignment.LEFT
                                }
                            }
                        }
                        newRow(22) {
                            attributes {
                                rowBorders {
                                    all {
                                        style = DefaultBorderStyle.DASHED
                                        width = 2.pt()
                                        color = Colors.LIGHT_GRAY
                                    }
                                }
                                alignment {
                                    horizontal = DefaultHorizontalAlignment.LEFT
                                }
                            }
                        }
                    }
                }
            }
        }.export("borders_configurations.pdf")
    }

    @Test
    fun `should correctly export document with several text lines with random attributes`() {
        document {
            page {
                text {
                    value = "Page title"
                    attributes {
                        textBoxStyle()
                        margins {
                            top { 2.pt() }
                        }
                    }
                }
                image {
                    filePath = "src/test/resources/kotlin.jpeg"
                    attributes {
                        imageStyles()
                        margins {
                            left { 10.pt() }
                            top { 10.pt() }
                        }
                    }
                }
                image {
                    filePath = "src/test/resources/kotlin.jpeg"
                    attributes {
                        width { 30.pt() }
                        height { 30.pt() }
                        borders { all { width = 5.pt() } }
                        margins {
                            left { 10.pt() }
                        }
                    }
                }
                footer {
                    text {
                        value<PageExecutionContext> { "Page: ${it.pageNumber}" }
                        attributes {
                            borders {
                                topBorderStyle = DefaultBorderStyle.DASHED
                                topBorderWidth = 0.5.pt()
                                topBorderColor = Colors.LIGHT_GRAY
                            }
                            alignment {
                                vertical = DefaultVerticalAlignment.MIDDLE
                                horizontal = DefaultHorizontalAlignment.CENTER
                            }
                        }
                    }
                }
            }
        }.export("textBoxesAndImages.pdf")
    }

    @Test
    fun `should correctly export two pages`() {
        document {
            page {
                text { value = "First page" }
                text { value = "First page" }
            }
            page {
                text { value = "Second page" }
            }
        }.export("explicitPages.pdf")
    }

    @Test
    fun `should export document with TextBox`() {
        document {
            page {
                text {
                    value<PageExecutionContext> { "HEADER: the page number is : ${it.pageNumber} (should be sheet NAME)" }
                    attributes {
                        height { 50.pt() }
                        width { 600.pt() }
                        background { color = Colors.LIGHT_GRAY }
                        text {
                            fontFamily = DefaultFonts.COURIER_NEW
                            fontColor = Colors.RED
                            fontSize = 10
                            italic = true
                            underline = true
                            weight = DefaultWeightStyle.BOLD
                            wrapText = true
                        }
                        alignment { vertical = DefaultVerticalAlignment.MIDDLE }
                        borders {
                            all {
                                color = Colors.RED
                                style = DefaultBorderStyle.DASHED
                                width = 2.pt()
                            }
                        }
                    }
                }
                image {
                    filePath = "src/test/resources/kotlin.jpeg"
                    attributes {
                        width { 100.pt() }
                        height { 100.pt() }
                    }
                }
                table {
                    dataSource(SampleProduct.create(100))
                    attributes { borders { all { color = Colors.LIGHT_GRAY; width = 0.5F.pt() } } }
                    columns {
                        column(SampleProduct::code) {
                            attributes { text { weight = DefaultWeightStyle.BOLD } }
                        }
                        column(SampleProduct::name)
                        column("img") { attributes { width { 30.pt() } } }
                    }
                    rows {
                        header("Id", "Name", "Image")
                        matching { gt(0) } assign {
                            cell("img") {
                                value = "src/test/resources/kotlin.jpeg"
                                typeHint { DefaultTypeHints.IMAGE_URI }
                            }
                            attributes { height { 30.pt() } }
                        }
                    }
                }
                text {
                    value<PageExecutionContext> { "FOOTER: the page number is : ${it.pageNumber}" }
                    attributes {
                        height { 15.pt() }
                        width { 200.pt() }
                        alignment { vertical = DefaultVerticalAlignment.MIDDLE }
                        background { color = Colors.BLACK }
                        text {
                            fontColor = Colors.WHITE
                            fontSize = 10
                            weight = DefaultWeightStyle.BOLD
                        }
                    }
                }
            }
        }.export(File("test.pdf"))
    }


    @Test
    fun `should export document with correctly handled table X overflows`() {
        document {
            page {
                table {
                    dataSource(SampleProduct.create(1))
                    attributes { borders { all { color = Colors.LIGHT_GRAY; width = 0.5F.pt() } } }
                    columns {
                        column(SampleProduct::code) {
                            attributes { text { weight = DefaultWeightStyle.BOLD } }
                        }
                        column(SampleProduct::name)
                        (0..1000).forEach { column("id$it") }
                        column("img") { attributes { width { 30.pt() } } }
                    }
                    rows {
                        matching { all() } assign {
                            (0..1000).forEach { cell("id$it") { value = "V$it" } }
                        }
                    }
                }
                text {
                    value<PageExecutionContext> { "FOOTER: the page number is : ${it.pageNumber}" }
                    attributes {
                        height { 15.pt() }
                        width { 200.pt() }
                        alignment { vertical = DefaultVerticalAlignment.MIDDLE }
                        background { color = Colors.BLACK }
                        text {
                            fontColor = Colors.WHITE
                            fontSize = 10
                            weight = DefaultWeightStyle.BOLD
                        }
                    }
                }
            }
        }.export(File("table_x_overflow.pdf"))
    }

    @Test
    fun `should export document with 'container'`() {
        document {
            page {
                container {
                    orientation = Orientation.HORIZONTAL
                    (0..10).forEach { index ->
                        text {
                            value<PageExecutionContext> { "This is ($index) text on page (${it.pageNumber})" }
                            attributes {
                                borders { all { style = DefaultBorderStyle.SOLID } }
                                margins { left { 1.pt() }; top { 1.pt() } }
                            }
                        }
                    }
                    image {
                        filePath = "src/test/resources/kotlin.jpeg"
                        attributes {
                            margins { left { 1.pt() }; top { 1.pt() } }
                            width { 50.pt() }
                            height { 50.pt() }
                            borders { all { style = DefaultBorderStyle.SOLID } }
                        }
                    }
                    table {
                        attributes {
                            margins { left { 10.pt() }; top { 10.pt() } }
                            borders {
                                bottomBorderWidth = 0.2.pt()
                                bottomBorderStyle = DefaultBorderStyle.SOLID
                            }
                        }
                        columns {
                            column(SampleCustomer::firstName)
                            column(SampleCustomer::lastName)
                            column(SampleCustomer::country)
                            column(SampleCustomer::city)
                            column(SampleCustomer::street)
                            column(SampleCustomer::houseNumber)
                            column(SampleCustomer::flat)
                        }
                        rows {
                            header {
                              columnTitles("First Name","Last Name","Country","City","Street","House Nr","Flat Nr")
                              attributes { text { weight = DefaultWeightStyle.BOLD } }
                            }

                        }
                        dataSource(SampleCustomer.create(10))
                    }
                    repeat((0..20).count()) {
                        image {
                            filePath = "src/test/resources/kotlin.jpeg"
                            attributes {
                                margins { left { 1.pt() }; top { 1.pt() } }
                                width { 50.pt() }
                                height { 50.pt() }
                                borders { all { style = DefaultBorderStyle.SOLID } }
                            }
                        }
                    }
                }
            }
        }.export(File("container.pdf"))
    }
}
