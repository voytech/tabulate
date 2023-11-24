package io.github.voytech.tabulate.attributes

import io.github.voytech.tabulate.components.table.api.builder.dsl.table
import io.github.voytech.tabulate.components.table.rendering.CellRenderable
import io.github.voytech.tabulate.components.table.template.export
import io.github.voytech.tabulate.core.api.builder.AttributeBuilder
import io.github.voytech.tabulate.core.model.Attribute
import io.github.voytech.tabulate.core.model.alignment.DefaultHorizontalAlignment
import io.github.voytech.tabulate.core.model.alignment.DefaultVerticalAlignment
import io.github.voytech.tabulate.core.model.attributes.AlignmentAttribute
import io.github.voytech.tabulate.core.model.attributes.BackgroundAttribute
import io.github.voytech.tabulate.core.model.attributes.BordersAttribute
import io.github.voytech.tabulate.core.model.attributes.TextStylesAttribute
import io.github.voytech.tabulate.core.model.background.DefaultFillType
import io.github.voytech.tabulate.core.model.border.BorderStyle
import io.github.voytech.tabulate.core.model.border.DefaultBorderStyle
import io.github.voytech.tabulate.core.model.color.Color
import io.github.voytech.tabulate.core.model.color.Colors
import io.github.voytech.tabulate.core.model.text.DefaultFonts
import io.github.voytech.tabulate.core.model.text.DefaultWeightStyle
import io.github.voytech.tabulate.excel.components.table.model.ExcelCellFills
import io.github.voytech.tabulate.excel.components.table.model.attributes.CellExcelDataFormatAttribute
import io.github.voytech.tabulate.test.CellPosition
import io.github.voytech.tabulate.test.assertions.AssertCellValue
import io.github.voytech.tabulate.test.assertions.AssertContainsAttributes
import io.github.voytech.tabulate.test.assertions.AssertEqualsAttribute
import io.github.voytech.tabulate.testsupport.PoiTableAssert
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.util.stream.Stream

@DisplayName("Tests for model attributes")
class CellExcelAttributesTests {

    @Test
    fun `should instantiate attributes model set to defaults`() {
        PoiTableAssert<Any>(
            tableName = "test",
            file = File("src/test/resources/default.xlsx"),
            attributeTests = mapOf(
                CellPosition(0, 0) to AssertContainsAttributes(
                    TextStylesAttribute(fontFamily = DefaultFonts.ARIAL),
                    BackgroundAttribute(),
                    AlignmentAttribute(),
                    BordersAttribute(),
                    CellExcelDataFormatAttribute(dataFormat = "General")
                )
            )
        ).perform()
    }

    @ParameterizedTest
    @MethodSource("cellAttributesProvider")
    fun `should export with cell attribute`(attr: AttributeBuilder<*>, expectedAttribute: Attribute<*>) {
        // when
        table {
            name = "test"
            rows {
                newRow {
                    cells {
                        cell {
                            value = "Value"
                            attributes {
                                attribute(attr)
                            }
                        }
                    }
                }
            }
        }.export(File("test.xlsx"))

        // then
        PoiTableAssert<Any>(
            tableName = "test",
            file = File("test.xlsx"),
            valueTests = mapOf(
                CellPosition(0, 0) to AssertCellValue(
                    expectedValue = "Value"
                )
            ),
            attributeTests = mapOf(
                CellPosition(0, 0) to AssertEqualsAttribute(expectedAttribute)
            )
        ).perform().also {
            it.cleanup()
        }
    }

    @Test
    @Disabled("Needs resolver for attribute.")
    fun `should install client defined attribute`() {
        // when
        table {
            name = "test"
            rows {
                newRow {
                    cells {
                        cell {
                            value = "Value"
                            attributes {
                                simpleTestCellAttrib {
                                    valueSuffix = "AdditionalAttribute"
                                }
                            }
                        }
                    }
                }
            }
        }.export(File("test1.xlsx"))

        // then
        PoiTableAssert<Any>(
            tableName = "test",
            file = File("test1.xlsx"),
            valueTests = mapOf(
                CellPosition(0, 0) to AssertCellValue(
                    expectedValue = "Value"
                )
            ),
            attributeTests = mapOf(
                CellPosition(0, 0) to AssertEqualsAttribute(
                    SimpleTestCellAttribute(valueSuffix = "AdditionalAttribute")
                )
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
            "Times New Roman", "Times Roman" , "Helvetica", "Arial", "Courier", "Courier New" , "Arial Black", "Calibri"
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
            TextStylesAttribute.builder<CellRenderable>().apply { fontFamily = DefaultFonts.valueOf(it.replace(" ","_").uppercase()) }
        } + listOf(
            TextStylesAttribute.builder<CellRenderable>().apply {
                fontFamily = DefaultFonts.TIMES_NEW_ROMAN
                fontSize = 12
                italic = true
                strikeout = true
                underline = true
                weight = DefaultWeightStyle.BOLD
            },
            TextStylesAttribute.builder<CellRenderable>().apply {
                fontFamily = DefaultFonts.TIMES_NEW_ROMAN
                wrapText = true
                rotation = 90
                ident = 2
            }
        )).map { Arguments.of(
            it,
            TextStylesAttribute(
                fontFamily = it.fontFamily,
                fontSize = it.fontSize,
                italic = it.italic,
                strikeout = it.strikeout,
                underline = it.underline,
                weight = it.weight,
                wrapText = it.wrapText,
                rotation = it.rotation,
                ident = it.ident,
                fontColor = it.color
            )
        ) }

        private fun cellAlignmentStyles(): List<Arguments> {
            return DefaultHorizontalAlignment.values().flatMap { horizontal ->
                DefaultVerticalAlignment.values().map { vertical ->
                    Arguments.of(
                        AlignmentAttribute.builder<CellRenderable>().apply {
                            this.horizontal = horizontal
                            this.vertical = vertical
                        },
                        AlignmentAttribute(
                            horizontal = horizontal,
                            vertical = vertical
                        )
                    )
                }
            }
        }

        private fun cellBackgroundStyles(): List<Arguments> {
            return (KNOWN_COLORS + null).flatMap { color ->
                DefaultFillType.values().map { fill -> BackgroundAttribute.builder<CellRenderable>().apply {
                    this.fill = fill
                    this.color = color
                } } +
                ExcelCellFills.values().map { fill -> BackgroundAttribute.builder<CellRenderable>().apply {
                    this.fill = fill
                    this.color = color
                } } +
                BackgroundAttribute.builder<CellRenderable>().apply { this.color = color }
            }.map { Arguments.of(
                it,
                BackgroundAttribute(
                    color = it.color,
                    fill = it.fill
                )
            ) }
        }

        private fun cellBorderStyles(): List<Arguments> {
            return (KNOWN_COLORS + null).flatMap { color ->
                DefaultBorderStyle.values().map { borderStyle ->
                    BordersAttribute.builder<CellRenderable>().apply {
                        leftBorderStyle = borderStyle
                        leftBorderColor = color
                        rightBorderStyle = borderStyle
                        rightBorderColor = color
                        topBorderStyle = borderStyle
                        topBorderColor = color
                        bottomBorderStyle = borderStyle
                        bottomBorderColor = color
                    }
                }
            }.map { Arguments.of(it, expectBorderStyleAttribute(it)) }
        }

        private fun expectBorderStyleAttribute(borderStyle: BordersAttribute.Builder): BordersAttribute {
            return BordersAttribute(
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