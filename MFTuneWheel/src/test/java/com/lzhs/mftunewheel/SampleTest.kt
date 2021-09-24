package com.lzhs.mftunewheel

import org.junit.Test

class SampleTest {

    @Test
    fun test(){
       val mutableList  = mutableListOf(1,2,3,4,5,6,7,8,9)
        println("原始数据 $mutableList")
        mutableList.removeIf {
            it%2==0
        }
        println("处理数据 $mutableList")

    }
}