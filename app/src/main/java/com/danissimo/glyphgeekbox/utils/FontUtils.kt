package com.danissimo.glyphgeekbox.utils

/**
 * Пиксельный шрифт 3x5 для цифр 0-9, точки, букв и символов.
 * Каждый символ представлен массивом из 5 интов, где 3 младших бита — это колонки.
 */
private val FONT_3X5 = mapOf(
    '0' to intArrayOf(0b111, 0b101, 0b101, 0b101, 0b111),
    '1' to intArrayOf(0b010, 0b110, 0b010, 0b010, 0b010),
    '2' to intArrayOf(0b111, 0b001, 0b111, 0b100, 0b111),
    '3' to intArrayOf(0b111, 0b001, 0b111, 0b001, 0b111),
    '4' to intArrayOf(0b101, 0b101, 0b111, 0b001, 0b001),
    '5' to intArrayOf(0b111, 0b100, 0b111, 0b001, 0b111),
    '6' to intArrayOf(0b111, 0b100, 0b111, 0b101, 0b111),
    '7' to intArrayOf(0b111, 0b001, 0b001, 0b001, 0b001),
    '8' to intArrayOf(0b111, 0b101, 0b111, 0b101, 0b111),
    '9' to intArrayOf(0b111, 0b101, 0b111, 0b001, 0b111),
    '.' to intArrayOf(0b000, 0b000, 0b000, 0b000, 0b010),
    'k' to intArrayOf(0b101, 0b101, 0b110, 0b101, 0b101),
    'm' to intArrayOf(0b101, 0b111, 0b111, 0b101, 0b101),
    'c' to intArrayOf(0b111, 0b100, 0b100, 0b100, 0b111),
    'a' to intArrayOf(0b111, 0b101, 0b111, 0b101, 0b101),
    'l' to intArrayOf(0b100, 0b100, 0b100, 0b100, 0b111),
    's' to intArrayOf(0b111, 0b100, 0b111, 0b001, 0b111),
    't' to intArrayOf(0b111, 0b010, 0b010, 0b010, 0b010),
    'e' to intArrayOf(0b111, 0b100, 0b111, 0b100, 0b111),
    'p' to intArrayOf(0b111, 0b101, 0b111, 0b100, 0b100),
    'r' to intArrayOf(0b111, 0b101, 0b110, 0b101, 0b101),
    '/' to intArrayOf(0b001, 0b001, 0b010, 0b100, 0b100),
    '?' to intArrayOf(0b111, 0b001, 0b010, 0b000, 0b010),
    'b' to intArrayOf(0b100, 0b100, 0b111, 0b101, 0b111),
    'v' to intArrayOf(0b101, 0b101, 0b101, 0b101, 0b010),
    'n' to intArrayOf(0b111, 0b101, 0b101, 0b101, 0b101),
    '%' to intArrayOf(0b100, 0b010, 0b001, 0b111, 0b000) // Упрощенный %
)

/**
 * Шрифт 5x3 для WiFi. '1', '2', '3' — уровни сигнала.
 */
private val FONT_WIFI_5X3 = mapOf(
    '1' to intArrayOf(0b00000, 0b00000, 0b00100),
    '2' to intArrayOf(0b00000, 0b01110, 0b00100),
    '3' to intArrayOf(0b11111, 0b01110, 0b00100)
)

/**
 * Шрифт 4x4 для мобильной сети. '1', '2', '3', '4' — уровни сигнала.
 */
private val FONT_MOBILE_4X4 = mapOf(
    '1' to intArrayOf(0b0000, 0b0000, 0b0000, 0b1000),
    '2' to intArrayOf(0b0000, 0b0000, 0b0100, 0b1100),
    '3' to intArrayOf(0b0000, 0b0010, 0b0110, 0b1110),
    '4' to intArrayOf(0b0001, 0b0011, 0b0111, 0b1111)
)

/**
 * Специальные символы. '*' — ключик (7x3).
 */
private val FONT_SPECIAL_7X3 = mapOf(
    '*' to intArrayOf(0b0100010, 0b1111101, 0b0000010)
)

/**
 * Рендерит текст шрифтом 3x5 на матрицу.
 */
fun renderText3x5(
    matrix: IntArray,
    size: Int,
    text: String,
    x: Int,
    y: Int,
    brightness: Int
) {
    var cursorX = x
    for (char in text.lowercase()) {
        val glyph = FONT_3X5[char]
        if (glyph != null) {
            renderGlyph(matrix, size, glyph, 3, 5, cursorX, y, brightness)
            cursorX += 4 // 3 пикселя на символ + 1 пиксель отступ
        } else if (char == ' ') {
            cursorX += 2 // Ширина пробела
        }
    }
}

/**
 * Возвращает ширину текста шрифтом 3x5 в пикселях.
 */
fun getTextWidth3x5(text: String): Int {
    var width = 0
    for (char in text.lowercase()) {
        if (FONT_3X5.containsKey(char)) {
            width += 4
        } else if (char == ' ') {
            width += 2
        }
    }
    return if (width > 0) width - 1 else 0
}

/**
 * Рендерит иконку WiFi (5x3).
 * @param level Уровень сигнала (1-3).
 */
fun renderWifi(matrix: IntArray, size: Int, level: Int, x: Int, y: Int, brightness: Int) {
    val levelChar = level.coerceIn(1, 3).toString()[0]
    val glyph = FONT_WIFI_5X3[levelChar] ?: return
    renderGlyph(matrix, size, glyph, 5, 3, x, y, brightness)
}

/**
 * Рендерит иконку мобильной сети (4x4).
 * @param level Уровень сигнала (1-4).
 */
fun renderMobile(matrix: IntArray, size: Int, level: Int, x: Int, y: Int, brightness: Int) {
    val levelChar = level.coerceIn(1, 4).toString()[0]
    val glyph = FONT_MOBILE_4X4[levelChar] ?: return
    renderGlyph(matrix, size, glyph, 4, 4, x, y, brightness)
}

/**
 * Рендерит иконку ключа (7x3).
 */
fun renderKey(matrix: IntArray, size: Int, x: Int, y: Int, brightness: Int) {
    val glyph = FONT_SPECIAL_7X3['*'] ?: return
    renderGlyph(matrix, size, glyph, 7, 3, x, y, brightness)
}

/**
 * Универсальная функция рендеринга глифа с поддержкой отсечения.
 */
private fun renderGlyph(
    matrix: IntArray,
    size: Int,
    glyph: IntArray,
    width: Int,
    height: Int,
    x: Int,
    y: Int,
    brightness: Int
) {
    for (row in 0 until height) {
        val drawY = y + row
        if (drawY in 0 until size) {
            val rowData = glyph[row]
            for (col in 0 until width) {
                val drawX = x + col
                if (drawX in 0 until size) {
                    if (((rowData shr (width - 1 - col)) and 1) == 1) {
                        val index = drawY * size + drawX
                        if (index in matrix.indices) {
                            matrix[index] = brightness
                        }
                    }
                }
            }
        }
    }
}
