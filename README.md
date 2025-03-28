# PhoneBot - LLM驱动的Android无障碍控制

这是一个利用大语言模型（LLM）通过Android无障碍服务来控制手机的项目。该项目允许LLM接收用户的指令，并通过函数调用执行相应的操作来控制Android设备。

## 项目概述

PhoneBot利用Google的Gemini模型的能力，结合Android无障碍服务API，实现了智能化的手机操作控制。通过自然语言交互，用户可以指示LLM执行各种手机操作，而无需直接与设备交互。

## 主要功能

- 基于Gemini的自然语言理解与处理
- 通过函数调用框架执行设备操作
- 利用Android无障碍服务实现UI交互
- 支持多轮对话的聊天界面

## 技术实现

### LLM集成

项目使用Google的Gemini AI模型，通过官方SDK进行集成：
- 使用`GenerativeModel`与Gemini进行通信
- 支持流式响应处理
- 利用ChatViewModel管理对话状态和多轮对话流程

### 函数调用实现

函数调用是本项目的核心，允许LLM执行预定义的操作：

1. 函数管理器设计：
   ```kotlin
   object FuncManager {
       private val _functionMap = mutableMapOf<String, BaseFuncModel>()
       val functionMap: Map<String, BaseFuncModel>
           get() = _functionMap

       init {
           // 注册所有实现
           val list = getSealedClassObjects(BaseFuncModel::class)
           list.forEach { model ->
               _functionMap[model.name] = model
           }
       }

       fun getDeclarations() = _functionMap.values.map { it.getFuncDeclaration() }

       /**
        * 统一函数调用入口
        *
        * 保证输出一个 json 字串
        */
       suspend fun executeFunction(functionName: String, args: Map<String, Any?>): String {
           val result = _functionMap[functionName]?.call(args) ?: Error(functionName)
           return result.toJson()
       }
   }
   ```

2. 函数模型基类：
   ```kotlin
   sealed class BaseFuncModel {
       abstract val name: String // 函数名
       abstract val description: String // 函数的功能描述
       abstract val parameters: List<Schema<*>> // 各个变量的定义
       abstract val requiredParameters: List<String> // 要求的输入参数

       /**
        * 用于调用的函数本体，为了方便转 json，直接返回 map
        */
       abstract suspend fun call(args: Map<String, Any?>): Map<String, Any?>

       fun getFuncDeclaration(): FunctionDeclaration = defineFunction(
           name = name,
           description = description,
           parameters = parameters,
           requiredParameters = requiredParameters
       )

       fun getFuncInstance() = ::call

       fun defaultMap(status: String, result: String = "") =
           mapOf<String, Any?>("status" to status, "result" to result)
   }
   ```

3. 函数实现示例：
   ```kotlin
   data object ExampleFuncModel : BaseFuncModel() {
       override val name: String = "get_user_email_address"
       override val description: String =
           "Retrieves an email address using a provided key, returns a JSON object with the result"
       override val parameters: List<Schema<*>> = listOf(
           Schema.str("key", "the authentication key to lookup the email"),
       )
       override val requiredParameters: List<String> = listOf("key")
       override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
           val key = args["key"] as? String ?: return defaultMap("error", "incorrect function calling")

           return when (key) {
               "niki" -> defaultMap("ok", "ni@gmail.com")
               "tom" -> defaultMap("ok", "tom1998@gmail.com")
               "den" -> defaultMap("ok", "d_e_nnn@gmail.com")
               else -> defaultMap("error", "incorrect key")
           }
       }
   }
   ```

4. 工具注册：
   ```kotlin
   val AppTools: List<Tool> by lazy {
       listOf(
           Tool(functionDeclarations = FuncManager.getDeclarations())
       )
   }
   ```

## 无障碍服务实现

项目利用Android的无障碍服务API实现界面交互：

```kotlin
class MyAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val nodeTree = ArrayList<Node>()
        if (event == null) {
            Log.d(TAG, "onAccessibilityEvent: event is null")
        } else {
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                // 处理窗口内容变化事件
                val packageName = event.packageName.toString()
                try {
                    val rootNodeInfo = rootInActiveWindow
                    if (rootNodeInfo != null) {
                        // 解析节点树...
                    }
                } catch (e: Exception) {
                    e.logE(TAG)
                }
            }
        }
    }

    private fun traverseNodeTree(node: AccessibilityNodeInfo?): List<Node>? {
        // 递归遍历节点树的实现
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // 服务连接后返回主屏幕
        performGlobalAction(GLOBAL_ACTION_HOME)
    }
    
    // 更多无障碍服务相关方法
}
```

## 开发自定义功能

要添加新的函数调用功能，需要遵循以下步骤：

1. 创建新的函数模型类，继承BaseFuncModel
2. 实现必要的抽象方法和属性
3. 使用`data object`定义实现类，自动注册到FuncManager
4. 确保函数返回统一格式的Map类型，推荐使用`defaultMap`方法创建标准格式

## 项目依赖

- Google Generative AI SDK - Gemini模型访问
- Android Accessibility Service API - 无障碍服务支持
- Zephyr工具库 - UI组件、网络、日志等基础功能
  - vbclass - ViewBinding简化
  - scaling-layout - UI适配
  - global-values - 全局常量
  - datastore - 数据存储
  - net - 网络操作
  - log - 日志工具
  - extension - Kotlin扩展

## 未来计划

- 完善无障碍服务实现
- 增加更多实用的设备控制功能
- 优化函数调用的错误处理
- 改进用户交互界面
- 支持更复杂的多轮对话场景

## 许可证

[许可证信息待添加]
