package io.github.voytech.tabulate.backends

import io.github.voytech.tabulate.components.container.api.builder.dsl.*
import io.github.voytech.tabulate.components.document.api.builder.dsl.document
import io.github.voytech.tabulate.components.document.api.builder.dsl.height
import io.github.voytech.tabulate.components.document.api.builder.dsl.width
import io.github.voytech.tabulate.components.document.template.export
import io.github.voytech.tabulate.components.image.api.builder.dsl.*
import io.github.voytech.tabulate.components.page.api.builder.dsl.page
import io.github.voytech.tabulate.components.page.model.PageExecutionContext
import io.github.voytech.tabulate.components.table.api.builder.dsl.*
import io.github.voytech.tabulate.components.table.model.RowCellExpression
import io.github.voytech.tabulate.components.table.model.attributes.cell.enums.DefaultTypeHints
import io.github.voytech.tabulate.components.table.template.AdditionalSteps
import io.github.voytech.tabulate.components.text.api.builder.dsl.*
import io.github.voytech.tabulate.core.InputParams
import io.github.voytech.tabulate.core.model.alignment.DefaultHorizontalAlignment
import io.github.voytech.tabulate.core.model.alignment.DefaultVerticalAlignment
import io.github.voytech.tabulate.core.model.color.Colors
import io.github.voytech.tabulate.core.model.text.DefaultWeightStyle
import io.github.voytech.tabulate.excel.setXlsxRowsCountInWindow
import io.github.voytech.tabulate.test.sampledata.SampleProduct
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import kotlin.reflect.KProperty1

@DisplayName("Regression testing all backends")
class CommonTests {

    @Test
    fun `should export document with tables next to each other`() {
        val doc = document {
            page {
                horizontal {
                    table {
                        dataSource(SampleProduct.create(15))
                        attributes {
                            borders { all { lightGray; 1.pt() } }
                        }
                        columns {
                            column(SampleProduct::code) {
                                attributes { text { bold } }
                            }
                            column(SampleProduct::name)
                        }
                        rows {
                            header {
                                columnTitles("code","name")
                                attributes {
                                    text {
                                        fontSize = 21
                                        italic = true
                                        bold
                                    }
                                }
                            }
                        }
                    }
                    table {
                        dataSource(SampleProduct.create(10))
                        attributes {
                            borders { all { black; 1.pt() } }
                        }
                        columns {
                            column(SampleProduct::code) { attributes { text { bold } } }
                            column(SampleProduct::name)
                        }
                        rows {
                            header("Id", "Name")
                        }
                    }
                }
            }
        }
        doc.export(File("two_tables.pdf"))
        //doc.export(File("two_tables.xlsx"))
    }

    @Test
    fun `should export document with different components arranged using layouts`() {
        val doc = document {
            page {
                vertical {
                    text {
                        value<PageExecutionContext> { "HEADER: the page number is : ${it.pageNumber} (should be sheet NAME)" }
                        attributes {
                            height { 75.pt() }
                            width { 600.pt() }
                            background { color = Colors.LIGHT_GRAY }
                            text {
                                courierNew; red; fontSize = 10; bold
                                fontSize = 10
                                italic = true
                                underline = true
                            }
                            alignment { middle; center }
                            borders {
                                all { red; solid; 2.pt() }
                            }
                        }
                    }
                    horizontal {
                        image {
                            filePath = "src/test/resources/kotlin.jpeg"
                            attributes {
                                width { 100.pt() }
                                height { 100.pt() }
                            }
                        }
                        image {
                            filePath = "src/test/resources/kotlin.jpeg"
                            attributes {
                                width { 100.pt() }
                                height { 100.pt() }
                            }
                        }
                    }
                    horizontal {
                        table {
                            dataSource(SampleProduct.create(15))
                            attributes {
                                borders { all { lightGray; 0.5F.pt() } }
                            }
                            columns {
                                column(SampleProduct::code) {
                                    attributes { text { bold } }
                                }
                                column(SampleProduct::name)
                                column("img") { attributes { width { 30.pt() } } }
                            }
                            rows {
                                header("Id", "Name", "Image")
                                matching { all() } assign { attributes { height { 30.pt() } } }
                                matching { gt(0) } assign {
                                    cell("img") {
                                        value = "src/test/resources/basketball.png"
                                        typeHint { DefaultTypeHints.IMAGE_URI }
                                    }
                                }
                            }
                        }
                        table {
                            dataSource(SampleProduct.create(10))
                            attributes {
                                borders { all { lightGray; 5.pt() } }
                            }
                            columns {
                                column(SampleProduct::code) { attributes { text { bold } } }
                                column(SampleProduct::name)
                            }
                            rows {
                                header("Id2", "Name2")
                            }
                        }
                    }
                    text {
                        value<PageExecutionContext> { "FOOTER: the page number is : ${it.pageNumber}" }
                        attributes {
                            height { 50.pt() }
                            width { 200.pt() }
                            alignment { vertical = DefaultVerticalAlignment.MIDDLE }
                            background { color = Colors.BLACK }
                            text {
                                color = Colors.WHITE
                                fontSize = 10
                                weight = DefaultWeightStyle.BOLD
                            }
                        }
                    }
                }
            }
        }
        //doc.export(File("test.xlsx"))
        doc.export(File("test.pdf"))
    }

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
                attributes { text { arialBlack; fontSize = 24; breakLines } }
                value = "$firstName $lastName"
            }
            text {
                attributes {
                    text { fontSize = 12; arialBlack; lightGray; breakLines }
                }
                value = "email: $email"
            }
            text {
                attributes {
                    text { fontSize = 12; arialBlack; lightGray; breakLines }
                }
                value = "tel: $phone"
            }
        }
    }

    private fun ContainerBuilderApi.job(from: LocalDate, to: LocalDate, role: String, company: String) {
        vertical {
            attributes {
                margins { top { 15.pt() }; }
            }
            vertical {
                text {
                    attributes {
                        text { arialBlack; white; bold; fontSize = 18; }
                        borders { all { 2.pt(); black } }
                        background { black }
                    }
                    value = company
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
    fun `example CV`() {
        val doc = document {
            page {
                horizontal {
                    attributes {
                        width { 900.pt() }
                    }
                    vertical {
                        textList("Programming Languages:", "Java", "Kotlin", "Typescript", "Python", "JavaScript")
                        textList("Frameworks:", "Spring Boot", "Spring", "Angular", "React", "Backbone.js")
                    }
                    vertical {
                        attributes { alignment { center; top } }
                        horizontal {
                            attributes {
                                width { 50.percents() }
                                height { 150.pt() }
                            }
                            vertical {
                                attributes { borders { all { lightGray; 1.pt(); solid } } }
                                contact("Firstname", "Lastname", "firstname.lastname@gmail.com", "600 600 600")
                            }
                            image {
                                filePath = "src/test/resources/kotlin.jpeg"
                                attributes {
                                    width { 150.pt() }
                                    height { 150.pt() }
                                    background { white }
                                    borders { all { lightGray; 1.pt(); solid } }
                                }
                            }
                        }
                        job(
                            LocalDate.of(2016, 10, 1),
                            LocalDate.of(2017, 10, 1),
                            "Senior Java Developer",
                            "Mamazon"
                        )
                        job(
                            LocalDate.of(2015, 10, 1),
                            LocalDate.of(2016, 10, 1),
                            "Senior Java Developer",
                            "Openhub"
                        )
                        job(
                            LocalDate.of(2014, 10, 1),
                            LocalDate.of(2015, 10, 1),
                            "Java Developer",
                            "My small business"
                        )
                        job(
                            LocalDate.of(2013, 10, 1),
                            LocalDate.of(2014, 10, 1),
                            "Java Developer",
                            "First Steps Company"
                        )
                        job(
                            LocalDate.of(2012, 10, 1),
                            LocalDate.of(2013, 10, 1),
                            "Java Developer",
                            "Alma mater"
                        )
                    }
                }
            }
        }
        //doc.export(File("cv.pdf"))
        doc.export(File("cv.xlsx"))
    }

    @Test
    fun `text measures`() {
        val doc = document {
            page {
                horizontal {
                    attributes {
                        width { 1000.pt() }
                        height { 700.pt() }
                    }
                    vertical {
                        text {
                            attributes {
                                text { arialBlack; white; bold; fontSize = 18; }
                                borders { all { 2.pt(); black } }
                                background { black }
                            }
                            value = "This is text at the top"
                        }
                        text {
                            attributes {
                                margins { top { 5.pt() } }
                                borders { all { 2.pt(); lightGray } }
                                text { arialBlack; lightGray; bold; fontSize = 16 }
                            }
                            value = "This is text on the second line"
                        }
                        text {
                            attributes {
                                margins { top { 5.pt() } }
                                borders { all { 2.pt(); lightGray } }
                                text { arialBlack; black; bold; fontSize = 14; underline }
                            }
                            value = "A third line text"
                        }
                        text {
                            value = "A text without any attributes"
                        }
                        text {
                            value = "Next line of text without any attributes"
                        }
                    }
                    vertical {
                        text {
                            attributes {
                                text { arialBlack; white; bold; fontSize = 18; }
                                borders { all { 2.pt(); black } }
                                background { black }
                            }
                            value = "This is text at the top. Second Row"
                        }
                        text {
                            attributes {
                                margins { top { 5.pt() } }
                                borders { all { 2.pt(); lightGray } }
                                text { arialBlack; lightGray; bold; fontSize = 16 }
                            }
                            value = "This is text on the second line. Second Row"
                        }
                        text {
                            attributes {
                                margins { top { 5.pt() } }
                                borders { all { 2.pt(); lightGray } }
                                text { arialBlack; black; bold; fontSize = 14; underline }
                            }
                            value = "A third line text"
                        }
                        text {
                            value = "A text without any attributes. Second Row"
                        }
                        text {
                            value = "Next line of text without any attributes. Second Row"
                        }
                    }
                    vertical {
                        attributes { width { 100.pt() } }
                        text {
                            value =
                                "A very very very long text with width 1.. and still writing.. and text wrapping by line breaking ... How long it will " +
                                        "take... how long. Please tell me! Write this text so long that it will cross the horizontal boundaries. Let us see " +
                                        "Still some text to write. A little longer words maybe ?. Almost done. I think now the text will finally stop rendering " +
                                        "due to crossing parent layout bounding box boundaries!"
                            attributes {
                                text { breakLines; courierNew; fontSize = 10 }
                                //alignment { justify }
                                borders { all { solid; red; 5.pt() } }
                            }
                        }
                    }
                }
            }
        }
        doc.export(File("text_measures.pdf"))
        doc.export(File("text_measures.xlsx"))
    }


    private fun <T : Any> RowBuilderApi<T>.dollarColumn(prop: KProperty1<T, Any?>) =
        cell(prop) {
            expression = RowCellExpression {
                "${(it.record?.let { obj -> (prop.get(obj) as BigDecimal).setScale(2, RoundingMode.HALF_UP) } ?: 0)} $"
            }
        }

    @Test
    fun multiplePages() {
        val doc = document {
            attributes {
                width { 612.pt() }
                height { 300.pt() }
            }
            page {
                header {
                    text {
                        value = "Some heading."
                        attributes {
                            width { 100.percents() }
                            height { 20.pt() }
                        }
                    }
                }
                footer {
                    text {
                        value<PageExecutionContext> { ctx -> "Page number: ${ctx.pageNumber}" }
                        attributes {
                            height { 30.pt() }
                            width { 100.percents() }
                        }
                    }
                }
                horizontal {
                    attributes { borders { all { lightGray; solid; 3.pt() } } }
                    table(typedTable<SampleProduct> {
                        //attributes { tableBorders { all { 2f.pt() } } }
                        columns {
                            column(SampleProduct::code) {
                                attributes {
                                    text { red; bold; courierNew }
                                    alignment { left; middle }
                                }
                            }
                            column(SampleProduct::name) {
                                attributes {
                                    //width { 100.pt() }
                                    alignment { center }
                                }
                            }
                            column(SampleProduct::description)
                            column(SampleProduct::price) {}
                        }
                        rows {
                            header("Id", "Name", "Description", "Price")
                            matching { gt(0) } assign { dollarColumn(SampleProduct::price) }
                            matching { odd() } assign { attributes { background { yellow } } }
                        }
                        dataSource(SampleProduct.create(152))
                    })
                }
            }
        }
        //doc.export("multiple_pages_plus_header_and_footer.xlsx",InputParams.params().setXlsxRowsCountInWindow(300))
        doc.export("multiple_pages_plus_header_and_footer.pdf")
    }
}