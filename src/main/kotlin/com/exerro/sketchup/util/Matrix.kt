package com.exerro.sketchup.util

import kotlin.math.roundToInt

class Matrix private constructor(
    val rows: Int,
    val columns: Int,
    val values: DoubleArray
) {
    operator fun get(row: Int, column: Int): Double {
        return values[row * columns + column]
    }

    operator fun times(other: Matrix): Matrix {
        assert(columns == other.rows) { "Invalid matrix dimensions for multiplication" }

        val indices = 0 until columns

        return mapped(rows, other.columns) { row, column ->
            indices.map { this[row, it] * other[it, column] } .sum()
        }
    }

    fun transpose(): Matrix {
        return mapped(rows = columns, columns = rows) { r, c -> this[c, r] }
    }

    fun inverse(): Matrix {
        assert(rows == columns) { "Cannot invert non-square matrix" }

        val left = values.copyOf()
        val right = identity(rows).values
        val n = rows

        for (i in 0 until n) { // from row 0 to the bottom
            val pivot = left[n * i + i] // get the pivot value

            for (j in (i + 1) until n) { // from row i to the bottom
                val coeff = left[n * j + i] / pivot // find C s.t. R(j) => R(j) - C*R(i) means R(j)(i) = 0
                for (k in 0 until n) { // subtract C*R(i)(k) from each R(j)(k)
                    left[j * n + k] -= coeff * left[n * i + k]
                    right[j * n + k] -= coeff * right[n * i + k]
                }
            }
        }

        for (i in (n - 1) downTo 0) {
            val pivot = left[i * n + i]
            for (j in 0 until i) {
                val coeff = left[j * n + i] / pivot
                for (k in 0 until n) {
                    left[j * n + k] -= coeff * left[i * n + k]
                    right[j * n + k] -= coeff * right[i * n + k]
                }
            }
            for (k in 0 until n) {
                if (left[n * i + i] == 0.0) error("Non-invertible matrix")
                right[i * n + k] /= left[n * i + i]
            }
        }

        return fromRawValues(rows, rows, right)
    }

    override fun toString(): String {
        val rows = (0 until rows).map { row ->
            (0 until columns).map { column ->
                ((this[row, column] * 100).roundToInt() / 100f).toString()
            }
        }
        val columnLengths = (0 until columns).map { column ->
            rows.map { it[column].length } .maxOrNull() ?: 0
        }
        return rows.joinToString("\n") { row -> row
            .mapIndexed { column, str -> (" ").repeat(columnLengths[column] - str.length) + str }
            .joinToString("  ")
        }
    }

    companion object {
        fun identity(n: Int) =
            mapped(n, n) { r, c -> if (r == c) 1.0 else 0.0 }

        fun fromRawValues(
            rows: Int,
            columns: Int,
            values: DoubleArray
        ) = Matrix(rows, columns, values)

        fun of(
            rows: Int,
            columns: Int,
            vararg values: Double
        ) = Matrix(rows, columns, values.toTypedArray().toDoubleArray())

        inline fun mapped(
            rows: Int,
            columns: Int,
            fn: (row: Int, column: Int) -> Double
        ) = fromRawValues(rows, columns, DoubleArray(rows * columns) {
            fn(it / columns, it % columns)
        })
    }
}

fun main() {
    println(Matrix.of(3, 3,
        1.0, 2.0, 3.0,
        4.0, 5.0, 6.0,
        7.0, 2.0, 9.0
    ).inverse())
}
