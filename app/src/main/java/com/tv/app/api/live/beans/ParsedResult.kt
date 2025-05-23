package com.tv.app.api.live.beans

sealed class ParsedResult {
    data object SetupCompleted : ParsedResult()

    data object TurnComplete : ParsedResult()

    data object GoAway : ParsedResult()

    data class OutputTranscription(val text: String) : ParsedResult()

    data class Audio(val pcmData: ByteArray, val sampleRate: Int) : ParsedResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Audio

            if (!pcmData.contentEquals(other.pcmData)) return false
            if (sampleRate != other.sampleRate) return false

            return true
        }

        override fun hashCode(): Int {
            var result = pcmData.contentHashCode()
            result = 31 * result + sampleRate
            return result
        }
    }
}