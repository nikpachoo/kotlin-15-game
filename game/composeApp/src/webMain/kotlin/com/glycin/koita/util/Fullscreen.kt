package com.glycin.koita.util

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """() => {
        const el = document.documentElement;
        const req = el.requestFullscreen || el.webkitRequestFullscreen || el.msRequestFullscreen;
        if (req && !document.fullscreenElement && !document.webkitFullscreenElement) {
            try { req.call(el); } catch (e) {}
        }
    }""",
)
external fun requestBrowserFullscreen()
