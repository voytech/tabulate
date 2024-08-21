package io.github.voytech.tabulate

import io.github.voytech.tabulate.Utils.sampleCustomersTable
import io.github.voytech.tabulate.components.container.api.builder.dsl.*
import io.github.voytech.tabulate.components.document.api.builder.dsl.document
import io.github.voytech.tabulate.components.document.template.export
import io.github.voytech.tabulate.components.image.api.builder.dsl.*
import io.github.voytech.tabulate.components.page.api.builder.dsl.page
import io.github.voytech.tabulate.components.page.model.PageExecutionContext
import io.github.voytech.tabulate.components.table.api.builder.dsl.*
import io.github.voytech.tabulate.components.text.api.builder.dsl.*
import io.github.voytech.tabulate.core.model.border.DefaultBorderStyle
import io.github.voytech.tabulate.test.sampledata.SampleCustomer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File

@DisplayName("Testing various containerisation's")
class ContainerTests {

    @Test
    fun `should measure,export horizontal layout with explicitly sized content`() {
        document {
            page {
                horizontal {
                    attributes { borders { all { 1.pt(); red }; } }
                    textValue { id = "text1"; attributes { width { 100.pt() };height { 25.pt() } };" Text1 " }
                    textValue { id = "text2"; attributes { width { 100.pt() };height { 25.pt() } };" Text2 " }
                    textValue { id = "text3"; attributes { width { 100.pt() };height { 25.pt() } };" Text3 " }
                }
            }
        }.export("container_1.pdf")
    }

    @Test
    fun `should export horizontal layout with explicitly sized content`() {
        document {
            page {
                horizontal {
                    textValue { id = "text1"; attributes { width { 100.pt() };height { 25.pt() } };" Text1 " }
                    textValue { id = "text2"; attributes { width { 100.pt() };height { 25.pt() } };" Text2 " }
                    textValue { id = "text3"; attributes { width { 100.pt() };height { 25.pt() } };" Text3 " }
                }
            }
        }.export("container_2.pdf")
    }

    @Test
    fun `should measure,export horizontal layout with content that needs measures`() {
        document {
            page {
                horizontal {
                    attributes { borders { all { 1.pt(); red }; } }
                    textValue { id = "text1"; " Text1 " }
                    textValue { id = "text2"; " Text2 " }
                    textValue { id = "text3"; " Text3 " }
                }
            }
        }.export("container_3.pdf")
    }

    @Test
    fun `should export horizontal layout with content that needs measures`() {
        document {
            page {
                horizontal {
                    textValue { id = "text1"; " Text1 " }
                    textValue { id = "text2"; " Text2 " }
                    textValue { id = "text3"; " Text3 " }
                }
            }
        }.export("container_4.pdf")
    }

    @Test
    fun `should export container with multiple tables`() {
        document {
            page {
                vertical {
                    attributes {
                        margins { left { 25.pt() }; top { 10.pt() } }
                    }
                    table(sampleCustomersTable(SampleCustomer.create(5)))
                    table(sampleCustomersTable(SampleCustomer.create(5)))
                    table(sampleCustomersTable(SampleCustomer.create(5)))
                    table(sampleCustomersTable(SampleCustomer.create(5)))
                    table(sampleCustomersTable(SampleCustomer.create(5)))
                    table(sampleCustomersTable(SampleCustomer.create(5)))
                    table(sampleCustomersTable(SampleCustomer.create(5)))
                }
            }
        }.export(File("container_5.pdf"))
    }

    @Test
    fun `test multiple 'text' components, overflow= retry, clip= disabled`() {
        document {
            page {
                header {
                    text {
                        id = "header"
                        value = "This is example showing multiple 'Text' components with overflow - clip"
                        attributes {
                            text { normal; calibri; fontSize = 14 }
                            alignment { middle;center }
                            width { 100.percents() }
                        }
                    }
                }
                horizontal {
                    forcePreMeasure
                    immediateIterations
                    (0..40).forEach { index ->
                        text {
                            id = "$index"
                            value<PageExecutionContext> { "This is ($index) text on page (${it.pageNumber})" }
                            attributes {
                                borders { all { solid } }
                                margins { left { 1.pt() }; top { 1.pt() } }
                                alignment { center; middle }
                                width { 10.percents() }
                                clip { disabled }
                                overflow { retry }
                                text { breakLines }
                            }
                        }
                    }
                }
                footer {
                    text {
                        id = "footer"
                        value<PageExecutionContext> { "[ ${it.pageNumber} ]" }
                        attributes {
                            width { 100.percents() }
                            height { 20.pt() }
                            alignment { center; middle }
                        }
                    }
                }
            }
        }.export(File("container_7.pdf"))
    }

    @Test
    fun `should export lots of text components horizontally using flow layout`() {
        document {
            page {
                horizontal {
                    forcePreMeasure
                    immediateIterations
                    (0..20).forEach { index ->
                        text {
                            id = "$index"
                            value<PageExecutionContext> { "This is ($index) text on page (${it.pageNumber})" }
                            attributes {
                                borders { all { solid } }
                                margins { left { 1.pt() }; top { 1.pt() } }
                                clip { disabled }
                                overflow { retry }
                            }
                        }
                    }
                }
            }
        }.export("container_8.pdf")
    }


}
