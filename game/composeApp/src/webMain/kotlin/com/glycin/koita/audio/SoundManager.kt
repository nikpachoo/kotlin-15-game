package com.glycin.koita.audio

import koita.composeapp.generated.resources.Res

private const val BASE_PATH = "files/audio"

@OptIn(ExperimentalWasmJsInterop::class)
object SoundManager {

    private val sounds = mutableMapOf<Sounds, HTMLAudioElement>()
    private val music = mutableMapOf<Music, HTMLAudioElement>()
    private var currentMusic: Music? = null

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
        currentMusic = music
    }

    fun switchLoop(target: Music, volume: Float = 1f) {
        if (currentMusic == target) return
        currentMusic?.let { stopLoop(it) }
        playLoop(target, volume)
    }

    fun stopCurrentLoop() {
        currentMusic?.let { stopLoop(it) }
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
        if (currentMusic == music) {
            currentMusic = null
        }
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
        loadSound(Sounds.ENEMY_DEATH, "enemy_death.wav")
        loadSound(Sounds.POWERUP_PICKUP, "powerup_pickup.wav")
        loadSound(Sounds.ULTIMATE_UNLOCK, "ultimate_unlock.wav")
        loadSound(Sounds.ULTIMATE_USE, "ultimate_use.wav")
        loadSound(Sounds.UPGRADE_UNLOCK, "upgrade_unlock.wav")
        loadSound(Sounds.GAME_OVER, "game_over.wav")
        loadSound(Sounds.GAME_WIN, "game_win.wav")
        loadMusic(Music.BACKGROUND, "background_music.wav")
        loadMusic(Music.BACKGROUND_TOP, "background_music_top.wav")
        loadMusic(Music.BACKGROUND_BOSS, "background_music_boss.wav")
    }
}
