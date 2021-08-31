package io.github.voytech.tabulate.reactor.template

import io.github.voytech.tabulate.reactor.data.Product
import io.github.voytech.tabulate.reactor.data.Products
import io.github.voytech.tabulate.template.TabulationFormat
import io.github.voytech.tabulate.testsupport.AttributedCellTest
import io.github.voytech.tabulate.testsupport.TestExportOperationsFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.*
import reactor.core.publisher.Flux
import java.io.File
import java.math.BigDecimal
import java.time.LocalDate
import java.util.stream.Stream

class FluxTemplateTest {

    @TestTemplate
    @ExtendWith(FluxTestTemplateInvocationContextProvider::class)
    fun `should tabulate records emitted from Flux publisher`(tabulateProducts: Flux<Product>) {
        TestExportOperationsFactory.test = AttributedCellTest { attributedCell ->
            Assertions.assertNotNull(attributedCell)
            when (attributedCell.rowIndex) {
                0 -> {
                    when (attributedCell.columnIndex) {
                        0 -> assertEquals(attributedCell.value.value, "camera")
                        1 -> assertEquals(attributedCell.value.value, "Sony Film")
                        2 -> assertEquals(attributedCell.value.value, "An excellent camera for non-professional usage")
                        3 -> assertEquals(attributedCell.value.value, "Sony")
                        4 -> assertTrue(attributedCell.value.value is LocalDate)
                        5 -> assertEquals(attributedCell.value.value, BigDecimal(200.00))
                    }
                }
            }
        }
        tabulateProducts.log().blockLast()
    }


    class FluxTestTemplateInvocationContextProvider : TestTemplateInvocationContextProvider {

        override fun supportsTestTemplate(context: ExtensionContext): Boolean = true

        override fun provideTestTemplateInvocationContexts(context: ExtensionContext): Stream<TestTemplateInvocationContext> =
            Stream.of(
                invocation(ExplicitTabulationFormat()),
                invocation(FileProbedTabulationFormat()),
                invocation(FileNameProbedTabulationFormat()),
            )

        private fun invocation(resolver: ParameterResolver) = object : TestTemplateInvocationContext {
            override fun getAdditionalExtensions(): MutableList<Extension> =
                mutableListOf(resolver)
        }

        class ExplicitTabulationFormat : ParameterResolver {
            override fun supportsParameter(
                parameterContext: ParameterContext,
                extensionContext: ExtensionContext,
            ): Boolean = true

            override fun resolveParameter(
                parameterContext: ParameterContext,
                extensionContext: ExtensionContext,
            ): Any = Products.CAMERAS.log().tabulate(TabulationFormat("test"), Unit) {
                name = "Products table"
                columns {
                    column(Product::code)
                    column(Product::name)
                    column(Product::description)
                    column(Product::manufacturer)
                }
            }
        }

        class FileProbedTabulationFormat : ParameterResolver {
            override fun supportsParameter(
                parameterContext: ParameterContext, extensionContext: ExtensionContext,
            ): Boolean = true

            override fun resolveParameter(
                parameterContext: ParameterContext,
                extensionContext: ExtensionContext,
            ): Any = Products.CAMERAS.log().tabulate(File("file.test")) {
                name = "Products table"
                columns {
                    column(Product::code)
                    column(Product::name)
                    column(Product::description)
                    column(Product::manufacturer)
                }
            }
        }

        class FileNameProbedTabulationFormat : ParameterResolver {
            override fun supportsParameter(
                parameterContext: ParameterContext, extensionContext: ExtensionContext,
            ): Boolean = true

            override fun resolveParameter(
                parameterContext: ParameterContext,
                extensionContext: ExtensionContext,
            ): Any = Products.CAMERAS.log().tabulate("file.test") {
                name = "Products table"
                columns {
                    column(Product::code)
                    column(Product::name)
                    column(Product::description)
                    column(Product::manufacturer)
                }
            }
        }

    }

}

