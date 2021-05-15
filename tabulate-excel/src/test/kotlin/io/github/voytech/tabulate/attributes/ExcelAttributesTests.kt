package io.github.voytech.tabulate.attributes

import io.github.voytech.tabulate.api.builder.dsl.table
import io.github.voytech.tabulate.excel.model.ExcelCellFills
import io.github.voytech.tabulate.excel.model.attributes.CellExcelDataFormatAttribute
import io.github.voytech.tabulate.model.CellType
import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.cell.*
import io.github.voytech.tabulate.model.attributes.cell.enums.*
import io.github.voytech.tabulate.model.attributes.cell.enums.contract.BorderStyle
import io.github.voytech.tabulate.template.export
import io.github.voytech.tabulate.testsupport.CellPosition
import io.github.voytech.tabulate.testsupport.PoiTableAssert
import io.github.voytech.tabulate.testsupport.cellassertions.AssertCellValue
import io.github.voytech.tabulate.testsupport.cellassertions.AssertContainsCellAttributes
import io.github.voytech.tabulate.testsupport.cellassertions.AssertEqualAttribute
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
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
    fun `should export with cell attribute`(attribute: CellAttribute, expectedAttribute: CellAttribute) {
        // when
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
        }.export(File("test1.xlsx"))

        // then
        PoiTableAssert<Any>(
            tableName = "test",
            file = File("test1.xlsx"),
            cellTests = mapOf(
                CellPosition(0, 0) to AssertCellValue(
                    expectedType = CellType.STRING,
                    expectedValue = "Value"
                ),
                CellPosition(0, 0) to AssertEqualAttribute(expectedAttribute)
            )
        ).perform().also {
            it.cleanup()
        }
    }

    @Test
    @Disabled("Needs resolver for attribute.")
    fun `should install client defined attribute`() {
        // when
        table<Any> {
            name = "test"
            columns { count = 1 }
            rows {
                row {
                    cells {
                        cell {
                            value = "Value"
                            attributes(SimpleTestCellAttribute(valueSuffix = "AdditionalAttribute"))
                        }
                    }
                }
            }
        }.export(File("test1.xlsx"))

        // then
        PoiTableAssert<Any>(
            tableName = "test",
            file = File("test1.xlsx"),
            cellTests = mapOf(
                CellPosition(0, 0) to AssertCellValue(
                    expectedType = CellType.STRING,
                    expectedValue = "Value"
                ),
                CellPosition(0, 0) to AssertEqualAttribute(SimpleTestCellAttribute(valueSuffix = "AdditionalAttribute"))
            )
        ).perform().also {
            it.cleanup()
        }
    }

    @Test
    fun `should correctly cache cell styles from attributes`() {
        // TODO write this test.
    }

    companion object {

        private val KNOWN_COLORS = listOf(
            Colors.AERO, Colors.AERO_BLUE, Colors.AMARANTH,
            Colors.AMBER, Colors.BLACK, Colors.WHITE,
            Colors.BLACK, Colors.RED, Colors.BLUE,
            Colors.GREEN, Colors.YELLOW, Colors.ORANGE,
            Colors.PINK
        )

        private val KNOWN_FONT_FAMILIES = listOf(
            "Times New Roman", "Times" , "Helvetica", "Arial", "Courier", "Courier New" ,
            "Verdana", "Georgia", "Comic Sans MS", "Trebuchet MS", "Arial Black" , "Tahoma",
            "Bodoni", "Futura", "Frutiger", "Garamond", "Avenir", "Impact", "Palatino",
            "Garamond", "Bookman", "Avant Garde", "Century Schoolbook", "Andale Mono", "Calibri"
        )

        @JvmStatic
        fun cellAttributesProvider(): Stream<Arguments> {
            return (
                textStyleAttributes() +
                cellAlignmentStyles() +
                cellBackgroundStyles() +
                cellBorderStyles()
            ).stream()
        }

        private fun textStyleAttributes(): List<Arguments> = (KNOWN_FONT_FAMILIES.map {
            CellTextStylesAttribute(fontFamily = it)
        } + listOf(
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
            )
        )).map { Arguments.of(it, it) }

        private fun cellAlignmentStyles(): List<Arguments> {
            return DefaultHorizontalAlignment.values().flatMap { horizontal ->
                DefaultVerticalAlignment.values().map { vertical ->
                    Arguments.of(
                        CellAlignmentAttribute(horizontal = horizontal, vertical = vertical),
                        CellAlignmentAttribute(horizontal = horizontal, vertical = vertical)
                    )
                }
            }
        }

        private fun cellBackgroundStyles(): List<Arguments> {
            return (KNOWN_COLORS + null).flatMap { color ->
                DefaultCellFill.values().map { fill -> CellBackgroundAttribute(fill = fill, color = color) } +
                ExcelCellFills.values().map { fill -> CellBackgroundAttribute(fill = fill, color = color) } +
                CellBackgroundAttribute(color = color)
            }.map { Arguments.of(it,it) }
        }

        private fun cellBorderStyles(): List<Arguments> {
            return (KNOWN_COLORS + null).flatMap { color ->
                DefaultBorderStyle.values().map { borderStyle ->
                    CellBordersAttribute(
                        leftBorderStyle = borderStyle,
                        leftBorderColor = color,
                        rightBorderStyle = borderStyle,
                        rightBorderColor = color,
                        topBorderStyle = borderStyle,
                        topBorderColor = color,
                        bottomBorderStyle = borderStyle,
                        bottomBorderColor = color
                    )
                }
            }.map { Arguments.of(it, expectBorderStyleAttribute(it)) }
        }

        private fun expectBorderStyleAttribute(borderStyle: CellBordersAttribute): CellBordersAttribute {
            return borderStyle.copy(
                leftBorderStyle = expectBorderStyle(borderStyle.leftBorderStyle),
                rightBorderStyle = expectBorderStyle(borderStyle.rightBorderStyle),
                topBorderStyle = expectBorderStyle(borderStyle.topBorderStyle),
                bottomBorderStyle = expectBorderStyle(borderStyle.bottomBorderStyle),
                leftBorderColor = expectBorderColor(borderStyle.leftBorderStyle, borderStyle.leftBorderColor),
                rightBorderColor = expectBorderColor(borderStyle.rightBorderStyle, borderStyle.rightBorderColor),
                topBorderColor = expectBorderColor(borderStyle.topBorderStyle, borderStyle.topBorderColor),
                bottomBorderColor = expectBorderColor(borderStyle.bottomBorderStyle, borderStyle.bottomBorderColor)
            )
        }

        private fun expectBorderStyle(borderStyle: BorderStyle?): BorderStyle {
            return when(borderStyle) {
                DefaultBorderStyle.INSET -> DefaultBorderStyle.NONE
                DefaultBorderStyle.GROOVE -> DefaultBorderStyle.NONE
                DefaultBorderStyle.OUTSET -> DefaultBorderStyle.NONE
                else -> borderStyle ?: DefaultBorderStyle.NONE
            }
        }

        private fun expectBorderColor(borderStyle: BorderStyle?, borderColor: Color?): Color? {
            return when(borderStyle) {
                DefaultBorderStyle.INSET -> null
                DefaultBorderStyle.GROOVE -> null
                DefaultBorderStyle.OUTSET -> null
                DefaultBorderStyle.NONE -> null
                else -> borderColor
            }
        }
    }
}