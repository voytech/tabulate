package io.github.voytech.tabulate.components.table.model

import io.github.voytech.tabulate.components.table.model.attributes.CellAttribute
import io.github.voytech.tabulate.components.table.model.attributes.ColumnAttribute
import io.github.voytech.tabulate.components.table.model.attributes.RowAttribute
import io.github.voytech.tabulate.components.table.model.attributes.TableAttribute
import io.github.voytech.tabulate.core.model.Model
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.DataSourceBinding

/**
 * A top-level definition of tabular layout. Aggregates column as well as all row definitions. It can also contain
 * globally defined attributes for table, cells, columns and rows. Such attributes applies to each model level
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class Table<T> internal constructor(
    /**
     * Name of a table. May be used as a sheet name (e.g.: in xlsx files).
     */
    @get:JvmSynthetic
    internal val name: String = "untitled",
    /**
     * First row index at which to start rendering table.
     */
    @get:JvmSynthetic
    internal val firstRow: Int? = 0,
    /**
     * First column index at which to start rendering table.
     */
    @get:JvmSynthetic
    internal val firstColumn: Int? = 0,
    /**
     * All column definitions.
     */
    @get:JvmSynthetic
    internal val columns: List<ColumnDef<T>> = emptyList(),
    /**
     * All row definitions
     */
    @get:JvmSynthetic
    internal val rows: List<RowDef<T>>?,
    /**
     * Table attributes.
     */
    @get:JvmSynthetic
    internal val tableAttributes: Attributes<TableAttribute<*>>?,
    /**
     * Cell attributes. To be applied on each cell within entire table.
     */
    @get:JvmSynthetic
    internal val cellAttributes: Attributes<CellAttribute<*>>?,
    /**
     * Column attributes. To be applied on each column within entire table.
     */
    @get:JvmSynthetic
    internal val columnAttributes: Attributes<ColumnAttribute<*>>?,
    /**
     * Row attributes. To be applied on each row within entire table.
     */
    @get:JvmSynthetic
    internal val rowAttributes: Attributes<RowAttribute<*>>?,

    @get:JvmSynthetic
    internal val dataSource: DataSourceBinding<T>?

) : Model {
    override fun getId(): String = name
    companion object {
        @JvmStatic
        fun jclass() : Class<Table<*>> = Table::class.java
    }
}
