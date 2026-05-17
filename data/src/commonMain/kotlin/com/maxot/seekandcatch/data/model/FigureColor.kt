package com.maxot.seekandcatch.data.model

@JvmInline
value class FigureColor(val argb: Int) {
    companion object {
        val Red     = FigureColor(0xFFFF0000.toInt())
        val Blue    = FigureColor(0xFF0000FF.toInt())
        val Green   = FigureColor(0xFF00FF00.toInt())
        val Yellow  = FigureColor(0xFFFFFF00.toInt())
        val Cyan    = FigureColor(0xFF00FFFF.toInt())
        val Magenta = FigureColor(0xFFFF00FF.toInt())
        val Black   = FigureColor(0xFF000000.toInt())
        val White   = FigureColor(0xFFFFFFFF.toInt())
    }
}
