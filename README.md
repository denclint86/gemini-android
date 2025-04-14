# PhoneBot - LLM驱动的Android无障碍控制

这是一个利用大语言模型（LLM）通过Android无障碍服务来控制手机的项目。该项目允许LLM接收用户的指令，并通过函数调用执行相应的操作来控制Android设备。

## 项目概述

PhoneBot利用Google的Gemini模型的能力，结合Android无障碍服务API，实现了智能化的手机操作控制。通过自然语言交互，用户可以指示LLM执行各种手机操作，而无需直接与设备交互。

## 主要功能

- 基于Gemini的自然语言理解与处理
- 通过函数调用框架执行设备操作
- 利用Android无障碍服务实现UI交互与界面元素分析
- 支持多轮对话的聊天界面
- 执行`Shell`命令（支持通过`Root`或`Shizuku`运行特权命令）
- 显示Toast消息通知
- 获取当前屏幕视图元素信息
- 查找和操作特定界面节点
- 获取屏幕尺寸和指标信息
- 支持滚动和点击操作
- 追踪前台应用变化
- 获取应用列表和前台应用信息
- 自动循环切换`api-key`以避免请求限制

## 技术实现

### LLM集成

项目使用Google的Gemini AI模型，通过官方SDK进行集成：
- 使用`GenerativeModel`与Gemini进行通信
- 支持流式响应处理
- 利用ChatViewModel管理对话状态和多轮对话流程
- 通过ChatManager实现LLM响应的处理和函数调用解析
- 实现`api-key`自动轮换机制，每次请求自动切换`api-key`

### `api-key`管理系统

为了解决API调用频率限制问题，项目实现了`api-key`轮换系统：
- 支持配置多个`api-key`存储在资源文件中
- 每次请求自动切换到下一个可用的`api-key`
- 使用`AtomicInteger`实现无锁切换，适合低并发场景
- 记录API切换频率，提供每分钟切换次数统计
- 通过DataStore持久化当前`api-key`索引，确保应用重启后连续性

### 函数调用框架

函数调用是本项目的核心，允许LLM执行预定义的操作：

- **函数管理器**：`FuncManager`统一管理所有可用功能函数
- **基础函数模型**：`BaseFuncModel`定义函数调用的标准接口和规范
- **功能实现**：
  - 屏幕视图获取（`VisibleViewsModel`）
  - `Shell`命令执行（`ShellExecutorModel`）
  - Toast消息显示（`ToastModel`）
  - 屏幕节点查找（`FindNodeModel`）
  - 屏幕尺寸获取（`ScreenMetricsModel`）
  - 界面滚动操作（`ScrollModel`）
  - 获取应用列表（`GetAppListModel`）
  - 获取前台应用信息（`GetForegroundAppInfoModel`）

### 无障碍服务实现

项目利用Android的无障碍服务API实现界面交互，主要功能包括：

- 自动收集和分析当前屏幕元素
- 提供界面元素的结构化描述
- 支持基于无障碍服务的界面操作
- 通过`AccessibilityServiceHelper`提供统一的操作接口
- 使用`AccessibilityNodeInfo`进行节点查找和操作
- 通过`ForegroundAppManager`监控前台应用变化

## `Shell`执行系统

项目实现了一个灵活的`Shell`命令执行系统：

- 支持三种执行方式：普通用户权限、Root权限和`Shizuku`权限
- 使用策略模式实现不同执行器：`UserExecutor`、`RootExecutor`和`ShizukuExecutor`
- 通过`ShellManager`提供统一的`Shell`命令执行入口
- 返回标准化的`ShellResult`结果

## `Shizuku`集成

项目整合了`Shizuku`服务，提供了在非`Root`环境下执行特权命令的能力：

- 实现了`Shizuku`权限请求和管理逻辑
- 利用`Shizuku`执行高权限`Shell`命令
- 单独模块化封装`Shizuku`功能，便于维护和更新

## 开发自定义功能

要添加新的函数调用功能，需要遵循以下步骤：

1. 创建新的函数模型类，继承`BaseFuncModel`
2. 实现必要的抽象方法和属性：`name`、`description`、`parameters`和`call`
3. 使用`data object`定义实现类，自动注册到`FuncManager`
4. 确保函数返回统一格式的Map类型

## 项目依赖

- Google Generative AI SDK - Gemini模型访问
- Android Accessibility Service API - 无障碍服务支持
- `Shizuku` API - 用于执行特权命令
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
- 安装`Shizuku`应用或拥有`Root`权限（用于执行特权命令）

## 当前状态与计划

- ✅ 基础无障碍服务框架
- ✅ 函数调用管理系统
- ✅ 核心功能：屏幕分析、`Shell`执行、Toast显示
- ✅ 基于Gemini的对话界面
- ✅ `Shizuku`服务集成
- ✅ 节点查找与操作功能
- ✅ 屏幕滚动操作支持
- ✅ 前台应用监控
- ✅ `api-key`轮换系统
- ✅ 获取屏幕指标信息功能
- ✅ 优化界面节点查找算法
- ✅ 改进UI交互体验
- ✅ 增强无障碍服务的元素操作能力
- ✅ 基本的点击操作功能
- 🔄 完善手势控制（如复杂的多指操作和自定义手势）
- ✅ 支持基本的多轮对话场景
- 🔄 添加本地模型集成选项
- 🔄 改进对话历史管理
- 🔄 支持自定义LLM换源
- 🔄 支持更全面的应用内Action调用