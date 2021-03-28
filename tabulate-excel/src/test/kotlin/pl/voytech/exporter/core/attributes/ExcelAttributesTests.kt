package pl.voytech.exporter.core.attributes

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import pl.voytech.exporter.core.api.builder.dsl.table
import pl.voytech.exporter.core.model.CellType
import pl.voytech.exporter.core.model.attributes.alias.CellAttribute
import pl.voytech.exporter.core.model.attributes.cell.*
import pl.voytech.exporter.core.model.attributes.cell.enums.DefaultCellFill
import pl.voytech.exporter.core.model.attributes.cell.enums.DefaultHorizontalAlignment
import pl.voytech.exporter.core.model.attributes.cell.enums.DefaultVerticalAlignment
import pl.voytech.exporter.core.model.attributes.cell.enums.DefaultWeightStyle
import pl.voytech.exporter.core.template.export
import pl.voytech.exporter.core.utils.PoiTableAssert
import pl.voytech.exporter.impl.template.excel.CellExcelDataFormatAttribute
import pl.voytech.exporter.impl.template.excel.poiExcelExport
import pl.voytech.exporter.impl.template.model.ExcelCellFills
import pl.voytech.exporter.testutils.CellPosition
import pl.voytech.exporter.testutils.cellassertions.AssertCellValue
import pl.voytech.exporter.testutils.cellassertions.AssertContainsCellAttributes
import pl.voytech.exporter.testutils.cellassertions.AssertEqualAttribute
import java.io.File
import java.io.FileOutputStream
import java.util.stream.Stream

@DisplayName("Tests for model attributes")
class ExcelAttributesTests {

    @Test
    fun `should instantiate attributes model set to defaults`() {
        PoiTableAssert<Any>(
            tableName = "test",
            file = File("src/test/resources/default.xlsx"),
            cellTests = mapOf(
                CellPosition(0, 0) to AssertContainsCellAttributes(
                    CellTextStylesAttribute(fontFamily = "Arial"),
                    CellBackgroundAttribute(),
                    CellAlignmentAttribute(),
                    CellBordersAttribute(),
                    CellExcelDataFormatAttribute(dataFormat = "General")
                )
            )
        ).perform()
    }

    @ParameterizedTest
    @MethodSource("cellAttributesProvider")
    fun `should export with cell attribute`(attribute: CellAttribute) {
        // when
        FileOutputStream(File("test1.xlsx")).use {
            table<Any> {
                name = "test"
                columns { count = 1 }
                rows {
                    row {
                        cells {
                            cell {
                                value = "Value"
                                attributes(attribute)
                            }
                        }
                    }
                }
            }.export(poiExcelExport(), it)
        }
        // then
        PoiTableAssert<Any>(
            tableName = "test",
            file = File("test1.xlsx"),
            cellTests = mapOf(
                CellPosition(0, 0) to AssertCellValue(
                    expectedType = CellType.STRING,
                    expectedValue = "Value"
                ),
                CellPosition(0, 0) to AssertEqualAttribute(attribute)
            )
        ).perform().also {
            it.cleanup()
        }
    }

    companion object {

        @JvmStatic
        fun cellAttributesProvider(): Stream<CellAttribute> {
            return (
                textStyleAttributes() +
                horizontalAlignments() +
                verticalAlignments() +
                cellBackgroundStyles()
            ).stream()
        }

        private fun textStyleAttributes(): List<CellAttribute> = listOf(
            CellTextStylesAttribute(
                fontFamily = "Times New Roman",
            ),
            CellTextStylesAttribute(
                fontFamily = "Times New Roman",
                fontSize = 12,
                italic = true,
                strikeout = true,
                underline = true,
                weight = DefaultWeightStyle.BOLD
            ),
            CellTextStylesAttribute(
                fontFamily = "Times New Roman",
                wrapText = true,
                rotation = 90,
                ident = 2
            ),
        )

        private fun horizontalAlignments(): List<CellAttribute> = DefaultHorizontalAlignment.values().map {
            CellAlignmentAttribute(horizontal = it)
        }

        private fun verticalAlignments(): List<CellAttribute> = DefaultVerticalAlignment.values().map {
            CellAlignmentAttribute(vertical = it)
        }

        private fun cellBackgroundStyles(): List<CellAttribute> = DefaultCellFill.values().map {
            CellBackgroundAttribute(
                fill = it,
                color = Colors.BLUE
            )
        } + ExcelCellFills.values().map {
            CellBackgroundAttribute(
                fill = it,
                color = Colors.GREEN
            )
        } + listOf(
            CellBackgroundAttribute(color = Colors.GREEN),
        )
    }
}