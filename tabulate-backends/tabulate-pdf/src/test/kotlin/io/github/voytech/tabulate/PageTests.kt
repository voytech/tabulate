package io.github.voytech.tabulate

import io.github.voytech.tabulate.components.container.api.builder.dsl.content
import io.github.voytech.tabulate.components.document.api.builder.dsl.document
import io.github.voytech.tabulate.components.document.template.export
import io.github.voytech.tabulate.components.page.api.builder.dsl.page
import io.github.voytech.tabulate.components.table.api.builder.dsl.*
import io.github.voytech.tabulate.components.text.api.builder.dsl.textValue
import io.github.voytech.tabulate.components.wrapper.api.builder.dsl.align
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Testing page pdf exports")
class PageTests {
    @Test
    fun `should correctly export two explicit pages`() {
        document {
            page {
                textValue { "Page 1" }
            }
            page {
                align { center; middle; fullWidth;fullHeight }
                    //@TODO there is a DOT due to bug since textValue is declared on parent PageBuilderApi as well as infix textValue on WrapperBuilderApi. Infix have lower precedence when resolving receiver.
                    .textValue { "Page 2" }
            }
            page {
                align { center; middle; fullWidth;fullHeight } content {
                    textValue  { "Page 3" }
                }
            }
        }.export("page_1.pdf")
    }
}