package org.myself.iamreviewing.domain.vo;

import org.myself.iamreviewing.domain.enums.FileType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentVO {
    private Long id;
    private String filename;
    private String filepath;
    private FileType fileType;
    private Long fileSize;//单位为字节
    private LocalDate uploadTime;
}
