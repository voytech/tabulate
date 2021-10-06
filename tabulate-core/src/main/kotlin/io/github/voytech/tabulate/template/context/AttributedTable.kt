package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.Table
import io.github.voytech.tabulate.model.attributes.alias.TableAttribute

data class AttributedTable(
    val tableAttributes: Set<TableAttribute>?,
) : ContextData()

internal fun <T> Table<T>.createContext(customAttributes: MutableMap<String, Any>): AttributedTable =
    AttributedTable(tableAttributes).apply { additionalAttributes = customAttributes }
