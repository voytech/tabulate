package io.github.voytech.tabulate.components.page.template

import io.github.voytech.tabulate.components.page.model.Page
import io.github.voytech.tabulate.components.commons.operation.newPage
import io.github.voytech.tabulate.components.page.model.PageExecutionContext
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.template.*
import io.github.voytech.tabulate.core.template.layout.Layout

class PageTemplate : ExportTemplate<PageTemplate, Page, PageTemplateContext>() {

    override fun doExport(templateContext: PageTemplateContext) = with(templateContext) {
        resumeAllSuspendedNodes()
        resetLayouts()
        stickyHeaderAndFooterWith { layout, leftTop ->
            model.exportContent(templateContext, leftTop.contentLayoutContext(layout))
        }
    }

    override fun doResume(templateContext: PageTemplateContext, resumeNext: ResumeNext) = with(templateContext) {
        stickyHeaderAndFooterWith { _, _ -> resumeNext() }
    }

    private fun PageTemplateContext.stickyHeaderAndFooterWith(block: (Layout<*, *, *>, Position?) -> Unit) {
        with(model) {
            createLayoutScope(orientation = Orientation.VERTICAL) {
                render(newPage(nextPageNumber(), name))
                exportHeader(this@stickyHeaderAndFooterWith)
                val size = footerSize(this@stickyHeaderAndFooterWith, this)
                val leftTop = footerLeftTop(size)
                block(this, leftTop)
                exportFooter(this@stickyHeaderAndFooterWith, leftTop.footerLayoutContext(size))
            }
        }
    }

    private fun Page.exportHeader(context: PageTemplateContext) {
        header?.export(context)
    }

    private fun Page.exportContent(context: PageTemplateContext, layoutContext: LayoutContext) {
        nodes?.forEach { it.export(context, layoutContext) }
    }

    private fun Page.footerSize(context: PageTemplateContext, layout: Layout<*, *, *>) = footer?.getSize(context)?.let {
        Size(it.width ?: Width(layout.maxRightBottom!!.x.value, layout.uom), it.height!!)
    }

    private fun Layout<*, *, *>.footerLeftTop(size: Size?): Position? =
        maxRightBottom?.let { maxRightBottom ->
            size?.let {
                Position(
                    leftTop.x,
                    maxRightBottom.y - it.height
                )
            }
        }

    private fun Position?.contentLayoutContext(layout: Layout<*, *, *>): LayoutContext =
        LayoutContext(maxRightBottom = this?.let { Position(layout.maxRightBottom!!.x, it.y) })

    private fun Position?.footerLayoutContext(size: Size?): LayoutContext =
        LayoutContext(
            leftTop = this,
            maxRightBottom = size?.let { this?.plus(size) }
        )


    private fun Page.exportFooter(context: PageTemplateContext, layoutContext: LayoutContext?) {
        footer?.export(context, layoutContext)
    }

    override fun createTemplateContext(
        parentContext: TemplateContext<*, *>,
        model: Page,
    ): PageTemplateContext = PageTemplateContext(
        model,
        parentContext.stateAttributes,
        parentContext.services
    )

}

class PageTemplateContext(
    model: Page,
    stateAttributes: MutableMap<String, Any>,
    services: ExportTemplateServices,
) : TemplateContext<PageTemplateContext, Page>(model, stateAttributes, services) {
    init {
        stateAttributes["_sheetName"] = model.name
    }

    fun nextPageNumber(): Int =
        StateAttributes(stateAttributes).run {
            ++ensureExecutionContext { PageExecutionContext() }.pageNumber
        }

}