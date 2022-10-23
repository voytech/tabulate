package io.github.voytech.tabulate

import io.github.voytech.tabulate.components.document.api.builder.dsl.document
import io.github.voytech.tabulate.components.document.template.export
import io.github.voytech.tabulate.components.margins.api.builder.dsl.margins
import io.github.voytech.tabulate.components.page.api.builder.dsl.PageBuilderApi
import io.github.voytech.tabulate.components.page.api.builder.dsl.page
import io.github.voytech.tabulate.components.table.api.builder.dsl.*
import io.github.voytech.tabulate.components.table.model.RowCellExpression
import io.github.voytech.tabulate.components.table.template.AdditionalSteps
import io.github.voytech.tabulate.components.text.api.builder.dsl.height
import io.github.voytech.tabulate.components.text.api.builder.dsl.text
import io.github.voytech.tabulate.core.model.alignment.DefaultHorizontalAlignment
import io.github.voytech.tabulate.core.model.alignment.DefaultVerticalAlignment
import io.github.voytech.tabulate.core.model.border.DefaultBorderStyle
import io.github.voytech.tabulate.core.model.color.Colors
import io.github.voytech.tabulate.core.model.text.DefaultFonts
import io.github.voytech.tabulate.core.model.text.DefaultWeightStyle
import io.github.voytech.tabulate.test.sampledata.SampleProduct
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
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

    @Test
    fun `should correctly export two on same sheet, one next to each others`() {
        val tableStyle = table {
            attributes {
                columnWidth { 110.px() }
                rowHeight { 20.px() }
                text { fontSize = 8 }
                alignment {
                    vertical = DefaultVerticalAlignment.BOTTOM
                    horizontal = DefaultHorizontalAlignment.LEFT
                }
                borders {
                    leftBorderColor = Colors.LIGHT_GRAY
                    leftBorderStyle = DefaultBorderStyle.DOTTED
                    leftBorderWidth = 1f.pt()
                    rightBorderColor = Colors.LIGHT_GRAY
                    rightBorderStyle = DefaultBorderStyle.SOLID
                    rightBorderWidth = 2f.pt()
                    topBorderColor = Colors.LIGHT_GRAY
                    topBorderStyle = DefaultBorderStyle.DASHED
                    topBorderWidth = 2f.pt()
                    bottomBorderColor = Colors.LIGHT_GRAY
                    bottomBorderStyle = DefaultBorderStyle.SOLID
                    bottomBorderWidth = 2.pt()
                }
            }
            columns {
                /*   column(0) { //TODO add support when overriding builder are defining columns - to lookup this column when generated only by index.
                       attributes {
                           text { fontColor = Colors.RED }
                           alignment {
                               horizontal = DefaultHorizontalAlignment.LEFT
                               vertical = DefaultVerticalAlignment.MIDDLE
                           }
                           borders { leftBorderWidth = 10f.pt() }
                       }
                   } */
            }
        }
        val headerStyle = table {
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

        val firstPage: (PageBuilderApi.()->Unit) = {
            header {
                text {
                    value = "Some heading."
                    attributes {
                        height { 10.pt() }
                    }
                }
            }
            footer {
                text {
                    value = "Some footer."
                    attributes {
                        height { 30.pt() }
                    }
                }
            }
            table(tableStyle + headerStyle + typedTable<SampleProduct> {
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
                                fontFamily =
                                    DefaultFonts.COURIER_NEW // TODO make accessing model enumerations easier by providing them into scope of the builder.
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
                    newRow(25) {
                        attributes {
                            borders { all { style = DefaultBorderStyle.NONE } } //TODO BUG FIX. borders on cell level dont apply here.
                            rowBorders {
                                all {
                                    color = Colors.RED
                                    style = DefaultBorderStyle.DOUBLE
                                }
                            }
                        }
                        cell(SampleProduct::code) { value = "Mid row" }
                        cell(SampleProduct::name) { value = "Mid row" }
                        cell(SampleProduct::description) { value = "Mid row" }
                        cell(SampleProduct::price) {  }
                    }
                    footer {
                        cell(SampleProduct::code) { value = "." }
                        cell(SampleProduct::name) { value = "." }
                        cell(SampleProduct::description) { value = "." }
                        cell(SampleProduct::price) { value = "." }
                    }
                    newRow(AdditionalSteps.TRAILING_ROWS) {
                        attributes { background { color = Colors.RED } ; borders { all { style = DefaultBorderStyle.NONE } } }
                        cell(SampleProduct::code) { value = "" }
                        cell(SampleProduct::name) { value = "" }
                        cell(SampleProduct::description) { value = "" }
                        cell(SampleProduct::price) {  }
                    }
                }
                dataSource(SampleProduct.create(154))
            })
            table(headerStyle + typedTable<SampleProduct> {
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
                    column(SampleProduct::price)
                    column(SampleProduct::name)
                    column(SampleProduct::description) { attributes { width { 100.pt() } } }
                    column(SampleProduct::code) { attributes { width { 50.pt() } } }
                }
                rows {
                    header("Id", "Name", "Description", "price")
                    matching { gt(0) } assign { dollarColumn(SampleProduct::price) }
                }
                dataSource(SampleProduct.create(125))
            })
        }

        document {
            page(firstPage)
            page {
                name = "second"
                table {
                    attributes {
                        margins {
                            left { 10.pt() }
                            top { 10.pt() }
                        }
                        columnWidth { 50.pt() }
                        text {
                            fontSize = 4
                        }
                    }
                    dataSource(SampleProduct.create(10))
                    columns {
                        column(SampleProduct::code) { attributes { text { ident = 3 } } }
                        column(SampleProduct::name)
                        column(SampleProduct::description)
                        column(SampleProduct::price)
                    }
                    rows {
                        matching { gt(0) } assign {
                            attributes {
                                borders {
                                    bottomBorderStyle = DefaultBorderStyle.DOUBLE
                                    bottomBorderWidth = 1.pt()
                                    bottomBorderColor = Colors.LIGHT_GRAY
                                }
                            }
                        }
                        header {
                            columnTitles("Id 2", "Name 2", "Description 2", "Price 2")
                            attributes {
                                alignment {
                                    horizontal = DefaultHorizontalAlignment.LEFT
                                }
                            }
                        }
                        matching { all() } assign { dollarColumn(SampleProduct::price) }
                    }
                }
                table {
                    attributes {
                        margins {
                            left { 5.pt() }
                            top { 5.pt() }
                        }
                        columnWidth { 80.pt() }
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
        }.export("test.pdf")
    }
}
