# PhoneBot - LLM驱动的Android无障碍控制

这是一个利用大语言模型（LLM）通过Android无障碍服务来控制手机的项目。该项目允许LLM接收用户的指令，并通过函数调用执行相应的操作来控制Android设备。

## 项目概述

PhoneBot利用Google的Gemini模型的能力，结合Android无障碍服务API，实现了智能化的手机操作控制。通过自然语言交互，用户可以指示LLM执行各种手机操作，而无需直接与设备交互。

## 主要功能

- 基于Gemini的自然语言理解与处理
- 通过函数调用框架执行设备操作
- 利用Android无障碍服务实现UI交互与界面元素分析
- 支持多轮对话的聊天界面
- 执行Shell命令（支持通过Shizuku运行特权命令）
- 显示Toast消息通知
- 获取当前屏幕视图元素信息

## 技术实现

### LLM集成

项目使用Google的Gemini AI模型，通过官方SDK进行集成：
- 使用`GenerativeModel`与Gemini进行通信
- 支持流式响应处理
- 利用ChatViewModel管理对话状态和多轮对话流程

### 函数调用框架

函数调用是本项目的核心，允许LLM执行预定义的操作：

- **函数管理器**：`FuncManager`统一管理所有可用功能函数
- **基础函数模型**：`BaseFuncModel`定义函数调用的标准接口和规范
- **功能实现**：
  - 屏幕视图获取（`VisibleViewsModel`）
  - Shell命令执行（`ShellExecutorModel`）
  - Toast消息显示（`ToastModel`）

### 无障碍服务实现

项目利用Android的无障碍服务API实现界面交互，主要功能包括：

- 自动收集和分析当前屏幕元素
- 提供界面元素的结构化描述
- 支持基于无障碍服务的界面操作

## Shizuku集成

项目整合了Shizuku服务，提供了在非Root环境下执行特权命令的能力：

- 支持通过ADB或Root方式启动Shizuku服务
- 实现了Shizuku权限请求和管理逻辑
- 利用Shizuku执行高权限Shell命令

## 开发自定义功能

要添加新的函数调用功能，需要遵循以下步骤：

1. 创建新的函数模型类，继承`BaseFuncModel`
2. 实现必要的抽象方法和属性
3. 使用`data object`定义实现类，自动注册到`FuncManager`
4. 确保函数返回统一格式的Map类型，推荐使用`defaultMap`方法创建标准格式

## 项目依赖

- Google Generative AI SDK - Gemini模型访问
- Android Accessibility Service API - 无障碍服务支持
- Shizuku API - 用于执行特权命令
- Zephyr工具库 - UI组件、网络、日志等基础功能
  - vbclass - ViewBinding简化
  - scaling-layout - UI适配
  - global-values - 全局常量
  - datastore - 数据存储
  - net - 网络操作
  - log - 日志工具
  - extension - Kotlin扩展

## 系统要求

- Android 8.0 (API 26) 或更高版本
- 安装Shizuku应用（用于执行特权命令）
- Google Play Service（用于Gemini API访问）

## 当前状态与计划

- ✅ 基础无障碍服务框架
- ✅ 函数调用管理系统
- ✅ 核心功能：屏幕分析、Shell执行、Toast显示
- ✅ 基于Gemini的对话界面
- ✅ Shizuku服务集成
- 🔄 改进UI交互体验
- 🔄 增强无障碍服务的操作能力
- 🔄 添加更多实用的设备控制功能
- 🔄 支持更复杂的多轮对话场景