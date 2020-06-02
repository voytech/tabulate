package pl.voytech.exporter.core.model

/**
 * Table row rendering context with row and object coordinates (row number within target table, as well as object
 * index within collection) and row-associated collection entry.
 * Instantiated indirectly by ExportingState class instance.
 * @author Wojciech Mąka
 */
data class RowData<T>(
    /**
     * index of a row in entire table (including synthetic rows).
     */
    val rowIndex: Int,
    /**
     * Index of an object within dataset.
     */
    val objectIndex: Int? = null,
    /**
     * Object from collection at objectIndex.
     */
    val record: T? = null,
    /**
     * collection of objects to be exported.
     */
    val dataset: Collection<T>
)
