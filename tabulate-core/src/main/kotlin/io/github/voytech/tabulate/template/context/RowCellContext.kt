package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.attributes.CellAttribute
import io.github.voytech.tabulate.model.attributes.cell.TypeHintAttribute

class RowCellContext(private val attributedContext: AttributedCell) :
        Context by attributedContext,
        RowCellCoordinate by attributedContext,
        ModelAttributeAccessor<CellAttribute<*>>(attributedContext) {

    val value: CellValue by attributedContext::value

    val rawValue: Any by value::value
}

@Suppress("UNCHECKED_CAST")
fun AttributedCell.crop(): RowCellContext = RowCellContext(this)

fun RowCellContext.getTypeHint(): TypeHintAttribute? = getFirstAttributeOrNull<TypeHintAttribute>()