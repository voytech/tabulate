package io.github.voytech.tabulate.components.sheet.template

import io.github.voytech.tabulate.components.sheet.model.Sheet
import io.github.voytech.tabulate.components.sheet.operation.context
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.*
import io.github.voytech.tabulate.core.template.layout.DefaultLayoutQueries

class SheetTemplate : ExportTemplate<Sheet, SheetTemplateContext> {
    override fun <R : RenderingContext> export(
        renderingContext: R,
        templateContext: SheetTemplateContext,
        apis: ExportTemplateApis<R>,
    ) = with(templateContext) {
        apis.resetLayouts()
        apis.getActiveLayout().newLayout(DefaultLayoutQueries()).let { layout ->
            apis.getOperations().render(renderingContext, model.context(templateContext))
            model.nodes?.forEach { it.export(renderingContext, templateContext, apis) }
            layout.finish()
        }
    }

    override fun modelClass(): Class<Sheet> = reify()

    override fun buildTemplateContext(
        parentContext: TemplateContext<*>,
        childModel: Sheet,
    ): SheetTemplateContext = SheetTemplateContext(
        childModel,
        parentContext.stateAttributes
    )
}

class SheetTemplateContext(
    model: Sheet,
    stateAttributes: MutableMap<String, Any>,
) : TemplateContext<Sheet>(model, stateAttributes) {
    init {
        stateAttributes["_sheetName"] = model.id
    }
}