package com.danissimo.glyphgeekbox.utils

fun generate_circle_points(R: Int, Xc : Int, Yc : Int): List<Pair<Int, Int>> {
    val res = ArrayList<Pair<Int, Int>>()
    val data = Array(8) { ArrayList<Pair<Int, Int>>() }
    var x = 0
    var y = (R/2)
    var delta = 1F - R
    while (x <= y){
        data[0].add(Xc + y to Yc + x)
        data[1].add(Xc + x to Yc + y)
        data[2].add(Xc - x to Yc + y)
        data[3].add(Xc - y to Yc + x)
        data[4].add(Xc - y to Yc - x)
        data[5].add(Xc - x to Yc - y)
        data[7].add(Xc + y to Yc - x)
        data[6].add(Xc + x to Yc - y)
        delta += if (delta < 0F)
            4F * x + 6F
        else
            4F * (x - y--) + 10F
        x++
    }
    for ((i, line)  in data.withIndex()){
        res += if (i % 2 == 0){
            line
        }else{
            line.reversed()
        }
    }
    return  res.distinct()
}