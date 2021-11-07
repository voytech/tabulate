package io.github.voytech.tabulate.template.context

import io.github.voytech.tabulate.model.Table
import io.github.voytech.tabulate.model.attributes.alias.TableAttribute

data class AttributedTable(
        override val attributes: Set<TableAttribute>?,
) : AttributedModel<TableAttribute>(attributes)

internal fun <T> Table<T>.createContext(customAttributes: MutableMap<String, Any>): AttributedTable =
    AttributedTable(tableAttributes).apply { additionalAttributes = customAttributes }
