# 主动请求

## 查询坐席状态 - get_status

请求参数：

| fieId        | type               | req | describe |
|--------------|--------------------|-----|----------|
| req_id       | string             | 是   | 请求唯一性ID  |
| msg_type     | string（get_status） | 是   | 消息类型     |
| company_code | string             | 是   | 企业ID     |
| agent_id     | string             | 是   | 坐席ID     |
| ext_id       | string             | 是   | 分机ID     |
| os           | string             | 是   | 设备系统     |

```json
{
  "req_id": "b69912ae-0414-111ee-aaf4-174c4c2dfe01",
  "company_code": "90001",
  "msg_type": "get_status",
  "agent_id": "90001_20000",
  "ext_id": "90001_20000",
  "os": "Windows"
} 
```



返回值：

| fieId            | type                 | req  | describe                 |
| ---------------- | -------------------- | ---- | ------------------------ |
| req_id           | string               | 是   | 请求唯一性ID             |
| msg_type         | string（get_status） | 是   | 消息类型                 |
| code             | string               | 是   | 0为成功，其他为失败      |
| msg              | string               | 是   | 请求结果描述             |
| status           | string               | 是   | 状态                     |
| sub_status       | string               | 否   | 子状态                   |
| call_stop_status | string               | 否   | 通话结束后进入的状态     |
| in_call_numbers  | list                 | 否   | 与当前坐席正在通话的号码 |



## 签入 - checkin

1. 、与netty交互

请求参数：

| fieId        | type              | req                  | describe            |
| ------------ | ----------------- | -------------------- | ------------------- |
| req_id       | string            | 是                   | 请求唯一性ID        |
| company_code | string            | 是                   | 企业ID              |
| msg_type     | string（checkin） | 是                   | 消息类型            |
| ext_id       | string            | 否(不填默认为坐席ID) | 分机ID              |
| agent_id     | string            | 是                   | 坐席ID              |
| agent_pwd    | string            | 是                   | 坐席密码            |
| agent_ip     | string            | 否                   | 坐席ip，多个”,”隔开 |
| start_status | string            | 否                   | 初始签入状态        |
| reset        | bool              | 否                   | 复位                |
| os           | string            | 是                   | 设备系统            |

返回值：

| fieId       | type            | req | describe         |
|-------------|-----------------|-----|------------------|
| req_id      | string          | 是   | 请求唯一性ID          |
| code        | string          | 是   | 0为成功，其他为失败       |
| msg_type    | string（checkin） | 是   | 消息类型             |
| msg         | string          | 是   | 请求结果描述           |
| ext_id      | string          | 是   | 分机ID             |
| pbx         | string          | 否   | pbx地址            |
| ext_pwd     | string          | 否   | 分机密码             |
| rtc_address | string          | 否   | 连接rtc地址          |
| login_type  | string          | 是   | 登录类型(1、RTC2、软电话) |

1. 注册RTC SDK内部逻辑

当login_type为1即需要注册rtc时候，SDK内部逻辑连接返回值中的rtc_address后注册

| fieId        | type   | req | describe |
|--------------|--------|-----|----------|
| company_code | string | 是   | 企业ID     |
| ext_pwd      | string | 是   | 分机密码     |
| ext_id       | string | 是   | 分机id     |

**示例：**

请求参数

```json
{
    "req_id": "22222222222",
    "company_code": "090008",
    "msg_type": "checkin",
    "ext_id": "090008_5000",
    "agent_id": "090008_6000",
    "agent_pwd": "919191Aa.",
    "agent_ip": "172.0.18.20",
    "os": "Windows"
}
```



返回成功结果

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"code":"0",

"msg_type":" checkin",

"msg":"success",

"ext_id":"123456",

"pbx":"172.0.18.24:5060",

"ext_pwd":"Aa123321." ,

"rtc_address":"wss://172.0.18.24/rtcwss",

"login_type": "1"

}

返回失败结果

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"code":"1",

"msg_type":" checkin",

"ext_id":"",

"msg":"密码错误",

"pbx":"",

"ext_pwd":",

"rtc_address":"",

"login_type": ""

}

## 签出 - checkout

请求参数：

| fieId        | type             | req | describe |
|--------------|------------------|-----|----------|
| req_id       | string           | 是   | 请求唯一性ID  |
| company_code | string           | 是   | 企业ID     |
| msg_type     | string（checkout） | 是   | 消息类型     |
| agent_id     | string           | 是   | 坐席ID     |
| ext_id       | string           | 是   | 分机ID     |
| os           | string           | 是   | 设备系统     |

返回值：

| fieId    | type             | req | describe   |
|----------|------------------|-----|------------|
| req_id   | string           | 是   | 请求唯一性ID    |
| code     | string           | 是   | 0为成功，其他为失败 |
| msg_type | string（checkout） | 是   | 消息类型       |
| msg      | string           | 是   | 请求结果描述     |

**示例：**

请求参数

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"company_code":"003096",

"msg_type":" checkout",

"agent_id":"888888",

"ext_id":"888888" ,

}

返回成功结果

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"code":"0",

"msg_type":" checkout",

"msg":"success" ,

}

返回失败结果

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"code":"1",

"msg_type":" checkout",

"msg":"迁出失败" ,

}

## 心跳包 （与neety，暂定）

请求参数：

| fieId        | type         | req | describe |
|--------------|--------------|-----|----------|
| req_id       | string       | 是   | 请求唯一性ID  |
| msg_type     | string（ping） | 是   | 消息类型     |
| company_code | string       | 是   | 企业ID     |
| agent_id     | string       | 是   | 坐席ID     |
| ext_id       | string       | 是   | 分机ID     |
| os           | string       | 是   | 设备系统     |

返回值：

| fieId    | type         | req | describe   |
|----------|--------------|-----|------------|
| req_id   | string       | 是   | 请求唯一性ID    |
| msg_type | string（pong） | 是   | 消息类型       |
| code     | string       | 是   | 0为成功，其他为失败 |
| msg      | string       | 是   | 请求结果描述     |

## 外呼 - call

请求参数：

| fieId          | type           | req         | describe                                                     |
| -------------- | -------------- | ----------- | ------------------------------------------------------------ |
| req_id         | string         | 是          | 请求唯一性ID                                                 |
| msg_type       | string（call） | 是          | 消息类型                                                     |
| company_code   | string         | 是          | 企业ID                                                       |
| agent_id       | string         | 是          | 坐席ID                                                       |
| ext_id         | string         | 是          | 分机ID                                                       |
| callee_number  | string         | 是          | 被叫号码(内线为坐席ID)                                       |
| out_line       | int            | 是          | 【0:内线， 1:外线，2:owt房间(会议)】， 默认外线              |
| audio          | int            | 是（默认3） | 【0:无语音流， 1:只发送语音流不接收， 2:只接收语音流不发送， 3:发送并接收语音流】默认3 |
| video          | int            | 是（默认0） | 【0:无视频流， 1:只发送视频流不接收， 2:只接收视频流不发送， 3:发送并接收视频流】默认0 |
| pixels         | string         | 否          | 视频分辨率【480， 720， 1280】 默认480(video不为0时可用)     |
| max_ring_time  | string         | 否          | 最大通话时间，默认8小时                                      |
| max_call_time  | string         | 否          | 最大外呼时长，超过则结束外呼，默认60s                        |
| record_start   | string         | 否          | 录制节点【NONE:不录制，RING:响铃录制，ANSWER:接通后录制】默认NONE |
| record_contact | bool           | 否          | 是否双向录制， 默认单向                                      |
| is_stereo      | bool           | 否          | 是否立体声录制，默认否(单声道)                               |
| record_suffix  | string         | 否          | 录制文件后缀【mp3, wav, mp4】默认mp3                         |
| os             | string         | 是          | 设备系统                                                     |
| work_order_id  | string         | 否          | 工单id(回呼时透传)                                           |

返回值：

| fieId    | type         | req | describe   |
|----------|--------------|-----|------------|
| req_id   | string       | 是   | 请求唯一性ID    |
| msg_type | string（call） | 是   | 消息类型       |
| code     | string       | 是   | 0为成功，其他为失败 |
| msg      | string       | 是   | 请求结果描述     |
| uuid     | string       | 是   | 外呼的uuid    |

**示例：**

请求参数

```json
{
	"req_id": "79b1c954-ebf1-87bc-8676-0ec0aca258b8",
	"company_code": "090008",
	"agent_id": "090008_6000",
	"ext_id": "090008_5000",
	"os": "Windows",
	"msg_type": "call",
	"callee_number": "13107618845",
	"out_line": 1,
	"audio": 3,
	"video": 0,
	"pixels": "",
	"max_ring_time": "",
	"max_call_time": "",
	"record_start": "",
	"record_contact": "",
	"is_stereo": "",
	"file_name": "",
    "work_order_id": ''
}
```



返回成功结果

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"code":"0",

"msg_type":"call",

"msg":"success" ,

}

返回失败结果

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"code":"1",

"msg_type":"call",

"msg":"呼出失败" ,

}

## 音视频切换 - change_media

1. 请求参数：

| fieId        | type                   | req  | describe                                                     |
| ------------ | ---------------------- | ---- | ------------------------------------------------------------ |
| req_id       | string                 | 是   | 请求唯一性ID                                                 |
| msg_type     | string（change_media） | 是   | 消息类型                                                     |
| company_code | string                 | 是   | 企业ID                                                       |
| agent_id     | string                 | 是   | 坐席ID                                                       |
| ext_id       | string                 | 是   | 分机ID                                                       |
| uuid         | string                 | 是   | 需要切换的通话uuid                                           |
| audio        | int                    | 是   | 【0:无语音流， 1:只发送语音流不接收， 2:只接收语音流不发送， 3:发送并接收语音流】默认3 |
| video        | int                    | 是   | 【0:无视频流， 1:只发送视频流不接收， 2:只接收视频流不发送， 3:发送并接收视频流】默认0 |
| pixels       | string                 | 否   | 视频分辨率【480， 720， 1280】 默认480(video不为0时可用)     |
| reneg_other  | bool                   | 否   | 是否发起另外一路，默认为true                                 |

```json
{
  "req_id": "11111",
  "company_code": "90001",
  "msg_type": "change_media",
  "agent_id": "90001_20000",
  "ext_id": "90001_20000",
  "uuid": "call-ad1196c846cc4b3b8fbc2dd4c777d3a9",
  "pixels": "480",
  "audio": 3,
  "video": 3,
  "os": "Windows"
}
```



## 切换状态（包括强制）- change_status

请求参数：

| fieId             | type                    | req  | describe                                                     |
| ----------------- | ----------------------- | ---- | ------------------------------------------------------------ |
| req_id            | string                  | 是   | 请求唯一性ID                                                 |
| msg_type          | string（change_status） | 是   | 消息类型                                                     |
| company_code      | string                  | 是   | 企业ID                                                       |
| agent_id          | string                  | 是   | 坐席ID                                                       |
| ext_id            | string                  | 是   | 分机ID                                                       |
| operated_ext_id   | string                  | 否   | 被强制操作的分机ID(仅当action为强制签出, 强制示闲, 强制示忙必填) |
| operated_agent_id | string                  | 否   | 被强制操作的坐席ID(仅当action为强制签出, 强制示闲, 强制示忙必填) |
| action            | string                  | 是   | 状态切换动作: <br />MAKE_BUSY 示忙 <br />MAKE_FREE 示闲 <br />MAKE_REST 进入小休<br /> MAKE_BUSY_AFTER_CALL_STOP 通话结束后自动示忙 <br />MAKE_FREE_AFTER_CALL_STOP 通话结束后自动示闲 <br />MAKE_REST_AFTER_CALL_STOP 通话结束后自动进入小休 <br />CHECKOUT_AFTER_CALL_STOP 通话结束后自动签出 <br />FORCE_CHECKOUT 强制签出 <br />FORCE_MAKE_FREE强制示闲 <br />FORCE_MAKE_BUSY 强制示忙 |
| rest_min          | string                  | 否   | 小休时间(单位：分)                                           |
| os                | string                  | 是   | 设备系统                                                     |

返回值：

| fieId             | type                  | req | describe   |
|-------------------|-----------------------|-----|------------|
| req_id            | string                | 是   | 请求唯一性ID    |
| msg_type          | string（change_status） | 是   | 消息类型       |
| code              | string                | 是   | 0为成功，其他为失败 |
| msg               | string                | 是   | 请求结果描述     |
| action            | string                | 是   | 状态切换动作     |
| last_agent_status | string                | 是   | 切换前的状态     |
| agent_status      | string                | 是   | 切换后的状态     |

**示例：**

请求参数

```json
{
  "req_id": "b69912ae-0414-11ee-aaf4-174c4c2dfe01",
  "msg_type": "change_status",
  "company_code": "90001",
  "agent_id": "90001_20000",
  "ext_id": "90001_20000",
  "action": "MAKE_FREE",
  "rest_min": null,
  "os": "Windows"
}
```



返回成功结果

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"code":"0",

"msg_type":" change_status",

"msg":"success" ,

"action":" MAKE_BUSY",

"last_agent_status":"OFFLINE" ,

"agent_status":"BUSY"

}

返回失败结果

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"code":"1",

"msg_type":" change_status",

"msg":"切换状态失败" ,

"action":" MAKE_BUSY"

}

## 保持/取消保持 - hold

请求参数：

| fieId        | type           | req  | describe           |
| ------------ | -------------- | ---- | ------------------ |
| req_id       | string         | 是   | 请求唯一性ID       |
| msg_type     | string（hold） | 是   | 消息类型           |
| company_code | string         | 是   | 企业ID             |
| agent_id     | string         | 是   | 坐席ID             |
| ext_id       | string         | 是   | 分机ID             |
| hold         | string         | 是   | 保持-1, 取消保持-0 |
| uuid         | string         | 是   | 需要保持的通话uuid |
| os           | string         | 是   | 设备系统           |

返回值：

| fieId    | type         | req | describe   |
|----------|--------------|-----|------------|
| req_id   | string       | 是   | 请求唯一性ID    |
| msg_type | string（hold） | 是   | 消息类型       |
| code     | string       | 是   | 0为成功，其他为失败 |
| msg      | string       | 是   | 请求结果描述     |
| hold     | string       | 是   | 当前状态       |

**示例：**

请求参数

```json
{
    "req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",
    "company_code":"090008",
    "msg_type":"hold",
    "hold":"1",
    "uuid":"call-e6d151f43c2c417d9d2e73dbf8761c88",
    "agent_id":"090008_6000",
    "ext_id":"090008_5000",
    "os": "Windows"
}
```



返回成功结果

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"code":"0",

"msg_type":"hold",

"hold":"1",

"msg":"success" ,

}

返回失败结果

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"code":"1",

"msg_type":"hold",

"hold":"0",

"msg":"保持失败" ,

}

## 静音/取消静音

> 客户端实现

请求参数：

| fieId        | type           | req  | describe               |
| ------------ | -------------- | ---- | ---------------------- |
| req_id       | string         | 是   | 请求唯一性ID           |
| msg_type     | string（mute） | 是   | 消息类型               |
| company_code | string         | 是   | 企业ID                 |
| agent_id     | string         | 是   | 坐席ID                 |
| ext_id       | string         | 是   | 分机ID                 |
| mute         | string         | 是   | 静音/取消静音 (0否1是) |
| uuid         | string         | 是   | 需要静音的通话uuid     |
| os           | string         | 是   | 设备系统               |

返回值：

| fieId    | type         | req | describe   |
|----------|--------------|-----|------------|
| req_id   | string       | 是   | 请求唯一性ID    |
| msg_type | string（hold） | 是   | 消息类型       |
| code     | string       | 是   | 0为成功，其他为失败 |
| msg      | string       | 是   | 请求结果描述     |
| mute     | string       | 是   | 当前状态       |

**示例：**

请求参数

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"company_code":"003096",

"msg_type":"mute",

"mute":"1",

"uuid":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"agent_id":"888888",

"ext_id":"888888" ,

}

返回成功结果

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"code":"0",

"msg_type":"mute",

"mute":"1",

"msg":"success" ,

}

返回失败结果

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"code":"1",

"msg_type":"mute",

"mute":"0",

"msg":"静音失败" ,

}

## 挂断 - hangup

请求参数：

| fieId        | type           | req | describe    |
|--------------|----------------|-----|-------------|
| req_id       | string         | 是   | 请求唯一性ID     |
| msg_type     | string（hangup） | 是   | 消息类型        |
| company_code | string         | 是   | 企业ID        |
| agent_id     | string         | 是   | 坐席ID        |
| ext_id       | string         | 是   | 分机ID        |
| uuid         | string         | 是   | 需要挂断的通话uuid |
| os           | string         | 是   | 设备系统        |

返回值：

| fieId    | type           | req | describe   |
|----------|----------------|-----|------------|
| req_id   | string         | 是   | 请求唯一性ID    |
| msg_type | string（hangup） | 是   | 消息类型       |
| code     | string         | 是   | 0为成功，其他为失败 |
| msg      | string         | 是   | 请求结果描述     |

**示例：**

请求参数

```json
{
  "req_id": "b69912ae-0414-111ee-aaf4-174c4c2dfe01",
  "company_code": "90001",
  "msg_type": "hangup",
  "uuid": "call-83c7b9a0e2f84f32a664462290b9ed0a",
  "agent_id": "90001_20000",
  "ext_id": "90001_20000",
  "os": "Windows"
}
```



返回成功结果

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"code":"0",

"msg_type":"hangup",

"msg":"success" ,

}

返回失败结果

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"code":"1",

"msg_type":"hangup",

"msg":"挂断失败" ,

}

## 转接 - trans

请求参数：

| fieId          | type            | req  | describe                                                     |
| -------------- | --------------- | ---- | ------------------------------------------------------------ |
| req_id         | string          | 是   | 请求唯一性ID                                                 |
| msg_type       | string（trans） | 是   | 消息类型                                                     |
| company_code   | string          | 是   | 企业ID                                                       |
| agent_id       | string          | 是   | 坐席ID                                                       |
| ext_id         | string          | 是   | 分机ID                                                       |
| uuid           | string          | 是   | 需要转接的通话uuid                                           |
| type           | string          | 是   | 转接模式: 1-盲转 2-咨询转 3-取消转接(咨询转)                 |
| trans_type     | string          | 是   | 转接人员类型: 1-坐席, 2-技能, 3-ivr, 4-外线, 5-满意度        |
| trans_number   | string          | 否   | 转接号码（坐席ID、技能ID、ivrID、不填为默认满意度）          |
| audio          | int             | 否   | 【0:无语音流， 1:只发送语音流不接收， 2:只接收语音流不发送， 3:发送并接收语音流】默认3 |
| video          | int             | 否   | 【0:无视频流， 1:只发送视频流不接收， 2:只接收视频流不发送， 3:发送并接收视频流】默认0 |
| pixels         | string          | 否   | 视频分辨率【480， 720， 1280】 默认480(video不为0时可用)     |
| max_ring_time  | string          | 否   | 最大通话时间，默认8小时                                      |
| max_call_time  | string          | 否   | 最大外呼时长，超过则结束外呼，默认60s                        |
| record_start   | string          | 否   | 录制节点【NONE:不录制，RING:响铃录制，ANSWER:接通后录制】默认NONE |
| record_contact | bool            | 否   | 是否双向录制， 默认单向                                      |
| is_stereo      | bool            | 否   | 是否立体声录制，默认否(单声道)                               |
| record_suffix  | string          | 否   | 录制文件后缀【mp3, wav, mp4】默认mp3                         |
| os             | string          | 是   | 设备系统                                                     |

返回值：

| fieId    | type          | req | describe   |
|----------|---------------|-----|------------|
| req_id   | string        | 是   | 请求唯一性ID    |
| msg_type | string（trans） | 是   | 消息类型       |
| code     | string        | 是   | 0为成功，其他为失败 |
| msg      | string        | 是   | 请求结果描述     |

**示例：**

请求参数

```json
{
  "req_id": "b69912ae-0414-11ee-aaf24-174c4c2dfe01",
  "company_code": "90001",
  "msg_type": "trans",
  "uuid": "call-fc63e8eeb061490abaecfbaa96278dbc",
  "agent_id": "90001_20000",
  "ext_id": "90001_20000",
  "os": "Windows",
  "type": "2",
  "trans_type": 1,
  "trans_number": "90001_20004"
}
```



返回成功结果

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"code":"0",

"msg_type":"transfer",

"msg":"success" ,

}

返回失败结果

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"code":"1",

"msg_type":"trans",

"msg":"转接失败" ,

}

## 咨询 - consult

请求参数：

| fieId          | type              | req  | describe                                                     |
| -------------- | ----------------- | ---- | ------------------------------------------------------------ |
| req_id         | string            | 是   | 请求唯一性ID                                                 |
| msg_type       | string（consult） | 是   | 消息类型                                                     |
| company_code   | string            | 是   | 企业ID                                                       |
| agent_id       | string            | 是   | 坐席ID                                                       |
| ext_id         | string            | 是   | 分机ID                                                       |
| uuid           | string            | 是   | 需要咨询的通话uuid                                           |
| type           | string            | 是   | 咨询类型（1-发起 2-取消）                                    |
| consult_type   | string            | 是   | 咨询人员: 0:内线， 1:外线                                    |
| consult_number | string            | 是   | 咨询号码（坐席ID 或者外线号码）                              |
| audio          | int               | 否   | 【0:无语音流， 1:只发送语音流不接收， 2:只接收语音流不发送， 3:发送并接收语音流】默认3 |
| video          | int               | 否   | 【0:无视频流， 1:只发送视频流不接收， 2:只接收视频流不发送， 3:发送并接收视频流】默认0 |
| pixels         | string            | 否   | 视频分辨率【480， 720， 1280】 默认480(video不为0时可用)     |
| max_ring_time  | string            | 否   | 最大通话时间，默认8小时                                      |
| max_call_time  | string            | 否   | 最大外呼时长，超过则结束外呼，默认60s                        |
| record_start   | string            | 否   | 录制节点【NONE:不录制，RING:响铃录制，ANSWER:接通后录制】默认NONE |
| record_contact | bool              | 否   | 是否双向录制， 默认单向                                      |
| is_stereo      | bool              | 否   | 是否立体声录制，默认否(单声道)                               |
| record_suffix  | string            | 否   | 录制文件后缀【mp3, wav, mp4】默认mp3                         |
| os             | string            | 是   | 设备系统                                                     |

返回值：

| fieId        | type              | req  | describe            |
| ------------ | ----------------- | ---- | ------------------- |
| req_id       | string            | 是   | 请求唯一性ID        |
| msg_type     | string（consult） | 是   | 消息类型            |
| code         | string            | 是   | 0为成功，其他为失败 |
| msg          | string            | 是   | 请求结果描述        |
| consult_uuid | string            | 否   | 被咨询方通话id      |

**示例：**

请求参数

```json
{
  "req_id": "b69912ae-0414-11ee-aaf24-174c4c2dfe01",
  "company_code": "090008",
  "msg_type": "consult",
  "type": "1",
  "consult_type": "0",
  "uuid": "call-e6d151f43c2c417d9d2e73dbf8761c88",
  "agent_id": "090008_6000",
  "ext_id": "090008_5000",
  "os": "Windows",
  "consult_number": "090008_6004"
}
```

返回成功结果

```json
{
    "req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",
    "code":"0",
    "msg_type":"consult",
    "msg":"success",
    "consult_uuid": "44782d4d-cc7d-4a8a-9f63-73f476851361"
}
```

返回失败结果

```json
{
    "req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",
    "code":"1",
    "msg_type":"consult",
    "msg":"咨询失败"
}
```
## 咨询中转接 - consult_to_trans

> 咨询过程中, 被咨询方未接, 直接转接给被咨询方

请求参数：

| fieId        | type                       | req  | describe       |
| ------------ | -------------------------- | ---- | -------------- |
| req_id       | string                     | 是   | 请求唯一性ID   |
| msg_type     | string（consult_to_trans） | 是   | 消息类型       |
| company_code | string                     | 是   | 企业ID         |
| agent_id     | string                     | 是   | 坐席ID         |
| ext_id       | string                     | 是   | 分机ID         |
| consult_uuid | string                     | 是   | 被咨询方通话id |
| os           | string                     | 是   | 设备系统       |

返回值：

| fieId    | type                       | req  | describe            |
| -------- | -------------------------- | ---- | ------------------- |
| req_id   | string                     | 是   | 请求唯一性ID        |
| msg_type | string（consult_to_trans） | 是   | 消息类型            |
| code     | string                     | 是   | 0为成功，其他为失败 |
| msg      | string                     | 是   | 请求结果描述        |

**示例：**

请求参数

```json
{
  "req_id": "b69912ae-0414-11ee-aaf24-174c4c2dfe01",
  "company_code": "090008",
  "msg_type": "consult_to_trans",
  "agent_id": "090008_6000",
  "ext_id": "090008_5000",
  "os": "Windows",
  "consult_uuid": "44782d4d-cc7d-4a8a-9f63-73f476851361"
}
```

返回成功结果

```json
{
    "req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",
    "code":"0",
    "msg_type":"consult_to_trans",
    "msg":"success" 
}
```

返回失败结果

```json
{
    "req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",
    "code":"1",
    "msg_type":"consult_to_trans",
    "msg":"咨询失败"
}
```
## 三方通话 - three_way

请求参数：

| fieId            | type                | req  | describe                        |
| ---------------- | ------------------- | ---- | ------------------------------- |
| req_id           | string              | 是   | 请求唯一性ID                    |
| msg_type         | string（three_way） | 是   | 消息类型                        |
| company_code     | string              | 是   | 企业ID                          |
| agent_id         | string              | 是   | 坐席ID                          |
| ext_id           | string              | 是   | 分机ID                          |
| uuid             | string              | 是   | 需要会议的通话uuid              |
| three_way_type   | string              | 是   | 三方通话人员（0:内线， 1:外线） |
| three_way_number | string              | 是   | 三方通话号码(内线为坐席ID)      |
| os               | string              | 是   | 设备系统                        |
| audio          | int               | 否   | 【0:无语音流， 1:只发送语音流不接收， 2:只接收语音流不发送， 3:发送并接收语音流】默认3 |
| video          | int               | 否   | 【0:无视频流， 1:只发送视频流不接收， 2:只接收视频流不发送， 3:发送并接收视频流】默认0 |
| pixels         | string            | 否   | 视频分辨率【480， 720， 1280】 默认480(video不为0时可用)     |
| max_ring_time  | string            | 否   | 最大通话时间，默认8小时                                      |
| max_call_time  | string            | 否   | 最大外呼时长，超过则结束外呼，默认60s                        |
| record_start   | string            | 否   | 录制节点【NONE:不录制，RING:响铃录制，ANSWER:接通后录制】默认NONE |
| record_contact | bool              | 否   | 是否双向录制， 默认单向                                      |
| is_stereo      | bool              | 否   | 是否立体声录制，默认否(单声道)                               |
| record_suffix  | string            | 否   | 录制文件后缀【mp3, wav, mp4】默认mp3                         |

返回值：

| fieId           | type                | req  | describe                 |
| --------------- | ------------------- | ---- | ------------------------ |
| req_id          | string              | 是   | 请求唯一性ID             |
| msg_type        | string（three_way） | 是   | 消息类型                 |
| code            | string              | 是   | 0为成功，其他为失败      |
| msg             | string              | 是   | 请求结果描述             |
| in_call_numbers | list                | 否   | 与当前坐席正在通话的号码 |

**示例：**

请求参数

```json
{
  "req_id": "b69912ae-0414-11ee-aaf24-174c4c2dfe01",
  "company_code": "90001",
  "msg_type": "three_way",
  "uuid": "call-4b8091dad15147ada5f7971fe097023d",
  "agent_id": "90001_20000",
  "ext_id": "90001_20000",
  "os": "Windows",
  "three_way_type": "0",
  "three_way_number": "90001_20004"
}
```

返回成功结果

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"code":"0",

"msg_type":" three_way",

"msg":"success" ,

}

返回失败结果

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"code":"1",

"msg_type":" three_way",

"msg":"三方通话失败" ,

}

## 监听 - eavesdrop

请求参数：

| fieId             | type              | req | describe |
|-------------------|-------------------|-----|----------|
| req_id            | string            | 是   | 请求唯一性ID  |
| msg_type          | string（eavesdrop） | 是   | 消息类型     |
| company_code      | string            | 是   | 企业ID     |
| agent_id          | string            | 是   | 坐席ID     |
| ext_id            | string            | 是   | 分机ID     |
| operated_ext_id   | string            | 是   | 被监听的分机ID |
| operated_agent_id | string            | 是   | 被监听的坐席ID |
| os                | string            | 是   | 设备系统     |

返回值：

| fieId    | type              | req | describe   |
|----------|-------------------|-----|------------|
| req_id   | string            | 是   | 请求唯一性ID    |
| msg_type | string（eavesdrop） | 是   | 消息类型       |
| code     | string            | 是   | 0为成功，其他为失败 |
| msg      | string            | 是   | 请求结果描述     |

**示例：**

请求参数

```json
{
    "req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",
    "company_code":"90001",
    "msg_type":"eavesdrop",
    "agent_id":"90001_20004",
    "ext_id":"90001_20004" ,
    "operated_ext_id":"90001_20000" ,
    "operated_agent_id":"90001_20000",
    "os": "Windows"
}
```

返回成功结果

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"code":"0",

"msg_type":" eavesdrop",

"msg":"success" ,

}

返回失败结果

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"code":"1",

"msg_type":" eavesdrop",

"msg":"监听失败" ,

}

## 耳语 - whisper

请求参数：

| fieId             | type              | req  | describe       |
| ----------------- | ----------------- | ---- | -------------- |
| req_id            | string            | 是   | 请求唯一性ID   |
| msg_type          | string（whisper） | 是   | 消息类型       |
| company_code      | string            | 是   | 企业ID         |
| agent_id          | string            | 是   | 坐席ID         |
| ext_id            | string            | 是   | 分机ID         |
| operated_ext_id   | string            | 是   | 被耳语的分机ID |
| operated_agent_id | string            | 是   | 被耳语的坐席ID |
| os                | string            | 是   | 设备系统       |

返回值：

| fieId    | type            | req | describe   |
|----------|-----------------|-----|------------|
| req_id   | string          | 是   | 请求唯一性ID    |
| msg_type | string（whisper） | 是   | 消息类型       |
| code     | string          | 是   | 0为成功，其他为失败 |
| msg      | string          | 是   | 请求结果描述     |

**示例：**

请求参数

```json
{
    "req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",
    "company_code":"90001",
    "msg_type":"whisper",
    "agent_id":"90001_20004",
    "ext_id":"90001_20004" ,
    "operated_ext_id":"90001_20000" ,
    "operated_agent_id":"90001_20000",
    "os": "Windows"
}
```

返回成功结果

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"code":"0",

"msg_type":"whisper",

"msg":"success" ,

}

返回失败结果

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"code":"1",

"msg_type":"whisper",

"msg":"耳语失败" ,

}

## 二次拨号（DTMF）

请求参数：

| fieId        | type           | req  | describe                         |
| ------------ | -------------- | ---- | -------------------------------- |
| req_id       | string         | 是   | 请求唯一性ID                     |
| msg_type     | string（dtmf） | 是   | 消息类型                         |
| company_code | string         | 是   | 企业ID                           |
| agent_id     | string         | 是   | 坐席ID                           |
| ext_id       | string         | 是   | 分机ID                           |
| uuid         | string         | 是   | 需要二次拨号的通话uuid           |
| content      | string         | 是   | 二次拨号内容                     |
| dtmf_type    | int            | 否   | 【0:info，1:inban，3:2833】默认3 |
| os           | string         | 是   | 设备系统                         |

返回值：

| fieId    | type         | req | describe   |
|----------|--------------|-----|------------|
| req_id   | string       | 是   | 请求唯一性ID    |
| msg_type | string（dtmf） | 是   | 消息类型       |
| code     | string       | 是   | 0为成功，其他为失败 |
| msg      | string       | 是   | 请求结果描述     |

**示例：**

请求参数

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"company_code":"003096",

"msg_type":"dtmf",

"uuid":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"agent_id":"888888",

"ext_id":"888888" ,

"content":"121",

}

返回成功结果

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"code":"0",

"msg_type":"dtmf",

"msg":"success" ,

}

返回失败结果

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"code":"1",

"msg_type":"dtmf",

"msg":"二次拨号失败" ,

}

以下为管理员功能 （13-17可合并，msg_type取不同参数）

## 代接 - substitute

请求参数：

| fieId             | type               | req | describe |
|-------------------|--------------------|-----|----------|
| req_id            | string             | 是   | 请求唯一性ID  |
| msg_type          | string（substitute） | 是   | 消息类型     |
| company_code      | string             | 是   | 企业ID     |
| agent_id          | string             | 是   | 坐席ID     |
| ext_id            | string             | 是   | 分机ID     |
| operated_ext_id   | string             | 是   | 被代接的分机ID |
| operated_agent_id | string             | 是   | 被代接的坐席ID |
| os                | string             | 是   | 设备系统     |

返回值：

| fieId    | type               | req | describe   |
|----------|--------------------|-----|------------|
| req_id   | string             | 是   | 请求唯一性ID    |
| msg_type | string（substitute） | 是   | 消息类型       |
| code     | string             | 是   | 0为成功，其他为失败 |
| msg      | string             | 是   | 请求结果描述     |

**示例：**

请求参数

```json
{ 
    "req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01", 
    "company_code":"90001", 
    "msg_type":"substitute", 
    "agent_id":"90001_20004", 
    "ext_id":"90001_20004" ,
    "operated_ext_id":"90001_20000" ,
    "operated_agent_id":"90001_20000",
    "os": "Windows"
} 
```

返回成功结果

```json
{

    "req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",
    "code":"0",
    "msg_type":"substitute",
    "msg":"success"
}
```

返回失败结果

```json
{
    "req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",
    "code":"1",
    "msg_type":"substitute",
    "msg":"代接失败"
}
```

## 强插

请求参数：

| fieId             | type               | req | describe |
|-------------------|--------------------|-----|----------|
| req_id            | string             | 是   | 请求唯一性ID  |
| msg_type          | string（force_call） | 是   | 消息类型     |
| company_code      | string             | 是   | 企业ID     |
| agent_id          | string             | 是   | 坐席ID     |
| ext_id            | string             | 是   | 分机ID     |
| operated_ext_id   | string             | 是   | 被强插的分机ID |
| operated_agent_id | string             | 是   | 被强插的坐席ID |
| os                | string             | 是   | 设备系统     |

返回值：

| fieId    | type               | req | describe   |
|----------|--------------------|-----|------------|
| req_id   | string             | 是   | 请求唯一性ID    |
| msg_type | string（force_call） | 是   | 消息类型       |
| code     | string             | 是   | 0为成功，其他为失败 |
| msg      | string             | 是   | 请求结果描述     |

**示例：**

请求参数

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"company_code":"003096",

"msg_type":" force_call",

"agent_id":"888888",

"ext_id":"888888" ,

"operated_ext_id":"888" ,

"operated_agent_id":"888"

}

返回成功结果

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"code":"0",

"msg_type":" force_call",

"msg":"success"

}

返回失败结果

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"code":"1",

"msg_type":" force_call",

"msg":"强插失败"

}



## 强拆 - interrupt_call

请求参数：

| fieId             | type                   | req | describe |
|-------------------|------------------------|-----|----------|
| req_id            | string                 | 是   | 请求唯一性ID  |
| msg_type          | string（interrupt_call） | 是   | 消息类型     |
| company_code      | string                 | 是   | 企业ID     |
| agent_id          | string                 | 是   | 坐席ID     |
| ext_id            | string                 | 是   | 分机ID     |
| operated_ext_id   | string                 | 是   | 被强拆的分机ID |
| operated_agent_id | string                 | 是   | 被强拆的坐席ID |
| os                | string                 | 是   | 设备系统     |

返回值：

| fieId    | type                   | req | describe   |
|----------|------------------------|-----|------------|
| req_id   | string                 | 是   | 请求唯一性ID    |
| msg_type | string（interrupt_call） | 是   | 消息类型       |
| code     | string                 | 是   | 0为成功，其他为失败 |
| msg      | string                 | 是   | 请求结果描述     |

**示例：**

请求参数

```json
{ 
    "req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01", 
    "company_code":"90001", 
    "msg_type":" interrupt_call", 
    "agent_id":"90001_20004", 
    "ext_id":"90001_20004" ,
    "operated_ext_id":"90001_20000" ,
    "operated_agent_id":"90001_20000",
    "os": "Windows"
} 
```



返回成功结果

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"code":"0",

"msg_type":" interrupt_call",

"msg":"success"

}

返回失败结果

{

"req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"code":"1",

"msg_type":" interrupt_call",

"msg":"强拆失败"

}

## 接听

返回值：

| fieId                 | type           | req    | describe                                          |
|-----------------------|----------------|--------|---------------------------------------------------|
| req_id                | string         | 是      | 请求唯一id                                            |
| msg_type              | string（answer） | 是      | 消息类型                                              |
| company_code          | string         | 是      | 企业ID                                              |
| agent_id              | string         | 是      | 坐席ID                                              |
| ext_id                | string         | 是      | 分机ID                                              |
| uuid                  | string         | 是      | 呼入uuid                                            |
| audio                 | int            | 是（默认3） | 【0:无语音流， 1:只发送语音流不接收， 2:只接收语音流不发送， 3:发送并接收语音流】默认3 |
| video                 | int            | 是（默认0） | 【0:无视频流， 1:只发送视频流不接收， 2:只接收视频流不发送， 3:发送并接收视频流】默认0 |
| pixels                | string         | 否      | 视频分辨率【480， 720， 1280】 默认480(video不为0时可用)          |
| pixels callin_channel | string         | 否      | 呼入通道(转接-TRANS、咨询-CONSULT、三方通话-THREE_WAY、空为正常)     |

**示例：**

{

"company_code":"003096",

"msg_type":"answer",

"agent_id":"888888" ,

"ext_id":"888888" ,

"uuid":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"audio":"3" ,

"video":"0" ,

"pixels":"" ,

}

# 被动接收

## 回调结果- callback

> 坐席发起一些操作, 通话成功建立之后回调通知SDK
>
> 分机的注销通知

返回值：

| fieId        | type               | req  | describe                                                     |
| ------------ | ------------------ | ---- | ------------------------------------------------------------ |
| req_id       | string             | 是   | 请求唯一性ID                                                 |
| msg_type     | string（callback） | 是   | 消息类型                                                     |
| company_code | string             | 是   | 企业ID                                                       |
| agent_id     | string             | 是   | 坐席ID                                                       |
| action       | string             | 是   | 操作类型:<br />CALL 坐席外呼<br />CONSULT 坐席发起咨询<br />TRANS 坐席发起转接<br />CONSULT_TO_TRANS 咨询中转接<br />THREE_WAY 坐席发起三方通话<br />EAVESDROP 管理员坐席发起监听<br />WHISPER 管理员坐席发耳语<br />SUBSTITUTE 代接<br />FORCE_CALL 强插(三方)<br />INTERRUPT_CALL 强拆<br /><br />CHANGE_MEDIA 音视频切换<br />EXT_UN_REG 分机注销 |
| phase        | string             | 是   | action进行阶段:<br />发起action INITIATE<br />开始通话 START_CALL<br />结束通话 END_CALL |
| curren_time  | string             | 否   | 当前时间                                                     |
| success      | bool               | 是   | 操作是否成功 true/false                                      |
| msg          | string             | 是   | 请求结果描述                                                 |
| os           | string             | 是   | 设备系统                                                     |
| last_audio      | int |否|【0:无语音流， 1:只发送语音流不接收， 2:只接收语音流不发送， 3:发送并接收语音流】 上一次的状态，接通，音视频切换时携带|
| audio           | int |否|0:无语音流， 1:只发送语音流不接收， 2:只接收语音流不发送， 3:发送并接收语音流】 接通，音视频切换时携带|
| last_video      | int |否|【0:无视频流， 1:只发送视频流不接收， 2:只接收视频流不发送， 3:发送并接收视频流】 上一次的状态，接通，音视频切换时携带|
| video           | int |否|【0:无视频流， 1:只发送视频流不接收， 2:只接收视频流不发送， 3:发送并接收视频流】 接通，音视频切换时携带|
| change_media_flag | int |否|音视频切换标志<br />0: 坐席发起音视频切换请求<br />1: 坐席发起音视频切换请求, 对方接通情况<br />2: 音视频切换应答(坐席视频外呼, 对方接通情况; 手机侧切换音视频 )|
| work_order_id | int |否|工单id (CALL透传)|

**示例：**

请求参数

```json
{ 
    "req_id":"b69912ae-0414-11ee-aaf4-174c4c2dfe01", 
    "company_code":"90001", 
    "msg_type":" callback", 
    "agent_id":"090008_6000", 
    "ext_id":"090008_5000" ,
    "action":"CONSULT" ,
    "success":true,
    "msg": "咨询成功, 通话建立!",
    "os": "Windows"
} 
```



返回成功结果



返回失败结果



## 呼入 - callin

返回值：

| fieId          | type             | req         | describe                                                     |
| -------------- | ---------------- | ----------- | ------------------------------------------------------------ |
| req_id         | string           | 是          | 请求唯一id                                                   |
| msg_type       | string（callin） | 是          | 消息类型                                                     |
| company_code   | string           | 是          | 企业ID                                                       |
| agent_id       | string           | 是          | 坐席ID                                                       |
| ext_id         | string           | 是          | 分机ID                                                       |
| uuid           | string           | 是          | 呼入uuid                                                     |
| caller_number  | string           | 是          | 呼入号码(来电)                                               |
| callee_number  | string           | 是          | 被叫号码(平台号码)                                           |
| flow_data      | string           | 否          | 随路参数                                                     |
| callin_channel | string           | 否          | 呼入通道(转接-TRANS、咨询-CONSULT、三方通话-THREE_WAY、空为正常) |
| is_video       | int              | 是          | 是否为视频通话 0否1是                                        |
| audio          | int              | 是（默认3） | 【0:无语音流， 1:只发送语音流不接收， 2:只接收语音流不发送， 3:发送并接收语音流】默认3 |
| video          | int              | 是（默认0） | 【0:无视频流， 1:只发送视频流不接收， 2:只接收视频流不发送， 3:发送并接收视频流】默认0 |
| pixels         | string           | 否          | 视频分辨率【480， 720， 1280】 默认480(video不为0时可用)     |
| work_order_id  | string           | 否          | 工单id(回呼时透传)                                           |

**示例：**

{

"company_code":"003096",

"msg_type":"callin",

"agent_id":"888888" ,

"ext_id":"888888" ,

"uuid":"b69912ae-0414-11ee-aaf4-174c4c2dfe01",

"caller_number":"18888888888",

"flow_data":"{xxx:xxx,xxx:xxx}" ,

"caller_channel":"" ,

"is_video":"0"

}

## 状态变化-agent_change

返回值：

| fieId             | type                   | req  | describe                 |
| ----------------- | ---------------------- | ---- | ------------------------ |
| req_id            | string                 | 是   | 请求id                   |
| msg_type          | string（agent_change） | 是   | 消息类型                 |
| company_code      | string                 | 是   | 企业ID                   |
| agent_id          | string                 | 是   | 坐席ID                   |
| ext_id            | string                 | 是   | 分机ID                   |
| agent_status      | string                 | 是   | 坐席状态                 |
| sub_agent_status  | string                 | 否   | 坐席子状态               |
| last_agent_status | string                 | 是   | 上一个坐席状态           |
| in_call_numbers   | list                   | 否   | 与当前坐席正在通话的号码 |
| reason            | string                 | 否   | 状态切换原因             |

**示例：**

{

"company_code":"003096",

"msg_type":"agent_change",

"agent_id":"888888" ,

" agent_status":"2",

" last_agent_status":"1" ,

}

# msg_type说明

```
checkin：签入
checkout：签出
ping：心跳
call：外呼
change_status：切换状态
hold：保持
mute：静音
hangup：挂断
trans：转接
consult：咨询
dtmf：二次拨号
eavesdrop：监听
whisper：耳语
substitute：代接
three_way: 三方通话
force_call：强插
interrupt_call：强拆
callin：呼入
pickup：接听
agent_change: 状态变化
```



# 坐席状态对照

坐席状态:

| 坐席状态名称 | 坐席状态英文  |
|--------|---------|
| 离线     | OFFLINE |
| 空闲     | FREE    |
| 忙碌     | BUSY    |
| 小休     | REST    |
| 振铃     | RINGING |
| 通话中    | CALLING |
| 事后处理   | ARRANGE |

坐席状态迁移动作action:

| 坐席状态迁移动作名称  | 坐席状态迁移动作英文                 |
|-------------|----------------------------|
| 签入          | CHECKIN                    |
| 签出          | CHECKOUT                   |
| 示忙          | MAKE_BUSY                  |
| 示闲          | MAKE_FREE                  |
| 进入小休        | MAKE_REST                  |
| 进入事后处理      | MAKE\_ ARRANGE             |
| 通话结束后自动示忙   | MAKE_BUSY_AFTER_CALL_STOP  |
| 通话结束后自动示闲   | MAKE_FREE_AFTER_CALL_STOP  |
| 通话结束后自动进入小休 | MAKE_REST_AFTER_CALL_STOP  |
| 通话结束后自动签出   | CHECKOUT \_AFTER_CALL_STOP |
| 强复位         | FORCE_RESET                |
| 强签          | FORCE\_ CHECKOUT           |
| 强制示闲        | FORCE\_ MAKE\_ FREE        |
| 强制示忙        | FORCE\_ MAKE_BUSY          |
