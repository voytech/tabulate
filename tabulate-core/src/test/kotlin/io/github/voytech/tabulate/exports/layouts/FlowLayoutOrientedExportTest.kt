package io.github.voytech.tabulate.exports.layouts

import io.github.voytech.tabulate.components.commons.operation.NewPage
import io.github.voytech.tabulate.components.container.api.builder.dsl.horizontal
import io.github.voytech.tabulate.components.container.api.builder.dsl.vertical
import io.github.voytech.tabulate.components.document.api.builder.dsl.createDocument
import io.github.voytech.tabulate.components.document.api.builder.dsl.document
import io.github.voytech.tabulate.components.image.operation.ImageRenderable
import io.github.voytech.tabulate.components.page.api.builder.dsl.page
import io.github.voytech.tabulate.components.table.api.builder.dsl.height
import io.github.voytech.tabulate.components.table.api.builder.dsl.table
import io.github.voytech.tabulate.components.table.rendering.CellRenderable
import io.github.voytech.tabulate.components.table.rendering.TableStartRenderable
import io.github.voytech.tabulate.components.text.api.builder.dsl.*
import io.github.voytech.tabulate.components.text.operation.TextRenderable
import io.github.voytech.tabulate.core.DocumentFormat
import io.github.voytech.tabulate.core.StandaloneExportTemplate
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.data.Product
import io.github.voytech.tabulate.data.Products
import io.github.voytech.tabulate.support.mock.*
import org.junit.jupiter.api.Test

class FlowLayoutOrientedExportTest {

    private fun horizontalLayoutAssertions(history: Iterator<InterceptedContext>) {
        history.assertRenderableBoundingBoxesInOrder(
            1,
            BoundingRectangle(Position(0.0F, 0.0F), Position(100.0F, 100.0F)),
            BoundingRectangle(Position(100.1F, 0F), Position(200.1F, 100.0F)),
            BoundingRectangle(Position(200.2F, 0F), Position(300.2F, 100.0F)),
            BoundingRectangle(Position(300.3F, 0F), Position(400.0F, 100.0F)),

            BoundingRectangle(Position(0.0F, 100.1F), Position(100.0F, 200.1F)),
            BoundingRectangle(Position(100.1F, 100.1F), Position(200.1F, 200.1F)),
            BoundingRectangle(Position(200.2F, 100.1F), Position(300.2F, 200.1F)),
            BoundingRectangle(Position(300.3F, 100.1F), Position(400.0F, 200.1F)),

            BoundingRectangle(Position(0.0F, 200.2F), Position(100.0F, 300.2F)),
            BoundingRectangle(Position(100.1F, 200.2F), Position(200.1F, 300.2F)),
            BoundingRectangle(Position(200.2F, 200.2F), Position(300.2F, 300.2F)),
            BoundingRectangle(Position(300.3F, 200.2F), Position(400.0F, 300.2F)),

            BoundingRectangle(Position(0.0F, 300.3F), Position(100.0F, 400.0F)),
            BoundingRectangle(Position(100.1F, 300.3F), Position(200.1F, 400.0F)),
            BoundingRectangle(Position(200.2F, 300.3F), Position(300.2F, 400.0F)),
            BoundingRectangle(Position(300.3F, 300.3F), Position(400.0F, 400.0F)),
        )
    }

    @Test
    fun `should render components horizontally in multiple rows when components size are explicit`() {
        // Disable tracking of measuring operations. Only rendering operations will appear in history.
        Spy.spy.documentHeight = 400F.asHeight()
        Spy.spy.documentWidth = 400F.asWidth()
        Spy.spy.trackMeasuring = false
        val doc = document {
            page {
                horizontal {
                    text { value = "txt1."; attributes { width { 100.pt() }; height { 100.pt() } } }
                    text { value = "txt2."; attributes { width { 100.pt() }; height { 100.pt() } } }
                    text { value = "txt3."; attributes { width { 100.pt() }; height { 100.pt() } } }
                    text { value = "txt4."; attributes { width { 100.pt() }; height { 100.pt() } } }

                    text { value = "txt5."; attributes { width { 100.pt() }; height { 100.pt() } } }
                    text { value = "txt6."; attributes { width { 100.pt() }; height { 100.pt() } } }
                    text { value = "txt7."; attributes { width { 100.pt() }; height { 100.pt() } } }
                    text { value = "txt8."; attributes { width { 100.pt() }; height { 100.pt() } } }

                    text { value = "txt9."; attributes { width { 100.pt() }; height { 100.pt() } } }
                    text { value = "txt10."; attributes { width { 100.pt() }; height { 100.pt() } } }
                    text { value = "txt11."; attributes { width { 100.pt() }; height { 100.pt() } } }
                    text { value = "txt12."; attributes { width { 100.pt() }; height { 100.pt() } } }

                    text { value = "txt13."; attributes { width { 100.pt() }; height { 100.pt() } } }
                    text { value = "txt14."; attributes { width { 100.pt() }; height { 100.pt() } } }
                    text { value = "txt15."; attributes { width { 100.pt() }; height { 100.pt() } } }
                    text { value = "txt16."; attributes { width { 100.pt() }; height { 100.pt() } } }
                }
            }
        }
        StandaloneExportTemplate(DocumentFormat.format("spy")).export(createDocument(doc), Unit)
        val history = Spy.spy.readHistory().asSequence().filter { it.context is TextRenderable }.iterator()
        horizontalLayoutAssertions(history)
    }

    @Test
    fun `should render components horizontally in multiple rows when components size are mocked`() {
        Spy.spy.documentHeight = 400F.asHeight()
        Spy.spy.documentWidth = 400F.asWidth()
        Spy.spy.trackMeasuring = false
        Spy.spy.measures.register<TextRenderable>({ _ -> true }) {
            Size(100F.asWidth(), 100F.asHeight())
        }
        val doc2 = document {
            page {
                horizontal {
                    text { value = "txt1." }
                    text { value = "txt2." }
                    text { value = "txt3." }
                    text { value = "txt4." }

                    text { value = "txt5." }
                    text { value = "txt6." }
                    text { value = "txt7." }
                    text { value = "txt8." }

                    text { value = "txt9." }
                    text { value = "txt10." }
                    text { value = "txt11." }
                    text { value = "txt12." }

                    text { value = "txt13." }
                    text { value = "txt14." }
                    text { value = "txt15." }
                    text { value = "txt16." }
                }
            }
        }
        StandaloneExportTemplate(DocumentFormat.format("spy")).export(createDocument(doc2), Unit)
        val history2 = Spy.spy.readHistory().asSequence().filter { it.context is TextRenderable }.iterator()
        horizontalLayoutAssertions(history2)
    }

    private fun verticalLayoutAssertions(history: Iterator<InterceptedContext>) {
        history.assertRenderableBoundingBoxesInOrder(
            1,
            BoundingRectangle(Position(0.0F, 0.0F), Position(100.0F, 100.0F)),
            BoundingRectangle(Position(0.0F, 100.1F), Position(100.0F, 200.1F)),
            BoundingRectangle(Position(0.0F, 200.2F), Position(100.0F, 300.2F)),
            BoundingRectangle(Position(0.0F, 300.3F), Position(100.0F, 400.0F)),

            BoundingRectangle(Position(100.1F, 0.0F), Position(200.1F, 100.0F)),
            BoundingRectangle(Position(100.1F, 100.1F), Position(200.1F, 200.1F)),
            BoundingRectangle(Position(100.1F, 200.2F), Position(200.1F, 300.2F)),
            BoundingRectangle(Position(100.1F, 300.3F), Position(200.1F, 400.0F)),

            BoundingRectangle(Position(200.2F, 0.0F), Position(300.2F, 100.0F)),
            BoundingRectangle(Position(200.2F, 100.1F), Position(300.2F, 200.1F)),
            BoundingRectangle(Position(200.2F, 200.2F), Position(300.2F, 300.2F)),
            BoundingRectangle(Position(200.2F, 300.3F), Position(300.2F, 400.0F)),

            BoundingRectangle(Position(300.3F, 0.0F), Position(400.0F, 100.0F)),
            BoundingRectangle(Position(300.3F, 100.1F), Position(400.0F, 200.1F)),
            BoundingRectangle(Position(300.3F, 200.2F), Position(400.0F, 300.2F)),
            BoundingRectangle(Position(300.3F, 300.3F), Position(400.0F, 400.0F)),
        )
    }

    @Test
    fun `should render components vertically in multiple rows when components size are explicit`() {
        // Disable tracking of measuring operations. Only rendering operations will appear in history.
        Spy.spy.documentHeight = 400F.asHeight()
        Spy.spy.documentWidth = 400F.asWidth()
        Spy.spy.trackMeasuring = false
        val doc = document {
            page {
                vertical {
                    text { value = "txt1."; attributes { width { 100.pt() }; height { 100.pt() } } }
                    text { value = "txt2."; attributes { width { 100.pt() }; height { 100.pt() } } }
                    text { value = "txt3."; attributes { width { 100.pt() }; height { 100.pt() } } }
                    text { value = "txt4."; attributes { width { 100.pt() }; height { 100.pt() } } }

                    text { value = "txt5."; attributes { width { 100.pt() }; height { 100.pt() } } }
                    text { value = "txt6."; attributes { width { 100.pt() }; height { 100.pt() } } }
                    text { value = "txt7."; attributes { width { 100.pt() }; height { 100.pt() } } }
                    text { value = "txt8."; attributes { width { 100.pt() }; height { 100.pt() } } }

                    text { value = "txt9."; attributes { width { 100.pt() }; height { 100.pt() } } }
                    text { value = "txt10."; attributes { width { 100.pt() }; height { 100.pt() } } }
                    text { value = "txt11."; attributes { width { 100.pt() }; height { 100.pt() } } }
                    text { value = "txt12."; attributes { width { 100.pt() }; height { 100.pt() } } }

                    text { value = "txt13."; attributes { width { 100.pt() }; height { 100.pt() } } }
                    text { value = "txt14."; attributes { width { 100.pt() }; height { 100.pt() } } }
                    text { value = "txt15."; attributes { width { 100.pt() }; height { 100.pt() } } }
                    text { value = "txt16."; attributes { width { 100.pt() }; height { 100.pt() } } }
                }
            }
        }
        StandaloneExportTemplate(DocumentFormat.format("spy")).export(createDocument(doc), Unit)
        val history = Spy.spy.readHistory().asSequence().filter { it.context is TextRenderable }.iterator()
        verticalLayoutAssertions(history)
    }

    @Test
    fun `should render components vertically in multiple rows when components size are mocked`() {
        Spy.spy.documentHeight = 400F.asHeight()
        Spy.spy.documentWidth = 400F.asWidth()
        Spy.spy.trackMeasuring = false
        Spy.spy.measures.register<TextRenderable>({ _ -> true }) {
            Size(100F.asWidth(), 100F.asHeight())
        }
        val doc2 = document {
            page {
                vertical {
                    text { value = "txt1." }
                    text { value = "txt2." }
                    text { value = "txt3." }
                    text { value = "txt4." }

                    text { value = "txt5." }
                    text { value = "txt6." }
                    text { value = "txt7." }
                    text { value = "txt8." }

                    text { value = "txt9." }
                    text { value = "txt10." }
                    text { value = "txt11." }
                    text { value = "txt12." }

                    text { value = "txt13." }
                    text { value = "txt14." }
                    text { value = "txt15." }
                    text { value = "txt16." }
                }
            }
        }
        StandaloneExportTemplate(DocumentFormat.format("spy")).export(createDocument(doc2), Unit)
        val history2 = Spy.spy.readHistory().asSequence().filter { it.context is TextRenderable }.iterator()
        verticalLayoutAssertions(history2)
    }

    @Test
    fun `should render components using FlowLayout on multiple pages (Container component)`() {
        Spy.spy.documentHeight = 400F.asHeight()
        Spy.spy.documentWidth = 400F.asWidth()
        // Every text is measured to be of size 200,50
        Spy.spy.measures.register<TextRenderable>({ _ -> true }) {
            Size(220F.asWidth(), 50F.asHeight())
        }
        // Every image is measured to be of size 200,200
        Spy.spy.measures.register<ImageRenderable>({ _ -> true }) {
            Size(400F.asWidth(), 400F.asHeight())
        }
        val doc = document {
            page {
                // This particular rendering will not request measuring of horizontal container children because
                // 'horizontal' does not contain borders and backgrounds so does not require to know its size a priori.
                horizontal {
                    descendantsImmediateIterations
                    // `overflow { retry }` means that at model component level, when rendering of renderable is skipped due to free-space exhaustion
                    // - entire model render iteration will be retried after allocating whole layout space on iteration that caused an overflow event.

                    // `clip { disabled }` - turns on skipping of renderable.
                    // This only makes sense to use both overflow=retry clip=disabled.
                    // If clip=enabled, then retrying would cause partial repetition of content
                    text { value = "txt1" ; attributes { overflow { retry }; clip { disabled } }}
                    text { value = "txt2" ; attributes { overflow { retry }; clip { disabled } }}
                    text { value = "txt3" ; attributes { overflow { retry }; clip { disabled } }}
                    text { value = "txt4" ; attributes { overflow { retry }; clip { disabled } }}
                    text { value = "txt5" ; attributes { overflow { retry }; clip { disabled } }}
                    text { value = "txt6" ; attributes { overflow { retry }; clip { disabled } }}
                    text { value = "txt7" ; attributes { overflow { retry }; clip { disabled } }}
                    text { value = "txt8" ; attributes { overflow { retry }; clip { disabled } }}
                    text { value = "txt9" ; attributes { overflow { retry }; clip { disabled } }}
                    text { value = "txt10" ; attributes { overflow { retry }; clip { disabled } }}
                    text { value = "txt11" ; attributes { overflow { retry }; clip { disabled } }}
                    text { value = "txt12" ; attributes { overflow { retry }; clip { disabled } }}
                }
            }
        }
        StandaloneExportTemplate(DocumentFormat.format("spy")).export(createDocument(doc), Unit)
        val history = Spy.spy.readHistory()
        history.assertOperationAssertionsInOrder(
            { op -> op.assertContextClass<NewPage>() },
            // Measure
            { op -> op.assertIsMeasuringOperation() && op.assertPredicate<TextRenderable> { it.text == "txt1" } },
            // Render
            { op -> op.assertIsRenderingOperation() && op.assertPredicate<TextRenderable> { it.text == "txt1" } },

            // Measure, but overflow is detected on X axis.
            { op -> op.assertIsMeasuringOperation() && op.assertPredicate<TextRenderable> { it.text == "txt2" } },
            // Measure in new iteration
            { op -> op.assertIsMeasuringOperation() && op.assertPredicate<TextRenderable> { it.text == "txt2" } },
            // Render in that iteration
            { op -> op.assertIsRenderingOperation() && op.assertPredicate<TextRenderable> { it.text == "txt2" } },

            { op -> op.assertIsMeasuringOperation() && op.assertPredicate<TextRenderable> { it.text == "txt3" } },
            { op -> op.assertIsMeasuringOperation() && op.assertPredicate<TextRenderable> { it.text == "txt3" } },
            { op -> op.assertIsRenderingOperation() && op.assertPredicate<TextRenderable> { it.text == "txt3" } },

            { op -> op.assertIsMeasuringOperation() && op.assertPredicate<TextRenderable> { it.text == "txt4" } },
            { op -> op.assertIsMeasuringOperation() && op.assertPredicate<TextRenderable> { it.text == "txt4" } },
            { op -> op.assertIsRenderingOperation() && op.assertPredicate<TextRenderable> { it.text == "txt4" } },

            { op -> op.assertIsMeasuringOperation() && op.assertPredicate<TextRenderable> { it.text == "txt5" } },
            { op -> op.assertIsMeasuringOperation() && op.assertPredicate<TextRenderable> { it.text == "txt5" } },
            { op -> op.assertIsRenderingOperation() && op.assertPredicate<TextRenderable> { it.text == "txt5" } },

            { op -> op.assertIsMeasuringOperation() && op.assertPredicate<TextRenderable> { it.text == "txt6" } },
            { op -> op.assertIsMeasuringOperation() && op.assertPredicate<TextRenderable> { it.text == "txt6" } },
            { op -> op.assertIsRenderingOperation() && op.assertPredicate<TextRenderable> { it.text == "txt6" } },

            { op -> op.assertIsMeasuringOperation() && op.assertPredicate<TextRenderable> { it.text == "txt7" } },
            { op -> op.assertIsMeasuringOperation() && op.assertPredicate<TextRenderable> { it.text == "txt7" } },
            { op -> op.assertIsRenderingOperation() && op.assertPredicate<TextRenderable> { it.text == "txt7" } },

            { op -> op.assertIsMeasuringOperation() && op.assertPredicate<TextRenderable> { it.text == "txt8" } },
            { op -> op.assertIsMeasuringOperation() && op.assertPredicate<TextRenderable> { it.text == "txt8" } },
            { op -> op.assertIsMeasuringOperation() && op.assertPredicate<TextRenderable> { it.text == "txt8" } },

            { op -> op.assertContextClass<NewPage>() },

            { op -> op.assertIsMeasuringOperation() && op.assertPredicate<TextRenderable> { it.text == "txt8" } },
            { op -> op.assertIsRenderingOperation() && op.assertPredicate<TextRenderable> { it.text == "txt8" } },

            { op -> op.assertIsMeasuringOperation() && op.assertPredicate<TextRenderable> { it.text == "txt9" } },
            { op -> op.assertIsMeasuringOperation() && op.assertPredicate<TextRenderable> { it.text == "txt9" } },
            { op -> op.assertIsRenderingOperation() && op.assertPredicate<TextRenderable> { it.text == "txt9" } },

            { op -> op.assertIsMeasuringOperation() && op.assertPredicate<TextRenderable> { it.text == "txt10" } },
            { op -> op.assertIsMeasuringOperation() && op.assertPredicate<TextRenderable> { it.text == "txt10" } },
            { op -> op.assertIsRenderingOperation() && op.assertPredicate<TextRenderable> { it.text == "txt10" } },

            { op -> op.assertIsMeasuringOperation() && op.assertPredicate<TextRenderable> { it.text == "txt11" } },
            { op -> op.assertIsMeasuringOperation() && op.assertPredicate<TextRenderable> { it.text == "txt11" } },
            { op -> op.assertIsRenderingOperation() && op.assertPredicate<TextRenderable> { it.text == "txt11" } },

            { op -> op.assertIsMeasuringOperation() && op.assertPredicate<TextRenderable> { it.text == "txt12" } },
            { op -> op.assertIsMeasuringOperation() && op.assertPredicate<TextRenderable> { it.text == "txt12" } },
            { op -> op.assertIsRenderingOperation() && op.assertPredicate<TextRenderable> { it.text == "txt12" } },
        )
    }

    @Test
    fun `should render sub components with descendantIterationsKinds=PostponedIterations`() {
        // Disable tracking of measuring operations. Only rendering operations will appear in history.
        Spy.spy.documentHeight = 800F.asHeight()
        Spy.spy.documentWidth = 600F.asWidth()
        Spy.spy.trackMeasuring = false
        // Make each table cell of size: 200x100 (Thus entire width will be occupied as page width = 600x800 by default)
        Spy.spy.measures.register<CellRenderable>({ _ -> true }) {
            Size(200F.asWidth(), 100F.asHeight())
        }
        Spy.spy.measures.register<TextRenderable>({ _ -> true }) {
            Size(200F.asWidth(), 100F.asHeight())
        }
        val doc = document {
            page {
                vertical {
                    // descendant's postponed iterations means that when there are
                    // multiple descendants and some of them requires continuation due to lack of space,
                    // firstly all component's first iterations will be rendered, and then theirs continuations in the
                    // order matching their initial appearance order.
                    descendantsPostponedIterations
                    table {
                        attributes { height { 500.pt() } } // Table will be truncated and continuation will be requested.
                        columns(Product::code, Product::name, Product::price)
                        dataSource(Products.items(12)) // 9x100 = 1200 > 800 (continuation of table will require next page rendering.)
                    }
                    text { value = "Text" } // First text will be rendered and then table continuation.
                }
            }
        }
        StandaloneExportTemplate(DocumentFormat.format("spy")).export(createDocument(doc), Unit)
        val history = Spy.spy.readHistory().asSequence().filter {
            it.context is TableStartRenderable ||
                    it.context is TextRenderable ||
                    it.context is NewPage
        }.iterator()

        history.assertAttributedContextsAppearanceInOrder(
            NewPage::class,
            TableStartRenderable::class,
            TextRenderable::class,
            TableStartRenderable::class,
            NewPage::class,
            TableStartRenderable::class
        )
    }

    @Test
    fun `should render sub components with descendantIterationsKinds=ImmediateIterations`() {
        // Disable tracking of measuring operations. Only rendering operations will appear in history.
        Spy.spy.documentHeight = 800F.asHeight()
        Spy.spy.documentWidth = 600F.asWidth()
        Spy.spy.trackMeasuring = false
        // Make each table cell of size: 200x100 (Thus entire width will be occupied as page width = 600x800 by default)
        Spy.spy.measures.register<CellRenderable>({ _ -> true }) {
            Size(200F.asWidth(), 100F.asHeight())
        }
        Spy.spy.measures.register<TextRenderable>({ _ -> true }) {
            Size(200F.asWidth(), 100F.asHeight())
        }
        val doc = document {
            page {
                vertical {
                    // descendant's immediate iterations means that when there are
                    // multiple descendants and some of them requires continuation due to lack of space,
                    // firstly all its iterations are to be rendered, and then other models (and their iterations).
                    descendantsImmediateIterations
                    table {
                        attributes { height { 500.pt() } } // Table will be truncated and continuation will be requested.
                        columns(Product::code, Product::name, Product::price)
                        dataSource(Products.items(12)) // 9x100 = 1200 > 800 (continuation of table will require next page rendering.)
                    }
                    text { value = "Text" } // First text will be rendered and then table continuation.
                }
            }
        }
        StandaloneExportTemplate(DocumentFormat.format("spy")).export(createDocument(doc), Unit)
        val history = Spy.spy.readHistory().asSequence().filter {
            it.context is TableStartRenderable ||
                    it.context is TextRenderable ||
                    it.context is NewPage
        }.iterator()

        history.assertAttributedContextsAppearanceInOrder(
            NewPage::class,
            TableStartRenderable::class,
            TableStartRenderable::class,
            NewPage::class,
            TableStartRenderable::class,
            TextRenderable::class,
        )

    }


}