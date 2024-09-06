package io.github.voytech.tabulate

import io.github.voytech.tabulate.components.document.api.builder.dsl.document
import io.github.voytech.tabulate.components.document.template.export
import io.github.voytech.tabulate.components.image.api.builder.dsl.*
import io.github.voytech.tabulate.components.page.api.builder.dsl.PageBuilderApi
import io.github.voytech.tabulate.components.page.api.builder.dsl.page
import io.github.voytech.tabulate.components.table.api.builder.dsl.*
import io.github.voytech.tabulate.components.text.api.builder.dsl.*
import io.github.voytech.tabulate.core.model.border.DefaultBorderStyle
import io.github.voytech.tabulate.core.model.color.Colors
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Testing various border styles")
class BordersTests {

    private fun PageBuilderApi.section(text: () -> String) {
        textValue {
            attributes {
                width { 100.percents() }
                alignment { center; middle }
                borders { top { 1.pt() }; bottom { 1.pt() } }
            }
            text()
        }
    }

    @Test
    fun `should correctly export border configurations`() {
        document {
            page {
                section { "TEXT-BOX BORDER STYLES" }

                textValue {
                    attributes {
                        margins { all { 5.pt() } }
                        borders {
                            right { solid; 1.pt(); black; }
                            top { solid; 1.pt(); black; }
                            bottom { solid; 1.pt(); black; }
                            left { solid; 1.pt(); black; }
                            corners { solid; 2.pt(); black; 1.radiusPt() }
                        }
                    }
                    "Solid borders"
                }

                textValue {
                    attributes {
                        margins { all { 5.pt() } }
                        borders {
                            all { solid; 2.pt(); black }
                            corners { solid; 2.pt(); black; 15.radiusPt() }

                        }
                    }
                    "Solid borders 5pt"
                }

                textValue {
                    attributes {
                        margins { all { 5.pt() } }
                        borders { all { double; black } }
                    }
                    "Double borders"
                }

                textValue {
                    attributes {
                        margins { all { 5.pt() } }
                        borders { all { double; 5.pt(); black } }
                    }
                    "Double borders 5pt"
                }

                textValue {
                    attributes {
                        margins { all { 5.pt() } }
                        borders { all { dashed; black } }
                    }
                    "Dashed borders"
                }

                textValue {
                    attributes {
                        margins { all { 5.pt() } }
                        borders { all { dashed; 5.pt(); black } }
                    }
                    "Dashed borders 5pt"
                }

                textValue {
                    attributes {
                        margins { all { 5.pt() } }
                        borders { all { dotted; Colors.BLACK; } }
                    }
                    "Dotted borders"
                }

                textValue {
                    attributes {
                        margins { all { 5.pt() } }
                        borders { all { dotted; 5.pt(); Colors.BLACK; } }
                    }
                    "Dotted borders 5pt"
                }

                textValue {
                    attributes {
                        margins { all { 5.pt() } }
                        borders {
                            top { solid; 1.pt(); black }
                            left { double; 1.pt(); black }
                            right { dotted; 1.pt(); black }
                            bottom { dashed; 1.pt(); black }
                        }
                    }
                    "Mixed  borders 1pt"
                }

                textValue {
                    attributes {
                        margins { all { 5.pt() } }
                        borders {
                            top { solid; 2.pt(); black }
                            left { double; 2.pt(); black }
                            right { dotted; 2.pt(); black }
                            bottom { dashed; 2.pt(); black }
                        }
                    }
                    "Mixed  borders 2pt"
                }

                textValue {
                    attributes {
                        margins { all { 5.pt() } }
                        borders {
                            top { solid; 5.pt(); black }
                            left { double; 5.pt(); black }
                            right { dotted; 5.pt(); black }
                            bottom { dashed; 5.pt(); black }
                        }
                    }
                    "Mixed borders 5pt"
                }

                textValue {
                    attributes {
                        margins { all { 5.pt() } }
                        borders {
                            top { inset; 2.pt(); lightGray }
                            left { inset; 2.pt(); lightGray }
                            right { inset; 2.pt(); lightGray }
                            bottom { inset; 2.pt(); lightGray }
                        }
                    }
                    "Inset borders 5pt"
                }

                textValue {
                    attributes {
                        margins { all { 5.pt() } }
                        borders {
                            top { outset; 5.pt(); lightGray }
                            left { outset; 5.pt(); lightGray }
                            right { outset; 5.pt(); lightGray }
                            bottom { outset; 5.pt(); lightGray }
                        }
                    }
                    "Inset borders 5pt"
                }

                section { "TABLE BORDER STYLES" }

                table {
                    attributes {
                        margins { all { 5.pt() } }
                        columnWidth { 80.pt() }
                        width { 100.percents() }
                        rowHeight { 20.pt() }
                    }
                    columns {
                        column(0) {}
                        column(1) {}
                        column(2) {}
                        column(3) {}
                    }
                    rows {
                        newRow {
                            attributes {
                                rowBorders {
                                    all {
                                        style = DefaultBorderStyle.DOUBLE
                                        2.pt()
                                        color = Colors.LIGHT_GRAY
                                    }
                                }
                                alignment { left }
                            }
                        }
                        newRow(2) {
                            attributes {
                                rowBorders {
                                    left { double; 2.pt(); lightGray }
                                    right { double; 3.pt(); lightGray }
                                    top { double; 4.pt(); lightGray }
                                    bottom { double; 1.pt(); lightGray }
                                }
                                alignment { left }
                            }
                        }
                        newRow(4) {
                            attributes {
                                rowBorders {
                                    left { double; 2.pt(); lightGray }
                                    right { double; 4.pt(); lightGray }
                                    top { double; 2.pt(); lightGray }
                                    bottom { double; 4.pt(); lightGray }
                                }
                                alignment { left }
                            }
                        }
                        newRow(6) {
                            attributes {
                                rowBorders {
                                    top { double; 2.pt(); lightGray }
                                    bottom { double; 4.pt(); lightGray }
                                }
                                alignment { left }
                            }
                        }
                        newRow(8) {
                            attributes {
                                rowBorders {
                                    left { double; 2.pt(); lightGray }
                                    right { double; 4.pt(); lightGray }
                                    bottom { solid; 0.5.pt(); lightGray }
                                }
                                alignment { left }
                            }
                        }
                        newRow(10) {
                            attributes {
                                rowBorders {
                                    left { double; 2.pt(); lightGray }
                                    right { double; 4.pt(); lightGray }
                                    top { double; 4.pt(); lightGray }
                                }
                                alignment { left }
                            }
                        }
                        newRow(12) {
                            attributes {
                                rowBorders {
                                    all {
                                        style = DefaultBorderStyle.INSET
                                        2.pt()
                                        color = Colors.LIGHT_GRAY
                                    }
                                }
                                alignment { left }
                            }
                        }
                        newRow(14) {
                            attributes {
                                rowBorders {
                                    all {
                                        style = DefaultBorderStyle.OUTSET
                                        2.pt()
                                        color = Colors.LIGHT_GRAY
                                    }
                                }
                                alignment { left }
                            }
                        }
                        newRow(16) {
                            attributes {
                                rowBorders {
                                    all {
                                        style = DefaultBorderStyle.GROOVE
                                        2.pt()
                                        color = Colors.LIGHT_GRAY
                                    }
                                }
                                alignment { left }
                            }
                        }
                        newRow(18) {
                            attributes {
                                rowBorders {
                                    left { groove; 2.pt(); lightGray }
                                    right { groove; 6.pt(); lightGray }
                                    top { groove; 2.pt(); lightGray }
                                    bottom { groove; 6.pt(); lightGray }
                                }
                                alignment { left }
                            }
                        }
                        newRow(20) {
                            attributes {
                                rowBorders {
                                    all {
                                        style = DefaultBorderStyle.DOTTED
                                        2.pt()
                                        color = Colors.LIGHT_GRAY
                                    }
                                }
                                alignment { left }
                            }
                        }
                        newRow(22) {
                            attributes {
                                rowBorders {
                                    all {
                                        style = DefaultBorderStyle.DASHED
                                        2.pt()
                                        color = Colors.LIGHT_GRAY
                                    }
                                }
                                alignment { left }
                            }
                        }
                    }
                }
            }
        }.export("borders_configurations.pdf")
    }
}
