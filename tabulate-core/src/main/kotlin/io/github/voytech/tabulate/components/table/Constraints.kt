package io.github.voytech.tabulate.components.table

import io.github.voytech.tabulate.components.table.model.CellDef
import io.github.voytech.tabulate.components.table.model.ColumnDef
import io.github.voytech.tabulate.components.table.model.RowDef
import io.github.voytech.tabulate.components.table.model.attributes.table.TemplateFileAttribute
import io.github.voytech.tabulate.components.table.operation.*
import io.github.voytech.tabulate.core.model.AttributeConstraintsProvider
import io.github.voytech.tabulate.core.model.AttributeConstraintsBuilder
import io.github.voytech.tabulate.core.model.attributes.*

class TableAttributesConstraints : AttributeConstraintsProvider {
    override fun defineConstraints(): AttributeConstraintsBuilder.() -> Unit = {

        disable<TemplateFileAttribute, ColumnDef<Any>>()
        disable<TemplateFileAttribute, CellDef<Any>>()
        disable<TemplateFileAttribute, RowDef<Any>>()
        disable<HeightAttribute, ColumnDef<Any>>()
        disable<HeightAttribute, CellDef<Any>>()
        disable<WidthAttribute, RowDef<Any>>()
        disable<WidthAttribute, CellDef<Any>>()

    }
}