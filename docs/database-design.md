# 电商平台数据库设计说明

## 1. 设计目标

本数据库面向 Spring Boot 3.x 电商平台后端，覆盖用户端与管理端核心业务：

- 用户注册、登录、资料维护、地址管理
- 管理员登录、角色权限管理、用户管理
- 商品分类、商品、SKU、图片管理
- 商品浏览、搜索、收藏、评价
- 购物车管理
- 订单创建、支付、状态流转与后台订单管理
- 轮播图、公告等运营内容管理

## 2. 通用规范

- 数据库：`ecommerce`
- 存储引擎：`InnoDB`
- 字符集：`utf8mb4`
- 排序规则：`utf8mb4_unicode_ci`
- 主键：所有业务表使用 `BIGINT AUTO_INCREMENT`
- 时间字段：所有表包含 `create_time`、`update_time`
- 逻辑删除：所有表包含 `deleted`，默认 `0` 表示未删除，`1` 表示已删除
- 金额字段：统一使用 `DECIMAL(10,2)`
- 状态字段：统一使用 `TINYINT`

## 3. 表模块划分

### 3.1 系统与用户模块

| 表名 | 说明 |
| --- | --- |
| `sys_user` | 用户表，同时支持普通用户与管理员 |
| `sys_role` | 角色表 |
| `sys_permission` | 权限表 |
| `sys_user_role` | 用户角色关联表 |
| `sys_role_permission` | 角色权限关联表 |
| `ums_user_address` | 用户收货地址表 |

`sys_user.user_type` 用于区分用户类型：

- `1`：普通用户
- `2`：管理员

`sys_role` 与 `sys_permission` 支持 RBAC 权限模型，管理端可基于角色分配菜单和接口权限。

### 3.2 商品模块

| 表名 | 说明 |
| --- | --- |
| `pms_category` | 商品分类表，支持多级分类 |
| `pms_product` | 商品 SPU 主表 |
| `pms_product_sku` | 商品 SKU 表 |
| `pms_product_image` | 商品图片表 |
| `pms_user_favorite` | 用户商品收藏表 |
| `pms_product_review` | 商品评价表 |

商品模型采用 SPU + SKU：

- `pms_product` 存储商品公共信息，如名称、详情、主图、基础价格、总库存
- `pms_product_sku` 存储具体规格，如颜色、容量、内存、独立价格与库存
- `pms_product_sku.attributes` 使用 JSON 存储规格属性，便于扩展

商品搜索支持：

- `pms_product.name` 普通索引
- `FULLTEXT(name, subtitle)` 全文索引
- 分类、状态、排序字段组合索引

### 3.3 购物车模块

| 表名 | 说明 |
| --- | --- |
| `ums_cart_item` | 购物车明细表 |

购物车通过 `user_id + product_id + sku_id` 唯一约束防止同一用户重复加入同一商品规格。

### 3.4 订单与支付模块

| 表名 | 说明 |
| --- | --- |
| `oms_order` | 订单主表 |
| `oms_order_item` | 订单明细表 |
| `oms_payment_record` | 支付记录表 |
| `oms_order_status_log` | 订单状态日志表 |

订单状态建议约定：

| 状态值 | 说明 |
| --- | --- |
| `0` | 待支付 |
| `1` | 已支付/待发货 |
| `2` | 已发货/待收货 |
| `3` | 已完成 |
| `4` | 已关闭 |
| `5` | 已取消 |
| `6` | 退款中 |
| `7` | 已退款 |

支付方式建议约定：

| 状态值 | 说明 |
| --- | --- |
| `1` | 支付宝 |
| `2` | 微信支付 |
| `3` | 银行卡 |
| `4` | 余额支付 |

支付状态建议约定：

| 状态值 | 说明 |
| --- | --- |
| `0` | 待支付 |
| `1` | 支付成功 |
| `2` | 支付失败 |
| `3` | 已退款 |

### 3.5 内容运营模块

| 表名 | 说明 |
| --- | --- |
| `cms_banner` | 轮播图表 |
| `cms_announcement` | 公告表 |

用于管理首页轮播图、运营跳转链接、系统公告等内容。

## 4. 关键关系说明

- `sys_user` 通过 `sys_user_role` 关联 `sys_role`
- `sys_role` 通过 `sys_role_permission` 关联 `sys_permission`
- `ums_user_address.user_id` 关联 `sys_user.id`
- `pms_product.category_id` 关联 `pms_category.id`
- `pms_product_sku.product_id` 关联 `pms_product.id`
- `pms_product_image.product_id` 关联 `pms_product.id`
- `pms_user_favorite.user_id` 关联 `sys_user.id`
- `pms_user_favorite.product_id` 关联 `pms_product.id`
- `pms_product_review.user_id` 关联 `sys_user.id`
- `pms_product_review.product_id` 关联 `pms_product.id`
- `pms_product_review.order_item_id` 关联 `oms_order_item.id`
- `ums_cart_item.user_id` 关联 `sys_user.id`
- `ums_cart_item.product_id` 关联 `pms_product.id`
- `ums_cart_item.sku_id` 关联 `pms_product_sku.id`
- `oms_order.user_id` 关联 `sys_user.id`
- `oms_order_item.order_id` 关联 `oms_order.id`
- `oms_payment_record.order_id` 关联 `oms_order.id`
- `oms_order_status_log.order_id` 关联 `oms_order.id`

## 5. 索引设计说明

### 5.1 唯一索引

- `sys_user.username`：保证用户名唯一
- `sys_user.phone`：保证手机号唯一
- `sys_user.email`：保证邮箱唯一
- `sys_role.role_code`：保证角色编码唯一
- `sys_permission.permission_code`：保证权限编码唯一
- `sys_user_role(user_id, role_id)`：避免重复绑定角色
- `sys_role_permission(role_id, permission_id)`：避免重复绑定权限
- `pms_product_sku.sku_code`：保证 SKU 编码唯一
- `pms_user_favorite(user_id, product_id)`：避免重复收藏
- `ums_cart_item(user_id, product_id, sku_id)`：避免购物车重复项
- `oms_order.order_no`：保证订单号唯一

### 5.2 查询索引

- 用户查询：`status`、`user_type`、`deleted`
- 分类查询：`parent_id`、`status + sort`
- 商品列表：`category_id`、`status + sort`、`name`、`deleted`
- 商品搜索：`FULLTEXT(name, subtitle)`
- 购物车：`user_id`、`product_id`、`sku_id`
- 订单列表：`user_id`、`order_status`、`create_time`
- 支付记录：`order_id`、`order_no`、`payment_status`
- 内容管理：`status + sort`、时间范围字段

## 6. 初始数据说明

`db/schema.sql` 内置以下初始数据：

- 管理员账号：`admin`
- 测试用户账号：`testuser`
- 默认角色：`ADMIN`、`USER`
- 默认权限菜单：系统管理、商品管理、订单管理、内容管理
- 默认商品分类：手机数码、电脑办公、家用电器、服饰鞋包、食品生鲜等
- 示例商品：智能手机、轻薄笔记本
- 示例轮播图、公告

初始用户密码字段使用 BCrypt 密文。实际项目中建议通过后端注册或重置密码接口重新生成密码，避免长期使用示例密文。

## 7. 使用方式

在 MySQL 8.0 中执行：

```sql
SOURCE db/schema.sql;
```

或在命令行执行：

```bash
mysql -uroot -p123456 < db/schema.sql
```

## 8. 后续扩展建议

后续可继续扩展以下模块：

- 优惠券：`sms_coupon`、`sms_coupon_user`
- 秒杀活动：`sms_flash_sale`、`sms_flash_sale_product`
- 物流信息：`oms_delivery`、`oms_delivery_trace`
- 售后退款：`oms_refund`、`oms_after_sale`
- 商品属性模板：`pms_attribute`、`pms_attribute_group`
- 库存流水：`pms_stock_log`
- 操作日志：`sys_operation_log`
