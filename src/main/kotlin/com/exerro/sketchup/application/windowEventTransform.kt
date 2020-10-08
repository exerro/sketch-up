package com.exerro.sketchup.application

import com.exerro.sketchup.api.streams.ConnectedObservable
import com.exerro.sketchup.api.streams.ConnectedObservableStream
import com.exerro.sketchup.api.util.fold
import com.exerro.sketchup.api.data.*
import org.lwjgl.glfw.GLFW.glfwGetTime

internal fun <Model> ConnectedObservableStream<ExtendedWindowEvent>.eventTransformedFold(
    initialModel: Model,
    updateModel: (Model, WindowEvent) -> Model
): ConnectedObservable<Model> {
    val c = fold(WrappedModelState(initialModel, PointerContext.Default)) { (currentModel, context), event -> when (event) {
        is RedrawEvent -> WrappedModelState(updateModel(currentModel, event), context)
        is PointerMoveEvent -> handlePointerMove(currentModel, context, event, updateModel)
        is WindowEvent -> WrappedModelState(updateModel(currentModel, event), PointerContext.Default)
        is PointerPressedEvent -> handlePointerPress(currentModel, context, event, updateModel)
        PointerReleasedEvent -> handlePointerRelease(currentModel, context, updateModel)
    } }

    return object: ConnectedObservable<Model> {
        override fun disconnect() = c.disconnect()
        override fun connect(onItem: (Model) -> Unit) = c.connect { onItem(it.currentModel) }
        override val latest: Model get() = c.latest.currentModel
    }
}

////////////////////////////////////////////////////////////////////////////////

private fun <Model> handlePointerMove(
    currentModel: Model,
    context: PointerContext<Model>,
    event: PointerMoveEvent,
    updateModel: (Model, WindowEvent) -> Model
) = when (context) {
    is PointerContext.Pressed -> {
        val path = Path.of(context.position, context.pressure) + Path.of(event.position, event.pressure)
        WrappedModelState(
            updateModel(updateModel(context.restoreModel, event), PointerDragEvent(context.mode, context.alternate, path, false)),
            PointerContext.PressedMoved(context.mode, path, context.restoreModel, context.alternate)
        )
    }
    is PointerContext.PressedMoved -> {
        val path = context.path + Path.of(event.position, event.pressure)
        WrappedModelState(
            updateModel(updateModel(context.restoreModel, event), PointerDragEvent(context.mode, context.alternate, path, false)),
            PointerContext.PressedMoved(context.mode, path, context.restoreModel, context.alternate)
        )
    }
    else -> WrappedModelState(updateModel(currentModel, event), context)
}

private fun <Model> handlePointerPress(
    currentModel: Model,
    context: PointerContext<Model>,
    event: PointerPressedEvent,
    updateModel: (Model, WindowEvent) -> Model
) = when (context) {
    PointerContext.Default -> WrappedModelState(
        updateModel(currentModel, PointerPressEvent(event.mode, false, Point(event.position, event.pressure))),
        PointerContext.Pressed(event.mode, event.position, event.pressure, currentModel, alternate = false)
    )
    is PointerContext.ReadyForAlternate -> {
        val alternate = isAlternate(context, event.mode, event.position)
        val model = if (alternate) context.restoreModel else currentModel
        WrappedModelState(
            updateModel(model, PointerPressEvent(event.mode, alternate, Point(event.position, event.pressure))),
            PointerContext.Pressed(event.mode, event.position, event.pressure, model, alternate = alternate)
        )
    }
    else -> WrappedModelState(currentModel, context)
}

private fun <Model> handlePointerRelease(
    currentModel: Model,
    context: PointerContext<Model>,
    updateModel: (Model, WindowEvent) -> Model
) = when (context) {
    is PointerContext.Pressed -> when (context.alternate) {
        true -> WrappedModelState(currentModel, PointerContext.Default)
        else -> WrappedModelState(currentModel, PointerContext.ReadyForAlternate(context.mode, context.restoreModel, context.position, glfwGetTime()))
    }
    is PointerContext.PressedMoved -> WrappedModelState(
        updateModel(context.restoreModel, PointerDragEvent(context.mode, context.alternate, context.path, true)),
        PointerContext.Default
    )
    else -> WrappedModelState(currentModel, PointerContext.Default)
}

////////////////////////////////////////////////////////////////////////////////

private data class WrappedModelState<Model>(
    val currentModel: Model,
    val context: PointerContext<Model>
)

private sealed class PointerContext<out Model> {
    object Default: PointerContext<Nothing>()

    data class ReadyForAlternate<Model>(
        val mode: PointerMode,
        val restoreModel: Model,
        val initialPosition: Vector<ScreenSpace>,
        val releaseTime: Double,
    ): PointerContext<Model>()

    data class Pressed<Model>(
        val mode: PointerMode,
        val position: Vector<ScreenSpace>,
        val pressure: Scalar<ScreenSpace>,
        val restoreModel: Model,
        val alternate: Boolean,
    ): PointerContext<Model>()

    data class PressedMoved<Model>(
        val mode: PointerMode,
        val path: Path<ScreenSpace>,
        val restoreModel: Model,
        val alternate: Boolean,
    ): PointerContext<Model>()
}

////////////////////////////////////////////////////////////////////////////////

private fun isAlternate(context: PointerContext.ReadyForAlternate<*>, mode: PointerMode, position: WindowPosition) =
    context.mode == mode && glfwGetTime() <= context.releaseTime + TIMEOUT && !movedTooFar(context.initialPosition, position)

private fun movedTooFar(a: WindowPosition, b: WindowPosition) =
    (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y) > MOVEMENT_THRESHOLD_SQUARED

////////////////////////////////////////////////////////////////////////////////

private const val TIMEOUT = 0.8
private const val MOVEMENT_THRESHOLD = 30f
private const val MOVEMENT_THRESHOLD_SQUARED = MOVEMENT_THRESHOLD * MOVEMENT_THRESHOLD
