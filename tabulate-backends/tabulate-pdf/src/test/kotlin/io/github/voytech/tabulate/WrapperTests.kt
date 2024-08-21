package io.github.voytech.tabulate

import io.github.voytech.tabulate.Utils.sampleCustomersTable
import io.github.voytech.tabulate.components.container.api.builder.dsl.*
import io.github.voytech.tabulate.components.document.api.builder.dsl.document
import io.github.voytech.tabulate.components.document.template.export
import io.github.voytech.tabulate.components.image.api.builder.dsl.*
import io.github.voytech.tabulate.components.page.api.builder.dsl.page
import io.github.voytech.tabulate.components.table.api.builder.dsl.*
import io.github.voytech.tabulate.components.text.api.builder.dsl.*
import io.github.voytech.tabulate.components.wrapper.api.builder.dsl.align
import io.github.voytech.tabulate.test.sampledata.SampleCustomer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File

@DisplayName("Wrapper component tests.")
class WrapperTests {

    @Test
    fun `should export side panel section and main section with contents aligned middle, center`() {
        document {
            page {
                horizontal {
                    textValue { " TOP " }
                    textValue { " TOP " }
                    textValue { " TOP " }
                }
                align { center; top; fullWidth; fullHeight } horizontal {
                    // left side panel
                    align { middle; center; width25; fullHeight } content  {
                        textValue { " LEFT " }
                        textValue { " LEFT " }
                        textValue { " LEFT " }
                    }
                    // center
                    align { middle; center; width50; fullHeight } vertical  {
                        textValue { " CENTER " }
                        textValue { " CENTER " }
                        textValue { " CENTER " }
                    }
                    align { middle; center; width25; fullHeight } content {
                        attributes { borders { all { 1.pt(); } } }
                        textValue { " RIGHT " }
                        textValue { " RIGHT " }
                        textValue { " RIGHT " }
                    }
                }
            }
        }.export(File("wrapper_1.pdf"))
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
                    align { left; top;fullWidth } table (sampleCustomersTable(SampleCustomer.create(5)) + {
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
                    align { right;fullWidth } table (sampleCustomersTable(SampleCustomer.create(5)) + {
                        attributes {
                            margins { top = 5.pt() }
                            tableBorders { all { 1.pt(); dotted } }
                        }
                    })
                    align { center;fullWidth } textValue  {
                        attributes {
                            alignment { center; top }
                            background { black }
                            text { white }
                            margins { top { 20.pt() } }
                            borders { all { 1.pt(); solid } }
                            width { 200.pt() }
                        }
                        "Third table"
                    }
                    align { center;fullWidth } table (sampleCustomersTable(SampleCustomer.create(8)) + {
                        attributes {
                            margins { top = 5.pt() }
                            tableBorders { all { 1.pt(); dotted } }
                        }
                    })
                }
            }
        }.export(File("wrapper_2.pdf"))
    }

}
