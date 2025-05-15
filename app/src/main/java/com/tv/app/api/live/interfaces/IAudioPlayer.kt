package com.tv.app.api.live.interfaces

// 音频播放接口
interface IAudioPlayer {
    var sampleRate: Int

    fun initialize(sampleRate: Int)
    fun playAudio(pcmData: ByteArray)
    fun release()
}