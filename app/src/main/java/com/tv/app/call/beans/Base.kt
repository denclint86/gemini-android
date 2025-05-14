package com.tv.app.call.beans

import com.google.gson.annotations.SerializedName


/*
  {
    "setup": {
      "model": "models/$modelName",
      "generation_config": {
        "response_modalities": ["$contentType"]
      }
   }
  }
 */


//{
//    data class RealtimeInputConfig(
//        @SerializedName("automatic_activity_detection") val automaticActivityDetection: AutomaticActivityDetection
//    ) {
//        data class AutomaticActivityDetection(
//            val disabled: Boolean
//        )
//    }
//}

/*
德语 - 德国 - de-DE
英语 - 澳大利亚 - en-AU
英语 - 英国 - en-GB
英语 - 印度 - en-IN
英语 - 美国 - en-US
西班牙语 - 美国 - es-US
法语 - 法国 - fr-FR
印地语 - 印度 - hi-IN
葡萄牙语 - 巴西 - pt-BR
阿拉伯语 - 通用 - ar-XA
西班牙语 - 西班牙 - es-ES
法语 - 加拿大 - fr-CA
印度尼西亚语 - 印度尼西亚 - id-ID
意大利语 - 意大利 - it-IT
日语 - 日本 - ja-JP
土耳其语 - 土耳其 - tr-TR
越南语 - 越南 - vi-VN
孟加拉语 - 印度 - bn-IN
古吉拉特语 - 印度 - gu-IN
卡纳达语 - 印度 - kn-IN
马拉雅拉姆语 - 印度 - ml-IN
马拉地语 - 印度 - mr-IN
泰米尔语 - 印度 - ta-IN
泰卢固语 - 印度 - te-IN
荷兰语 - 荷兰 - nl-NL
韩语 - 韩国 - ko-KR
中文普通话 - 中国 - cmn-CN
波兰语 - 波兰 - pl-PL
俄语 - 俄罗斯 - ru-RU
泰语 - 泰国 - th-TH
 */
data class SpeechConfig(
    @SerializedName("language_code") val languageCode: String,
    @SerializedName("voice_config") val voiceConfig: VoiceConfig? = null
) {
    constructor(languageCode: String, modelName: String) : this(
        languageCode,
        VoiceConfig(modelName)
    )
}

data class VoiceConfig(
    @SerializedName("prebuilt_voice_config") val prebuiltVoiceConfig: PrebuiltVoiceConfig
) {
    constructor(modelName: String) : this(PrebuiltVoiceConfig(modelName))

    /*
Puck X
Charon X
Kore X G
Fenrir X
Aoede ok
Leda good
Orus X
Zephyr ok
 */
    data class PrebuiltVoiceConfig(
        @SerializedName("voice_name") val voiceName: String
    )
}