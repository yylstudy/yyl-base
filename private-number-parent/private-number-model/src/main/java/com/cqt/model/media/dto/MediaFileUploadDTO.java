package com.cqt.model.media.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @author linshiqiang
 * @date 2022/5/16 14:26
 */
@Data
public class MediaFileUploadDTO implements Serializable {

    private static final long serialVersionUID = -6874136491857911543L;

    private MultipartFile file;

    @NotEmpty(message = "vccId不能为空")
    private String vccId;

    @NotEmpty(message = "fileName不能为空")
    private String fileName;
}
