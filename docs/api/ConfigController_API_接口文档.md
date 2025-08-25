# ConfigController API 接口文档

## 概述
ConfigController 提供交通配置系统的配置管理功能，严格实现以下约束：
- **所有参数只能修改，不能删除，也不能增加**
- **检测点参数只读，不允许增加，也不允许修改**

**Base URL**: `/api/config`

---

## 🔍 查询接口

### 1. 获取完整配置

**接口**: `GET /full`

**描述**: 获取系统完整配置信息

**请求参数**: 无

**响应示例**:
```json
{
  "global": {
    "allRed": 120,
    "maxAllRed": 300,
    "platformUrl": "122.5.105.22:2020",
    "signalControllerList": [
      {"name": "上行入口信号机", "id": "37"},
      {"name": "信号机2", "id": "38"}
    ]
  },
  "segments": {
    "size": 4,
    "segmentList": [
      {
        "segmentId": 1,
        "name": "路段1",
        "length": 1000,
        "minRed": 5,
        "maxRed": 60,
        "minGreen": 5,
        "maxGreen": 15,
        "upsigid": "37",
        "downsigid": "38"
      }
    ]
  },
  "detectPoints": {
    "detectPointList": [
      {
        "index": 1,
        "details": "路段1上行检测"
      }
    ]
  },
  "waitingAreas": {
    "waitingAreas": [
      {
        "index": 1,
        "upCapacity": 2,
        "downCapacity": 2
      }
    ]
  }
}
```

---

### 2. 获取全局配置

**接口**: `GET /global`

**描述**: 获取全局配置参数

**请求参数**: 无

**响应示例**:
```json
{
  "allRed": 120,
  "maxAllRed": 300,
  "platformUrl": "122.5.105.22:2020",
  "signalControllerList": [
    {"name": "上行入口信号机", "id": "37"},
    {"name": "信号机2", "id": "38"},
    {"name": "信号机3", "id": "41"},
    {"name": "信号机4", "id": "42"},
    {"name": "下行入口信号机", "id": "43"}
  ]
}
```

---

### 3. 获取所有路段配置

**接口**: `GET /segments`

**描述**: 获取所有路段配置列表

**请求参数**: 无

**响应示例**:
```json
[
  {
    "segmentId": 1,
    "name": "路段1",
    "length": 1000,
    "minRed": 5,
    "maxRed": 60,
    "minGreen": 5,
    "maxGreen": 15,
    "upsigid": "37",
    "downsigid": "38"
  },
  {
    "segmentId": 2,
    "name": "路段2",
    "length": 1000,
    "minRed": 5,
    "maxRed": 60,
    "minGreen": 5,
    "maxGreen": 15,
    "upsigid": "38",
    "downsigid": "41"
  }
]
```

---

### 4. 根据信号灯ID获取路段配置

**接口**: `GET /segments/{sigid}`

**描述**: 根据信号灯ID获取特定路段配置

**请求参数**:
- `sigid` (Path): 信号灯ID，例如 "37"

**响应示例**:
```json
{
  "segmentId": 1,
  "name": "路段1",
  "length": 1000,
  "minRed": 5,
  "maxRed": 60,
  "minGreen": 5,
  "maxGreen": 15,
  "upsigid": "37",
  "downsigid": "38"
}
```

**错误响应**:
```json
// 404 Not Found
{
  "timestamp": "2025-08-20T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "path": "/api/config/segments/99"
}
```

---

### 5. 根据名称获取路段配置

**接口**: `GET /segments/byname/{name}`

**描述**: 根据路段名称获取配置

**请求参数**:
- `name` (Path): 路段名称，例如 "路段1"

**响应示例**:
```json
{
  "segmentId": 1,
  "name": "路段1",
  "length": 1000,
  "minRed": 5,
  "maxRed": 60,
  "minGreen": 5,
  "maxGreen": 15,
  "upsigid": "37",
  "downsigid": "38"
}
```

---

### 6. 获取所有检测点配置（只读）

**接口**: `GET /detectpoints`

**描述**: 获取所有检测点配置，仅查询用途

**请求参数**: 无

**响应示例**:
```json
[
  {
    "index": 1,
    "details": "路段1上行检测"
  },
  {
    "index": 2,
    "details": "路段1下行检测"
  },
  {
    "index": 3,
    "details": "路段2上行检测"
  }
]
```

---

### 7. 根据索引获取检测点配置（只读）

**接口**: `GET /detectpoints/{index}`

**描述**: 根据索引获取特定检测点配置

**请求参数**:
- `index` (Path): 检测点索引，例如 1

**响应示例**:
```json
{
  "index": 1,
  "details": "路段1上行检测"
}
```

---

### 8. 获取所有等待区配置

**接口**: `GET /waitingareas`

**描述**: 获取所有等待区配置列表

**请求参数**: 无

**响应示例**:
```json
[
  {
    "index": 1,
    "upCapacity": 2,
    "downCapacity": 2
  },
  {
    "index": 2,
    "upCapacity": 2,
    "downCapacity": 2
  },
  {
    "index": 3,
    "upCapacity": 2,
    "downCapacity": 2
  }
]
```

---

### 9. 根据索引获取等待区配置

**接口**: `GET /waitingareas/{index}`

**描述**: 根据索引获取特定等待区配置

**请求参数**:
- `index` (Path): 等待区索引，例如 1

**响应示例**:
```json
{
  "index": 1,
  "upCapacity": 2,
  "downCapacity": 2
}
```

---

## ✏️ 修改接口

### 10. 更新全局配置

**接口**: `PUT /global`

**描述**: 更新全局配置参数（仅允许修改AllRed和MaxAllRed）

**请求参数**:
```json
{
  "allRed": 150,
  "maxAllRed": 350
}
```

**字段说明**:
- `allRed`: 全红时间（秒），范围：1-600
- `maxAllRed`: 最大全红时间（秒），范围：1-1200，且必须 >= allRed

**成功响应**:
```json
{
  "success": true,
  "message": "全局配置更新成功",
  "data": {
    "allRed": 150,
    "maxAllRed": 350
  },
  "timestamp": 1692537600000
}
```

**错误响应**:
```json
{
  "success": false,
  "message": "AllRed参数必须大于0",
  "timestamp": 1692537600000,
  "status": 400
}
```

---

### 11. 更新路段配置

**接口**: `PUT /segments/{sigid}`

**描述**: 更新特定路段的配置参数（不允许修改segmentId和upsigid）

**请求参数**:
- `sigid` (Path): 信号灯ID

**请求体**:
```json
{
  "segmentId": 1,
  "name": "路段1_修改",
  "length": 1200,
  "minRed": 6,
  "maxRed": 65,
  "minGreen": 6,
  "maxGreen": 18,
  "upsigid": "37",
  "downsigid": "38"
}
```

**字段说明**:
- `name`: 路段名称
- `length`: 路段长度（米），必须 > 0
- `minRed`: 最小红灯时间（秒），必须 > 0
- `maxRed`: 最大红灯时间（秒），必须 >= minRed
- `minGreen`: 最小绿灯时间（秒），必须 > 0
- `maxGreen`: 最大绿灯时间（秒），必须 >= minGreen

**成功响应**:
```json
{
  "success": true,
  "message": "路段配置更新成功",
  "data": {
    "segmentId": 1,
    "name": "路段1_修改",
    "length": 1200,
    "minRed": 6,
    "maxRed": 65,
    "minGreen": 6,
    "maxGreen": 18,
    "upsigid": "37",
    "downsigid": "38"
  },
  "timestamp": 1692537600000
}
```

**错误响应**:
```json
{
  "success": false,
  "message": "不允许修改路段ID",
  "timestamp": 1692537600000,
  "status": 400
}
```

---

### 12. 更新等待区配置

**接口**: `PUT /waitingareas/{index}`

**描述**: 更新等待区容量配置（不允许修改index）

**请求参数**:
- `index` (Path): 等待区索引

**请求体**:
```json
{
  "index": 1,
  "upCapacity": 3,
  "downCapacity": 4
}
```

**字段说明**:
- `upCapacity`: 上行容量，范围：1-100
- `downCapacity`: 下行容量，范围：1-100

**成功响应**:
```json
{
  "success": true,
  "message": "等待区配置更新成功",
  "data": {
    "index": 1,
    "upCapacity": 3,
    "downCapacity": 4
  },
  "timestamp": 1692537600000
}
```

---

## ❌ 禁止操作接口

### 13. 禁止添加路段配置

**接口**: `POST /segments`

**描述**: 此操作被禁止

**响应**:
```json
{
  "success": false,
  "message": "不允许添加新的路段配置，只能修改现有路段参数",
  "timestamp": 1692537600000,
  "status": 403
}
```

---

### 14. 禁止删除路段配置

**接口**: `DELETE /segments/{sigid}`

**描述**: 此操作被禁止

**响应**:
```json
{
  "success": false,
  "message": "不允许删除路段配置，只能修改现有路段参数",
  "timestamp": 1692537600000,
  "status": 403
}
```

---

### 15-19. 禁止检测点相关操作

**接口**:
- `POST /detectpoints`
- `PUT /detectpoints/{index}`
- `DELETE /detectpoints/{index}`

**响应**:
```json
{
  "success": false,
  "message": "检测点配置为只读，不允许修改/添加/删除",
  "timestamp": 1692537600000,
  "status": 403
}
```

---

### 20-21. 禁止等待区增删操作

**接口**:
- `POST /waitingareas`
- `DELETE /waitingareas/{index}`

**响应**:
```json
{
  "success": false,
  "message": "不允许添加/删除等待区配置，只能修改现有等待区参数",
  "timestamp": 1692537600000,
  "status": 403
}
```

---

## 🛠️ 工具接口

### 22. 刷新配置缓存

**接口**: `POST /refresh`

**描述**: 强制刷新配置缓存

**请求参数**: 无

**响应示例**:
```json
{
  "success": true,
  "message": "配置缓存刷新成功",
  "timestamp": 1692537600000
}
```

---

### 23. 健康检查

**接口**: `GET /health`

**描述**: 检查配置服务健康状态

**请求参数**: 无

**响应示例**:
```json
{
  "success": true,
  "message": "配置服务正常",
  "configExists": true,
  "timestamp": 1692537600000
}
```

---

### 24. 获取配置约束说明

**接口**: `GET /constraints`

**描述**: 获取系统配置约束规则和允许的操作

**请求参数**: 无

**响应示例**:
```json
{
  "rules": {
    "全局配置": "只允许修改AllRed和MaxAllRed参数，不允许添加或删除参数",
    "路段配置": "只允许修改现有路段的参数，不允许添加或删除路段",
    "检测点配置": "完全只读，不允许任何修改、添加或删除操作",
    "信号控制器列表": "只读，不允许修改",
    "等待区配置": "只允许修改现有等待区的容量参数，不允许添加或删除等待区"
  },
  "allowedOperations": {
    "GET": ["/full", "/global", "/segments", "/segments/{sigid}", "/detectpoints", "/detectpoints/{index}", "/waitingareas", "/waitingareas/{index}"],
    "PUT": ["/global", "/segments/{sigid}", "/waitingareas/{index}"],
    "POST": ["/refresh"],
    "禁止操作": ["POST /segments", "DELETE /segments/{sigid}", "POST /detectpoints", "PUT /detectpoints/{index}", "DELETE /detectpoints/{index}", "POST /waitingareas", "DELETE /waitingareas/{index}"]
  },
  "version": "1.0",
  "lastUpdated": 1692537600000
}
```

---

### 25. 调试配置信息

**接口**: `GET /debug`

**描述**: 获取配置加载状态和统计信息

**请求参数**: 无

**响应示例**:
```json
{
  "configLoaded": true,
  "segmentsCount": 4,
  "detectPointsCount": 8,
  "waitingAreasCount": 3,
  "globalConfigExists": true,
  "detectPointsReadonly": true,
  "constraints": {
    "allowModifyGlobal": true,
    "allowModifySegments": true,
    "allowModifyDetectPoints": false,
    "allowModifyWaitingAreas": true,
    "allowAddDelete": false
  }
}
```

---

## 📋 通用错误码

| HTTP状态码 | 描述 | 示例场景 |
|-----------|------|----------|
| 200 | 成功 | 正常获取/更新配置 |
| 400 | 请求参数错误 | 参数验证失败 |
| 403 | 操作被禁止 | 尝试执行不允许的操作 |
| 404 | 资源不存在 | 查询不存在的配置项 |
| 500 | 服务器内部错误 | 配置文件读写异常 |

---

## 💡 使用建议

1. **在集成前先调用** `/api/config/constraints` 了解所有约束规则
2. **根据响应的** `success` 字段判断操作结果
3. **对于修改操作，建议先查询现有配置再进行更新**
4. **使用** `/api/config/debug` 接口进行问题排查
5. **所有时间参数单位为秒，容量参数为正整数**