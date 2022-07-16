package io.github.voytech.tabulate.excel.components.table

import io.github.voytech.tabulate.components.table.operation.CellContext
import io.github.voytech.tabulate.components.table.operation.TableStart
import io.github.voytech.tabulate.core.model.AttributeConstraintsBuilder
import io.github.voytech.tabulate.core.model.AttributeConstraintsProvider
import io.github.voytech.tabulate.excel.components.table.model.attributes.CellCommentAttribute
import io.github.voytech.tabulate.excel.components.table.model.attributes.CellExcelDataFormatAttribute
import io.github.voytech.tabulate.excel.components.table.model.attributes.FilterAndSortTableAttribute
import io.github.voytech.tabulate.excel.components.table.model.attributes.PrintingAttribute

class ExcelTableAttributesConstraints : AttributeConstraintsProvider {
    override fun defineConstraints(): AttributeConstraintsBuilder.() -> Unit = {

        enable<FilterAndSortTableAttribute, TableStart>()
        enable<PrintingAttribute,TableStart>()
        enable<CellCommentAttribute, CellContext>()
        enable<CellExcelDataFormatAttribute, CellContext>()

    }
}