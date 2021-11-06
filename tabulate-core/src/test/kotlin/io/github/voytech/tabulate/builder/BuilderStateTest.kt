package io.github.voytech.tabulate.builder

import io.github.voytech.tabulate.api.builder.*
import io.github.voytech.tabulate.api.builder.exception.BuilderException
import io.github.voytech.tabulate.model.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.logging.Logger
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BuilderStateTest {

    data class ExportedData(
        val string: String,
        val int: Int,
        val bigDecimal: BigDecimal,
        val boolean: Boolean,
        val long: Long,
        val double: Double,
        val float: Float,
        val localDate: LocalDate,
        val localDateTime: LocalDateTime,
    )

    @Test
    fun `should define columns`() {
        val builder = TableBuilderState<ExportedData>().apply {
            columnsBuilderState.ensureColumnBuilder("customCol1") {}
            columnsBuilderState.ensureColumnBuilder(ExportedData::string.id()) {}
        }
        assertEquals(2, builder.columnsBuilderState.columnBuilderStates.size)

        with(builder.columnsBuilderState.find(ColumnKey("customCol1"))) {
            assertEquals("customCol1", this?.id?.name)
            assertEquals(0, this?.index)
        }

        with(builder.columnsBuilderState.find(ColumnKey(property = ExportedData::string.id()))) {
            assertEquals(ExportedData::string.id(), this?.id?.property)
            assertEquals(1, this?.index)
        }

        builder.columnsBuilderState.ensureColumnBuilder("customCol1") {}

        assertEquals(2, builder.columnsBuilderState.columnBuilderStates.size)

        with(builder.columnsBuilderState.find(ColumnKey("customCol1"))) {
            assertEquals("customCol1", this?.id?.name)
            assertEquals(0, this?.index)
        }

        val exception = assertThrows<BuilderException> {
            builder.columnsBuilderState.ensureColumnBuilder("customCol1") {
                it.index = 1
            }
        }
        assertEquals("Could not set column index 1 because index is in use by another column.", exception.message)

        builder.columnsBuilderState.ensureColumnBuilder("customCol1") {
            it.index = 2
        }

        assertEquals(2, builder.columnsBuilderState.columnBuilderStates.size)
        with(builder.columnsBuilderState.find(ColumnKey("customCol1"))) {
            assertEquals("customCol1", this?.id?.name)
            assertEquals(2, this?.index)
        }

        builder.columnsBuilderState.ensureColumnBuilder("customCol2") {
            it.index = 4
        }
        assertEquals(3, builder.columnsBuilderState.columnBuilderStates.size)

        with(builder.columnsBuilderState.find(ColumnKey("customCol2"))) {
            assertEquals(4, this?.index)
        }

        builder.columnsBuilderState.ensureColumnBuilder("customCol3") {}
        assertEquals(4, builder.columnsBuilderState.columnBuilderStates.size)
        with(builder.columnsBuilderState.find(ColumnKey("customCol3"))) {
            assertEquals(5, this?.index)
        }
    }

    private fun `create TableBuilderState with implicit columns`(): TableBuilderState<ExportedData> =
        step("Create TableBuilderState with implicit columns") {
            TableBuilderState<ExportedData>().apply {
                rowsBuilderState.addRowBuilder { row ->
                    row.cellsBuilderState.addCellBuilder { cell ->
                        cell.value = "Cell 0"
                        cell.rowSpan = 2
                    }
                    row.cellsBuilderState.addCellBuilder { cell ->
                        cell.value = "Cell 1"
                        cell.rowSpan = 2
                    }
                }
            }
        }

    @Test
    fun `should correctly validate against existing column definition`()  {
        `create TableBuilderState with implicit columns`().also { builder ->
            step("Check if validates index") {
                assertNotNull(builder.columnsBuilderState.ensureColumnBuilder(0) {
                    it.index = 0
                })
                val ex = assertThrows<BuilderException> {
                    builder.columnsBuilderState.ensureColumnBuilder(0) {
                        it.index = 1
                    }
                }
                assertEquals("Could not set column index 1 because index is in use by another column.", ex.message)
            }
            step("Check if validates key") {
                assertNotNull(builder.columnsBuilderState.ensureColumnBuilder(0) {
                    it.id = ColumnKey("column-0")
                })
                val ex = assertThrows<BuilderException> {
                    builder.columnsBuilderState.ensureColumnBuilder(0) {
                        it.id = ColumnKey("column-1")
                    }
                }
                assertEquals("Could not set column id ColumnKey = column-1 because this id is in use by another column.", ex.message)
            }
        }
    }

    @Test
    fun `should redefine column`() {
        val builder = `create TableBuilderState with implicit columns`()
        step("Check column default keys and indices") {
            with(builder.columnsBuilderState.find(ColumnKey("column-0"))) {
                assertEquals(0, this?.index)
            }
            with(builder.columnsBuilderState.find(ColumnKey("column-1"))) {
                assertEquals(1, this?.index)
            }
        }
        step("Check if cells can be addressed by default indices") {
            with(builder.rowsBuilderState.rowBuilderStates.first()) {
                assertEquals("Cell 0", cells[builder.columnsBuilderState.find(0)!!.id]!!.value)
                assertEquals("Cell 1", cells[builder.columnsBuilderState.find(1)!!.id]!!.value)
            }
        }
        step("Change column-0 index") {
            builder.columnsBuilderState.ensureColumnBuilder("column-0") {
                it.index = 2
            }
        }
        step("Check that there is no column at old index") {
            assertNull(builder.columnsBuilderState.find(0))
        }
        step("Check that it is possible to find cells using new index") {
            with(builder.rowsBuilderState.rowBuilderStates.first()) {
                assertEquals("Cell 0", cells[builder.columnsBuilderState.find(2)!!.id]!!.value)
                assertEquals("Cell 1", cells[builder.columnsBuilderState.find(1)!!.id]!!.value)
            }
        }
        step("Change id of column at specified index") {
            assertEquals(
                builder.columnsBuilderState.find(2),
                builder.columnsBuilderState.ensureColumnBuilder(2)
            )
            builder.columnsBuilderState.ensureColumnBuilder(2) {
                it.id = ColumnKey.field(ExportedData::string)
            }
        }
        step("Access column by new id") {
            assertEquals(2, builder.columnsBuilderState.find(ColumnKey(property = ExportedData::string.id()))!!.index)
        }
    }

    private fun <T> ColumnsBuilderState<T>.find(key: ColumnKey<T>): ColumnBuilderState<T>? =
        columnBuilderStates.find { it.id == key }

    private fun <T> ColumnsBuilderState<T>.find(index: Int): ColumnBuilderState<T>? =
        columnBuilderStates.find { it.index == index }

    @Test
    fun `should create subsequent rows`() {
        val builder = step("Create TableBuilderState with rows") {
            TableBuilderState<ExportedData>().apply {
                rowsBuilderState.addRowBuilder()
                rowsBuilderState.addRowBuilder()
            }
        }
        step("Check if rows are created at specified index") {
            assertEquals(
                RowIndexPredicateLiteral<ExportedData>(eq(0)),
                builder.rowsBuilderState.findRowBuilder(RowIndexPredicateLiteral(eq(0)))?.qualifier?.index
            )

            assertEquals(
                RowIndexPredicateLiteral<ExportedData>(eq(1)),
                builder.rowsBuilderState.findRowBuilder(RowIndexPredicateLiteral(eq(1)))?.qualifier?.index
            )
        }
    }

    @Test
    fun `should create subsequent rows using complex, predicate based indexing`() {
        step("Single index predicate literal") {
            val builder = TableBuilderState<ExportedData>().apply {
                rowsBuilderState.addRowBuilder(RowIndexPredicateLiteral(lte(5)))
                rowsBuilderState.addRowBuilder()
            }
            assertEquals(2, builder.rowsBuilderState.rowBuilderStates.size)
            assertEquals(
                RowIndexPredicateLiteral<ExportedData>(lte(5)),
                builder.rowsBuilderState.findRowBuilder(RowIndexPredicateLiteral(lte(5)))?.qualifier?.index
            )
            assertEquals(
                RowIndexPredicateLiteral<ExportedData>(eq(6)),
                builder.rowsBuilderState.findRowBuilder(RowIndexPredicateLiteral(eq(6)))?.qualifier?.index
            )
        }

        step("Composite index predicate literal") {
            val builder = TableBuilderState<ExportedData>().apply {
                rowsBuilderState.addRowBuilder(RowIndexPredicateLiteral(
                    lte(5) or (gte(10) and lte(15)))
                )
                rowsBuilderState.addRowBuilder()
            }
            assertEquals(2, builder.rowsBuilderState.rowBuilderStates.size)
            assertEquals(
                RowIndexPredicateLiteral<ExportedData>(eq(16)),
                builder.rowsBuilderState.findRowBuilder(RowIndexPredicateLiteral(eq(16)))?.qualifier?.index
            )
        }
    }

    @Test
    fun `should alter existing row addressed by index predicate literal`() {
        TableBuilderState<ExportedData>().apply {
            rowsBuilderState.addRowBuilder(RowIndexPredicateLiteral(lte(5)))
            rowsBuilderState.addRowBuilder()
        }.also { builder ->
            assertEquals(2, builder.rowsBuilderState.rowBuilderStates.size)
        }.also { builder ->
            builder.rowsBuilderState.addRowBuilder(RowIndexPredicateLiteral(lte(5)))
        }.also { builder ->
            assertEquals(2, builder.rowsBuilderState.rowBuilderStates.size)
            assertEquals(
                RowIndexPredicateLiteral<ExportedData>(lte(5)),
                builder.rowsBuilderState.findRowBuilder(RowIndexPredicateLiteral(lte(5)))?.qualifier?.index
            )
        }
    }

    @Test
    fun `should validate against upstream row spans`() {
        TableBuilderState<ExportedData>().apply {
            rowsBuilderState.addRowBuilder(
                RowIndexPredicateLiteral(eq(8))
            ) { row ->
                row.cellsBuilderState.addCellBuilder { cell ->
                    cell.rowSpan = 2
                }
                assertEquals(1, row.cells.size)
                assertNotNull(row.cells[ColumnKey("column-0")])
            }
            rowsBuilderState.addRowBuilder(RowIndexPredicateLiteral(eq(9))) { row_2 ->
                row_2.cellsBuilderState.addCellBuilder {}
                assertEquals(1, row_2.cells.size)
                assertNotNull(row_2.cells[ColumnKey("column-1")])
            }

            rowsBuilderState.addRowBuilder(RowIndexPredicateLiteral(eq(9))) { row_2 ->
                row_2.cellsBuilderState.addCellBuilder {}
                assertEquals(2, row_2.cells.size)
                assertNotNull(row_2.cells[ColumnKey("column-2")])
            }
            rowsBuilderState.addRowBuilder(RowIndexPredicateLiteral(eq(10))) { row_3 ->
                row_3.cellsBuilderState.addCellBuilder {}
                assertEquals(1, row_3.cells.size)
                assertNotNull(row_3.cells[ColumnKey("column-0")])
            }
        }
    }

    @Test
    fun `should validate against upstream row spans of a row addressed by index predicate`() {
        fun <T> `check throws on colliding row spans`(rowsBuilderState: RowsBuilderState<T>, index: Int) {
            rowsBuilderState.addRowBuilder(
                RowIndexPredicateLiteral(eq(index))
            ) { row ->
                assertEquals(0, row.cells.size)
                val ex = assertThrows<BuilderException> {
                    row.cellsBuilderState.addCellBuilder(0) {}
                }
                assertEquals("Cannot create cell at ColumnKey = column-0 due to 'rowSpan' lock.", ex.message)
                row.cellsBuilderState.addCellBuilder {}
                assertEquals(1, row.cells.size)
                assertNotNull(row.cells[ColumnKey("column-1")])
            }
        }

        fun <T> `check if succeeds on not colliding row spans`(rowsBuilderState: RowsBuilderState<T>, index: Int) {
            rowsBuilderState.addRowBuilder(
                RowIndexPredicateLiteral(eq(index))
            ) { row ->
                assertEquals(0, row.cells.size)
                row.cellsBuilderState.addCellBuilder(0) {}
                assertEquals(1, row.cells.size)
                assertNotNull(row.cells[ColumnKey("column-0")])
            }
        }

        TableBuilderState<ExportedData>().apply {
            step("Add row with composite index predicate literal and single cell spanning 4 rows") {
                rowsBuilderState.addRowBuilder(
                    RowIndexPredicateLiteral(lte(6) or (gte(11) and lte(11)))
                ) { row ->
                    assertEquals(0, row.cells.size)
                    row.cellsBuilderState.addCellBuilder { it.rowSpan = 4 }
                    assertEquals(1, row.cells.size)
                    assertNotNull(row.cells[ColumnKey("column-0")])
                }
            }
            step("Check if adding cell at row on index colliding with row-spanned cell throws an exception (first range)") {
                (0..7).forEach { `check throws on colliding row spans`(rowsBuilderState, it) }
            }
            step("Check if adding cell at row on index not colliding with row-spanned cell succeeds") {
                (8..10).forEach { `check if succeeds on not colliding row spans`(rowsBuilderState, it) }
            }
            step("Check if adding cell at row on index colliding with row-spanned cell throws an exception (second range)") {
                (11..14).forEach { `check throws on colliding row spans`(rowsBuilderState, it) }
            }
            step("Check if adding cell at row on index after row span boundaries succeeds") {
                `check if succeeds on not colliding row spans`(rowsBuilderState, 15)
            }
        }
    }

    private fun <T> RowsBuilderState<T>.findRowBuilder(index: RowIndexPredicateLiteral<T>): RowBuilderState<T>? =
        rowBuilderStates.find { it.qualifier.index?.let { i -> i == index } ?: false }

    private fun <T> step(title: String, block: () -> T): T {
        log.info("Executing step: $title")
        return block()
    }

    companion object {
        val log: Logger = Logger.getLogger(BuilderStateTest::class.java.name)
    }
}