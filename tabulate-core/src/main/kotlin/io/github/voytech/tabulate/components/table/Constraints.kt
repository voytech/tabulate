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
import io.github.voytech.tabulate.components.table.model.attributes.table.TemplateFileAttribute
import io.github.voytech.tabulate.components.table.operation.*
import io.github.voytech.tabulate.core.model.AttributeConstraintsProvider
import io.github.voytech.tabulate.core.model.AttributeConstraintsBuilder

class TableAttributesConstraints : AttributeConstraintsProvider {
    override fun defineConstraints(): AttributeConstraintsBuilder.() -> Unit = {

        disable<TemplateFileAttribute, ColumnDef<Any>>()
        disable<TemplateFileAttribute, CellDef<Any>>()
        disable<TemplateFileAttribute, RowDef<Any>>()
        disable<RowBordersAttribute, ColumnDef<Any>>()
        disable<RowBordersAttribute, CellDef<Any>>()
        disable<RowHeightAttribute, ColumnDef<Any>>()
        disable<RowHeightAttribute, CellDef<Any>>()
        disable<ColumnWidthAttribute, RowDef<Any>>()
        disable<ColumnWidthAttribute, CellDef<Any>>()

        enable<TemplateFileAttribute, TableStart>()
        enable<TemplateFileAttribute, TableEnd>()
        enable<ColumnWidthAttribute, ColumnStart>()
        enable<ColumnWidthAttribute, ColumnEnd>()

        enable<RowBordersAttribute, RowStart>()
        enable<RowBordersAttribute, RowEnd<Any>>()
        enable<RowHeightAttribute, RowStart>()
        enable<RowHeightAttribute, RowEnd<Any>>()

        enable<CellTextStylesAttribute, CellContext>()
        enable<CellBordersAttribute, CellContext>()
        enable<CellBackgroundAttribute, CellContext>()
        enable<CellAlignmentAttribute, CellContext>()
    }
}