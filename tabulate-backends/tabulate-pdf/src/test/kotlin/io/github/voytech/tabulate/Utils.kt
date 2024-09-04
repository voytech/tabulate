package io.github.voytech.tabulate

import io.github.voytech.tabulate.components.image.api.builder.dsl.ImageAttributesBuilderApi
import io.github.voytech.tabulate.components.image.api.builder.dsl.background
import io.github.voytech.tabulate.components.image.api.builder.dsl.borders
import io.github.voytech.tabulate.components.table.api.builder.dsl.*
import io.github.voytech.tabulate.components.table.model.RowCellExpression
import io.github.voytech.tabulate.components.text.api.builder.dsl.*
import io.github.voytech.tabulate.test.sampledata.SampleCustomer
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.reflect.KProperty1

object Utils {
    fun sampleCustomersTable(
        data: List<SampleCustomer>,
        vararg props: KProperty1<SampleCustomer, *>
    ): (TableBuilderApi<SampleCustomer>.() -> Unit) = typedTable {
        attributes {
            margins { all { 2.pt()} }
            borders { bottom { 0.5.pt(); solid } }
            tableBorders { all { 3.pt();solid; black } }
            text { breakWords; black }
        }
        if (props.isNotEmpty()) {
            columns { props.forEach { column(it) } }
        } else {
            columns {
                column(SampleCustomer::firstName)
                column(SampleCustomer::lastName)
                column(SampleCustomer::country)
                column(SampleCustomer::city)
                column(SampleCustomer::street)
                column(SampleCustomer::houseNumber)
                column(SampleCustomer::flat)
            }
        }
        if (props.isEmpty()) {
            rows {
                header {
                    columnTitles("First Name", "Last Name", "Country", "City", "Street", "House Nr", "Flat Nr")
                    attributes { text { bold; black; fontSize = 12 } }
                }
            }
        }
        dataSource(data)
    }


    fun <T : Any> RowBuilderApi<T>.dollarColumn(prop: KProperty1<T, Any?>) =
        cell(prop) {
            expression = RowCellExpression {
                "${(it.record?.let { obj -> (prop.get(obj) as BigDecimal).setScale(2, RoundingMode.HALF_UP) } ?: 0)} $"
            }
        }

    val tableHeaderStyle = table {
        rows {
            header {
                attributes {
                    text { white; italic = true; bold }
                    background { black }
                    rowBorders {// TODO - simplify border API. Add builder methods like 'horizontalBorders', 'verticalBorders', 'allBorders'
                        left { red; solid; 1f.pt() }
                        top { red; solid; 1f.pt() }
                        right { red; solid; 1f.pt() }
                        bottom { red; solid; 1f.pt() }
                    }
                }
            }
        }
    }


    val textBoxStyle: TextAttributesBuilderApi.() -> Unit = {
        height { 20.pt() }
        text { courierNew }
        alignment { center; middle }
        background { lightGray; solid }
        borders {
            all { double; 3.pt(); black }
        }
    }

    val imageStyles: ImageAttributesBuilderApi.() -> Unit = {
        background { lightGray; solid }
        borders {
            all { solid; 2.pt(); lightGray }
        }
    }
}