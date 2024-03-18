package io.github.voytech.tabulate

import io.github.voytech.tabulate.components.container.api.builder.dsl.*
import io.github.voytech.tabulate.components.document.api.builder.dsl.document
import io.github.voytech.tabulate.components.document.template.export
import io.github.voytech.tabulate.components.image.api.builder.dsl.*
import io.github.voytech.tabulate.components.page.api.builder.dsl.page
import io.github.voytech.tabulate.components.page.model.PageExecutionContext
import io.github.voytech.tabulate.components.table.api.builder.dsl.*
import io.github.voytech.tabulate.components.text.api.builder.dsl.*
import io.github.voytech.tabulate.components.wrapper.api.builder.dsl.align
import io.github.voytech.tabulate.components.wrapper.api.builder.dsl.height
import io.github.voytech.tabulate.core.model.border.DefaultBorderStyle
import io.github.voytech.tabulate.core.model.text.DefaultWeightStyle
import io.github.voytech.tabulate.test.sampledata.SampleCustomer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File

@DisplayName("Testing various containerisation's")
class ContainerTests {

    private fun defaultTable(data: List<SampleCustomer>): (TableBuilderApi<SampleCustomer>.() -> Unit) = typedTable {
        attributes {
            borders {
                bottom { 0.2.pt(); solid }
            }
            text { breakWords; black }
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
                columnTitles("First Name", "Last Name", "Country", "City", "Street", "House Nr", "Flat Nr")
                attributes { text { weight = DefaultWeightStyle.BOLD; black; fontSize = 12 } }
            }
        }
        dataSource(data)
    }

    @Test
    fun `should export document with 'container'`() {
        document {
            page {
                vertical {
                    section {
                        (0..10).forEach { index ->
                            text {
                                value<PageExecutionContext> { "This is ($index) text on page (${it.pageNumber})" }
                                attributes {
                                    borders { all { style = DefaultBorderStyle.SOLID } }
                                    margins { left { 1.pt() }; top { 1.pt() } }
                                }
                            }
                        }
                    }
                    vertical {
                        attributes {
                            margins {
                                left { 25.pt() }
                                top { 10.pt() }
                            }
                        }
                        table(defaultTable(SampleCustomer.create(5)))
                        table(defaultTable(SampleCustomer.create(5)))
                        table(defaultTable(SampleCustomer.create(5)))
                        table(defaultTable(SampleCustomer.create(5)))
                        table(defaultTable(SampleCustomer.create(5)))
                        table(defaultTable(SampleCustomer.create(5)))
                        table(defaultTable(SampleCustomer.create(5)))
                    }
                    section {
                        attributes { margins { top { 10.pt() } } }
                        text { value<PageExecutionContext> { "This is page ${it.pageNumber}" } }
                    }
                }

            }
        }.export(File("container_1.pdf"))
    }

    @Test
    fun `should export document with 'container' aligned horizontally in the middle`() {
        document {
            page {
                align { middle;center;fullSize } vertical {
                    attributes { borders { all { 1.pt(); dotted } } }
                    align { left;top; fullWidth; } text {
                        value = "First table"
                        attributes {
                            alignment { center; top }
                            background { black }
                            text { white }
                            borders { all { 3.pt(); solid } }
                        }
                    }
                    align { left; top;fullWidth } table (defaultTable(SampleCustomer.create(5)) + {
                        attributes {
                            margins { top { 5.pt() } }
                            tableBorders { all { 10.pt(); solid } }
                        }
                    })
                    align { right;top;fullWidth } text {
                        value = "Second table"
                        attributes {
                            alignment { center; top }
                            margins { top = 20.pt() }
                            borders { all { 1.pt(); solid } }
                            background { black }
                            text { white }
                            width { 200.pt() }
                        }
                    }
                    align { right;fullWidth } table (defaultTable(SampleCustomer.create(5)) + {
                        attributes {
                            margins { top = 5.pt() }
                            tableBorders { all { 1.pt(); dotted } }
                        }
                    })
                    align { center;fullWidth } text {
                        value = "Third table"
                        attributes {
                            alignment { center; top }
                            background { black }
                            text { white }
                            margins { top { 20.pt() } }
                            borders { all { 1.pt(); solid } }
                            width { 200.pt() }
                        }
                    }
                    align { center;fullWidth } table (defaultTable(SampleCustomer.create(8)) + {
                        attributes {
                            margins { top = 5.pt() }
                            tableBorders { all { 1.pt(); dotted } }
                        }
                    })
                }
            }
        }.export(File("container_2.pdf"))
    }

    @Test
    fun `test multiple 'text' components, width set, overflow retry, clip disabled`() {
        document {
            page {
                vertical {
                    section {
                        descendantsImmediateIterations
                        (0..40).forEach { index ->
                            text {
                                value<PageExecutionContext> { "This is ($index) text on page (${it.pageNumber})" }
                                attributes {
                                    borders { all { solid } }
                                    margins { left { 1.pt() }; top { 1.pt() } }
                                    alignment { center; middle }
                                    width { 10.percents() }
                                    clip { disabled }
                                    overflow { retry }
                                    text { breakWords } // TODO breakLines causes strange gap!!!!
                                }
                            }
                        }
                    }
                }
                footer {
                    text {
                        value<PageExecutionContext> { "[ ${it.pageNumber} ]" }
                        attributes {
                            width { 100.percents() }
                            height { 20.pt() }
                            alignment { center; middle }
                        }
                    }
                }

            }
        }.export(File("container_3.pdf"))
    }

}
