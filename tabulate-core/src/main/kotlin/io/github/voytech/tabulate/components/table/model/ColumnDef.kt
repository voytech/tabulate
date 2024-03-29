package io.github.voytech.tabulate.components.table.model

import io.github.voytech.tabulate.core.model.AttributedModelOrPart
import io.github.voytech.tabulate.core.model.Attributes

/**
 * Defines table column. Column groups cells where each cell represents single field or property of single collection element.
 * Column must contain mandatory column key - an identifier which is used on row cell to establish cell-to-column relationship.
 * Column may contain column and cell attributes for customising table appearance.
 * @author Wojciech Mąka
 * @since 0.1.0
 */
internal class ColumnDef<T> internal constructor(
    /**
     * Column key is an unique identifier of a column within table structure.
     *
     * Column may be addressed either by using simple string identifier (a column name),
     * or by using [PropertyReferenceColumnKey] instance.
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
    internal val index: Int,
    /**
     * column attributes for controlling appearance of entire column, which are applicable column-wide not per cell. (e.g. width)
     */
    override val attributes: Attributes?,
) : AttributedModelOrPart {
    @JvmSynthetic
    internal fun resolveRawValue(value: T?): Any? = value?.let { id.property?.getPropertyValue(it) }
}
