package io.github.voytech.tabulate

import io.github.voytech.tabulate.components.container.api.builder.dsl.*
import io.github.voytech.tabulate.components.document.api.builder.dsl.document
import io.github.voytech.tabulate.components.document.template.export
import io.github.voytech.tabulate.components.page.api.builder.dsl.page
import io.github.voytech.tabulate.components.text.api.builder.dsl.*
import io.github.voytech.tabulate.components.wrapper.api.builder.dsl.align
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Various text rendering tests. text wrapping, clipping...")
class TextTests {

    @Test
    fun `should export document with text`() {
        document {
            page {
                horizontal {
                    content {
                        immediateIterations
                        attributes {
                            width { 50.percents() }
                            borders { all { solid; black; 10.pt() } }
                        }
                        text {
                            value =
                                "A very very very long text with width 1.. and still writing.. and text wrapping by line breaking ... How long it will " +
                                        "take... how long. Please tell me! Write this text so long that it will cross the horizontal boundaries. Let us see " +
                                        "Still some text to write. A little longer words maybe ?. Almost done. I think now the text will finally stop rendering " +
                                        "due to crossing parent layout bounding box boundaries!"
                            attributes {
                                width { 150.pt() }
                                text { breakWords; courierNew; fontSize = 10 }
                                alignment { justify }
                                borders { all { solid; red; 5.pt() } }
                            }
                        }
                        text {
                            value =
                                "This text will not cause words to break, instead text lines may break only when space or  sign is present so that words are kept intact. " +
                                        "And how long this text will be... how long? Please tell me!!! Write this text so long that it will cross the horizontal boundaries. Let us see " +
                                        "Still some text to write. A little longer words maybe ?. Almost done. I think now the text will finally stop rendering " +
                                        "due to crossing parent layout bounding box boundaries!"
                            attributes {
                                clip { enabled }; text { breakLines; lineSpacing = 1.1F }; alignment { left }
                                borders { all { solid; green; 5.pt() } }
                            }
                        }
                        text {
                            value =
                                "A very very very long text with width 3 and this text will be clipped as it cannot wrap and clip is enabled"
                            attributes { clip { enabled }; text { noWrap }; borders { all { 0.5.pt();solid } } }
                        }
                        text {
                            value = "A very very very long text with width 4"
                            attributes { clip { disabled }; overflow { retry }; text { noWrap }; borders { all { 0.5.pt();solid } } }
                        }
                        text {
                            value = "A very very very long text with width 5"
                            attributes { clip { disabled }; text { noWrap }; borders { all { 0.5.pt();solid } } }
                        }
                    }
                    content {
                        attributes {
                            width { 50.percents() }
                            borders { all { dotted; black } }
                        }
                        text {
                            attributes {
                                text { breakLines; timesNewRoman; fontSize = 12; bold; italic = true }
                                alignment { justify }
                                margins { left { 5.pt() }; top { 5.pt() } }
                                borders { all { lightGray; 3.pt() } }
                            }
                            value =
                                "This text will not cause words to break, instead text lines may break only when space or  sign is present so that words are kept intact. " +
                                        "And how long this text will be... how long? Please tell me!!! Write this text so long that it will cross the horizontal boundaries. Let us see " +
                                        "Still some text to write. A little longer words maybe ?. Almost done. I think now the text will finally stop rendering " +
                                        "due to crossing parent layout bounding box boundaries!"
                        }
                    }
                }
            }
        }.export("text_1.pdf")
    }

    @Test
    fun `should correctly export two pages`() {
        document {
            page {
                textValue {
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore\n" +
                            "magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea\n" +
                            "commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla\n" +
                            "pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est\n" +
                            "laborum.\n"
                }
                textValue {
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore\n" +
                            "magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea\n" +
                            "commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla\n" +
                            "pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est\n" +
                            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore\n" +
                            "magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea\n" +
                            "commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla\n" +
                            "pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est\n"
                }
            }
            page {
                align { fullSize; center; middle } content {
                    forcePreMeasure
                    textValue {
                        attributes {
                            borders { all { red; 6.pt(); solid } }
                            alignment { justify }
                            clip { disabled }
                        }
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore\n" +
                                "magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea\n" +
                                "commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla\n" +
                                "pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est\n" +
                                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore\n" +
                                "magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea\n" +
                                "commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla\n" +
                                "pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est\n" +
                                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore\n" +
                                "magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea\n" +
                                "commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla\n" +
                                "pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est\n" +
                                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore\n" +
                                "magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea\n" +
                                "commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla\n" +
                                "pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est\n"
                    }

                }
            }
        }.export("text_2.pdf")
    }
}