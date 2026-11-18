# 前端第三方库（本地 vendor）

不通过 CDN 加载；答辩/断网环境必须可用。

| 文件 | 版本 | 用途 |
|---|---|---|
| `gsap.min.js` | 3.12.7 | 动画核心 |
| `ScrollTrigger.min.js` | 3.12.7 | 卡片入场（M2）、阅读进度（M4） |
| `Flip.min.js` | 3.12.7 | 详情弹层（M1）、分类排序（M6） |
| `Draggable.min.js` | 3.12.7 | 后台分类拖拽（M6） |

- 来源：npm 包 `gsap@3.12.7` 的 `dist/` 目录，保留原始 license 文件头。
- 许可：GSAP Standard License，见 https://gsap.com/standard-license
- 加载顺序：`gsap` → `ScrollTrigger` → `Flip` → `Draggable` → 页面脚本，全部 `defer`。
- 升级时四个文件一起换版本，并按 `docs/UIUX-小红书.md` §8.1 回归六个动效点。
