package io.github.voytech.tabulate.attributes

import io.github.voytech.tabulate.components.table.api.builder.dsl.columnWidth
import io.github.voytech.tabulate.components.table.api.builder.dsl.table
import io.github.voytech.tabulate.components.table.api.builder.dsl.width
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
                columnWidth { 500.px() }
            }
            columns {
                column(0) {}
                column(1) {
                    attributes {
                        width {
                            1000.px()
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