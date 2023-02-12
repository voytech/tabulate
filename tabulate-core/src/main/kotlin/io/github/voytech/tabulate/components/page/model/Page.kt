package io.github.voytech.tabulate.components.page.model

import io.github.voytech.tabulate.components.commons.operation.newPage
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.template.ResumeNext
import io.github.voytech.tabulate.core.template.layout.Layout

class Page internal constructor(
    @get:JvmSynthetic
    internal val name: String = "untitled",
    @get:JvmSynthetic
    internal val nodes: List<AbstractModel<*>>? = null,
    internal val header: AbstractModel<*>? = null,
    internal val footer: AbstractModel<*>? = null,
) : AbstractModel<Page>() {

    override fun doExport(exportContext: ModelExportContext<Page>) = with(exportContext) {
        resumeAllSuspendedNodes()
        clearLayouts()
        stickyHeaderAndFooterWith { layout, leftTop ->
            model.exportContent(exportContext, leftTop.contentLayoutContext(layout))
        }
    }

    override fun doResume(exportContext: ModelExportContext<Page>, resumeNext: ResumeNext) = with(exportContext) {
        stickyHeaderAndFooterWith { _, _ -> resumeNext() }
    }

    private fun ModelExportContext<Page>.nextPageNumber(): Int =
        customStateAttributes.run {
            ++ensureExecutionContext { PageExecutionContext() }.pageNumber
        }

    private fun ModelExportContext<Page>.stickyHeaderAndFooterWith(block: (Layout, Position?) -> Unit) {
        createLayoutScope(orientation = Orientation.VERTICAL) {
            render(newPage(nextPageNumber(), name))
            exportHeader(this@stickyHeaderAndFooterWith)
            val size = footerSize(this@stickyHeaderAndFooterWith, this)
            val leftTop = footerLeftTop(size)
            block(this, leftTop)
            exportFooter(this@stickyHeaderAndFooterWith, leftTop.footerLayoutContext(size))
        }
    }

    private fun exportHeader(templateContext: ModelExportContext<Page>) {
        header?.export(templateContext)
    }

    private fun exportContent(templateContext: ModelExportContext<Page>, layoutContext: LayoutContext) {
        nodes?.forEach { it.export(templateContext, layoutContext) }
    }

    private fun footerSize(templateContext: ModelExportContext<Page>, layout: Layout) =
        footer?.measure(templateContext)?.let {
            Size(it.width ?: Width(layout.maxRightBottom!!.x.value, layout.uom), it.height!!)
        }

    private fun Layout.footerLeftTop(size: Size?): Position? =
        maxRightBottom?.let { maxRightBottom ->
            size?.let {
                Position(
                    leftTop.x,
                    maxRightBottom.y - it.height
                )
            }
        }

    private fun Position?.contentLayoutContext(layout: Layout): LayoutContext =
        LayoutContext(maxRightBottom = this?.let { Position(layout.maxRightBottom!!.x, it.y) })

    private fun Position?.footerLayoutContext(size: Size?): LayoutContext =
        LayoutContext(
            leftTop = this,
            maxRightBottom = size?.let { this?.plus(size) }
        )

    private fun exportFooter(templateContext: ModelExportContext<Page>, layoutContext: LayoutContext?) {
        footer?.export(templateContext, layoutContext)
    }

}
