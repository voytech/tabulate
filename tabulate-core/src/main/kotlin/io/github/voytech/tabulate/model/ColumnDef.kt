package io.github.voytech.tabulate.model

import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.ColumnAttribute

internal class ColumnDef<T> internal constructor(
    @get:JvmSynthetic
    internal val id: ColumnKey<T>,
    @get:JvmSynthetic
    internal val index: Int?,
    @get:JvmSynthetic
    internal val columnAttributes: Set<ColumnAttribute>?,
    @get:JvmSynthetic
    internal val cellAttributes: Set<CellAttribute>?
) {
    @JvmSynthetic
    internal fun resolveRawValue(value: T?): Any? = value?.let { id.property?.getPropertyValue(it) }
}
