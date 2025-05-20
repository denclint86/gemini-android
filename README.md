# Gemini - Android

这是一个利用大语言模型（LLM）构建的通用自动化框架，通过自然语言交互调用预定义函数，执行多样化任务。典型应用是结合 Android 无障碍服务实现智能手机控制，用户可通过对话自动化操控设备，提升效率。



## 项目概述

项目通过 Google Gemini 模型的自然语言处理能力，结合灵活的函数调用框架，支持用户以对话方式触发复杂任务。典型场景是利用 Android 无障碍服务 API 实现智能化 UI 交互和设备控制，也可通过扩展工具适配任意功能。



## 主要功能

- 基于 Gemini 模型的自然语言理解与处理
- 支持全双工语音交互
- 通过函数调用框架执行操作（如 UI 交互、系统控制）
- 灵活配置 LLM 请求参数，如流式传输、温度、TopK 等
- 利用 Android 无障碍服务实现 UI 元素分析与操作
- 支持多轮对话界面，基于 MVI 架构管理
- 执行 Shell 命令，支持 Root 或 Shizuku 特权模式
- 获取屏幕视图树及前台应用信息
- 支持常规操作：点击、滑动、文本输入、URI 跳转（如打开地图、拨打电话）
- 监控前台应用变化，获取应用列表
- 自动轮换 API Key，避免请求限速



## 技术亮点



### 通用框架设计

项目核心是一个可扩展的自动化框架：

- 通过标准化函数接口，动态注册和调用功能
- 支持任意任务扩展，只需开发对应的工具函数
- 结合 LLM 智能解析用户意图，适配多样化场景
- 典型实现（如手机控制）仅为框架能力的示例

### LLM 集成

通过 Google Gemini SDK 深度整合大语言模型：

- 支持多种生成参数配置（如温度、Top_P、Top_K、频率惩罚等）
- 采用 MVI 架构管理多轮对话和 UI 状态
- 实现 API Key 自动轮换，确保请求稳定性

### API Key 管理

为应对 API 调用频率限制，设计了高效的 Key 管理系统：

- 支持多 API Key 配置，自动切换
- 轻量级无锁机制，适配低并发场景
- 通过持久化存储维护 Key 索引，保障连续性
- 提供调用速率统计，便于性能监控

### 架构设计

项目采用现代化开发思想，注重解耦与扩展性：

- 基于面向接口思想和 MVI 架构，降低模块耦合
- 多模块化拆分，合理分包，便于维护
- 提供扩展函数，增强代码复用性和可读性

### 函数调用框架

函数调用框架是项目核心，支持 LLM 执行多样化操作：

- 统一管理函数，支持动态注册和扩展
- 标准化接口，简化开发流程
- 当前支持的功能（以手机控制为例）：
  - 获取屏幕视图树信息
  - 针对无障碍视图节点执行的 UI 操作（点击、滑动、文本设置）
  - 通过 Intent 和 URI 实现地图导航、电话拨打等
  - 显示 Toast 提示
  - 获取应用列表及前台应用信息
  - 模拟按键事件，如返回键
  - 执行特权 Shell 命令（安全性受控）

### 无障碍服务

通过 Android 无障碍服务 API，实现智能 UI 交互：

- 动态解析屏幕视图树，提取界面元素
- 支持复杂节点操作，如点击、滑动、文本设置

### Shell 执行系统

提供灵活的 Shell 命令执行框架：

- 支持普通权限、Root 权限及 Shizuku 权限
- 返回标准化执行结果，确保一致性

### Shizuku 集成

通过 Shizuku 服务，支持非 Root 环境下的特权操作：

- 封装权限请求和管理逻辑
- 实现高权限 Shell 命令执行
- 模块化设计，便于升级和维护



## 开发自定义功能

添加新功能只需以下步骤：

1. 定义新函数模型，遵循标准接口
2. 实现函数名、描述、参数及执行逻辑
3. 自动注册到函数管理框架
4. 返回统一格式的结果，便于 LLM 处理

```kotlin
package com.tv.tool.models

...

data object ToastModel : BaseFuncModel() {
    override val name: String = "display_toast"
    override val description: String =
        "向用户发送 toast 消息"
    override val parameters: List<Schema<*>> = listOf(
        Schema.str("msg", "toast 的消息内容")
    )
    override val requiredParameters: List<String> = listOf("msg")

    override suspend fun call(args: Map<String, Any?>): Map<String, Any?> {
        val msg = args.readAsString("msg") ?: return errorFuncCallMap()

        return try {
            withContext(Dispatchers.Main) {
                toast(msg)
            }

            successMap()
        } catch (t: Throwable) {
            errorMap(t)
        }
    }
}
```



## 框架扩展性

手机控制只是框架的一个典型应用。通过设计新工具函数，框架可支持任意场景，例如：

- 自动化测试：批量执行 UI 操作或脚本
- 跨应用集成：通过 Intent 实现复杂工作流
- 系统管理：结合 Shell 命令执行设备优化
- 自定义任务：根据用户需求开发特定功能