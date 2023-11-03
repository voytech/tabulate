package io.github.voytech.tabulate

import io.github.voytech.tabulate.components.document.api.builder.dsl.document
import io.github.voytech.tabulate.components.document.template.export
import io.github.voytech.tabulate.components.image.api.builder.dsl.*
import io.github.voytech.tabulate.components.page.api.builder.dsl.PageBuilderApi
import io.github.voytech.tabulate.components.page.api.builder.dsl.page
import io.github.voytech.tabulate.components.page.model.PageExecutionContext
import io.github.voytech.tabulate.components.table.api.builder.dsl.*
import io.github.voytech.tabulate.components.table.model.RowCellExpression
import io.github.voytech.tabulate.components.table.model.attributes.cell.enums.DefaultTypeHints
import io.github.voytech.tabulate.components.table.template.AdditionalSteps
import io.github.voytech.tabulate.components.text.api.builder.dsl.*
import io.github.voytech.tabulate.components.wrapper.api.builder.dsl.align
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
class BasicExportsTests {

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
                    text { white; italic = true; bold }
                    background { black }
                    rowBorders {// TODO - simplify border API. Add builder methods like 'horizontalBorders', 'verticalBorders', 'allBorders'
                        left { red; solid; 1f.pt() }
                        top { red; solid; 1f.pt() }
                        right { red; solid; 1f.pt() }
                        bottom { red; solid; 1f.pt() }
                    }
                }
            }
        }
    }


    val textBoxStyle: TextAttributesBuilderApi.() -> Unit = {
        height { 20.pt() }
        text { courierNew }
        alignment { center; middle }
        background { lightGray; solid }
        borders {
            all { double; 3.pt(); black }
        }
    }

    val imageStyles: ImageAttributesBuilderApi.() -> Unit = {
        background { lightGray; solid }
        borders {
            all { solid; 2.pt(); lightGray }
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
                            all { 0.5.pt(); solid; lightGray }
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
                                alignment { middle }
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
                                alignment { middle }
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
                        width { 100.percents() }
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
                        all { 1.pt(); lightGray; solid }
                    }
                    text {
                        fontSize = 10
                    }
                }
                columns {
                    column(SampleProduct::code) {
                        attributes {
                            text { bold; courierNew; fontSize = 10 }
                            alignment { left; middle }
                            borders { left { 2f.pt() } }
                        }
                    }
                    column(SampleProduct::name)
                    column(SampleProduct::description)
                    column(SampleProduct::price) {
                        attributes {
                            alignment { right;middle }
                            borders { right { 10f.pt() } }
                        }
                    }
                }
                rows {
                    header("Id", "Name", "Description", "Price")
                    matching { gt(0) } assign { dollarColumn(SampleProduct::price) }
                    matching { odd() } assign { attributes { background { yellow } } }
                    //newRow(20)  { cell(SampleProduct::code) { rowSpan = 6} }
                    newRow(25) {
                        attributes {
                            borders { all { none } } //TODO BUG FIX. borders on cell level dont apply here.
                            rowBorders {
                                all { red; double }
                            }
                        }
                        cell(SampleProduct::code) {
                            colSpan = 2
                            value = "Mid row col span = 2"
                            attributes { alignment { center } }
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
                            background { red }; borders {
                            all { none }
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
                        width { 100.percents() }
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
                align { center; fullWidth } table (tableHeaderStyle + typedTable<SampleProduct> {
                    attributes {
                        margins {
                            top { 10.pt() }
                            left { 10.pt() }
                        }
                        borders {
                            all { 1.pt(); lightGray; solid }
                        }
                        text {
                            fontSize = 10
                            breakWords
                        }
                    }
                    columns {
                        column(SampleProduct::code) { attributes { width { 30.pt() }; } }
                        column(SampleProduct::name)
                        column(SampleProduct::price)
                        column(SampleProduct::description)
                    }
                    rows {
                        header {
                            columnTitles("Id", "Name", "Description", "Price")
                            attributes {
                                text { black }
                                background { white }
                            }
                        }
                        matching { gt(0) } assign { dollarColumn(SampleProduct::price) }
                        matching { odd() } assign { attributes { background { yellow } } }
                        footer {
                            cell(SampleProduct::code) { value = "." }
                            cell(SampleProduct::name) { value = "." }
                            cell(SampleProduct::description) { value = "." }
                            cell(SampleProduct::price) { value = "." }
                        }
                    }
                    dataSource(SampleProduct.create(55))
                })
            }
            page {
                align { center; fullWidth; } table (tableHeaderStyle + typedTable<SampleCustomer> {
                    attributes {
                        margins {
                            top { 10.pt() }
                            left { 10.pt() }
                        }
                        borders {
                            all { 1.pt(); lightGray; solid }
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
                            width { 100.percents() }
                            height { 10.pt() }
                        }
                    }
                }
                footer {
                    text {
                        value<PageExecutionContext> { ctx -> "Page number: ${ctx.pageNumber}" }
                        attributes {
                            textBoxStyle()
                            height { 30.pt() }
                            width { 100.percents() }
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
                                text { red; bold; courierNew }
                                alignment { left; middle }
                                borders { left { 2f.pt() } }
                            }
                        }
                        column(SampleProduct::name) {
                            attributes {
                                width { 100.pt() }
                                alignment { center }
                            }
                        }
                        column(SampleProduct::description)
                        column(SampleProduct::price) {}
                    }
                    rows {
                        header("Id", "Name", "Description", "Price")
                        matching { gt(0) } assign { dollarColumn(SampleProduct::price) }
                        matching { odd() } assign { attributes { background { yellow } } }
                        footer {
                            cell(SampleProduct::code) { value = "." }
                            cell(SampleProduct::name) { value = "." }
                            cell(SampleProduct::description) { value = "." }
                            cell(SampleProduct::price) { value = "." }
                        }
                        newRow(AdditionalSteps.TRAILING_ROWS) {
                            attributes {
                                background { red }
                                borders { all { none } }
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
                            top { 0.5f.pt(); lightGray; solid }
                        }
                        text { black; courierNew; fontSize = 7 }
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
                        borders { all { 5.pt() } }
                    }
                }
                footer {
                    text {
                        value<PageExecutionContext> { "Page: ${it.pageNumber}" }
                        attributes {
                            borders { top { solid; 0.5f.pt(); lightGray } }
                            alignment { middle; center }
                            width { 100.percents() }
                        }
                    }
                }
            }
        }.export("textBoxesAndImages.pdf")
    }

    @Test
    fun `should correctly export two pages`() {
        val imageBorders: (ImageAttributesBuilderApi.() -> Unit) = {
            borders {
                all {
                    solid; 2.pt(); black
                }
            }
        }
        val defaultMargins: (ImageAttributesBuilderApi.() -> Unit) = {
            margins {
                left { 5.pt() }
                top { 5.pt() }
            }
        }
        val uniformSize: (ImageAttributesBuilderApi.() -> Unit) = {
            width { 100.px() }
            height { 100.px() }
        }
        document {
            page {
                text {
                    value =
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore\n" +
                                "magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea\n" +
                                "commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla\n" +
                                "pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est\n" +
                                "laborum.\n"
                }
                image {
                    filePath =
                        "https://as1.ftcdn.net/v2/jpg/06/03/14/68/1000_F_603146889_Hja6dZyh2DGHT7wZp3tKxZZnoI2bR7iQ.jpg"
                    attributes {
                        uniformSize()
                        defaultMargins()
                        imageBorders()
                    }
                }
                image {
                    filePath =
                        "https://as1.ftcdn.net/v2/jpg/06/03/14/68/1000_F_603146889_Hja6dZyh2DGHT7wZp3tKxZZnoI2bR7iQ.jpg"
                    attributes {
                        uniformSize()
                        defaultMargins()
                        imageBorders()
                    }
                }
                text {
                    value =
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore\n" +
                                "magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea\n" +
                                "commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla\n" +
                                "pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est\n" +
                                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore\n" +
                                "magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea\n" +
                                "commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla\n" +
                                "pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est\n"
                }

            }
            page {
                align { fullSize; center; middle } text {
                    attributes {
                        borders { all { red; 6.pt() } }
                        alignment { justify }
                    }
                    value =
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore\n" +
                                "magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea\n" +
                                "commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla\n" +
                                "pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est\n" +
                                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore\n" +
                                "magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea\n" +
                                "commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla\n" +
                                "pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est\n" +
                                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore\n" +
                                "magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea\n" +
                                "commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla\n" +
                                "pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est\n" +
                                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore\n" +
                                "magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea\n" +
                                "commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla\n" +
                                "pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est\n"
                }

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
                            courierNew; red; fontSize = 10; italic = true; underline = true; bold; wrapText = true
                        }
                        alignment { middle }
                        borders {
                            all {
                                red; dashed; 2.pt()
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
                    attributes { borders { all { color = Colors.LIGHT_GRAY; 0.5F.pt() } } }
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
                        alignment { middle }
                        background { black }
                        text { white; fontSize = 10; bold }
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
                    attributes { borders { all { lightGray; 0.5F.pt() } } }
                    columns {
                        column(SampleProduct::code) {
                            attributes { text { bold } }
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
                        alignment { middle }
                        background { black }
                        text { white; fontSize = 10; bold }
                    }
                }
            }
        }.export(File("table_x_overflow.pdf"))
    }

}
