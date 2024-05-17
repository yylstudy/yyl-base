package com.cqt.model.bind.axe.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author linshiqiang
 * date:  2023-02-22 9:36
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetExtNumVO {

    private String telX;

    private String extNum;

    private Boolean success;
}
