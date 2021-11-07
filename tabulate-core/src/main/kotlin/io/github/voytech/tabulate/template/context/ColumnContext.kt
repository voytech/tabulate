package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.attributes.ColumnAttribute

class ColumnContext(private val attributedContext: AttributedColumn):
        Context by attributedContext,
        ColumnCoordinate by attributedContext,
        ModelAttributeAccessor<ColumnAttribute<*>>(attributedContext) {
            val currentPhase: ColumnRenderPhase? = attributedContext.currentPhase
        }

fun AttributedColumn.crop(): ColumnContext = ColumnContext(this)

enum class ColumnRenderPhase {
    BEFORE_FIRST_ROW,
    AFTER_LAST_ROW
}