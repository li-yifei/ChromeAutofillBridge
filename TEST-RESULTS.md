# 实机验证记录 — 2026-09-07

## Vivaldi 适配验证 — 2026-10-26

- 官方 ARM64 APK：Vivaldi 8.2.4147.173，包名 `com.vivaldi.browser`。
- Android 15 Lenovo TB322FC 上安装成功；查询其 `AutofillThirdPartyModeContentProvider` 返回 `autofill_third_party_state=0`。
- DEX 中保留 `WebContents`、`GURL` 类型；`WebContentsImpl` 的两个无参 GURL 方法为 `k`、`u`，GURL 的 String 字段为 `a`，符合现有反射策略的类型形状。
- 本地适配加入 Vivaldi 主进程、LSPosed scope，并按浏览器包名查询 Provider。`testDebugUnitTest assembleDebug lintDebug` 通过。
- 当前平板的 LSPosed 1.9.2-it (7502) 管理器显示 API 100；模块要求 API 101+。模块的实际加载、网页来源绑定、inline 候选、点选填入及内置自动填充共存仍待 API 101+ 设备验证。
- 后续在 Pixel 11 Pro XL / Android 17 / LSPosed 2.2.1 API 102 实测，模块成功加载。
- Vivaldi 的 content view 覆盖了虚拟结构和填入方法；适配这两个入口，统一使用 Android 兼容结构及其 accessibility ID，保留来源检查。
- 用户在 Bitwarden 创建 autofill.me 测试记录后，Vivaldi 出现浮动候选和 Gboard inline 候选；点击 inline 候选后用户名变为 `auto`。用户也确认两种候选形态均已出现。密码具体值未读取，表单未提交。
- 浏览器内置密码、支付方式和地址入口同时可见；完整卡片、TOTP、passkey 流程仍待验证。
- 用户同时确认该记录在 Chrome 生效。

## 域名丢失复现

在 `https://fill.dev/form/login-simple` 聚焦输入框，兼容结构有 122 个节点、3 个输入节点，`webDomain=0`。第三方 AutofillService 只能按 Chrome 包名匹配。这是早期版本的缺陷；“出现候选并填入”本身不足以证明网站匹配正确。

## 根因定位

1. Android `View.populateVirtualStructure` 复制输入类型、值与虚拟 ID，遗漏网页来源。
2. Chrome 的兼容桥没有把当前文档来源写入兼容节点。
3. 给文档根节点设置域名后，深层字段仍可能回退到包名；深层节点需要各自的来源元数据。

## 修复验证

| 场景 | 观察 | 结果 |
|---|---|---|
| fill.dev 登录表单 | 122 节点、19 个节点有域名；Bitwarden 显示“fill.dev用のアイテム” | 通过 |
| 同一 tab 从 fill.dev 导航到 example.com，临时添加合成登录表单 | 第三方候选更新为“example.com用のアイテム” | 通过 |
| 顶层 HTTPS 页中加入 sandbox=allow-scripts 的 srcdoc iframe | 结构保留顶层输入、排除 iframe 输入；聚焦 iframe 后 Gboard 无第三方候选 | 通过 |
| Chrome Google 模式 | `autofill_third_party_state=0`；Google 原生密码/卡片/地址入口仍显示 | 通过 |
| API 与产物 | API 102 框架成功加载 API 101 模块；编译、安装和 APK 签名校验通过 | 通过 |

合成测试只改变当前测试 tab 的 DOM，不向服务器提交表单。没有创建测试凭据。测试结束通过导航清除合成页面。

## 尚待验证

- 其他第三方 AutofillService 的 inline 候选与点选填入。
- Google 信用卡完整填入、第三方 TOTP，以及 Google/第三方两家的 passkey 完整登录。
- 所有重定向/同源换页时序、无痕、多 Activity、其他版本的回归。

原始诊断代码已移除；统计值与 UI 的域名标题是本记录的证据。跨来源 iframe 已采取排除策略；同源 iframe 暂时也被排除。

测试时开启的保持亮屏设置已恢复为原值 0；临时 ADB DevTools 转发已移除。

## 用户回归反馈 — v0.3.0

用户确认工作资料（work profile）中的 1Password 可用。该反馈覆盖用户实际测试的工作资料环境；1Password 的具体版本和逐项流程未记录，passkey、信用卡与 TOTP 仍按上文列为待验证。
