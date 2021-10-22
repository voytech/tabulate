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
            columnsBuilderState.addColumnBuilder("customCol1") {
                it.columnType = CellType.STRING
            }
            columnsBuilderState.addColumnBuilder(ExportedData::string.id()) {
                it.columnType = CellType.STRING
            }
        }
        assertEquals(2,builder.columnsBuilderState.columnBuilderStates.size)

        with(builder.columnsBuilderState.find(ColumnKey("customCol1"))) {
            assertEquals("customCol1", this?.id?.id)
            assertEquals(0, this?.index)
            assertEquals(CellType.STRING,this?.columnType)
        }

        with(builder.columnsBuilderState.find(ColumnKey(ref = ExportedData::string.id()))) {
            assertEquals(ExportedData::string.id(), this?.id?.ref)
            assertEquals(1, this?.index)
            assertEquals(CellType.STRING,this?.columnType)
        }

        builder.columnsBuilderState.addColumnBuilder("customCol1") {
            it.columnType = CellType.NUMERIC
        }

        assertEquals(2,builder.columnsBuilderState.columnBuilderStates.size)

        with(builder.columnsBuilderState.find(ColumnKey("customCol1"))) {
            assertEquals("customCol1", this?.id?.id)
            assertEquals(0, this?.index)
            assertEquals(CellType.NUMERIC,this?.columnType)
        }

        val exception = assertThrows<BuilderException> {
            builder.columnsBuilderState.addColumnBuilder("customCol1") {
                it.index = 1
            }
        }
        assertEquals("Could not set column index 1 because index is in use by another column.", exception.message)

        builder.columnsBuilderState.addColumnBuilder("customCol1") {
            it.index = 2
        }

        assertEquals(2,builder.columnsBuilderState.columnBuilderStates.size)
        with(builder.columnsBuilderState.find(ColumnKey("customCol1"))) {
            assertEquals("customCol1", this?.id?.id)
            assertEquals(2, this?.index)
            assertEquals(CellType.NUMERIC,this?.columnType)
        }

        builder.columnsBuilderState.addColumnBuilder("customCol2") {
            it.index = 4
        }
        assertEquals(3,builder.columnsBuilderState.columnBuilderStates.size)

        with(builder.columnsBuilderState.find(ColumnKey("customCol2"))) {
            assertEquals(4, this?.index)
        }

        builder.columnsBuilderState.addColumnBuilder("customCol3") {}
        assertEquals(4,builder.columnsBuilderState.columnBuilderStates.size)
        with(builder.columnsBuilderState.find(ColumnKey("customCol3"))) {
            assertEquals(5, this?.index)
        }
    }

    private fun <T> ColumnsBuilderState<T>.find(key: ColumnKey<T>): ColumnBuilderState<T>? =
        columnBuilderStates.find { it.id == key}

    @Test
    fun `should create subsequent rows`() {
        val builder = TableBuilderState<ExportedData>().apply {
            rowsBuilderState.addRowBuilder()
            rowsBuilderState.addRowBuilder()
        }

        assertEquals(
            RowIndexPredicateLiteral<ExportedData>(eq(0)),
            builder.rowsBuilderState.findRowBuilder(RowIndexPredicateLiteral(eq(0)))?.qualifier?.index
        )

        assertEquals(
            RowIndexPredicateLiteral<ExportedData>(eq(1)),
            builder.rowsBuilderState.findRowBuilder(RowIndexPredicateLiteral(eq(1)))?.qualifier?.index
        )
    }

    @Test
    fun `should redefine column at index`() {

    }

    @Test
    fun `should create subsequent row with complex index "index predicate literal"`() {
        testCase("Single boolean predicate literal") {
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
        testCase("Compound boolean predicates literal") {
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
    fun `should alter existing row builder when addressing by "index predicate literal"`() {
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

    }

    private fun <T> RowsBuilderState<T>.findRowBuilder(index: RowIndexPredicateLiteral<T>): RowBuilderState<T>? =
        rowBuilderStates.find { it.qualifier.index?.let { i -> i == index } ?: false }


    private fun testCase(title: String, block: () -> Unit) {
        log.info("Executing test case < $title >")
        block()
    }

    companion object {
        val log: Logger = Logger.getLogger(BuilderStateTest::class.java.name)
    }
}