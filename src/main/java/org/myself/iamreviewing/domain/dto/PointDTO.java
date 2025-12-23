package org.myself.iamreviewing.domain.dto;

import cn.hutool.core.collection.ListUtil;
import org.myself.iamreviewing.domain.enums.DifiicultyLevel;
import org.myself.iamreviewing.domain.enums.Memoried;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointDTO {
    private String name;
    private String description;
    private String category="未分类";
    private final LocalDate createDate= LocalDate.now();
    private DifiicultyLevel difiicultyLevel= DifiicultyLevel.THREE;
    private Memoried memoried=Memoried.NO;
    private List<AttachmentDTO> attachmentDTOS= ListUtil.empty();
}
