package com.exerro.sketchup.api.data

enum class InteractionType {
    /** Drawing operation. */
    Sketch,
    /** Basic movement around the sketch. */
    Navigation,
    /** Movement around the sketch based on entity metadata. */
    SpecialNavigation,
    /** Selection of entities based on their geometry. */
    SelectSpatial,
    /** Selection of entities based on their insertion time. */
    SelectTemporal,
    /** Selection of entities based on entity metadata. */
    SelectMeta,
    /** Generic application wide action. */
    Action,
}
