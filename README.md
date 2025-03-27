# PhoneBot - LLM驱动的Android无障碍控制

这是一个利用大语言模型（LLM）通过Android无障碍服务来控制手机的项目。该项目允许LLM接收用户的指令，并通过函数调用执行相应的操作来控制Android设备。

## 项目概述

PhoneBot利用Gemini模型的能力，结合Android无障碍服务API，实现了智能化的手机操作控制。通过自然语言交互，用户可以指示LLM执行各种手机操作，而无需直接与设备交互。

## 主要功能

- 基于LLM的自然语言理解
- 通过函数调用执行设备操作
- 利用Android无障碍服务实现UI交互
- 安全的设备控制机制

## 技术实现

### LLM集成

项目使用Google的Gemini AI模型，通过官方SDK进行集成：
- 使用`GenerativeModel`与Gemini进行通信
- 支持流式响应处理
- 利用ChatViewModel管理对话状态

### 函数调用实现

函数调用是本项目的核心，允许LLM执行预定义的操作。请参考`FunctionCallingTest.kt`文件中的实现：

1. 定义函数工具（Tool）：
   ```kotlin
   val exampleTool = Tool(
       functionDeclarations = listOf(
           defineFunction(
               name = EXAMPLE_TOOL_FUNC_NAME,
               description = "enter user's given string, returns an email address if correct",
               parameters = listOf(
                   Schema.str(EXAMPLE_TOOL_ARG_NAME, "a string given by user"),
               ),
               requiredParameters = listOf(EXAMPLE_TOOL_ARG_NAME)
           )
       )
   )
   ```

2. 函数实现类：
   ```kotlin
   class FunctionProvider {
       private fun test(args: Map<String, String?>): String {
           val arg = args[EXAMPLE_TOOL_ARG_NAME] ?: return "incorrect function calling"
           return if (arg == "niki") "asd@gmail.com" else "incorrect key"
       }

       private val functionMap = mapOf<String, (Map<String, String?>) -> String>(
           EXAMPLE_TOOL_FUNC_NAME to ::test
       )

       fun executeFunction(functionName: String, args: Map<String, String?>): FunctionResult {
           // 执行函数并返回结果
       }
   }
   ```

3. 处理函数调用结果：
   ```kotlin
   // 在ChatViewModel中
   chunk.functionCalls.forEach { func ->
       pendingFunctionCalls.add(func.name to func.args)
   }
   
   // 处理函数调用并将结果返回给LLM
   pendingFunctionCalls.forEach { (name, args) ->
       val result = functionProvider.executeFunction(name, args).toJson()
       // 将函数结果发送回LLM
   }
   ```

## 开发自定义功能

要添加新的函数调用功能，需要参考以下步骤：

1. 在新建的类中定义函数声明（Tool）
2. 在FunctionProvider中实现相应的处理逻辑
3. 将函数映射添加到functionMap中
4. 确保函数返回值遵循FunctionResult格式

## 使用方法

1. 启动应用
2. 输入自然语言指令
3. LLM会理解指令并执行相应的函数调用
4. 查看执行结果并根据需要继续交互

## 依赖项

- Google Generative AI SDK
- Android Accessibility Service API
- Zephyr工具库（用于UI组件、日志等）

## 未来计划

- 增加更多设备控制功能
- 提升无障碍服务的稳定性
- 支持更复杂的多轮交互场景
- 优化函数调用的错误处理

## 许可证

[许可证信息待添加]
