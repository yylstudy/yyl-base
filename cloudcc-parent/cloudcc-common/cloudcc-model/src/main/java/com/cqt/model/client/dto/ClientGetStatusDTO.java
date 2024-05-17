package com.cqt.model.client.dto;

import com.cqt.model.client.base.ClientRequestBaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-05 14:28
 * SDK 查询坐席状态
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClientGetStatusDTO extends ClientRequestBaseDTO implements Serializable {

}
