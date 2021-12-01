package io.github.voytech.tabulate.model

import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.ColumnAttribute

/**
 * Defines single table column. Column groups cells that expose specific property of collection elements.
 * Column must contain mandatory column key - an identifier which is used on row cell to establish cell-to-column binding.
 * Column may contain column and cell attributes that enable table appearance customisation.
 * @author Wojciech Mąka
 */
internal class ColumnDef<T> internal constructor(
    /**
     * Column key is an unique identifier of an column within table structure.
     *
     * Column may be addressed using simple string identifier - a column name,
     * or by using [PropertyReferenceColumnKey] instance (for Kotlin DSL type-safe API an [PropertyLiteralColumnKey]
     * instance will be used, while for java fluent builder API it will be an instance of [NamedPropertyReferenceColumnKey]).
     *
     * [PropertyReferenceColumnKey] addressing should be used if there is need to establish
     * binding between column and collection elements property (to use object property value as a cell value later on)
     */
    @get:JvmSynthetic
    internal val id: ColumnKey<T>,
    /**
     * An index at which column should be rendered in the exported table.
     */
    @get:JvmSynthetic
    internal val index: Int?,
    /**
     * column attributes for controlling appearance of entire column, which are applicable column-wide not per cell. (e.g. width)
     */
    @get:JvmSynthetic
    internal val columnAttributes: Set<ColumnAttribute>?,
    /**
     * cell attributes for controlling appearance of all cells defined within particular column.
     */
    @get:JvmSynthetic
    internal val cellAttributes: Set<CellAttribute>?
) {
    @JvmSynthetic
    internal fun resolveRawValue(value: T?): Any? = value?.let { id.property?.getPropertyValue(it) }
}
