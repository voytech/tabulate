package io.github.voytech.tabulate.components.page.template

import io.github.voytech.tabulate.components.page.model.Page
import io.github.voytech.tabulate.components.commons.operation.newPage
import io.github.voytech.tabulate.core.template.ExportTemplate
import io.github.voytech.tabulate.core.template.ExportTemplateServices
import io.github.voytech.tabulate.core.template.TemplateContext

class PageTemplate : ExportTemplate<PageTemplate, Page, PageTemplateContext>() {

    override fun doExport(templateContext: PageTemplateContext) = with(templateContext) {
        resumeAllSuspendedNodes()
        resetLayouts()
        render(newPage(templateContext.model.name))
        model.exportChildren(templateContext)
    }

    private fun Page.exportChildren(context: PageTemplateContext) {
        nodes?.forEach { it.export(context) }
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