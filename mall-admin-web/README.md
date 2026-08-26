# Mall Admin Web

Mall MVP 的运营管理端，基于 Vue 3、TypeScript、Vite、Element Plus 和 Pinia。

当前已接入同仓库 Spring Boot 后端的 `/api/admin/**` 接口，覆盖：

- 管理员登录、JWT 鉴权和基于角色/资源的权限控制
- 品牌、分类、属性、商品和 SKU 管理
- 订单列表、订单详情与发货
- 会员查询、详情与启用/禁用
- 退款查询、审核通过与拒绝
- 管理员、角色、资源及其分配关系

## 本地运行

需要 Node.js `^20.19.0 || >=22.12.0`，并确保后端运行在
`http://localhost:8080`。

```bash
npm ci
npm run dev
```

默认访问 <http://localhost:5173>。开发环境由 Vite 将 `/api` 代理到后端；
生产环境请求同域 `/api`，由 Nginx 或网关转发。

## 常用命令

| 命令 | 说明 |
| --- | --- |
| `npm run dev` | 启动开发服务器 |
| `npm run build` | 类型检查并生成生产包 |
| `npm run lint` | ESLint 检查并自动修复 |
| `npm run preview` | 本地预览生产包 |

项目整体说明、数据库初始化和 Docker 启动方式见[仓库根 README](../README.md)。

## 上游与许可证

本管理端基于 [macrozheng/mall-admin-web](https://github.com/macrozheng/mall-admin-web)
改造，原项目采用 Apache License 2.0，详见当前目录的 [LICENSE](LICENSE)。
