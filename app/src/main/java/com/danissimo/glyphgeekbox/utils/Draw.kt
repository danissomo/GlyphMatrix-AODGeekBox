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

fun generate_all_circle_points(D: Int): List<Pair<Int, Int>> {
    val res = ArrayList<Pair<Int, Int>>()
    val radius = D / 2.0
    val centerX = (D - 1) / 2.0
    val centerY = (D - 1) / 2.0

    for (y in 0 until D) {
        for (x in 0 until D) {
            val dx = x - centerX
            val dy = y - centerY
            if (dx * dx + dy * dy <= radius * radius) {
                res.add(x to y)
            }
        }
    }
    return res
}

val sLettersMap: MutableMap<Char?, String?> = object : HashMap<Char?, String?>() {
    init {
        this.put('a', "0000,0110,1001,1111,1001,1001")
        this.put('b', "0000,1110,1001,1110,1001,1110")
        this.put('c', "0000,0111,1000,1000,1000,0111")
        this.put('d', "0000,1110,1001,1001,1001,1110")
        this.put('e', "0000,0111,1000,1110,1000,0111")
        this.put('f', "0000,1111,1000,1110,1000,1000")
        this.put('g', "0000,0111,1000,1011,1001,0111")
        this.put('h', "0000,1001,1001,1111,1001,1001")
        this.put('i', "000,111,010,010,010,111")
        this.put('j', "0000,0011,0001,0001,1001,0110")
        this.put('k', "0000,1001,1010,1100,1010,1001")
        this.put('l', "0000,1000,1000,1000,1000,0111")
        this.put('m', "0000,01010,10101,10101,10101,10101")
        this.put('n', "0000,10001,11001,10101,10011,10001")
        this.put('o', "0000,0110,1001,1001,1001,0110")
        this.put('p', "0000,1110,1001,1110,1000,1000")
        this.put('q', "0000,0110,1001,1001,1010,0101")
        this.put('r', "0000,1110,1001,1110,1010,1001")
        this.put('s', "0000,0111,1000,0110,0001,1110")
        this.put('t', "000,111,010,010,010,010")
        this.put('u', "0000,1001,1001,1001,1001,0110")
        this.put('v', "0000,101,101,101,101,010")
        this.put('w', "00000,10001,10101,10101,10101,01010")
        this.put('x', "0000,1001,1001,0110,1001,1001")
        this.put('y', "0000,1001,1001,0111,0001,0110")
        this.put('z', "0000,1111,0001,0110,1000,1111")
        this.put('0', "0000,0110,1001,1001,1001,0110")
        this.put('1', "0000,0010,0110,1010,0010,0010")
        this.put('2', "0000,1110,0001,0110,1000,1111")
        this.put('3', "0000,1110,0001,0111,0001,1110")
        this.put('4', "0000,1001,1001,0111,0001,0001")
        this.put('5', "0000,1111,1000,1110,0001,1110")
        this.put('6', "0000,0110,1000,1110,1001,0110")
        this.put('7', "0000,1110,0001,0001,0001,0001")
        this.put('8', "0000,0110,1001,0110,1001,0110")
        this.put('9', "0000,1110,1001,1111,0001,1110")
    }
}

fun getTextWidth(text: String): Int {
    var rs = 0
    for (ch in text.lowercase()) {
        if (ch == ' ') rs += 3
        else{
            rs += sLettersMap[ch]?.split(",")?.maxBy { it.length }?.length?: 0
            rs += 1
        }
    }
    return rs
}