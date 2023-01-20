package io.github.voytech.tabulate.components.document.model

import io.github.voytech.tabulate.components.document.operation.DocumentEnd
import io.github.voytech.tabulate.components.document.operation.DocumentStart
import io.github.voytech.tabulate.core.model.AbstractModel
import io.github.voytech.tabulate.core.model.Attributes
import io.github.voytech.tabulate.core.model.ModelExportContext
import io.github.voytech.tabulate.core.model.ModelWithAttributes

class Document internal constructor(
    @get:JvmSynthetic
    internal val nodes: List<AbstractModel<*>>,
    @get:JvmSynthetic override val attributes: Attributes?,
    override val id: String
) : ModelWithAttributes<Document>() {

    override fun doExport(templateContext: ModelExportContext<Document>) = with(templateContext) {
        createLayoutScope {
            with(model) {
                render(DocumentStart(templateContext))
                nodes.forEach { it.export(templateContext) }
                render(DocumentEnd(templateContext))
            }
        }
    }
}