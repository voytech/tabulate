package io.github.voytech.tabulate.components.spacing.template

import io.github.voytech.tabulate.components.spacing.model.Spacing
import io.github.voytech.tabulate.core.model.Position
import io.github.voytech.tabulate.core.reify
import io.github.voytech.tabulate.core.template.*
import io.github.voytech.tabulate.core.template.layout.DefaultLayoutQueries

class SpacingTemplate : ExportTemplate<Spacing, SpacingTemplateContext> {
    override fun <R : RenderingContext> export(
        renderingContext: R,
        templateContext: SpacingTemplateContext,
        apis: ExportTemplateApis<R>,
    ) = with(templateContext) {
        Position(
            apis.getActiveLayoutBoundaries().rightBottom.x + templateContext.model.size.width,
            apis.getActiveLayoutBoundaries().rightBottom.y + templateContext.model.size.height
        ).let { leftTop ->
            val layout = apis.getActiveLayout()
                .newLayout(DefaultLayoutQueries(), leftTop, templateContext.model.childOrientation)
            model.child?.export(renderingContext, templateContext, apis)
            layout.finish()
        }
    }

    override fun modelClass(): Class<Spacing> = reify()

    override fun buildTemplateContext(
        parentContext: TemplateContext<*>,
        childModel: Spacing,
    ): SpacingTemplateContext = SpacingTemplateContext(
        childModel,
        parentContext.stateAttributes
    )
}

class SpacingTemplateContext(
    model: Spacing,
    stateAttributes: MutableMap<String, Any>,
) : TemplateContext<Spacing>(model, stateAttributes)