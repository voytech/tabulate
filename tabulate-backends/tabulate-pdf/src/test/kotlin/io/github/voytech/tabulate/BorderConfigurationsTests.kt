package io.github.voytech.tabulate

import io.github.voytech.tabulate.components.document.api.builder.dsl.document
import io.github.voytech.tabulate.components.document.template.export
import io.github.voytech.tabulate.components.image.api.builder.dsl.*
import io.github.voytech.tabulate.components.page.api.builder.dsl.page
import io.github.voytech.tabulate.components.table.api.builder.dsl.*
import io.github.voytech.tabulate.components.text.api.builder.dsl.*
import io.github.voytech.tabulate.core.model.alignment.DefaultHorizontalAlignment
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
                                    leftBorderStyle = DefaultBorderStyle.DOUBLE
                                    leftBorderWidth = 2.pt()
                                    leftBorderColor = Colors.LIGHT_GRAY
                                    rightBorderStyle = DefaultBorderStyle.DOUBLE
                                    rightBorderWidth = 3.pt()
                                    rightBorderColor = Colors.LIGHT_GRAY
                                    topBorderStyle = DefaultBorderStyle.DOUBLE
                                    topBorderWidth = 4.pt()
                                    topBorderColor = Colors.LIGHT_GRAY
                                    bottomBorderStyle = DefaultBorderStyle.DOUBLE
                                    bottomBorderWidth = 1.pt()
                                    bottomBorderColor = Colors.LIGHT_GRAY
                                }
                                alignment { left }
                            }
                        }
                        newRow(4) {
                            attributes {
                                rowBorders {
                                    leftBorderStyle = DefaultBorderStyle.DOUBLE
                                    leftBorderWidth = 2.pt()
                                    leftBorderColor = Colors.LIGHT_GRAY
                                    rightBorderStyle = DefaultBorderStyle.DOUBLE
                                    rightBorderWidth = 4.pt()
                                    rightBorderColor = Colors.LIGHT_GRAY
                                    topBorderStyle = DefaultBorderStyle.DOUBLE
                                    topBorderWidth = 2.pt()
                                    topBorderColor = Colors.LIGHT_GRAY
                                    bottomBorderStyle = DefaultBorderStyle.DOUBLE
                                    bottomBorderWidth = 4.pt()
                                    bottomBorderColor = Colors.LIGHT_GRAY
                                }
                                alignment { left }
                            }
                        }
                        newRow(6) {
                            attributes {
                                rowBorders {
                                    topBorderStyle = DefaultBorderStyle.DOUBLE
                                    topBorderWidth = 2.pt()
                                    topBorderColor = Colors.LIGHT_GRAY
                                    bottomBorderStyle = DefaultBorderStyle.DOUBLE
                                    bottomBorderWidth = 4.pt()
                                    bottomBorderColor = Colors.LIGHT_GRAY
                                }
                                alignment { left }
                            }
                        }
                        newRow(8) {
                            attributes {
                                rowBorders {
                                    leftBorderStyle = DefaultBorderStyle.DOUBLE
                                    leftBorderWidth = 2.pt()
                                    leftBorderColor = Colors.LIGHT_GRAY
                                    rightBorderStyle = DefaultBorderStyle.DOUBLE
                                    rightBorderWidth = 4.pt()
                                    rightBorderColor = Colors.LIGHT_GRAY
                                    bottomBorderStyle = DefaultBorderStyle.SOLID
                                    bottomBorderWidth = 0.5.pt()
                                    bottomBorderColor = Colors.LIGHT_GRAY
                                }
                                alignment { left }
                            }
                        }
                        newRow(10) {
                            attributes {
                                rowBorders {
                                    leftBorderStyle = DefaultBorderStyle.DOUBLE
                                    leftBorderWidth = 2.pt()
                                    leftBorderColor = Colors.LIGHT_GRAY
                                    rightBorderStyle = DefaultBorderStyle.DOUBLE
                                    rightBorderWidth = 4.pt()
                                    rightBorderColor = Colors.LIGHT_GRAY
                                    topBorderStyle = DefaultBorderStyle.DOUBLE
                                    topBorderWidth = 4.pt()
                                    topBorderColor = Colors.LIGHT_GRAY
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
                                    leftBorderStyle = DefaultBorderStyle.GROOVE
                                    leftBorderWidth = 2.pt()
                                    leftBorderColor = Colors.LIGHT_GRAY
                                    rightBorderStyle = DefaultBorderStyle.GROOVE
                                    rightBorderWidth = 6.pt()
                                    rightBorderColor = Colors.LIGHT_GRAY
                                    topBorderStyle = DefaultBorderStyle.GROOVE
                                    topBorderWidth = 2.pt()
                                    topBorderColor = Colors.LIGHT_GRAY
                                    bottomBorderStyle = DefaultBorderStyle.GROOVE
                                    bottomBorderWidth = 6.pt()
                                    bottomBorderColor = Colors.LIGHT_GRAY
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
