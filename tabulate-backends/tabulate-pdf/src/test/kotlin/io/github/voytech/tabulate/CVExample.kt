package io.github.voytech.tabulate

import io.github.voytech.tabulate.components.container.api.builder.dsl.*
import io.github.voytech.tabulate.components.document.api.builder.dsl.document
import io.github.voytech.tabulate.components.document.template.export
import io.github.voytech.tabulate.components.image.api.builder.dsl.*
import io.github.voytech.tabulate.components.page.api.builder.dsl.page
import io.github.voytech.tabulate.components.page.model.PageExecutionContext
import io.github.voytech.tabulate.components.table.api.builder.dsl.*
import io.github.voytech.tabulate.components.text.api.builder.dsl.*
import io.github.voytech.tabulate.components.wrapper.api.builder.dsl.align
import io.github.voytech.tabulate.core.model.border.DefaultBorderStyle
import io.github.voytech.tabulate.core.model.color.Colors
import io.github.voytech.tabulate.core.model.text.DefaultWeightStyle
import io.github.voytech.tabulate.test.sampledata.SampleCustomer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.awt.Color
import java.io.File
import java.time.LocalDate

@DisplayName("CV Generator test.")
class CVExample {

    private fun ContainerBuilderApi.textList(type: String, vararg skills: String) {
        vertical {
            attributes { margins { top { 20.pt() } } }
            text {
                value = type
                attributes {
                    margins { left { 5.pt() }; top { 5.pt() } }
                    text { courier; black; bold; fontSize = 14; breakLines }
                }
            }
            vertical {
                attributes {
                    margins { left { 5.pt() }; top { 5.pt() } }
                }
                skills.forEach {
                    text {
                        attributes { text { courier; black }; margins { top { 5.pt() } } }
                        value = it
                    }
                }
            }
        }
    }

    private fun ContainerBuilderApi.contact(firstName: String, lastName: String, email: String, phone: String) {
        vertical {
            text {
                attributes { text { arialBlack; fontSize = 24; breakWords } }
                value = "$firstName $lastName"
            }
            text {
                attributes {
                    text { fontSize = 12; arialBlack; lightGray; breakWords }
                }
                value = "email: $email"
            }
            text {
                attributes {
                    text { fontSize = 12; arialBlack; lightGray; breakWords }
                }
                value = "tel: $phone"
            }
        }
    }

    private fun ContainerBuilderApi.job(from: LocalDate, to: LocalDate, role: String, customer: String) {
        vertical {
            attributes {
                margins { top { 10.pt() }; left { 10.pt()} }
                width { 100.percents() }
                borders {
                    left { solid; lightGray; 4.pt() }
                }
            }
            vertical {
                attributes { margins { left { 10.pt() } } }
                text {
                    attributes {
                        text { arialBlack; white; bold; fontSize = 18; }
                        borders { all { 2.pt(); black } }
                        background { black }
                    }
                    value = customer
                }
                text {
                    attributes {
                        margins { top { 5.pt() } }
                        borders { all { 2.pt(); lightGray } }
                        text { arialBlack; lightGray; bold; fontSize = 16 }
                    }
                    value = "$from - $to"
                }
                text {
                    attributes {
                        margins { top { 5.pt() } }
                        borders { all { 2.pt(); lightGray } }
                        text { arialBlack; black; bold; fontSize = 14; underline }
                    }
                    value = role
                }
            }
        }
    }


    @Test
    fun `should export simple CV pdf`() {
        document {
            page {
                align { center; fullWidth; top } horizontal {
                    align { left; width25 } vertical {
                        attributes {
                            width { 100.percents() }
                            height { 100.percents() }
                            background { lightGray }
                            borders { all { red; 20.pt(); solid } }
                        }
                        vertical {
                            textList("Programming Languages:", "Java", "Kotlin", "Typescript", "Python", "JavaScript")
                            textList("Frameworks:", "Spring Boot", "Spring", "Angular", "React", "Backbone.js")
                        }
                    }
                    align { right; width75 } vertical {
                        attributes {
                            alignment { center; top }
                            background { white }
                            width { 100.percents() }
                            height { 100.percents() }
                            borders { all { green; 20.pt(); solid } }
                        }
                        horizontal {
                            attributes {
                                width { 100.percents() }
                                height { 150.pt() }
                                borders { all { blue; 15.pt(); solid } }
                            }
                            align { left; middle; halfWidth; fullHeight } vertical {
                                attributes { borders { all { lightGray; 10.pt(); solid } } }
                                contact("Firstname", "Lastname", "firstname.lastname@gmail.com", "600 600 600")
                            }
                            align { right; halfWidth } vertical {
                                attributes {
                                    width { 150.pt() }
                                    height { 150.pt() }
                                    borders { all { red; 10.pt(); solid } }
                                }
                               /* image {
                                    filePath = "src/test/resources/kotlin.jpeg"
                                    attributes {
                                        width { 150.pt() }
                                        height { 150.pt() }
                                        borders { all { 3.pt(); double; lightGray } }
                                    }
                                }*/
                            }
                        }
                        job(LocalDate.of(2016,10,1),LocalDate.of(2017,10,1),"Senior Java Developer", "A SuperHero Company.")
                        job(LocalDate.of(2015,10,1),LocalDate.of(2016,10,1),"Senior Java Developer", "Briefings and lore")
                        job(LocalDate.of(2014,10,1),LocalDate.of(2015,10,1),"Java Developer", "STEM group")
                        job(LocalDate.of(2013,10,1),LocalDate.of(2014,10,1),"Java Developer", "Constant Creation Curations")
                        job(LocalDate.of(2012,10,1),LocalDate.of(2013,10,1),"Java Developer", "Client Of Youth Margins.")
                    }
                }
                /*footer {
                    text {
                        attributes {
                            text { black }
                            width { 100.percents() }
                            alignment { center }
                            borders { all { lightGray; dotted; 1.pt() } }
                        }
                        value<PageExecutionContext> { "Page : ${it.pageNumber}" }
                    }
                }*/
            }
        }.export(File("cv.pdf"))
    }


}
