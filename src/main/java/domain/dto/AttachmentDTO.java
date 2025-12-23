package domain.dto;

import domain.enums.FileType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentDTO {
    private String filename;
    private String filepath;
    private FileType fileType;
    private Long fileSize= 0L;//单位为字节
}
