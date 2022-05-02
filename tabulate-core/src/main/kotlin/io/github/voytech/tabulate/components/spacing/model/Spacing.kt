package io.github.voytech.tabulate.components.spacing.model

import io.github.voytech.tabulate.components.spacing.template.SpacingTemplate
import io.github.voytech.tabulate.core.model.Model
import io.github.voytech.tabulate.core.model.Orientation
import io.github.voytech.tabulate.core.model.Size
import io.github.voytech.tabulate.core.template.ExportTemplate
import io.github.voytech.tabulate.core.template.TemplateContext

class Spacing internal constructor(
    @get:JvmSynthetic
    internal val id: String,
    @get:JvmSynthetic
    internal val size: Size,
    @get:JvmSynthetic
    internal val childOrientation: Orientation,
    @get:JvmSynthetic
    internal val child: Model<*>? = null,
) : Model<Spacing> {
    override fun getId(): String = id

    override fun getExportTemplate(): ExportTemplate<Spacing, out TemplateContext<Spacing>> = SpacingTemplate()
}

