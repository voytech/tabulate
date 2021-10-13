package io.github.voytech.tabulate.builder

import io.github.voytech.tabulate.api.builder.ColumnBuilderState
import io.github.voytech.tabulate.api.builder.ColumnsBuilderState
import io.github.voytech.tabulate.api.builder.TableBuilderState
import io.github.voytech.tabulate.api.builder.exception.BuilderException
import io.github.voytech.tabulate.model.CellType
import io.github.voytech.tabulate.model.ColumnKey
import io.github.voytech.tabulate.model.id
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

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

        val exception = assertThrows<BuilderException>("") {
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
    }

    private fun <T> ColumnsBuilderState<T>.find(key: ColumnKey<T>): ColumnBuilderState<T>? =
        columnBuilderStates.find { it.id == key}
}