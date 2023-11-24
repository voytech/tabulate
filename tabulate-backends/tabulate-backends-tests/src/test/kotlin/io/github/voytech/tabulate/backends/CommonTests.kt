package io.github.voytech.tabulate.backends

import io.github.voytech.tabulate.components.container.api.builder.dsl.horizontal
import io.github.voytech.tabulate.components.document.api.builder.dsl.document
import io.github.voytech.tabulate.components.document.template.export
import io.github.voytech.tabulate.components.page.api.builder.dsl.page
import io.github.voytech.tabulate.components.table.api.builder.dsl.*
import io.github.voytech.tabulate.test.sampledata.SampleProduct
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File

@DisplayName("Regression testing all backends")
class CommonTests {

    @Test
    fun `should export document with tables next to each other`() {
        document {
            page {
                horizontal {
                    table {
                        dataSource(SampleProduct.create(15))
                        attributes {
                            borders { all { lightGray; 1.pt() } }
                            columnWidth { auto = true }
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
                            borders { all { black; 5.pt() } }
                            columnWidth { auto = true }
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
}