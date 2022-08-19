package io.github.voytech.tabulate.components.margins.template

import io.github.voytech.tabulate.components.margins.model.Margins
import io.github.voytech.tabulate.core.model.Position
import io.github.voytech.tabulate.core.template.ExportTemplate
import io.github.voytech.tabulate.core.template.ExportTemplateServices
import io.github.voytech.tabulate.core.template.TemplateContext
import io.github.voytech.tabulate.core.template.layout.DefaultLayoutQueries

class MarginsTemplate : ExportTemplate<MarginsTemplate, Margins, MarginsTemplateContext>() {
    override fun doExport(templateContext: MarginsTemplateContext) = with(templateContext) {
        Position(
            services.getActiveLayoutBoundaries().rightBottom.x + model.size.width,
            services.getActiveLayoutBoundaries().leftTop.y + model.size.height
        ).let {
            createLayoutScope(DefaultLayoutQueries(), it, model.childOrientation) {
                model.child?.export(templateContext)
            }
        }
    }

    override fun createTemplateContext(
        parentContext: TemplateContext<*, *>,
        model: Margins,
    ): MarginsTemplateContext = MarginsTemplateContext(
        model,
        parentContext.stateAttributes,
        parentContext.services
    )

}

class MarginsTemplateContext(
    model: Margins, stateAttributes: MutableMap<String, Any>, services: ExportTemplateServices,
) : TemplateContext<MarginsTemplateContext, Margins>(model, stateAttributes, services)