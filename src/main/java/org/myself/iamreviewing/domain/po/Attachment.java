package org.myself.iamreviewing.domain.po;

import org.myself.iamreviewing.domain.enums.FileType;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDate;

@Data
public class Attachment {

    private Long id;
    @NonNull
    private String filename;
    @NonNull
    private Long pointId;
    @NonNull
    private String filepath;
    @NonNull
    private FileType fileType;
    @NonNull
    private Long fileSize= 0L;//单位为字节
    private final LocalDate uploadTime= LocalDate.now();

}
