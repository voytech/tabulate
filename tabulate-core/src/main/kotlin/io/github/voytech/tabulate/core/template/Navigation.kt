package io.github.voytech.tabulate.core.template

import io.github.voytech.tabulate.DefaultComplexIterator
import io.github.voytech.tabulate.ResettableComplexIterator
import io.github.voytech.tabulate.core.model.*
import io.github.voytech.tabulate.core.model.attributes.MarginsAttribute
import io.github.voytech.tabulate.core.template.layout.*

class Navigation(
    val root: AbstractModel<*>,
    val parent: AbstractModel<*>?,
    val active: AbstractModel<*>,
    private val children: MutableList<AbstractModel<*>> = mutableListOf(),
) {
    val parentContext: ModelExportContext? by lazy { parent?.context }

    fun addChild(child: AbstractModel<*>) {
        if (child != active && child != parent && child != root) {
            children += child
        }
    }

    fun lookupAncestors(predicate: (ModelExportContext) -> Boolean): ModelExportContext? {
        var tmp = parent
        while (tmp != null && !predicate(tmp.context)) {
            tmp = tmp.context.navigation.parent
        }
        return tmp?.context
    }

    fun onEachChild(block: (AbstractModel<*>) -> Unit) {
        children.forEach(block)
    }

    fun checkAnyChildren(block: (AbstractModel<*>) -> Boolean): Boolean = children.any {
        it.context.let { ctx -> block(ctx.navigation.active) || ctx.navigation.checkAnyChildren(block) }
    }

    fun traverse(block: (AbstractModel<*>) -> Unit) {
        block(active).also {
            onEachChild { it.context.navigation.traverse(block) }
        }
    }

    companion object {
        fun rootNavigation(root: AbstractModel<*>) = Navigation(root, null, root)
    }
}

data class LayoutScope(val layout: NavigableLayout, val policy: LayoutPolicy) {
    @Suppress("UNCHECKED_CAST")
    fun <P : LayoutPolicy> policy(): P = policy as P
}

class Layouts(
    private val policySupplier: () -> LayoutPolicy,
    private val queue: MutableList<LayoutScope> = mutableListOf(),
    private val iterators: ResettableComplexIterator<LayoutScope, ExportPhase> = DefaultComplexIterator(queue),
) {

    operator fun invoke(): ResettableComplexIterator<LayoutScope, ExportPhase> = iterators

    fun size(): Int = queue.size

    fun clear() {
        queue.clear()
        iterators.reset(ExportPhase.MEASURING)
        iterators.reset(ExportPhase.RENDERING)
    }

    fun lastScope(): LayoutScope? = queue.lastOrNull()

    fun last(): NavigableLayout? = lastScope()?.layout

    operator fun plusAssign(layout: NavigableLayout) {
        queue += LayoutScope(layout, policySupplier())
    }

}

class NavigableLayout(
    private val navi: Navigation,
    private val layout: DefaultLayout,
    private val orientation: Orientation,
) : Layout by layout {

    var onFinish: ((NavigableLayout) -> Unit)? = null

    private var rightBottom: Position by layout::rightBottom

    internal var nextLayoutLeftTop: Position? = null

    private fun getClosestAncestorLayout(): NavigableLayout? =
        navi.lookupAncestors { it.layouts.last() != null }?.layouts?.last()

    private fun extendParent(position: Position) {
        if (navi.parent != null) {
            getClosestAncestorLayout()?.extend(position)
        }
    }

    override fun extend(position: Position) {
        extendParent(position)
        layout.extend(position)
    }

    override fun extend(width: Width) = extend(Position(rightBottom.x + width, rightBottom.y))

    override fun extend(x: X) = extend(Position(x, rightBottom.y))

    override fun extend(height: Height) = extend(Position(rightBottom.x, rightBottom.y + height))

    override fun extend(y: Y) = extend(Position(rightBottom.x, y))

    override fun LayoutElementBoundingBox.applyOnLayout() {
        extend(absoluteX + width.orZero())
        extend(absoluteY + height.orZero())
    }

    internal fun finish() {
        onFinish?.invoke(this)
        if (navi.active != navi.root) {
            getClosestAncestorLayout()?.let { parentLayout ->
                parentLayout.nextLayoutLeftTop = if (parentLayout.orientation == Orientation.HORIZONTAL) {
                    Position(parentLayout.rightBottom.x + EPSILON, parentLayout.leftTop.y)
                } else {
                    Position(parentLayout.leftTop.x, parentLayout.rightBottom.y + EPSILON)
                }
            }
        }
    }
}

fun ModelExportContext.getClosestLayoutAwareAncestor(): ModelExportContext? =
    navigation.lookupAncestors { it.layouts.last() != null }

fun ModelExportContext.getClosestAncestorLayout(): NavigableLayout? =
    getClosestLayoutAwareAncestor()?.layouts?.last()

fun ModelExportContext.getEnclosingMaxRightBottom(): Position? =
    getClosestLayoutAwareAncestor()?.layouts?.last()?.maxRightBottom

fun ModelExportContext.getRenderingLayout(): NavigableLayout? = layouts.last()

internal fun ModelExportContext.createLayout(box: LayoutConstraints): LayoutScope =
    getClosestLayoutAwareAncestor().let { ancestor ->
        val wrapping = ancestor?.getRenderingLayout()
        box.copy(
            uom = wrapping?.uom ?: box.uom,
            orientation = box.orientation,
            leftTop = box.leftTop ?: wrapping?.nextLayoutLeftTop ?: wrapping?.leftTop ?: Position.start(box.uom),
            maxRightBottom = box.maxRightBottom ?: getEnclosingMaxRightBottom()
        ).let {
            DefaultLayout(
                uom = it.uom,
                leftTop = resolveMargins(it.leftTop ?: error("leftTop position is required for new layout!")),
                maxRightBottom = it.maxRightBottom
            ).let { layout ->
                layouts += (NavigableLayout(navigation, layout, it.orientation))
                layouts().next(phase)
            }
        }
    }


fun ModelExportContext.shouldCreateLayout(): Boolean =
    layouts.size() == 0 || (layouts().currentIndex(phase) + 1 == layouts.size())


fun <R> ModelExportContext.withinLayoutScope(
    constraints: LayoutConstraints, block: LayoutScope.() -> R,
): R = if (shouldCreateLayout()) {
    createLayout(constraints).run {
        block(this).also {
            layout.finish()
        }
    }
} else {
    layouts().next(phase).run {
        layout.collapse()
        block(this).also {
            layout.finish()
        }
    }
}

private fun ModelExportContext.resolveMargins(sourcePosition: Position): Position {
    val model = (navigation.active as? AttributedModelOrPart<*>)
    return model?.attributes?.get(MarginsAttribute::class.java)?.let {
        Position(it.left + sourcePosition.x, it.top + sourcePosition.y)
    } ?: sourcePosition
}