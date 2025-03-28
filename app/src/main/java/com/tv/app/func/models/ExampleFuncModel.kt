package com.tv.app.func.models

@Deprecated("")
class ExampleFuncModel
//
//import com.google.ai.client.generativeai.type.Schema
//
//
///**
// * 实现示例
// */
//data object ExampleFuncModel : BaseFuncModel() {
//    override val name: String = "get_user_email_address"
//    override val description: String =
//        "Retrieves an email address using a provided key, returns a JSON object with the result"
//    override val parameters: List<Schema<*>> = listOf(
//        Schema.str("key", "the authentication key to lookup the email"),
//    )
//    override val requiredParameters: List<String> = listOf("key")
//    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
//        val key = args["key"] as? String ?: return defaultMap("error", "incorrect function calling")
//
//        return when (key) {
//            "niki" ->
//                defaultMap("ok", "ni@gmail.com")
//
//            "tom" ->
//                defaultMap("ok", "tom1998@gmail.com")
//
//            "den" ->
//                defaultMap("ok", "d_e_nnn@gmail.com")
//
//            else ->
//                defaultMap("error", "incorrect key")
//        }
//    }
//}