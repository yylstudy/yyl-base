package com.cqt.model.client.vo;

import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientChangeMediaDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-06-29 9:42
 * SDK 音视频切换请求 响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ClientChangeMediaVO extends ClientResponseBaseVO implements Serializable {

    private static final long serialVersionUID = -2372939590647360393L;

    /**
     * 【0:无语音流， 1:只发送语音流不接收， 2:只接收语音流不发送， 3:发送并接收语音流】默认3
     */
    private Integer audio;

    /**
     * 【0:无视频流， 1:只发送视频流不接收， 2:只接收视频流不发送， 3:发送并接收视频流】默认0
     */
    private Integer video;

    /**
     * 视频分辨率【480， 720， 1280】 默认480(video不为0时可用)
     */
    private String pixels;

    /**
     * 是  | 通话ID
     */
    private String uuid;

    /**
     * 响应
     */
    public static ClientChangeMediaVO response(ClientChangeMediaDTO clientChangeMediaDTO, String code, String msg) {
        ClientChangeMediaVO responseBaseVO = new ClientChangeMediaVO();
        responseBaseVO.setReqId(clientChangeMediaDTO.getReqId());
        responseBaseVO.setCompanyCode(clientChangeMediaDTO.getCompanyCode());
        responseBaseVO.setMsgType(clientChangeMediaDTO.getMsgType());
        responseBaseVO.setCode(code);
        responseBaseVO.setMsg(msg);
        responseBaseVO.setReply(true);
        responseBaseVO.setAudio(clientChangeMediaDTO.getAudio());
        responseBaseVO.setVideo(clientChangeMediaDTO.getVideo());
        responseBaseVO.setPixels(clientChangeMediaDTO.getPixels());
        responseBaseVO.setUuid(clientChangeMediaDTO.getUuid());
        return responseBaseVO;
    }
}
