package io.github.voytech.tabulate.components.sheet.template

import io.github.voytech.tabulate.components.sheet.model.Sheet
import io.github.voytech.tabulate.components.sheet.operation.context
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.*
import io.github.voytech.tabulate.core.template.operation.Operations

class SheetTemplate : CompositeExportTemplate<Sheet, SheetTemplateContext>() {
    override fun <R : RenderingContext> export(
        renderingContext: R,
        operations: Operations<R>?,
        templateContext: SheetTemplateContext,
        registry: ExportTemplateRegistry<R>,
    ) {
        with(templateContext) {
            operations?.render(renderingContext, model.context(templateContext))
            model.node?.export(renderingContext, templateContext, registry)
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