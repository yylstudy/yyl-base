package com.cqt.model.bind.axe.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-02-22 9:18
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsablePoolVO {

    private List<String> poolList;

    private String cityCode;
}
