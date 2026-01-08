> 此文件由github copilot生成

# CCB - Minecraft 插件

项目 CCB 是一个用于 Bukkit/Paper (Spigot) 的 Minecraft 插件，提供在玩家潜行时生成并跟随的盔甲架特效、声音与粒子效果。插件兼容传统 Bukkit 调度器和 Folia（现代多线程调度器），并包含可配置项以调整行为与视觉效果。

**主要功能**
- 在玩家潜行时（sneak）生成盔甲架并跟随玩家位置
- 根据配置播放不同音效（例如 Thorns、Slime）
- 达到一定次数后触发喷射粒子与掉落物（例如牛奶桶）特效
- 支持伴侣检测（近距离的已启用玩家）以生成额外水花粒子效果
- 支持 Folia 调度器与 Bukkit 调度器自动切换

**环境与依赖**
- Java 17+（或插件编译/运行所需的 Java 版本）
- Bukkit / Spigot / Paper 服务器（与 `plugin.yml` 中声明的 API 版本兼容）
- Gradle（仓库内已包含 Gradle Wrapper）

**构建**
1. 在 Windows 上运行：

```
gradlew.bat build
```

2. 在 Unix / macOS 上运行：

```
./gradlew build
```

构建成功后，插件 Jar 文件通常位于 `build/libs/` 目录下。将生成的 Jar 文件放到 Minecraft 服务器的 `plugins/` 目录，然后重启或加载插件。

**配置**
配置文件位于插件资源目录中（`src/main/resources/config.yml`），部署到服务器后会复制到插件数据目录下的 `config.yml`。以下是常见的可配置项示例（以代码中可见项为准）：

- `debug`：布尔值，开启后输出调试日志
- `sneak-count.min`：潜行计数触发下限
- `sneak-count.max`：潜行计数触发上限
- `offsetX`, `offsetY`, `offsetZ`：盔甲架相对玩家的位置偏移
- `soundThorns`, `soundSlime`：是否播放对应音效
- `sizeSmall`：盔甲架是否为小尺寸
- `alwaysOn`：某些性别设置下是否总是启用

实际的完整配置项请参考 `config.yml` 或项目源码中的 `PlayerData` 与 `PlayerListener`。

**运行与调试**
- 在开发时可以使用内置的 Gradle Wrapper 来编译并复制 Jar 到本地测试服务器。
- 若使用 IDE（如 IntelliJ IDEA），建议使用与项目一致的 Java 版本并导入 Gradle 项目。

**查看命令与元数据**
插件的命令与权限在 `plugin.yml` 中声明，若要了解使用方式请查看该文件。

**贡献**
- 欢迎通过 Pull Request 或 Issue 提交 bug 报告与改进建议。

**许可证**
- 项目中未显式包含许可证信息时，请在合并或分发前确认版权与许可证要求。

---

如果你希望，我可以：
- 根据 `config.yml` 自动生成更详细的配置说明段落；
- 帮你生成一个简单的部署脚本或 GitHub Actions 用于自动构建。
