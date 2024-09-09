package io.github.voytech.tabulate

import io.github.voytech.tabulate.components.container.api.builder.dsl.*
import io.github.voytech.tabulate.components.document.api.builder.dsl.document
import io.github.voytech.tabulate.components.document.template.export
import io.github.voytech.tabulate.components.image.api.builder.dsl.*
import io.github.voytech.tabulate.components.page.api.builder.dsl.PageBuilderApi
import io.github.voytech.tabulate.components.page.api.builder.dsl.page
import io.github.voytech.tabulate.components.table.api.builder.dsl.*
import io.github.voytech.tabulate.components.text.api.builder.dsl.*
import io.github.voytech.tabulate.components.wrapper.api.builder.dsl.align
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate

@DisplayName("CV Generator test.")
class CVExample {

    class AvatarBuilder {
        lateinit var imagePath: String
        lateinit var email: String
        lateinit var phoneNumber: String
        lateinit var postalCode: String
        lateinit var city: String
        lateinit var street: String
        lateinit var country: String
    }

    private fun ContainerBuilderApi.avatar(builder: AvatarBuilder.() -> Unit) {
        AvatarBuilder().apply { builder() }.let {
            vertical {
                attributes { margins { all { 5.pt() } } }
                image {
                    filePath = it.imagePath
                    attributes {
                        width { 125.pt() }; height { 150.pt() }
                        borders { all { solid; 1.pt(); lightGray } }
                    }
                }
                textValue { "E-mail: ${it.email}" }
                textValue { "Phone: ${it.phoneNumber}" }
                textValue { "Country: ${it.country}" }
                textValue { "City: ${it.city}" }
                textValue { "Street: ${it.street}" }
                textValue { "Postal code: ${it.postalCode}" }
            }
        }
    }

    class ProfileBuilder {
        lateinit var firstName: String
        lateinit var lastName: String
        lateinit var description: String
    }

    private fun ContainerBuilderApi.profile(builder: ProfileBuilder.() -> Unit) {
        ProfileBuilder().apply { builder() }.let {
            vertical {
                attributes { margins { all { 5.pt() } } }
                textValue {
                    attributes {
                        text { fontSize = 38; bold }
                        margins { all { 5.pt() } }
                    }
                    it.firstName
                }
                textValue {
                    attributes {
                        text { fontSize = 38; bold }
                        margins { all { 5.pt() } }
                    }
                    it.lastName
                }
                vertical {
                    attributes {
                        background { lightGray }
                        borders { all { 1.pt() }; radius { 10.pt(); } }
                    }
                    textValue { attributes { text { bold; fontSize = 16 } }; "Profile" }
                    textValue {
                        attributes {
                            width { 100.percents() }
                            margins { all { 5.pt() } }
                            text { breakLines }
                            alignment { justify; top }
                        }
                        it.description
                    }
                }
            }
        }
    }

    class EmploymentEntryBuilder {
        lateinit var client: String
        lateinit var role: String
        lateinit var title: String
        lateinit var from: LocalDate
        lateinit var to: LocalDate
        lateinit var description: String
    }

    private fun ContainerBuilderApi.employmentEntry(builder: EmploymentEntryBuilder.() -> Unit) {
        EmploymentEntryBuilder().apply { builder() }.let {
            vertical {
                attributes {
                    background { lighterGray }
                    borders { all { 1.pt() }; radius { 10.pt(); } }
                }
                attributes { margins { all { 5.pt() } } }
                textValue {
                    attributes {
                        text { bold; fontSize = 12; white };
                        background { black }
                        margins { all { 2.pt() } }
                    }
                    it.client
                }
                textValue {
                    attributes {
                        text { bold; fontSize = 10 }
                        margins { all { 2.pt() } }
                    };
                    "${it.from} - ${it.to}"
                }
                textValue {
                    attributes {
                        margins { all { 5.pt() } }
                        text { breakLines }
                        alignment { justify; top }
                        //clip { disabled }
                        //overflow { retry }
                    }
                    it.description
                }
            }
        }
    }

    private fun PageBuilderApi.divider() =
        content {
            attributes {
                margins { top { 2.pt() } }
                width { 100.percents() }
                borders { bottom { 1.pt(); solid; lightGray } }
            }
        }

    @Test
    fun `should export CV template pdf`() {
        document {
            page {
                align { left; top; fullWidth; } horizontal {
                    avatar {
                        imagePath = "src/test/resources/kotlin.jpeg"
                        email = "email@email.com"
                        phoneNumber = "600-600-600"
                        city = "City"
                        country = "Country"
                        street = "Street "
                        postalCode = "00-000"
                    }
                    profile {
                        firstName = "First name"
                        lastName = "Last name"
                        description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                                "Donec molestie blandit pretium. Fusce tincidunt urna vel dignissim gravida. " +
                                "Nunc sit amet lacinia sem, non lobortis sapien. " +
                                "Proin suscipit, odio quis aliquam facilisis, ex augue eleifend leo, in tristique libero nisl in tortor. " +
                                "Donec vel sollicitudin massa, quis malesuada justo. " +
                                "In non leo scelerisque justo feugiat sagittis at tristique quam. Duis ornare sed leo in dignissim. " +
                                "Aenean eget sodales magna, eleifend aliquet purus.\n" +
                                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                                "Donec molestie blandit pretium. Fusce tincidunt urna vel dignissim gravida. " +
                                "Nunc sit amet lacinia sem, non lobortis sapien. " +
                                "Proin suscipit, odio quis aliquam facilisis, ex augue eleifend leo, in tristique libero nisl in tortor. " +
                                "Donec vel sollicitudin massa, quis malesuada justo. " +
                                "In non leo scelerisque justo feugiat sagittis at tristique quam. Duis ornare sed leo in dignissim. " +
                                "Aenean eget sodales magna, eleifend aliquet purus.\n"
                    }
                }
                divider()
                horizontal {
                    attributes { height { 100.percents() } }
                    align { left; top; width75; } vertical {
                        attributes {
                            borders { right { lightGray; 1.pt(); solid } }
                            width { 100.percents() }
                            height { 100.percents() }

                        }
                        textValue { attributes { text { bold; fontSize = 16 } };"Employment History" }
                        employmentEntry {
                            client = "Client 1"
                            from = LocalDate.now()
                            to = LocalDate.now()
                            role = "Fullstack development"
                            title = "Senior Java Programmer"
                            description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                                    "Donec molestie blandit pretium. Fusce tincidunt urna vel dignissim gravida. " +
                                    "Nunc sit amet lacinia sem, non lobortis sapien. " +
                                    "Proin suscipit, odio quis aliquam facilisis, ex augue eleifend leo, in tristique libero nisl in tortor. " +
                                    "Donec vel sollicitudin massa, quis malesuada justo. " +
                                    "In non leo scelerisque justo feugiat sagittis at tristique quam. Duis ornare sed leo in dignissim. " +
                                    "Aenean eget sodales magna, eleifend aliquet purus.\n"
                        }
                        employmentEntry {
                            client = "Client 2. Let make this client name a bit longer"
                            from = LocalDate.now()
                            to = LocalDate.now()
                            role = "Fullstack development"
                            title = "Senior Java Programmer"
                            description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                                    "Donec molestie blandit pretium. Fusce tincidunt urna vel dignissim gravida. " +
                                    "Nunc sit amet lacinia sem, non lobortis sapien. " +
                                    "Proin suscipit, odio quis aliquam facilisis, ex augue eleifend leo, in tristique libero nisl in tortor. " +
                                    "Donec vel sollicitudin massa, quis malesuada justo. " +
                                    "In non leo scelerisque justo feugiat sagittis at tristique quam. Duis ornare sed leo in dignissim. " +
                                    "Aenean eget sodales magna, eleifend aliquet purus.\n"
                        }
                        employmentEntry {
                            client = "Client 3. Lesser description"
                            from = LocalDate.now()
                            to = LocalDate.now()
                            role = "Fullstack development"
                            title = "Senior Java Programmer"
                            description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                                    "Donec molestie blandit pretium. Fusce tincidunt urna vel dignissim gravida. " +
                                    "Nunc sit amet lacinia sem, non lobortis sapien. "
                        }
                        employmentEntry {
                            client = "Client 3. very long description"
                            from = LocalDate.now()
                            to = LocalDate.now()
                            role = "Fullstack development"
                            title = "Senior Java Programmer"
                            description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                                    "Donec molestie blandit pretium. Fusce tincidunt urna vel dignissim gravida. " +
                                    "Nunc sit amet lacinia sem, non lobortis sapien. " +
                                    "Proin suscipit, odio quis aliquam facilisis, ex augue eleifend leo, in tristique libero nisl in tortor. " +
                                    "Donec vel sollicitudin massa, quis malesuada justo. " +
                                    "In non leo scelerisque justo feugiat sagittis at tristique quam. Duis ornare sed leo in dignissim. " +
                                    "Aenean eget sodales magna, eleifend aliquet purus.\n" +
                                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit." +
                                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit." +
                                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit." +
                                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit." +
                                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit." +
                                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit." +
                                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit." +
                                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit." +
                                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit." +
                                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit." +
                                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit." +
                                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit." +
                                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit." +
                                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit." +
                                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit." +
                                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit."

                        }
                    }
                    align { right; top; width25 } vertical {
                        attributes {
                            margins { all { 5.pt() } }
                            width { 100.percents() }
                            height { 100.percents() }
                            background { lightGray }
                            borders { all { 1.pt() }; radius { 10.pt(); } }
                        }
                        textValue { attributes { text { bold; fontSize = 16 } };"Skills" }
                    }
                }
                divider()
            }
        }.export(File("cv_template.pdf"))
    }


}
