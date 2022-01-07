package io.github.voytech.tabulate.model

import io.github.voytech.tabulate.model.attributes.alias.CellAttribute
import io.github.voytech.tabulate.model.attributes.alias.ColumnAttribute
import io.github.voytech.tabulate.model.attributes.alias.RowAttribute
import io.github.voytech.tabulate.model.attributes.alias.TableAttribute

/**
 * A top-level definition of tabular layout. Aggregates column as well as all row definitions. It can also contain
 * globally defined attributes for table, cells, columns and rows. Such attributes applies to each model level
 * @author Wojciech Mąka
 */
internal class Table<T> internal constructor(
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
    internal val tableAttributes: Set<TableAttribute>?,
    /**
     * Cell attributes. To be applied on each cell within entire table.
     */
    @get:JvmSynthetic
    internal val cellAttributes: Set<CellAttribute>?,
    /**
     * Column attributes. To be applied on each column within entire table.
     */
    @get:JvmSynthetic
    internal val columnAttributes: Set<ColumnAttribute>?,
    /**
     * Row attributes. To be applied on each row within entire table.
     */
    @get:JvmSynthetic
    internal val rowAttributes: Set<RowAttribute>?
)
