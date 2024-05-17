package com.cqt.agent.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author linshiqiang
 * @date 2022/7/28 14:03
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewFileDTO {

    private String filePath;

    private String xml;
}
