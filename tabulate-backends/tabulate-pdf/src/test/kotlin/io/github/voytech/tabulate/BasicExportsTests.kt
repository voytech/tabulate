package io.github.voytech.tabulate

import io.github.voytech.tabulate.Utils.dollarColumn
import io.github.voytech.tabulate.Utils.imageStyles
import io.github.voytech.tabulate.Utils.tableHeaderStyle
import io.github.voytech.tabulate.Utils.textBoxStyle
import io.github.voytech.tabulate.components.container.api.builder.dsl.content
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


    @Test
    fun `should correctly export two on same sheet, first above second`() {
        document {
            page {
                align { center; fullWidth; top; } table (tableHeaderStyle + typedTable<SampleProduct> {
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
                align { center; fullWidth; middle } table (tableHeaderStyle + typedTable<SampleCustomer> {
                    attributes {
                        margins {
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
                        imageStyles
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
    fun `should export document Image, Text and paged Table`() {
        document {
            page {
                text {
                    value<PageExecutionContext> { "HEADER: the page number is : ${it.pageNumber} (should be sheet NAME)" }
                    attributes {
                        height { 50.pt() }
                        width { 600.pt() }
                        background { color = Colors.LIGHT_GRAY }
                        text {
                            red; fontSize = 10;
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
                    dataSource(SampleProduct.create(40))
                    attributes {
                        borders { all { lightGray; 0.5F.pt() } }
                        clip { disabled }
                        text { breakWords }

                    }
                    columns {
                        column(SampleProduct::code) {
                            attributes { text { bold } }
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
                        clip { disabled }
                        overflow { retry }
                        text { white; bold }
                    }
                }
            }
        }.export(File("various_1.pdf"))
    }




}
