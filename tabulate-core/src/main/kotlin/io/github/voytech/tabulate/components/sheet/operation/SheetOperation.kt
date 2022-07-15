package io.github.voytech.tabulate.components.sheet.operation

import io.github.voytech.tabulate.components.sheet.model.Sheet
import io.github.voytech.tabulate.components.sheet.template.SheetTemplateContext
import io.github.voytech.tabulate.core.template.RenderingContext
import io.github.voytech.tabulate.core.template.operation.AttributedContext
import io.github.voytech.tabulate.core.template.operation.Operation

class SheetContext(val sheetName: String) : AttributedContext<SheetContext>()

fun interface RenderSheetOperation<CTX : RenderingContext> : Operation<CTX, SheetContext>

fun Sheet.context(templateContext: SheetTemplateContext): SheetContext =
    SheetContext(id).also {
        it.additionalAttributes = templateContext.stateAttributes
    }