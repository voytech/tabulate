package pl.voytech.exporter.impl.template.excel

import pl.voytech.exporter.core.model.hints.RowHint
import pl.voytech.exporter.core.model.hints.TableHint
import pl.voytech.exporter.core.model.hints.rendering.CellBackgroundHint
import pl.voytech.exporter.core.model.hints.rendering.CellFontHint
import pl.voytech.exporter.core.template.*
import pl.voytech.exporter.impl.template.excel.PoiWrapper.assertCell
import pl.voytech.exporter.impl.template.excel.PoiWrapper.getWorkbook
import kotlin.reflect.KClass

class CellFontHintOperation: CellHintOperation<CellFontHint> {

    override fun hintType(): KClass<CellFontHint>  = CellFontHint::class

    override fun apply(state: DelegateState, coordinates: Coordinates, hint: CellFontHint) {
        assertCell(state, coordinates).let {cell ->
            getWorkbook(state).createCellStyle().also {
                val font = getWorkbook(state).createFont()
                font.fontName = hint.fontFamily
                font.color = hint.fontColor
                font.fontHeight = hint.fontSize.toShort()
                it.setFont(font)
                cell.cellStyle = it
            }
        }
    }

}

class CellBackgroundHintOperation: CellHintOperation<CellBackgroundHint> {
    override fun hintType(): KClass<CellBackgroundHint> = CellBackgroundHint::class

    override fun apply(state: DelegateState, coordinates: Coordinates, hint: CellBackgroundHint) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

val tableHintsOperations = emptyList<TableHintOperation<TableHint>>()
val rowHintsOperations = emptyList<RowHintOperation<RowHint>>()
val cellHintsOperations = listOf(CellFontHintOperation(), CellBackgroundHintOperation())