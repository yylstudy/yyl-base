package com.cqt.model.cdr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RemoteQualityCdrDTO<T> {
    private String vccid;
    private String type;
    private List<T> extInfos;
}
