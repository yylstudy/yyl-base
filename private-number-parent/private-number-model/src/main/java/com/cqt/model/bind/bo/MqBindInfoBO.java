package com.cqt.model.bind.bo;

import com.cqt.model.bind.ax.entity.PrivateBindInfoAx;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxb;
import com.cqt.model.bind.axbn.entity.PrivateBindInfoAxbn;
import com.cqt.model.bind.axe.entity.PrivateBindInfoAxe;
import com.cqt.model.bind.axebn.entity.PrivateBindInfoAxebn;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author linshiqiang
 * @date 2021/11/19 16:00
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MqBindInfoBO implements Serializable {

    private static final long serialVersionUID = 7263446463445344441L;

    /**
     * 操作类型
     */
    private String operateType;

    private String numType;

    private String vccId;

    private String cityCode;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date dateTime;

    private String date;

    private PrivateBindInfoAxb privateBindInfoAxb;

    private PrivateBindInfoAxbn privateBindInfoAxbn;

    private PrivateBindInfoAxe privateBindInfoAxe;

    private PrivateBindInfoAxebn privateBindInfoAxebn;

    private PrivateBindInfoAx privateBindInfoAx;

}
