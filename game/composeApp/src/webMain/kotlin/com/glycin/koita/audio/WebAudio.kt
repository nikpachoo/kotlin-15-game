package com.glycin.koita.audio

import kotlin.js.JsAny

@OptIn(ExperimentalWasmJsInterop::class)
external class HTMLAudioElement : JsAny {
    var src: String
    var volume: Double
    var loop: Boolean
    var currentTime: Double
    val paused: Boolean
    fun play(): JsAny?
    fun pause()
    fun load()
    fun cloneNode(deep: Boolean): HTMLAudioElement
}

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(src) => new Audio(src)")
external fun createAudio(src: String): HTMLAudioElement