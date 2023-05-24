package io.github.voytech.tabulate

import io.github.voytech.tabulate.components.container.api.builder.dsl.*
import io.github.voytech.tabulate.components.document.api.builder.dsl.document
import io.github.voytech.tabulate.components.document.template.export
import io.github.voytech.tabulate.components.page.api.builder.dsl.page
import io.github.voytech.tabulate.components.text.api.builder.dsl.text
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Various layout rendering calculations scenarios...")
class LayoutsRenderingTests {
    // 1. Size computed by components measuring,
    // 2. Simple Relative size calculations,
    // 3. Relative layout size calculation, when one child layout has size computed by components measuring, and second child layout has relative percentage size. //TODO this will not pass!
    // 4. Continuations layouts.
}