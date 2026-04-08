package com.glycin.koita.audio

import koita.composeapp.generated.resources.Res

private const val BASE_PATH = "files/audio"

@OptIn(ExperimentalWasmJsInterop::class)
object SoundManager {

    private val sounds = mutableMapOf<Sounds, HTMLAudioElement>()
    private val music = mutableMapOf<Music, HTMLAudioElement>()

    var musicVolume: Float = 0.25f
        set(value) {
            field = value.coerceIn(0f, 1f)
            updateMusicVolumes()
        }

    var sfxVolume: Float = 1f
        set(value) {
            field = value.coerceIn(0f, 1f)
        }

    init {
        loadAll()
    }

    fun playOneShot(sound: Sounds, volume: Float = 1f) {
        val clip = sounds[sound] ?: return
        val clone = clip.cloneNode(false)
        clone.volume = (volume * sfxVolume).toDouble().coerceIn(0.0, 1.0)
        clone.play()
    }

    fun playLoop(music: Music, volume: Float = 1f) {
        val clip = this.music[music] ?: return
        clip.loop = true
        clip.volume = (volume * musicVolume).toDouble().coerceIn(0.0, 1.0)
        if (clip.paused) {
            clip.play()
        }
    }

    private fun updateMusicVolumes() {
        for ((_, clip) in music) {
            if (!clip.paused) {
                clip.volume = musicVolume.toDouble().coerceIn(0.0, 1.0)
            }
        }
    }

    fun stopLoop(music: Music) {
        val clip = this.music[music] ?: return
        clip.loop = false
        clip.pause()
        clip.currentTime = 0.0
    }

    fun stop(sound: Sounds) {
        val clip = sounds[sound] ?: return
        clip.pause()
        clip.currentTime = 0.0
    }

    private fun loadSound(sound: Sounds, fileName: String) {
        sounds[sound] = createAudio(Res.getUri("$BASE_PATH/$fileName"))
    }

    private fun loadMusic(music: Music, fileName: String) {
        this.music[music] = createAudio(Res.getUri("$BASE_PATH/$fileName"))
    }

    private fun loadAll() {
        loadSound(Sounds.JUMP, "jump.wav")
        loadSound(Sounds.DIG, "dig.wav")
        loadSound(Sounds.SHOOT, "shoot.wav")
        loadSound(Sounds.EXPLODE, "explosion.wav")
        loadSound(Sounds.HIT, "hit.wav")
        loadMusic(Music.BACKGROUND, "background_music.wav")
    }
}
