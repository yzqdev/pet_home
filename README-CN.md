# Pet Home

[English](https://github.com/yzqdev/pet_home/blob/1.21.1-neoforge/README.md) | [简体中文](https://github.com/yzqdev/pet_home/blob/1.21.1-neoforge/README-CN.md)

本模组是 [Domestication Innovation（驯养革新）](https://modrinth.com/mod/domestication-innovation)的非官方高版本重制版（原模组似乎已停止维护），包含优化、新内容与重写。它与原模组并不完全相同，原模组的新功能不保证全部移植。

相关链接：[原版 CurseForge](https://www.curseforge.com/minecraft/mc-mods/domestication-innovation) | [原版 Modrinth](https://modrinth.com/mod/domestication-innovation)

## 简介

Pet Home 的创建只有一个目标：让 Minecraft 中可驯服的生物不仅有用、有趣，更让"驯服生物"成为一种值得选择的玩法——大幅改进驯服生物的行为方式与世界的交互。我们不仅扩展了原版中可驯服的生物种类，还修复了这些生物的诸多问题，新增了大量提升驯服体验的物品与方块，并引入了全新的**宠物附魔系统**。这些改动对原版生物和其他模组新增的可驯服生物同样生效。

安装本模组后，你的宠物再也不用宅在家里看戏——它们可以放心地陪你战斗、探索，不必担心永久失去它们，也不必担心它们被其他生物或模组宠物完全碾压。

## 功能列表

- 新增可附魔的项圈。
- 不同的附魔项圈赋予宠物不同的能力。
- 玩家不会伤害宠物和幼年生物。
- 新增出售附魔与宠物物品的村民。
- 宠物店会在村庄中生成（同时兼容 [Repurposed Structures](https://modrinth.com/mod/repurposed-structures) 的村庄变体）。

## 功能总览

### 更多可驯服生物

- 现在可以驯服**美西螈 / 狐狸 / 青蛙 / 马**（每种都可在配置中开关）。
  - **美西螈** — 喂**热带鱼桶**（或**热带鱼**）；4 次尝试中约 3 次成功。
  - **狐狸** — 给两只狐狸喂**甜浆果**使其繁殖：生下的幼狐就是驯服的。
  - **青蛙** — 喂**蜘蛛眼**（物品标签 `pet_home:tame_frogs_with`）；4 次尝试中约 3 次成功。
  - **兔子** — 喂**干草块**；2 次尝试中约 1 次成功。
  - **马 / 驴 / 骡 / 羊驼** — 按原版方式驯服（反复骑乘直到不再把你甩下）；本模组让它们成为可戴项圈附魔、睡宠物床、听指令的完整宠物。
- 给已驯服的兔子喂**阴恶胡萝卜**可将其变为杀手兔。
- 转换链：给马喂**烂苹果**变成**僵尸马**，再喂**阴恶胡萝卜**变成**骷髅马**。
- 宠物支持**三态指令**（坐下 / 跟随 / 自由活动）；**指挥鼓**方块可以把指令广播给周围你的所有宠物（支持红石触发）。

### 宠物项圈与 40 种宠物附魔

合成**项圈**给宠物戴上，再对宠物使用宠物附魔书即可附加附魔。40 种宠物附魔包括：

- **战斗类**：连锁闪电、冲击波、暗影之手、心理墙、磁性、类吸血鬼、冰霜利齿、幻影移形、麻痹、混乱之脑、暴力、平摊、恐吓
- **生存类**：额外生命、稳固、防火、免疫屏障、偏转、火焰保护、抗毒、冒泡、虚空之云、全面召回、生命虹吸、治疗之息、恢复生命、两栖化
- **实用类**：瞬速行者、夜视、物品栏关联、经验转移、矿物之味、群集、贪吃、洞察、爆炸无效
- **诅咒类**：枯萎诅咒、恶名诅咒、变小诅咒

### 物品与方块

- **宠物床**（16 种颜色）：宠物的家，记住它们归宿的位置。
- **迷途灯笼**：走远或被卸载的宠物会被传送回灯笼旁边。
- **羽毛钓竿**：用法类似钓竿，但可以把宠物"钓"回身边。
- **捕网 / 捕网发射器**：捕捉生物并放到你想放的地方。
- **契约**：转让（或解除）宠物的主人关系。
- **指挥鼓**：向附近的宠物广播坐下 / 跟随 / 自由活动指令，支持红石控制。
- **烂苹果 / 阴恶胡萝卜**：生物转换物品，见上文。
- **宠物罗盘**：右键打开宠物列表（名称 / 类型 / 位置 / 维度 / 状态），选中后可传送玩家到宠物或召回宠物到身边（两个功能由配置独立开关，默认均开启）。用指南针和皮革合成。

### 驯兽师村民

全新村民职业，共 5 个等级，出售宠物附魔书与宠物物品（本模组的附魔书由驯兽师出售，而不是图书匠）。

### 宠物信息面板

准星指向自己的宠物时，屏幕顶部会显示信息面板：名字、血量、指令状态、宠物床与宠物附魔。可在配置中改为按住潜行键才显示。

未安装 [Jade](https://modrinth.com/mod/jade) 时，只要看向自己的宠物面板就会出现；安装 Jade 后面板默认隐藏（Jade 的提示框已展示同样信息），把 `petInfoOverlayIgnoreJade` 设为 true 可恢复显示。

### 友好保护系统

主人不能伤害自己的宠物、宠物之间不能互相伤害、幼年生物受保护、尊重队伍规则——每条规则都是独立的配置项，还提供可配置的物品/生物黑白名单。

### 兼容性

- [Jade](https://modrinth.com/mod/jade)：在提示框中显示宠物的主人 / 指令信息。
- [车万女仆 Touhou Little Maid](https://www.curseforge.com/minecraft/mc-mods/touhou-little-maid) 联动。
- [Repurposed Structures](https://modrinth.com/mod/repurposed-structures)：宠物店也会在其村庄变体中生成（恶地、白桦、樱花、丛林、沼泽、蘑菇岛等）；原版村庄自动生成宠物店。

### 配置

- 游戏内完整设置界面（模组列表"配置"按钮或 `/pet_home_config` 命令，仅 1.20.1），或手动编辑 `config/pet_home.toml`。
 

## 游戏截图

| 宠物床 | 暗影之手 | 冲击波 |
| --- | --- | --- |
| ![has petbed](https://cdn.modrinth.com/data/cached_images/ec137249f938c6170a7cc6fa85d66d76339d44d4.png) | ![hand](https://cdn.modrinth.com/data/cached_images/ad532a01c32a3ae172bcd7bf4776fc376e212502.png) | ![sonic boom](https://cdn.modrinth.com/data/cached_images/7d7f37f8120281c14b6aeec0da54334fc015bbb9.jpeg) |

| 宠物信息面板（无 Jade） | 宠物罗盘 |
| --- | --- |
| ![宠物信息面板（无 Jade），zh](https://cdn.modrinth.com/data/cached_images/24ad466908bedbbddbb1d139311ba17f74dd3003.webp) | ![pet compass zh](https://cdn.modrinth.com/data/cached_images/58fba2924684ef0569e360487feec2f41e37f0c3_0.webp) |

| 友好保护系统 | 设置界面 |
| --- | --- |
| ![friendly fire](https://cdn.modrinth.com/data/cached_images/029b2f2060ef7b4134e149e60920eaafde7a5c1b_0.webp) | ![config screen](https://cdn.modrinth.com/data/cached_images/1333f626af50bfd15233076c8b0e9c1a29e4ea1b_0.webp) |

## 与原模组的区别

**重构与维护**

- **无需 Citadel** —— 宠物数据由本模组自行存储；原模组硬性依赖 [Citadel](https://modrinth.com/mod/citadel)。


**新系统与新内容**

- **友好保护系统** —— 每条规则独立可配置（含物品/生物黑白名单）。
- 在原版附魔之外**新增 9 本宠物附魔书**——**冲击波、稳固、平摊、暴力、麻痹、混乱之脑、夜视、经验转移、洞察**——且本模组的附魔书由新增的**驯兽师**村民出售，而不是图书匠。
- **原模组没有的全新功能**：**宠物罗盘**（宠物列表 + 双向传送）、**宠物信息面板** HUD、[Repurposed Structures](https://modrinth.com/mod/repurposed-structures) 村庄变体中的**宠物店**生成（原模组已支持原版村庄生成，本模组新增对其村庄变体的兼容）。

**体验优化**

- 完整的游戏内设置界面（1.20.1），不用手改 toml。
- [Jade](https://modrinth.com/mod/jade) 与[车万女仆](https://modrinth.com/mod/touhou-little-maid)集成。
