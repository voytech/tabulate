package io.github.voytech.tabulate.support

import io.github.voytech.tabulate.components.table.model.Table
import io.github.voytech.tabulate.components.table.template.TableRenderIterations
import io.github.voytech.tabulate.components.table.template.TableRowsRenderer
import io.github.voytech.tabulate.core.model.*

fun AbstractModel.createTableContext(customAttributes: MutableMap<String, Any>): ModelExportContext =
    with(spyExportInstance()) {
        createStandaloneExportContext(StateAttributes(customAttributes))
    }

internal fun <T : Any> Table<T>.createRowsRenderer(customAttributes: MutableMap<String, Any>,data: List<T> = emptyList()): TableRowsRenderer<T> {
    if (data.isNotEmpty()) {
        val dataAsAttribute = mutableMapOf<String, Any>("_dataSourceOverride" to DataSourceBinding(data))
        customAttributes += dataAsAttribute
    }
    val ctx = createTableContext(customAttributes)
    var renderer: TableRowsRenderer<T>? = null
    ctx.api {
        val tableIterations = TableRenderIterations(iterations())
        renderer = TableRowsRenderer(
            this@createRowsRenderer, emptyList(), this, 0, tableIterations, getCustomAttributes()
        )
    }
    return renderer!!
}
