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
       // map: <function_name, func_model>
       val functionMap = mapOf<String, BaseFuncModel<*>>(
           ExampleFuncModel.FUNC_NAME to ExampleFuncModel()
       )

       fun executeFunction(functionName: String, args: Map<String, String?>): String {
           val function = functionMap[functionName]?.getFuncInstance()
           val result = function?.invoke(args) ?: "{\"unknown_function\": \"$functionName\"}"
           return result.toJson()
       }
   }
   ```

2. 函数模型基类：
   ```kotlin
   abstract class BaseFuncModel<T> {
       abstract val name: String
       abstract val description: String
       abstract val parameters: List<Schema<*>>
       abstract val requiredParameters: List<String>
       
       abstract fun call(args: Map<String, Any?>): T
       
       fun getFuncDeclaration(): FunctionDeclaration { ... }
       fun getFuncInstance(): (Map<String, String?>) -> T { ... }
   }
   ```

3. 函数实现示例：
   ```kotlin
   class ExampleFuncModel : BaseFuncModel<ExampleFuncModel.JSONResult>() {
       companion object {
           const val FUNC_NAME = "get_user_email_address"
       }

       override fun call(args: Map<String, Any?>): JSONResult {
           val arg = args["key"] ?: return JSONResult("error", "incorrect function calling")
           return if (arg == "niki") JSONResult("ok", "asd@gmail.com") 
                  else JSONResult("error", "incorrect key")
       }

       data class JSONResult(val status: String, val result: String)
   }
   ```

4. 工具注册：
   ```kotlin
   val AppTools: List<Tool> by lazy {
       listOf(
           Tool(
               functionDeclarations = FuncManager.functionMap.values.map { it.getFuncDeclaration() }
           )
       )
   }
   ```

## 无障碍服务实现

项目利用Android的无障碍服务API实现界面交互：

```kotlin
class MyAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 处理无障碍事件
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // 服务连接后的初始化
    }
    
    // 更多无障碍服务相关方法
}
```

## 开发自定义功能

要添加新的函数调用功能，需要遵循以下步骤：

1. 创建新的函数模型类，继承BaseFuncModel
2. 实现必要的抽象方法和属性
3. 在FuncManager的functionMap中注册该函数
4. 确保函数返回值格式规范，推荐使用自定义数据类

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
