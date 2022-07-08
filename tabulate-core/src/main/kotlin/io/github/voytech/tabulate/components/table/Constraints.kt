package io.github.voytech.tabulate.components.table

import io.github.voytech.tabulate.components.table.model.CellDef
import io.github.voytech.tabulate.components.table.model.ColumnDef
import io.github.voytech.tabulate.components.table.model.RowDef
import io.github.voytech.tabulate.components.table.model.attributes.cell.CellAlignmentAttribute
import io.github.voytech.tabulate.components.table.model.attributes.cell.CellBackgroundAttribute
import io.github.voytech.tabulate.components.table.model.attributes.cell.CellBordersAttribute
import io.github.voytech.tabulate.components.table.model.attributes.cell.CellTextStylesAttribute
import io.github.voytech.tabulate.components.table.model.attributes.column.ColumnWidthAttribute
import io.github.voytech.tabulate.components.table.model.attributes.row.RowBordersAttribute
import io.github.voytech.tabulate.components.table.model.attributes.row.RowHeightAttribute
import io.github.voytech.tabulate.components.table.operation.*
import io.github.voytech.tabulate.core.model.AttributeConstraintsProvider
import io.github.voytech.tabulate.core.model.AttributeConstraintsBuilder

class TableAttributesConstraints: AttributeConstraintsProvider {
    override fun defineConstraints(): AttributeConstraintsBuilder.() -> Unit = {

        disableOnModel(ColumnDef::class.java, RowBordersAttribute::class.java)
        disableOnModel(ColumnDef::class.java, RowHeightAttribute::class.java)
        disableOnModel(RowDef::class.java, ColumnWidthAttribute::class.java)
        disableOnModel(CellDef::class.java, RowBordersAttribute::class.java)
        disableOnModel(CellDef::class.java, RowHeightAttribute::class.java)
        disableOnModel(CellDef::class.java, ColumnWidthAttribute::class.java)

        enableOnContext(ColumnStart::class.java, ColumnWidthAttribute::class.java)
        enableOnContext(ColumnEnd::class.java, ColumnWidthAttribute::class.java)

        enableOnContext(RowStart::class.java, RowBordersAttribute::class.java)
        enableOnContext(RowEnd::class.java, RowBordersAttribute::class.java)
        enableOnContext(RowStart::class.java, RowHeightAttribute::class.java)
        enableOnContext(RowEnd::class.java, RowHeightAttribute::class.java)

        enableOnContext(CellContext::class.java, CellTextStylesAttribute::class.java)
        enableOnContext(CellContext::class.java, CellBordersAttribute::class.java)
        enableOnContext(CellContext::class.java, CellBackgroundAttribute::class.java)
        enableOnContext(CellContext::class.java, CellAlignmentAttribute::class.java)

    }
}