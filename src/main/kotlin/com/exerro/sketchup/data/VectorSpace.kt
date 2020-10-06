package com.exerro.sketchup.data

sealed class VectorSpace
object ScreenSpace: VectorSpace()
object ViewSpace: VectorSpace()
object WorldSpace: VectorSpace()
