package pl.voytech.exporter.core.model

/**
 * Row processing context.
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
