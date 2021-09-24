package com.example.lzhs

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        var mul = mutableListOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        mul.forEach {
            if (it % 2 == 0) {
                mul.remove(it)
            }
        }
        println("$mul")
    }


}