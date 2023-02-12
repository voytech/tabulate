package io.github.voytech.tabulate.components.table.model

import io.github.voytech.tabulate.components.table.template.TableExport
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.ResumeNext
import io.github.voytech.tabulate.core.template.layout.GridLayoutPolicy

/**
 * A top-level definition of tabular layout. Aggregates column as well as all row definitions. It can also contain
 * globally defined attributes for table, cells, columns and rows. Such attributes applies to each model level
 * @author Wojciech Mąka
 * @since 0.1.0
 */
class Table<T : Any> internal constructor(
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
    internal val dataSource: DataSourceBinding<T>?,

    override val attributes: Attributes?,

    ) : ModelWithAttributes<Table<T>>(), LayoutPolicyProvider<GridLayoutPolicy> {

    companion object {
        @JvmStatic
        fun <T : Any> jclass(): Class<Table<T>> = reify()
    }

    override val policy: GridLayoutPolicy = GridLayoutPolicy()

    override val planSpaceOnExport = true

    private lateinit var export: TableExport<T>

    override fun createExportContext(parentContext: ModelExportContext<*>): ModelExportContext<Table<T>> {
        return super.createExportContext(parentContext).also { ctx ->
            ctx.customStateAttributes["_sheetName"] = name
            export = TableExport(
                ctx, policy, (dataSource ?: ctx.customStateAttributes["_dataSourceOverride"])?.dataSource ?: emptyList()
            )
        }
    }

    override fun doExport(exportContext: ModelExportContext<Table<T>>) =
        exportContext.exportOrResume()

    override fun doResume(exportContext: ModelExportContext<Table<T>>, resumeNext: ResumeNext) =
        exportContext.exportOrResume()

    override fun takeMeasures(exportContext: ModelExportContext<Table<T>>) =
        export.takeMeasures()

    private fun ModelExportContext<Table<T>>.exportOrResume() {
        createLayoutScope {
            export.exportOrResume()
        }
    }

}
