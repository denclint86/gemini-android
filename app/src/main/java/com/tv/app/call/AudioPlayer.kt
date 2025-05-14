package com.tv.app.call

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.zephyr.global_values.TAG
import com.zephyr.log.logE

class AudioPlayer : IAudioPlayer {
    private var audioTrack: AudioTrack? = null
    override var sampleRate = 24000
    private val channelConfig = AudioFormat.CHANNEL_OUT_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private var isInitialized = false // 跟踪初始化状态

    @Synchronized
    override fun initialize(sampleRate: Int) {
        try {
            this.sampleRate = sampleRate
            val bufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            audioTrack?.release()
            audioTrack = AudioTrack(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelConfig)
                    .setEncoding(audioFormat)
                    .build(),
                bufferSize,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            ).also {
                it.play()
                isInitialized = it.state == AudioTrack.STATE_INITIALIZED
                logE(
                    TAG,
                    "AudioTrack 初始化成功，采样率: $sampleRate Hz, 初始化状态: $isInitialized"
                )
            }
        } catch (e: Exception) {
            logE(TAG, "AudioTrack 初始化失败: ${e.message}")
            isInitialized = false
            audioTrack?.release()
            audioTrack = null
        }
    }

    @Synchronized
    override fun playAudio(pcmData: ByteArray) {
        if (!isInitialized || audioTrack?.state != AudioTrack.STATE_INITIALIZED) {
            logE(TAG, "AudioTrack 未初始化或状态异常，跳过播放")
            return
        }
        if (pcmData.isEmpty()) {
            logE(TAG, "PCM 数据为空，跳过播放")
            return
        }
        try {
            audioTrack?.write(pcmData, 0, pcmData.size)
            logE(TAG, "PCM 数据写入 AudioTrack，长度: ${pcmData.size}")
        } catch (e: Exception) {
            logE(TAG, "PCM 数据写入失败: ${e.message}")
            isInitialized = false
            audioTrack?.release()
            audioTrack = null
        }
    }

    @Synchronized
    override fun release() {
        audioTrack?.release()
        audioTrack = null
        isInitialized = false
        logE(TAG, "AudioTrack 已释放")
    }
}