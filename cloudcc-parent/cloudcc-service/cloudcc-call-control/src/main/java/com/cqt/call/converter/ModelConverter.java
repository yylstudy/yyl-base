package com.cqt.call.converter;

import com.cqt.model.cdr.entity.ExtStatusLog;
import com.cqt.model.client.dto.ClientAnswerDTO;
import com.cqt.model.client.dto.ClientCallDTO;
import com.cqt.model.client.dto.ClientChangeMediaDTO;
import com.cqt.model.client.dto.ClientPlayRecordControlDTO;
import com.cqt.model.ext.dto.ExtStatusDTO;
import com.cqt.model.ext.dto.ExtStatusTransferDTO;
import com.cqt.model.freeswitch.dto.api.AnswerDTO;
import com.cqt.model.freeswitch.dto.api.MediaToggleDTO;
import com.cqt.model.freeswitch.dto.api.OriginateDTO;
import com.cqt.model.freeswitch.dto.api.PlaybackControlDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * @author linshiqiang
 * date:  2023-07-03 17:51
 */
@Mapper(componentModel = "spring")
public interface ModelConverter {

    ModelConverter INSTANCE = Mappers.getMapper(ModelConverter.class);

    /**
     * 前端外呼参数 -> 底层外呼参数
     */
    @Mapping(source = "calleeNumber", target = "callerNumber")
    @Mapping(source = "extId", target = "calleeNumber")
    OriginateDTO clientCall2Originate(ClientCallDTO clientCallDTO);

    /**
     * 分机状态迁移上下文 -> 分机状态迁移日志实体
     */
    @Mapping(target = "reason", ignore = true)
    @Mapping(target = "logId", ignore = true)
    ExtStatusLog extStatusTransfer2ExtStatusLog(ExtStatusTransferDTO extStatusTransferDTO);

    /**
     * 分机状态迁移日志实体 -> 分机实时状态redis数据
     */
    ExtStatusDTO extStatusLog2ExtStatus(ExtStatusLog extStatusLog);

    /**
     * 前端接听请求 -> 底层接听请求
     */
    AnswerDTO client2base(ClientAnswerDTO clientAnswerDTO);

    /**
     * 前端 音视频切换 -> 底层 音视频切换
     */
    MediaToggleDTO client2fs(ClientChangeMediaDTO clientChangeMediaDTO);

    /**
     * SDK放音控制对象 -> 底层放音控制对象
     */
    PlaybackControlDTO client2base(ClientPlayRecordControlDTO clientPlayRecordControlDTO);
}
