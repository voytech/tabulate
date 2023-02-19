package io.github.voytech.tabulate.core.template

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

    fun traverse(block: (AbstractModel<*>) -> Unit) {
        block(active).also {
            onEachChild { it.context.navigation.traverse(block) }
        }
    }

    companion object {
        fun rootNavigation(root: AbstractModel<*>) = Navigation(root, null, root)
    }
}

class Layouts(val layoutPolicy: LayoutPolicy) {
    var renderingLayout: NavigableLayout? = null
    var measuringLayout: DefaultLayout? = null

    fun drop() {
        renderingLayout = null
    }

    companion object {
        fun rootLayouts() = Layouts(DefaultLayoutPolicy())
    }
}

class NavigableLayout(
    private val navi: Navigation,
    private val layout: DefaultLayout,
    private val orientation: Orientation,
) : Layout by layout {

    private var rightBottom: Position by layout::rightBottom

    internal var nextLayoutLeftTop: Position? = null

    private fun getClosestAncestorLayout(): NavigableLayout? =
        navi.lookupAncestors { it.layouts.renderingLayout != null }?.layouts?.renderingLayout

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
    navigation.lookupAncestors { it.layouts.renderingLayout != null }

fun ModelExportContext.getClosestAncestorLayout(): Layout? = getClosestLayoutAwareAncestor()?.layouts?.renderingLayout

fun ModelExportContext.getEnclosingMaxRightBottom(): Position? =
    getClosestLayoutAwareAncestor()?.layouts?.renderingLayout?.maxRightBottom

fun ModelExportContext.getRenderingLayout(): NavigableLayout? = layouts.renderingLayout

fun <R> ModelExportContext.setMeasuringLayout(uom: UnitsOfMeasure, block: (DefaultLayout) -> R): R =
    DefaultLayout(uom, Position.start(uom), getClosestLayoutAwareAncestor()?.getRenderingLayout()?.maxRightBottom).let {
        layouts.measuringLayout = it
        block(it)
    }

internal fun ModelExportContext.setLayout(
    uom: UnitsOfMeasure = UnitsOfMeasure.PT,
    leftTop: Position? = null,
    maxRightBottom: Position? = null,
    orientation: Orientation? = null,
): NavigableLayout = getClosestLayoutAwareAncestor().let { ancestor ->
    val wrapping = ancestor?.getRenderingLayout()
    attachLayout(
        uom = wrapping?.uom ?: uom,
        orientation = orientation ?: Orientation.HORIZONTAL,
        leftTop = leftTop ?: wrapping?.nextLayoutLeftTop ?: wrapping?.leftTop ?: Position.start(uom),
        maxRightBottom = maxRightBottom ?: getEnclosingMaxRightBottom(),
    )
}

private fun ModelExportContext.attachLayout(
    uom: UnitsOfMeasure,
    leftTop: Position,
    maxRightBottom: Position?,
    orientation: Orientation,
): NavigableLayout = DefaultLayout(
    uom = uom,
    leftTop = resolveMargins(leftTop),
    maxRightBottom = maxRightBottom
).let { NavigableLayout(navigation, it, orientation) }.also {
    layouts.renderingLayout = it
}

fun ModelExportContext.createLayoutScope(
    uom: UnitsOfMeasure = UnitsOfMeasure.PT,
    childLeftTop: Position? = null,
    maxRightBottom: Position? = null,
    orientation: Orientation? = null,
    block: Layout.() -> Unit,
) = setLayout(uom, childLeftTop, maxRightBottom, orientation).apply(block).finish()

private fun ModelExportContext.resolveMargins(sourcePosition: Position): Position {
    val model = (navigation.active as? AttributedModelOrPart<*>)
    return model?.attributes?.get(MarginsAttribute::class.java)?.let {
        Position(it.left + sourcePosition.x, it.top + sourcePosition.y)
    } ?: sourcePosition
}