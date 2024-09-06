package io.github.voytech.tabulate

import io.github.voytech.tabulate.Utils.dollarColumn
import io.github.voytech.tabulate.components.container.api.builder.dsl.*
import io.github.voytech.tabulate.components.document.api.builder.dsl.document
import io.github.voytech.tabulate.components.document.template.export
import io.github.voytech.tabulate.components.page.api.builder.dsl.PageBuilderApi
import io.github.voytech.tabulate.components.page.api.builder.dsl.page
import io.github.voytech.tabulate.components.page.model.PageExecutionContext
import io.github.voytech.tabulate.components.table.api.builder.dsl.*
import io.github.voytech.tabulate.components.table.model.attributes.cell.enums.DefaultTypeHints
import io.github.voytech.tabulate.components.table.template.AdditionalSteps
import io.github.voytech.tabulate.components.table.template.tabulate
import io.github.voytech.tabulate.components.text.api.builder.dsl.*
import io.github.voytech.tabulate.test.sampledata.SampleCustomer
import io.github.voytech.tabulate.test.sampledata.SampleProduct
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File
import java.math.BigDecimal

@DisplayName("Testing various table exports")
class TableTests {

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
                                value = "src/test/resources/kotlin.jpeg"
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
        }.export("table_1.pdf")
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
        }.export(File("table_2.pdf"))
    }

    @Test
    fun `should correctly export single table on multiple pages`() {
        val firstPage: (PageBuilderApi.() -> Unit) = {
            header {
                textValue {
                    attributes {
                        Utils.textBoxStyle
                        width { 100.percents() }
                    }
                    "Some heading."
                }
            }
            table(Utils.tableHeaderStyle + typedTable<SampleProduct> {
                attributes {
                    margins { top { 10.pt() }; left { 10.pt() } }
                    borders { all { 1.pt(); lightGray; solid } }
                    text { fontSize = 10 }
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
                            rowBorders { all { red; double } }
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
            footer {
                text {
                    value<PageExecutionContext> { ctx -> "Page number: ${ctx.pageNumber}" }
                    attributes {
                        Utils.textBoxStyle
                        width { 100.percents() }
                        height { 30.pt() }
                    }
                }
            }
        }
        document {
            page(firstPage)
        }.export("table_3.pdf")
    }

    @Test
    fun `should tabulate collection into pdf`() {
        SampleProduct.create(14).tabulate("table_4.pdf") {
            attributes {
                borders { all { 0.5.pt();solid;lightGray } }
                tableBorders { all { 1.pt();solid;red } }
            }
            columns {
                column(SampleProduct::code) { attributes { width { 100.pt() } } }
                column(SampleProduct::name) { attributes { width { 100.pt() } } }
                column(SampleProduct::description) { attributes { width { 100.pt() } } }
                column(SampleProduct::price) { attributes { width { 100.pt() } } }
            }
            rows {
                matching { gt(0) } assign { dollarColumn(SampleProduct::price) }
            }
        }
    }

    private fun TableBuilderApi<*>.tableNumberHeaderRow(seq: Int) {
        rows {
            newRow {
                cell(0) {
                    value = "$seq"
                    colSpan = 2
                    attributes { alignment { center }; text { bold } }
                }
            }
        }
    }

    private fun ContainerBuilderApi.containerAttributes() {
        attributes {
            height { 36.percents() }
            margins { all { 5.pt() } }
            borders { all { solid; green; 6.pt() } }
        }
    }

    @Test
    fun `should export multiple tables vertically with overflow handling`() {
        document {
            page {
                vertical {
                    immediateIterations
                    //forcePreMeasure
                    containerAttributes()
                    repeat((1..19).count()) {
                        table(
                            Utils.sampleCustomersTable(
                                SampleCustomer.create(5),
                                SampleCustomer::firstName,
                                SampleCustomer::lastName
                            ) + typedTable<SampleCustomer> {
                                columns {
                                    //column(SampleCustomer::firstName) { attributes { width { 50.pt() } }}
                                    //column(SampleCustomer::lastName) { attributes { width { 50.pt() } }}
                                }
                                name = "$it";attributes { clip { disabled } }
                                tableNumberHeaderRow(it)
                            }
                        )
                    }
                }
            }
        }.export(File("table_5.pdf"))
    }

    @Test
    fun `should export tables with  column0 width=15,clip=disabled`() {
        document {
            page {
                vertical {
                    immediateIterations
                    //forcePreMeasure
                    containerAttributes()
                    repeat((1..19).count()) {
                        table(
                            Utils.sampleCustomersTable(
                                SampleCustomer.create(5),
                                SampleCustomer::firstName,
                                SampleCustomer::lastName
                            ) + typedTable<SampleCustomer> {
                                columns {
                                    column(SampleCustomer::firstName) { attributes { width { 15.pt() } }}
                                }
                                name = "$it";attributes { clip { disabled } }
                                tableNumberHeaderRow(it)
                            }
                        )
                    }
                }
            }
        }.export(File("table_6.pdf"))
    }

    @Test
    fun `should export tables with  column0Width=50,column1Width=150,clip=disabled`() {
        document {
            page {
                vertical {
                    immediateIterations
                    containerAttributes()
                    repeat((1..19).count()) {
                        table(
                            Utils.sampleCustomersTable(
                                SampleCustomer.create(5),
                                SampleCustomer::firstName,
                                SampleCustomer::lastName
                            ) + typedTable<SampleCustomer> {
                                columns {
                                    column(SampleCustomer::firstName) { attributes { width { 50.pt() } }}
                                    column(SampleCustomer::lastName) { attributes { width { 150.pt()  } }}
                                }
                                name = "$it";attributes { clip { disabled } }
                                tableNumberHeaderRow(it)
                            }
                        )
                    }
                }
            }
        }.export(File("table_7.pdf"))
    }

    @Test
    fun `should export tables with row0Height=50,column1Width=150,clip=disabled`() {
        document {
            page {
                vertical {
                    immediateIterations
                    containerAttributes()
                    repeat((1..19).count()) {
                        table(
                            Utils.sampleCustomersTable(
                                SampleCustomer.create(5),
                                SampleCustomer::firstName,
                                SampleCustomer::lastName
                            ) + typedTable<SampleCustomer> {
                                columns {
                                    column(SampleCustomer::lastName) { attributes { width { 150.pt()  } }}
                                }
                                name = "$it";attributes { clip { disabled } }
                                tableNumberHeaderRow(it)
                                rows {
                                    matching { eq(0) } assign {
                                        attributes { height { 50.pt() }  }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }.export(File("table_8.pdf"))
    }

    @Test
    fun `should export tables with row0Height=50,column1Width=150,clip=enabled`() {
        document {
            page {
                vertical {
                    immediateIterations
                    containerAttributes()
                    repeat((1..19).count()) {
                        table(
                            Utils.sampleCustomersTable(
                                SampleCustomer.create(5),
                                SampleCustomer::firstName,
                                SampleCustomer::lastName
                            ) + typedTable<SampleCustomer> {
                                name = "$it";
                                attributes {
                                    clip { enabled }
                                    tableBorders { all { 1.pt();solid;black;  } }
                                }
                                columns {
                                    column(SampleCustomer::lastName) { attributes { width { 150.pt()  } }}
                                }
                                tableNumberHeaderRow(it)
                                rows {
                                    matching { eq(0) } assign {
                                        attributes { height { 50.pt() }  }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }.export(File("table_9.pdf"))
    }
}