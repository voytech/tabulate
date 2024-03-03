package io.github.voytech.tabulate.exports.layouts

import io.github.voytech.tabulate.components.commons.operation.NewPage
import io.github.voytech.tabulate.components.container.api.builder.dsl.horizontal
import io.github.voytech.tabulate.components.document.api.builder.dsl.createDocument
import io.github.voytech.tabulate.components.document.api.builder.dsl.document
import io.github.voytech.tabulate.components.image.api.builder.dsl.image
import io.github.voytech.tabulate.components.image.operation.ImageRenderable
import io.github.voytech.tabulate.components.page.api.builder.dsl.page
import io.github.voytech.tabulate.components.text.api.builder.dsl.text
import io.github.voytech.tabulate.components.text.operation.TextRenderable
import io.github.voytech.tabulate.core.DocumentFormat
import io.github.voytech.tabulate.core.StandaloneExportTemplate
import io.github.voytech.tabulate.core.model.Size
import io.github.voytech.tabulate.core.model.asHeight
import io.github.voytech.tabulate.core.model.asWidth
import io.github.voytech.tabulate.core.operation.boundingBox
import io.github.voytech.tabulate.support.mock.Spy
import io.github.voytech.tabulate.support.mock.components.NewPageOperation
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LayoutsTest {

    @Test
    fun `should render flow layout items on multiple pages`() {
        // Every text is measured to be of size 200,50
        Spy.spy.measures.register<TextRenderable>({ _ -> true}) {
            Size(220F.asWidth(), 50F.asHeight())
        }
        // Every image is measured to be of size 200,200
        Spy.spy.measures.register<ImageRenderable>({ _ -> true}) {
            Size(400F.asWidth(), 400F.asHeight())
        }
        // TODO below flow layout does not play with multi paging because Document re-attempts child exports only when there are pending continuations (should also re-attempt when there models that are not started for exporting.)
        // TODO FlowLayout could have method for passing measured size of a next component which position is to be resolved (passing of model size should be before mdl.export call) - this prevent rendering of elements which are about to be clipped.
        val doc = document {
            page {
                horizontal {
                    text { value = "Some text 0." }
                    text { value = "Some text 1." }
                    text { value = "Some text 2." }
                    text { value = "Some text 3." }
                    image { filePath = "image1.png" }
                    image { filePath = "image2.png" }
                    image { filePath = "image3.png" }
                    image { filePath = "image4.png" }
                    image { filePath = "image5.png" }
                    image { filePath = "image6.png" }
                }
            }
        }
        StandaloneExportTemplate(DocumentFormat.format("spy")).export(createDocument(doc), Unit)
        val history = Spy.spy.readHistory()

        assertTrue { history.hasNext() }
        var current = history.next()
        assertTrue { current.operation is NewPageOperation }
        assertEquals(1, (current.context as NewPage).pageNumber)

        assertTrue { history.hasNext() }
        current = history.next()
        assertNotNull(current.context.boundingBox())
        assertEquals("Some text 0.",(current.context as TextRenderable).text)
        var currentBbox = requireNotNull(current.context.boundingBox())
        assertTrue(currentBbox.isDefined())
        assertEquals(0.001F,currentBbox.absoluteX.value)
        assertEquals(0.002F,currentBbox.absoluteY.value) //TODO why 0.002F ?
        assertEquals(220.0F, requireNotNull(currentBbox.width).value)
        assertEquals(50.0F, requireNotNull(currentBbox.height).value)

        assertTrue { history.hasNext() }
        current = history.next()
        currentBbox = requireNotNull(current.context.boundingBox())
        assertTrue(currentBbox.isDefined())
    }

    @Test
    fun `should define layout with sub-layout`() {

    }

    @Test
    fun `should define spreadsheet queries and correctly convert between standard units and column or row numbers`() {

    }

    @Test
    fun `should define table layout and correctly convert between standard units and column or row numbers`() {

    }


}