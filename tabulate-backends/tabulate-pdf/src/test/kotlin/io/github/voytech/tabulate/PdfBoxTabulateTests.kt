package io.github.voytech.tabulate

import io.github.voytech.tabulate.components.document.api.builder.dsl.document
import io.github.voytech.tabulate.components.document.template.export
import io.github.voytech.tabulate.components.sheet.api.builder.dsl.sheet
import io.github.voytech.tabulate.components.spacing.api.builder.dsl.space
import io.github.voytech.tabulate.components.table.api.builder.dsl.header
import io.github.voytech.tabulate.components.table.api.builder.dsl.table
import io.github.voytech.tabulate.components.table.model.attributes.column.columnWidth
import io.github.voytech.tabulate.components.table.model.attributes.row.height
import io.github.voytech.tabulate.test.sampledata.SampleProduct
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Testing various pdf exports")
class PdfBoxTabulateTests {

    @Test
    fun `should correctly export two on same sheet, one next to each others`() {
        document {
            sheet {
                space { widthInPoints = 10F }
                table<SampleProduct> {
                    attributes { columnWidth { px = 100 } }
                    columns {
                        column(SampleProduct::code)
                        column(SampleProduct::name)
                        column(SampleProduct::description)
                    }
                    rows {
                        header {
                            columnTitles("Id", "Name", "Description")
                            attributes { height { px = 50 } }
                        }
                    }
                    dataSource(SampleProduct.create(40))
                }
                space { widthInPoints = 100F }
                table<SampleProduct> {
                    attributes { columnWidth { px = 100 } }
                    columns {
                        column(SampleProduct::code)
                    }
                    rows { header("Id") }
                    dataSource(SampleProduct.create(10))
                }
            }
        }.export("text.pdf")
    }
}
