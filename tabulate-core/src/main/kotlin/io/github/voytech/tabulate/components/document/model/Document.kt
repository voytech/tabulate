package io.github.voytech.tabulate.components.document.model

import io.github.voytech.tabulate.components.document.operation.DocumentEnd
import io.github.voytech.tabulate.components.document.operation.DocumentStart
import io.github.voytech.tabulate.core.model.*

class Document internal constructor(
    @get:JvmSynthetic
    override val models: List<AbstractModel>,
    @get:JvmSynthetic override val attributes: Attributes?,
    override val id: String,
) : AbstractContainerModelWithAttributes() {

    override fun doExport(api: ExportApi) = api {
        render(DocumentStart(api))
        models.forEach { child ->
            do {
                child.export()
            } while (api.iterations().haveChildrenPendingIterations())
        }
        render(DocumentEnd(api))
    }

}