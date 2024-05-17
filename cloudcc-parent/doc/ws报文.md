# 签入

```json
{
  "req_id": "22222222222",
  "company_code": "90001",
  "msg_type": "checkin",
  "ext_id": "90001_20000",
  "agent_id": "90001_20000",
  "agent_pwd": "Aa123321.",
  "agent_ip": "172.0.18.24",
  "os": "Windows"
}
```

# 外呼

```json
{
  "req_id": "11111",
  "company_code": "90001",
  "msg_type": "call",
  "callee_number": "90001_20003",
  "agent_id": "90001_20000",
  "ext_id": "90001_20000",
  "is_video": "0",
  "pixels": "",
  "out_line": 0,
  "audio": 3,
  "video": 3,
  "max_ring_time": "",
  "max_call_time": "",
  "record_start": "",
  "record_contact": "",
  "is_stereo": "",
  "os": "Windows",
  "record_suffix": "wav"
}
```

# 咨询

```json
{
  "req_id": "b69912ae-0414-11ee-aaf24-174c4c2dfe01",
  "company_code": "90001",
  "msg_type": "consult",
  "type": "1",
  "consult_type": "0",
  "uuid": "call-c9dba05f401f4b4dae601d3f7002d178",
  "agent_id": "90001_20000",
  "ext_id": "90001_20000",
  "os": "Windows",
  "consult_number": "90001_40001"
}
```

# 转接

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

```json
{
  "req_id": "b69912ae-0414-11ee-aaf24-174c4c2dfe01",
  "company_code": "90001",
  "msg_type": "trans",
  "uuid": "call-9ae84b1052cd43739d88bf86304e68d3",
  "agent_id": "90001_20000",
  "ext_id": "90001_20000",
  "os": "Windows",
  "type": "1",
  "trans_type": 4,
  "trans_number": "13107618845"
}
```

# 三方通话

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

# 状态切换

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

# 挂断

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

# 查询坐席状态

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

# 强拆

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

# 代接

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

