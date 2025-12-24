package org.myself.iamreviewing.domain.vo;

import cn.hutool.core.collection.ListUtil;
import org.myself.iamreviewing.domain.enums.DifiicultyLevel;
import org.myself.iamreviewing.domain.enums.Memoried;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointVO {
    private Long id;
    private String name;
    private String description;
    private String category="未分类";
    private final LocalDate createDate= LocalDate.now();
    private DifiicultyLevel difiicultyLevel= DifiicultyLevel.THREE;
    private Memoried memoried=Memoried.NO;
    private List<AttachmentVO> attachmentVOS= ListUtil.empty();

}
