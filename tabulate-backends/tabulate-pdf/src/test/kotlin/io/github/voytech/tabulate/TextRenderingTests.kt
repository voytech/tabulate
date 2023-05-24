package io.github.voytech.tabulate

import io.github.voytech.tabulate.components.container.api.builder.dsl.*
import io.github.voytech.tabulate.components.document.api.builder.dsl.document
import io.github.voytech.tabulate.components.document.template.export
import io.github.voytech.tabulate.components.page.api.builder.dsl.page
import io.github.voytech.tabulate.components.text.api.builder.dsl.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Various text rendering tests. text wrapping, clipping...")
class TextRenderingTests {

    @Test
    fun `should export document with text`() {
        document {
            page {
                horizontal {
                    section {
                        attributes {
                            width { 50.percents() }
                            height { 20.percents() }
                            borders { all { dashed; black; 3.pt() } }
                        }
                        text {
                            value =
                                "A very very very long text with width 1.. and still writing.. and text wrapping by line breaking ... How long it will " +
                                        "take... how long. Please tell me! Write this text so long that it will cross the horizontal boundaries. Let us see " +
                                        "Still some text to write. A little longer words maybe ?. Almost done. I think now the text will finally stop rendering " +
                                        "due to crossing parent layout bounding box boundaries!"
                            attributes {
                                width { 100.pt() }
                                text { breakWords; courierNew; fontSize = 10 }
                                borders { all { solid;red; 5.pt() } }
                            }
                        }
                        text {
                            value =
                                "This text will not cause words to break, instead text lines may break only when space or  sign is present so that words are kept intact." +
                                        "And how long this text will be... how long? \nPlease tell me!!!\n Write this text so long that it will cross the horizontal boundaries. Let us see " +
                                        "Still some text to write. A little longer words maybe ?. Almost done. I think now the text will finally stop rendering " +
                                        "due to crossing parent layout bounding box boundaries!"
                            attributes {
                                clip { enabled }; text { breakLines; lineSpacing = 1.1F }; alignment { center }
                                borders { all { solid; green; 5.pt() } }
                            }
                        }
                        text {
                            value = "A very very very long text with width 3"
                            attributes { clip { enabled }; text { noWrap } }
                        }
                        text {
                            value = "A very very very long text with width 4"
                            attributes { clip { enabled }; text { noWrap } }
                        }
                        text {
                            value = "A very very very long text with width 5"
                            attributes { clip { enabled }; text { noWrap } }
                        }
                    }
                    section {
                        attributes {
                            width { 50.percents() }
                            borders { all { dotted; black } }
                        }
                        text {
                            attributes {
                                text { breakLines; timesNewRoman; fontSize = 12; bold;  italic=true }
                                margins { left { 5.pt()}; top { 5.pt() } }
                                borders { all { lightGray; 3.pt() } }
                            }
                            value = "This text will not cause words to break, instead text lines may break only when space or  sign is present so that words are kept intact." +
                                    "And how long this text will be... how long? \nPlease tell me!!!\n Write this text so long that it will cross the horizontal boundaries. Let us see " +
                                    "Still some text to write. A little longer words maybe ?. Almost done. I think now the text will finally stop rendering " +
                                    "due to crossing parent layout bounding box boundaries!"
                        }
                    }
                }
            }
        }.export("text_0.pdf")
    }
}