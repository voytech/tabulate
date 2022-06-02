package io.github.voytech.tabulate.core.template

import io.github.voytech.tabulate.core.model.Model
import io.github.voytech.tabulate.core.template.spi.*
import java.util.*

/**
 * Top level method that loads instance of [Identifiable] factory through ServiceLoader infrastructure.
 * It uses [DocumentFormat] to determine factory matching specific [RenderingContext]
 * @author Wojciech Mąka
 * @since 0.*.*
 */
inline fun <reified E : Identifiable<CTX>, CTX : RenderingContext> loadFirstByDocumentFormat(format: DocumentFormat): E? =
    ServiceLoader.load(E::class.java)
        .filterIsInstance<E>().find {
            if (format.provider.isNullOrBlank()) {
                format.id == it.getDocumentFormat().id
            } else {
                format.provider == it.getDocumentFormat().provider.providerId
            }
        }

/**
 * Top level method that loads many instances of [Identifiable] factory through ServiceLoader infrastructure.
 * It uses [DocumentFormat] to determine factory matching specific [RenderingContext]
 * @author Wojciech Mąka
 * @since 0.*.*
 * */
inline fun <reified E : Identifiable<CTX>, CTX : RenderingContext> loadAllByDocumentFormat(format: DocumentFormat): List<E> =
    ServiceLoader.load(E::class.java)
        .filter {
            if (format.provider.isNullOrBlank()) {
                format.id == it.getDocumentFormat().id
            } else {
                format.provider == it.getDocumentFormat().provider.providerId
            }
        }

/**
 * Top level method that loads many instances of providers that can match specific rendering context through ServiceLoader infrastructure.
 * @author Wojciech Mąka
 * @since 0.*.*
 */
inline fun <reified E : RenderingContextAware<CTX>, CTX : RenderingContext> loadRenderingContextAware(
    renderingContext: Class<CTX>
): List<E> =
    ServiceLoader.load(E::class.java)
        .filter { renderingContext.isAssignableFrom(it.getRenderingContextClass()) }


/**
 * Top level method that loads instances of [ExportOperationProvider] associated by [DocumentFormat] compatible [RenderingContext] class.
 * @author Wojciech Mąka
 * @since 0.*.*
 */
internal fun <R: RenderingContext> loadExportOperationFactories(format: DocumentFormat): DiscoveredExportOperationFactories<R> =
    loadAllByDocumentFormat<ExportOperationsProvider<R, *>, R>(format).associateBy {
        it.getModelClass()
    }


/**
 * Top level method that loads instances of [AttributeOperationsFactory] compatible with particular [RenderingContext]
 * @author Wojciech Mąka
 * @since 0.*.*
 */
internal fun <R: RenderingContext, MDL: Model<MDL>> loadAttributeOperationFactories(renderingContext: Class<R>): List<AttributeOperationsProvider<R,MDL>> =
    loadRenderingContextAware(renderingContext)

/**
 * Top level method that loads instance of [ExportTemplateApis] that matches particular [DocumentFormat].
 * Registry contains all discoverable instances of [ExportTemplate] and all document format related instances of [ExportOperationsProvider]
 * @author Wojciech Mąka
 * @since 0.*.*
 */
internal fun <R: RenderingContext> loadRegistry(format: DocumentFormat): ExportTemplateApis<R> =
    ExportTemplateApis(
        operationsFactories = loadExportOperationFactories(format)
    )

/**
 * Top level method that loads instance of [RenderingContext] identified by particular [DocumentFormat].
 * @author Wojciech Mąka
 * @since 0.*.*
 */
internal fun loadRenderingContext(format: DocumentFormat): RenderingContext =
    loadFirstByDocumentFormat<ExportOperationsProvider<RenderingContext, *>, RenderingContext>(format)?.createRenderingContext()
        ?: error("cannot create rendering context")


