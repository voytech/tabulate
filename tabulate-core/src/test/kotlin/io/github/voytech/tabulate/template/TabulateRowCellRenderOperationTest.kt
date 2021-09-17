package io.github.voytech.tabulate.template

import io.github.voytech.tabulate.api.builder.dsl.cell
import io.github.voytech.tabulate.api.builder.dsl.footer
import io.github.voytech.tabulate.api.builder.dsl.header
import io.github.voytech.tabulate.data.Product
import io.github.voytech.tabulate.data.Products
import io.github.voytech.tabulate.model.attributes.cell.*
import io.github.voytech.tabulate.model.attributes.cell.enums.DefaultWeightStyle
import io.github.voytech.tabulate.testsupport.AttributedCellTest
import io.github.voytech.tabulate.testsupport.TestExportOperationsFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.fail

class TabulateRowCellRenderOperationTest {

    @Test
    fun `should tabulate plain collection without additional features`() {
        TestExportOperationsFactory.test = AttributedCellTest { attributedCell ->
            Assertions.assertNotNull(attributedCell)
            when (attributedCell.rowIndex) {
                0 -> {
                    when (attributedCell.columnIndex) {
                        0 -> assertEquals(attributedCell.value.value, "code1")
                        1 -> assertEquals(attributedCell.value.value, "name1")
                        2 -> assertEquals(attributedCell.value.value, "description1")
                        3 -> assertEquals(attributedCell.value.value, "manufacturer1")
                        4 -> fail("no column definition present")
                        5 -> fail("no column definition present")
                    }
                }
            }
        }

        Products.ITEMS.tabulate(TabulationFormat("test"), Unit) {
            name = "Products table"
            columns {
                column(Product::code)
                column(Product::name)
                column(Product::description)
                column(Product::manufacturer)
            }
        }
    }

    @Test
    fun `should append header and footer rows`() {
        TestExportOperationsFactory.test = AttributedCellTest { attributedCell ->
            Assertions.assertNotNull(attributedCell)
            when (attributedCell.rowIndex) {
                0 -> {
                    when (attributedCell.columnIndex) {
                        0 -> assertEquals("Code", attributedCell.value.value)
                        1 -> assertEquals("Name", attributedCell.value.value)
                        2 -> assertEquals("Description", attributedCell.value.value)
                        3 -> assertEquals("Manufacturer", attributedCell.value.value)
                        4 -> fail("no column definition present")
                        5 -> fail("no column definition present")
                    }
                }
                1 -> {
                    when (attributedCell.columnIndex) {
                        0 -> assertEquals("code1", attributedCell.value.value)
                        1 -> assertEquals("name1", attributedCell.value.value)
                        2 -> assertEquals("description1", attributedCell.value.value)
                        3 -> assertEquals("manufacturer1", attributedCell.value.value)
                        4 -> fail("no column definition present")
                        5 -> fail("no column definition present")
                    }
                }
                3 -> {
                    when (attributedCell.columnIndex) {
                        0 -> assertEquals("First footer cell.", attributedCell.value.value)
                        1 -> fail("no column definition present")
                        2 -> fail("no column definition present")
                        3 -> fail("no column definition present")
                        4 -> fail("no column definition present")
                        5 -> fail("no column definition present")
                    }
                }
            }
        }
        Products.ITEMS.tabulate(TabulationFormat("test"), Unit) {
            name = "Products table"
            columns {
                column(Product::code)
                column(Product::name)
                column(Product::description)
                column(Product::manufacturer)
            }
            rows {
                header("Code", "Name", "Description", "Manufacturer")
                footer {
                    cell { value = "First footer cell." }
                }
            }
        }
    }

    @Test
    fun `should append trailing custom row - index mode`() {
        var additionalRowOccurs = false
        TestExportOperationsFactory.test = AttributedCellTest { attributedCell ->
            Assertions.assertNotNull(attributedCell)
            when (attributedCell.rowIndex) {
                0 -> {
                    when (attributedCell.columnIndex) {
                        0 -> assertEquals("code1", attributedCell.value.value)
                        1 -> assertEquals("name1", attributedCell.value.value)
                        2 -> assertEquals("description1", attributedCell.value.value)
                        3 -> assertEquals("manufacturer1", attributedCell.value.value)
                        4 -> fail("no column definition present")
                        5 -> fail("no column definition present")
                    }
                }
                5 -> {
                    when (attributedCell.columnIndex) {
                        0 -> {
                            assertEquals(attributedCell.value.value, "Custom row cell")
                            additionalRowOccurs = true
                        }
                    }
                }
            }
        }

        Products.ITEMS.tabulate(TabulationFormat("test"), Unit) {
            name = "Products table"
            columns {
                column(Product::code)
                column(Product::name)
                column(Product::description)
                column(Product::manufacturer)
            }
            rows {
                row(5) {
                    cell {
                        value = "Custom row cell"
                    }
                }
            }
        }

        assertTrue(additionalRowOccurs)
    }

    @Test
    fun `should correctly compute effective attributes on cell level`() {
        val occurrenceMap = mutableMapOf<String, Boolean>()
        TestExportOperationsFactory.test = AttributedCellTest { attributedCell ->
            Assertions.assertNotNull(attributedCell)
            when (attributedCell.rowIndex) {
                0 -> {
                    when (attributedCell.columnIndex) {
                        0 -> {
                            occurrenceMap["0.0"] = true
                            assertEquals("Green cell", attributedCell.value.value)
                            assertEquals(CellTextStylesAttribute(fontColor = Colors.GREEN),
                                attributedCell.attributes!!.filterIsInstance(CellTextStylesAttribute::class.java).first(),
                                "Should be green cell")
                            assertEquals(CellBackgroundAttribute(color = Colors.WHITE),
                                attributedCell.attributes!!.filterIsInstance(CellBackgroundAttribute::class.java).first(),
                                "Should have background color defined")
                        }
                        1 -> fail("no column definition present")
                    }
                }
                1 -> {
                    when (attributedCell.columnIndex) {
                        0 -> {
                            occurrenceMap["1.0"] = true
                            assertEquals("White cell", attributedCell.value.value)
                            assertEquals(CellTextStylesAttribute(fontColor = Colors.WHITE),
                                attributedCell.attributes!!.filterIsInstance(CellTextStylesAttribute::class.java).first(),
                                "Should be white cell")
                            assertEquals(CellBackgroundAttribute(color = Colors.WHITE),
                                attributedCell.attributes!!.filterIsInstance(CellBackgroundAttribute::class.java).first(),
                                "Should have background color defined")
                        }
                        1 -> fail("no column definition present")
                    }
                }
                2 -> {
                    when (attributedCell.columnIndex) {
                        0 -> {
                            occurrenceMap["2.0"] = true
                            assertEquals("Red cell", attributedCell.value.value)
                            assertEquals(CellTextStylesAttribute(fontColor = Colors.RED),
                                attributedCell.attributes!!.filterIsInstance(CellTextStylesAttribute::class.java).first(),
                                "Should be red cell")
                            assertEquals(CellBackgroundAttribute(color = Colors.WHITE),
                                attributedCell.attributes!!.filterIsInstance(CellBackgroundAttribute::class.java).first(),
                                "Should have background color defined")
                        }
                        1 -> {
                            occurrenceMap["2.1"] = true
                            assertEquals("Black cell", attributedCell.value.value)
                            assertEquals(CellTextStylesAttribute(fontColor = Colors.BLACK),
                                attributedCell.attributes!!.filterIsInstance(CellTextStylesAttribute::class.java).first(),
                                "Should be black cell")
                            assertEquals(CellBackgroundAttribute(color = Colors.WHITE),
                                attributedCell.attributes!!.filterIsInstance(CellBackgroundAttribute::class.java).first(),
                                "Should have background color defined")
                        }
                    }
                }
            }
        }

        Products.ITEMS.tabulate(TabulationFormat("test"), Unit) {
            name = "Products table"
            attributes {
                text { fontColor = Colors.BLACK }
                background { color = Colors.WHITE }
            }
            columns {
                column(Product::code) {
                    attributes {
                        text { fontColor = Colors.RED }
                    }
                }
                column(Product::name)
                column(Product::description)
                column(Product::manufacturer)
            }
            rows {
                row {
                    attributes { text { fontColor = Colors.WHITE } }
                    cell {
                        attributes { text { fontColor = Colors.GREEN } }
                        value = "Green cell"
                    }
                }
                row {
                    attributes { text { fontColor = Colors.WHITE } }
                    cell {
                        value = "White cell"
                    }
                }
                row {
                    cell {
                        value = "Red cell"
                    }
                    cell(Product::name) {
                        value = "Black cell"
                    }
                }
            }
        }
        assertTrue(occurrenceMap["0.0"] ?: false)
        assertTrue(occurrenceMap["1.0"] ?: false)
        assertTrue(occurrenceMap["2.0"] ?: false)
        assertTrue(occurrenceMap["2.1"] ?: false)
    }

    @Test
    fun `should merge complex attributes from multiple levels`() {
        val occurrenceMap = mutableMapOf<String, Boolean>()
        TestExportOperationsFactory.test = AttributedCellTest { attributedCell ->
            Assertions.assertNotNull(attributedCell)
            when (attributedCell.rowIndex) {
                0 -> {
                    when (attributedCell.columnIndex) {
                        0 -> {
                            occurrenceMap["0.0"] = true
                            assertEquals("Black, strikeout, bold, italic cell", attributedCell.value.value)
                            assertEquals(
                                CellTextStylesAttribute(
                                    fontColor = Colors.BLACK,
                                    strikeout = true,
                                    weight = DefaultWeightStyle.BOLD,
                                    italic = true
                                ),
                                attributedCell.attributes!!.filterIsInstance(CellTextStylesAttribute::class.java).first(),
                                "Should be Black, strikeout, bold, italic cell"
                            )
                        }
                        1 -> fail("no column definition present")
                    }
                }
                1 -> {
                    when (attributedCell.columnIndex) {
                        0 -> {
                            occurrenceMap["1.0"] = true
                            assertEquals("Black, strikeout, bold cell", attributedCell.value.value)
                            assertEquals(
                                CellTextStylesAttribute(
                                    fontColor = Colors.BLACK,
                                    strikeout = true,
                                    weight = DefaultWeightStyle.BOLD
                                ),
                                attributedCell.attributes!!.filterIsInstance(CellTextStylesAttribute::class.java).first(),
                                "Should be Black, strikeout, bold cell"
                            )
                        }
                        1 -> fail("no column definition present")
                    }
                }
                2 -> {
                    when (attributedCell.columnIndex) {
                        0 -> {
                            occurrenceMap["2.0"] = true
                            assertEquals("Black, strikeout cell", attributedCell.value.value)
                            assertEquals(
                                CellTextStylesAttribute(fontColor = Colors.BLACK, strikeout = true),
                                attributedCell.attributes!!.filterIsInstance(CellTextStylesAttribute::class.java).first(),
                                "Should be Black, strikeout cell"
                            )
                        }
                        1 -> {
                            occurrenceMap["2.1"] = true
                            assertEquals("Black cell", attributedCell.value.value)
                            assertEquals(CellTextStylesAttribute(fontColor = Colors.BLACK),
                                attributedCell.attributes!!.filterIsInstance(CellTextStylesAttribute::class.java).first(),
                                "Should be black cell")
                        }
                    }
                }
            }
        }

        Products.ITEMS.tabulate(TabulationFormat("test"), Unit) {
            name = "Products table"
            attributes { text { fontColor = Colors.BLACK } }
            columns {
                column(Product::code) {
                    attributes { text { strikeout = true } }
                }
                column(Product::name)
                column(Product::description)
                column(Product::manufacturer)
            }
            rows {
                row {
                    attributes { text { weight = DefaultWeightStyle.BOLD } }
                    cell {
                        attributes { text { italic = true } }
                        value = "Black, strikeout, bold, italic cell"
                    }
                }
                row {
                    attributes { text { weight = DefaultWeightStyle.BOLD } }
                    cell {
                        value = "Black, strikeout, bold cell"
                    }
                }
                row {
                    cell {
                        value = "Black, strikeout cell"
                    }
                    cell(Product::name) {
                        value = "Black cell"
                    }
                }
            }
        }
        assertTrue(occurrenceMap["0.0"] ?: false)
        assertTrue(occurrenceMap["1.0"] ?: false)
        assertTrue(occurrenceMap["2.0"] ?: false)
        assertTrue(occurrenceMap["2.1"] ?: false)
    }

    @Disabled("Can not work at the moment.")
    @Test
    fun `should append trailing custom row - predicate mode`() {
        var additionalRowOccurs = false
        TestExportOperationsFactory.test = AttributedCellTest { attributedCell ->
            Assertions.assertNotNull(attributedCell)
            when (attributedCell.rowIndex) {
                0 -> {
                    when (attributedCell.columnIndex) {
                        0 -> assertEquals("code1", attributedCell.value.value)
                        1 -> assertEquals("name1", attributedCell.value.value)
                        2 -> assertEquals("description1", attributedCell.value.value)
                        3 -> assertEquals("manufacturer1", attributedCell.value.value)
                        4 -> fail("no column definition present")
                        5 -> fail("no column definition present")
                    }
                }
                5 -> {
                    when (attributedCell.columnIndex) {
                        0 -> {
                            assertEquals(attributedCell.value.value, "Custom row cell")
                            additionalRowOccurs = true
                        }
                    }
                }
            }
        }

        Products.ITEMS.tabulate(TabulationFormat("test"), Unit) {
            name = "Products table"
            columns {
                column(Product::code)
                column(Product::name)
                column(Product::description)
                column(Product::manufacturer)
            }
            rows {
                row {
                    insertWhen {
                        it.rowIndexValue() == 5
                    }
                    cell {
                        value = "Custom row cell"
                    }
                }
            }
        }
        assertTrue(additionalRowOccurs)
    }

}