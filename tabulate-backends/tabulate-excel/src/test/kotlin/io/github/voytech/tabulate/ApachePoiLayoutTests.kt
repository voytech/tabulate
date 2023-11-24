package io.github.voytech.tabulate

import io.github.voytech.tabulate.components.container.api.builder.dsl.horizontal
import io.github.voytech.tabulate.components.container.api.builder.dsl.vertical
import io.github.voytech.tabulate.components.document.api.builder.dsl.document
import io.github.voytech.tabulate.components.document.template.export
import io.github.voytech.tabulate.components.image.api.builder.dsl.height
import io.github.voytech.tabulate.components.image.api.builder.dsl.image
import io.github.voytech.tabulate.components.image.api.builder.dsl.width
import io.github.voytech.tabulate.components.page.api.builder.dsl.page
import io.github.voytech.tabulate.components.page.model.PageExecutionContext
import io.github.voytech.tabulate.components.table.api.builder.dsl.*
import io.github.voytech.tabulate.components.table.model.attributes.cell.enums.DefaultTypeHints
import io.github.voytech.tabulate.components.text.api.builder.dsl.*
import io.github.voytech.tabulate.core.model.alignment.DefaultVerticalAlignment
import io.github.voytech.tabulate.core.model.color.Colors
import io.github.voytech.tabulate.core.model.text.DefaultWeightStyle
import io.github.voytech.tabulate.test.sampledata.SampleProduct
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File

@DisplayName("Testing layouts with excel")
class ApachePoiLayoutTests {

    @Test
    fun `should export document with tables next to each other`() {
        document {
            page {
                horizontal {
                    table {
                        dataSource(SampleProduct.create(15))
                        attributes {
                            borders { all { lightGray; 1.pt() } }
                        }
                        columns {
                            column(SampleProduct::code) {
                                attributes { text { bold } }
                            }
                            column(SampleProduct::name)
                        }
                        rows {
                            header("Id", "Name")
                        }
                    }
                    table {
                        dataSource(SampleProduct.create(10))
                        attributes {
                            borders { all { lightGray; 5.pt() } }
                        }
                        columns {
                            column(SampleProduct::code) { attributes { text { bold } } }
                            column(SampleProduct::name)
                        }
                        rows {
                            header("Id", "Name")
                        }
                    }
                }
            }
        }.export(File("two_tables.xlsx"))
    }

    @Test
    fun `should export document with different components arranged using layouts`() {
        document {
            page {
                vertical {
                    text {
                        value<PageExecutionContext> { "HEADER: the page number is : ${it.pageNumber} (should be sheet NAME)" }
                        attributes {
                            height { 50.pt() }
                            width { 600.pt() }
                            background { color = Colors.LIGHT_GRAY }
                            text {
                                courierNew; red; fontSize = 10; bold
                                fontSize = 10
                                italic = true
                                underline = true
                            }
                            alignment { middle }
                            borders {
                                all { red; dotted; 2.pt() }
                            }
                        }
                    }
                    horizontal {
                        image {
                            filePath = "src/test/resources/kotlin.jpeg"
                            attributes {
                                width { 100.pt() }
                                height { 100.pt() }
                            }
                        }
                        image {
                            filePath = "src/test/resources/kotlin.jpeg"
                            attributes {
                                width { 100.pt() }
                                height { 100.pt() }
                            }
                        }
                    }
                    horizontal {
                        table {
                            dataSource(SampleProduct.create(15))
                            attributes {
                                borders { all { lightGray; 0.5F.pt() } }
                            }
                            columns {
                                column(SampleProduct::code) {
                                    attributes { text { bold } }
                                }
                                column(SampleProduct::name)
                                column("img")
                            }
                            rows {
                                header("Id", "Name", "Image")
                                matching { gt(0) } assign {
                                    cell("img") {
                                        value =
                                            "https://cdn.pixabay.com/photo/2013/07/12/14/07/basketball-147794_960_720.png"
                                        typeHint { DefaultTypeHints.IMAGE_URI }
                                    }
                                }
                            }
                        }
                        table {
                            dataSource(SampleProduct.create(10))
                            attributes {
                                borders { all { lightGray; 5.pt() } }
                            }
                            columns {
                                column(SampleProduct::code) { attributes { text { bold } } }
                                column(SampleProduct::name)
                            }
                            rows {
                                header("Id", "Name")
                            }
                        }
                    }
                    text {
                        value<PageExecutionContext> { "FOOTER: the page number is : ${it.pageNumber}" }
                        attributes {
                            height { 50.pt() }
                            width { 200.pt() }
                            alignment { vertical = DefaultVerticalAlignment.MIDDLE }
                            background { color = Colors.BLACK }
                            text {
                                color = Colors.WHITE
                                fontSize = 10
                                weight = DefaultWeightStyle.BOLD
                            }
                        }
                    }
                }
            }
        }.export(File("test.xlsx"))
    }
}