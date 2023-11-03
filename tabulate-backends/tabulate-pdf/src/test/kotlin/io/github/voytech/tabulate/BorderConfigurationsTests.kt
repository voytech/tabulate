package io.github.voytech.tabulate

import io.github.voytech.tabulate.components.document.api.builder.dsl.document
import io.github.voytech.tabulate.components.document.template.export
import io.github.voytech.tabulate.components.image.api.builder.dsl.*
import io.github.voytech.tabulate.components.page.api.builder.dsl.page
import io.github.voytech.tabulate.components.table.api.builder.dsl.*
import io.github.voytech.tabulate.components.text.api.builder.dsl.*
import io.github.voytech.tabulate.core.model.border.DefaultBorderStyle
import io.github.voytech.tabulate.core.model.color.Colors
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Testing various border styles")
class BorderConfigurationsTests {
    @Test
    fun `should correctly export border configurations`() {
        document {
            page {
                table {
                    attributes {
                        margins {
                            left { 5.pt() }
                            top { 5.pt() }
                        }
                        columnWidth { 80.pt() }
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
