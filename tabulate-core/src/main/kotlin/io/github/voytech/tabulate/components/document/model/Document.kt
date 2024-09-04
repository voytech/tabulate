package io.github.voytech.tabulate.components.document.model

import io.github.voytech.tabulate.components.document.operation.DocumentEnd
import io.github.voytech.tabulate.components.document.operation.DocumentStart
import io.github.voytech.tabulate.components.page.model.Page
import io.github.voytech.tabulate.core.layout.RegionConstraints.Companion.atLeftTop
import io.github.voytech.tabulate.core.model.*

class Document internal constructor(
    @get:JvmSynthetic
    override val models: List<AbstractModel>,
    @get:JvmSynthetic override val attributes: Attributes?,
    override val id: String,
) : AbstractContainerModelWithAttributes() {

    override fun doExport(api: ExportApi): Unit = api {
        render(DocumentStart(api))
        ifPaging { page ->
            do {
                page.export(atLeftTop())
            } while (page.isRunning())
        } orElse {
            models.forEach { it.export() }
        }
        render(DocumentEnd(api))
    }

    private inner class Else(private val pageable: Boolean) {
        infix fun orElse(block: () -> Unit) {
            if (!pageable) {
                if (models.any { it is Page }) {
                    error("Document can only accept single Page or any number of non-Page models!")
                } else {
                    block()
                }
            }
        }
    }

    private fun ifPaging(block: (child: Page) -> Unit): Else {
        var pageable = false
        if (models.all { it is Page }) {
            pageable = true
            models.forEach { page ->
                block(page as Page)
            }
        }
        return Else(pageable)
    }

}