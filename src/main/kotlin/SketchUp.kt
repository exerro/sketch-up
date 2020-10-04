
fun main() {
    val windowID = initialiseGLFW(1080, 720, "Sketch-Up")
    val events = glfwHookEvents(windowID)
    val sub = events.filterIsInstance<PointerEvent>().connect {
        println("Got a pointer event")
        it.updates.connect(Unit) { _, event ->
            println(event)
        }
    }

    glfwLoop(windowID)
    glfwClose(windowID)
    sub.disconnect()
}
