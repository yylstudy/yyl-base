package com.cqt.base.adapter;

import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author linshiqiang
 * date:  2023-07-17 13:45
 * 前端请求参数坐席id和分机id 前要加company_code, 或去除
 * 企业id_坐席id, 企业id_分机id
 */
public enum ClientRequestParamAdapter {

    INSTANCE;

    private static final String FILL_TEMPLATE = "{}_{}";

    private static final String REMOVE_TEMPLATE = "{}_";

    private static final String COMPANY_CODE = "company_code";

    private static final String AGENT_ID = "agent_id";

    private static final String EXT_ID = "ext_id";

    private static final String OPERATED_EXT_ID = "operated_ext_id";

    private static final String OPERATED_AGENT_ID = "operated_agent_id";

    private static final List<String> COLUMN_LIST = new ArrayList<>();

    static {
        COLUMN_LIST.add(AGENT_ID);
        COLUMN_LIST.add(EXT_ID);
        COLUMN_LIST.add(OPERATED_EXT_ID);
        COLUMN_LIST.add(OPERATED_AGENT_ID);
    }

    /**
     * 添加前缀 companyCode_
     */
    public String fillSuffixCompanyCode(ObjectMapper objectMapper, String requestBody) throws Exception {
        Map<String, Object> objectMap = objectMapper.readValue(requestBody, new TypeReference<Map<String, Object>>() {
        });
        // TODO company_code为空情况  必填参数校验
        Object companyCode = objectMap.get(COMPANY_CODE);
        for (String column : COLUMN_LIST) {
            Object id = objectMap.get(column);
            if (!StrUtil.isEmptyIfStr(id)) {
                String fillId = StrFormatter.format(FILL_TEMPLATE, companyCode, id);
                objectMap.put(column, fillId);
            }
        }

        return objectMapper.writeValueAsString(objectMap);
    }

    /**
     * 删除前缀 companyCode_
     */
    public String removeSuffixCompanyCode(ObjectMapper objectMapper, String requestBody) throws Exception {
        Map<String, Object> objectMap = objectMapper.readValue(requestBody, new TypeReference<Map<String, Object>>() {
        });
        Object companyCode = objectMap.get(COMPANY_CODE);
        for (String column : COLUMN_LIST) {
            Object id = objectMap.get(column);
            if (!StrUtil.isEmptyIfStr(id)) {
                String fillId = (String) id;
                String companyCodeSuffix = StrFormatter.format(REMOVE_TEMPLATE, companyCode);
                if (fillId.startsWith(companyCodeSuffix)) {
                    objectMap.put(column, StrUtil.removePrefix(fillId, companyCodeSuffix));
                }
            }
        }

        return objectMapper.writeValueAsString(objectMap);
    }
}
