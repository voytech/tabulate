package io.github.voytech.tabulate.components.page.template

import io.github.voytech.tabulate.components.page.model.Page
import io.github.voytech.tabulate.components.commons.operation.newPage
import io.github.voytech.tabulate.core.model.Orientation
import io.github.voytech.tabulate.core.model.Position
import io.github.voytech.tabulate.core.template.ExportTemplate
import io.github.voytech.tabulate.core.template.ExportTemplateServices
import io.github.voytech.tabulate.core.template.LayoutContext
import io.github.voytech.tabulate.core.template.TemplateContext
import io.github.voytech.tabulate.core.template.layout.Layout

class PageTemplate : ExportTemplate<PageTemplate, Page, PageTemplateContext>() {

    override fun doExport(templateContext: PageTemplateContext) = with(templateContext) {
        resumeAllSuspendedNodes()
        resetLayouts()
        with(model) {
           createLayoutScope(orientation = Orientation.VERTICAL) {
               render(newPage(name))
               exportHeader(templateContext)
               exportContent(templateContext)
               exportFooter(templateContext,this)
           }
        }
    }

    private fun Page.exportHeader(context: PageTemplateContext) {
        header?.getSize(context)
        header?.export(context)
    }

    private fun Page.exportContent(context: PageTemplateContext) {
        nodes?.forEach { it.export(context) }
    }

    private fun Page.exportFooter(context: PageTemplateContext, layout: Layout<*,*,*>) {
        footer?.let { model ->
            val footerSize = model.getSize(context)
            val footerTop = footerSize?.height?.let { layout.maxRightBottom?.y?.minus(it) }
            model.export(context, LayoutContext(
                leftTop = Position(layout.leftTop.x,footerTop ?: layout.rightBottom.y))
            )
        }
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
}