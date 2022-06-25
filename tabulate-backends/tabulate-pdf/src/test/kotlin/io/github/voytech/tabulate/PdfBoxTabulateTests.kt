package io.github.voytech.tabulate

import io.github.voytech.tabulate.components.document.api.builder.dsl.document
import io.github.voytech.tabulate.components.document.template.export
import io.github.voytech.tabulate.components.sheet.api.builder.dsl.sheet
import io.github.voytech.tabulate.components.spacing.api.builder.dsl.space
import io.github.voytech.tabulate.components.table.api.builder.dsl.header
import io.github.voytech.tabulate.components.table.api.builder.dsl.table
import io.github.voytech.tabulate.components.table.model.attributes.Colors
import io.github.voytech.tabulate.components.table.model.attributes.cell.text
import io.github.voytech.tabulate.components.table.model.attributes.column.columnWidth
import io.github.voytech.tabulate.test.sampledata.SampleProduct
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Testing various pdf exports")
class PdfBoxTabulateTests {

    @Test
    fun `should correctly export two on same sheet, one next to each others`() {
        document {
            sheet {
                space {
                    width { 25.pt() }
                }
                table<SampleProduct> {
                    attributes { columnWidth { px = 100 } }
                    columns {
                        column(SampleProduct::code) { attributes { text { fontColor = Colors.RED } }}
                        column(SampleProduct::name)
                        column(SampleProduct::description)
                        column(SampleProduct::price)
                    }
                    rows { header("Id", "Name", "Description", "Price") }
                    dataSource(SampleProduct.create(4))
                }
                space { height { 45.pt() }  }
                table<SampleProduct> {
                    attributes { columnWidth {  px = 100 } }
                    columns {
                        column(SampleProduct::code)
                        column(SampleProduct::name)
                        column(SampleProduct::description)
                        column(SampleProduct::price)
                    }
                    rows { header("Id 2", "Name 2", "Description 2", "Price 2") }
                    dataSource(SampleProduct.create(14))
                }
            }
            sheet {
                space {
                    width { 25.pt() }
                }
                table<SampleProduct> {
                    attributes { columnWidth {  px = 100 } }
                    dataSource(SampleProduct.create(10))
                    columns {
                        column(SampleProduct::code)
                        column(SampleProduct::name)
                    }
                    rows { header("Id 2", "Name 2", "Description 2", "Price 2") }
                }
            }
        }.export("test.pdf")
    }
}
