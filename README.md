<p align="center">
  <img src="./app/src/androidMain/res/mipmap-xxxhdpi/ic_launcher_round.png" alt="HeartWith" width="150"/>
</p>

<h1 align="center">HeartWith</h1>

<p align="center">
  Android BLE 心率采集客户端 · 通过蓝牙低功耗连接手环/心率带，实时采集并上传心率数据
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-API 26%2B-green" alt="minSdk 26"/>
  <img src="https://img.shields.io/badge/Kotlin-Multiplatform-blue" alt="KMP"/>
  <img src="https://img.shields.io/badge/license-MIT-orange" alt="MIT License"/>
</p>

---

## 功能特性

- **BLE 心率采集** — 通过标准 Heart Rate Service (0x180D) 订阅手环/心率带的实时心率数据
- **自动扫描连接** — 低功耗扫描附近 BLE 设备，优先识别心率服务，支持手动选择或自动重连上次设备
- **后台持续采集** — 前台服务保活，APP 切到后台后依然保持 BLE 连接和数据采集
- **批量上传** — 心率数据在本地缓存后批量上传至服务器，支持 CBOR 序列化压缩传输
- **断线自动重连** — 设备断开后自动扫描重连，采用指数退避策略减少功耗
- **开机自启动** — 监听 BOOT_COMPLETED 广播，重启后自动恢复采集
- **通知栏状态** — 前台服务通知显示当前心率、采集模式和上传状态

## 技术栈

| 层级 | 技术 |
|------|------|
| 语言 | Kotlin Multiplatform |
| UI | Compose Multiplatform + MiuiX |
| 网络 | Ktor Client (OkHttp) + CBOR |
| 序列化 | kotlinx.serialization |
| 蓝牙 | Android BLE API (GATT) |
| 后台 | Foreground Service + AlarmManager |
| 构建 | Gradle Kotlin DSL + AGP |

## 项目结构

```
HeartWith/
├── app/                          # Android 应用模块
│   ├── build.gradle.kts
│   └── src/androidMain/
│       ├── AndroidManifest.xml
│       ├── kotlin/com/heartwith/app/
│       │   ├── MainActivity.kt                 # 主界面 + Compose UI
│       │   ├── AndroidHeartRateCollector.kt    # BLE 扫描/连接/数据采集核心
│       │   ├── HeartRateForegroundService.kt   # 前台服务 + 后台保活
│       │   ├── HeartRateCollectorRuntime.kt    # 采集器单例管理
│       │   ├── HeartwithAutoStartReceiver.kt   # 开机自启动广播接收器
│       │   └── HeartwithRestartReceiver.kt     # 闹钟触发的重启接收器
│       └── res/                                # 图标、样式等资源
├── shared/                       # KMP 共享模块
│   └── src/commonMain/kotlin/com/heartwith/shared/
│       ├── HeartRateProtocol.kt   # 心率数据解析 + 批量缓冲
│       ├── HeartwithApi.kt        # HTTP API 客户端
│       ├── Models.kt              # 数据模型定义
│       ├── HeartwithScreen.kt     # Compose UI 界面
│       └── HeartwithTheme.kt      # 主题配置
├── gradle/                       # Gradle 配置
├── ci/                           # CI 签名文件
├── .github/workflows/            # GitHub Actions
└── LICENSE
```

## 构建

### 环境要求

- JDK 17
- Android SDK (compileSdk 37)
- Gradle 8.x

### 本地构建

```bash
# Debug 版本
./gradlew assembleDebug

# Release 版本
./gradlew assembleRelease
```

产物路径：`app/build/outputs/apk/release/HeartWith-{version}-release.apk`

### CI/CD

项目配置了 GitHub Actions：

- **build.yml** — PR 或手动触发时构建 Debug APK
- **release.yml** — 发布流程（签名 + Release APK）

## 权限说明

| 权限 | 用途 |
|------|------|
| `BLUETOOTH_SCAN` | 扫描附近 BLE 设备 |
| `BLUETOOTH_CONNECT` | 连接 BLE 设备并进行 GATT 通信 |
| `FOREGROUND_SERVICE` | 前台服务保活 |
| `FOREGROUND_SERVICE_CONNECTED_DEVICE` | Android 14+ 前台服务类型 |
| `WAKE_LOCK` | 保持 CPU 唤醒以维持 BLE 连接 |
| `SCHEDULE_EXACT_ALARM` | 精确定时器用于后台恢复 |
| `RECEIVE_BOOT_COMPLETED` | 开机自启动 |
| `POST_NOTIFICATIONS` | 显示采集状态通知 |
| `INTERNET` | 上传心率数据至服务器 |

## 工作原理

```
手环/心率带                    Android 客户端                   服务器
   │                              │                              │
   │◄── BLE 扫描发现 ─────────────│                              │
   │                              │                              │
   │── GATT 连接建立 ────────────►│                              │
   │                              │                              │
   │── HR Measurement 通知 ──────►│  缓存到 HeartRateBatcher     │
   │   (0x2A37)                   │  (前台 10s / 后台 30s 一批)  │
   │                              │                              │
   │                              │── CBOR 批量上传 ────────────►│
   │                              │   POST /api/v1/hr/batches    │
   │                              │                              │
   │                              │◄── 上传确认 + 策略调整 ──────│
```

## 服务器

客户端默认连接 `http://10.0.2.2:8000`（Android 模拟器回环地址），可在 APP 内修改服务器地址。

服务端 API 端点：

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/collector/sessions` | 创建采集会话 |
| POST | `/api/v1/hr/batches` | 上传心率数据批次 |
| GET | `/api/v1/lobby/participants` | 获取大厅参与者 |
| GET | `/api/v1/participants/{id}/series` | 获取参与者心率序列 |

## License

[MIT](LICENSE) LICENSE
