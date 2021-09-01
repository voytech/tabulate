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
                        0 -> assertEquals(attributedCell.value.value, "code1")
                        1 -> assertEquals(attributedCell.value.value, "name1")
                        2 -> assertEquals(attributedCell.value.value, "description1")
                        3 -> assertEquals(attributedCell.value.value, "manufacturer1")
                        4 -> assertTrue(attributedCell.value.value is LocalDate)
                        5 -> assertEquals(attributedCell.value.value, BigDecimal(1000.00))
                    }
                }
            }
        }
        tabulateProducts.blockLast().also {
            File("file.test").delete()
        }
    }

    class FluxTestTemplateInvocationContextProvider : TestTemplateInvocationContextProvider {

        override fun supportsTestTemplate(context: ExtensionContext): Boolean = true

        override fun provideTestTemplateInvocationContexts(context: ExtensionContext): Stream<TestTemplateInvocationContext> =
            Stream.of(
                invocation(ExplicitTabulationFormat(), "tabulate(TabulationFormat(\"test\"), Unit)"),
                invocation(FileProbedTabulationFormat(), "tabulate(File(\"file.test\"))"),
                invocation(FileNameProbedTabulationFormat(), "tabulate(\"file.test\")"),
            )

        private fun invocation(resolver: ParameterResolver, displayName: String = "") =
            object : TestTemplateInvocationContext {
                override fun getDisplayName(invocationIndex: Int): String = displayName
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
            ): Any = Products.ITEMS.log().tabulate(TabulationFormat("test"), Unit) {
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
            ): Any = Products.ITEMS.log().tabulate(File("file.test")) {
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
            ): Any = Products.ITEMS.log().tabulate("file.test") {
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

