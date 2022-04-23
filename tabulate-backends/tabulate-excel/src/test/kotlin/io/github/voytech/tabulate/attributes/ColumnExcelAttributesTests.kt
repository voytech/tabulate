package io.github.voytech.tabulate.attributes

import io.github.voytech.tabulate.components.table.api.builder.dsl.table
import io.github.voytech.tabulate.components.table.model.attributes.column.columnWidth
import io.github.voytech.tabulate.components.table.model.attributes.column.width
import io.github.voytech.tabulate.components.table.template.export
import io.github.voytech.tabulate.testsupport.PoiTableAssert
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File

@DisplayName("Tests for model attributes")
class ColumnExcelAttributesTests {

    @Test
    fun `should export with column attribute`() {
        // when
        table {
            name = "test"
            attributes {
                columnWidth { px = 500 }
            }
            columns {
                column(0) {}
                column(1) {
                    attributes {
                        width {
                            px = 1000
                        }
                    }
                }
            }
            rows {
                newRow {
                    cell {
                        value = "Value 1"
                    }
                    cell {
                        value = "Value 2"
                    }
                }
            }
        }.export(File("test.xlsx"))

        PoiTableAssert<Any>(
            tableName = "test",
            file = File("test.xlsx"),
            attributeTests = mapOf()
        ).perform().also {
            it.cleanup()
        }

    }

}