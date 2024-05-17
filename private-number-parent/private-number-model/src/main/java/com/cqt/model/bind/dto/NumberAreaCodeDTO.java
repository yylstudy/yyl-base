package com.cqt.model.bind.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author linshiqiang
 * date:  2023-06-30 10:55
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class NumberAreaCodeDTO {

    private String number;

    private String areaCode;
}
