package domain.dto;

import cn.hutool.core.collection.ListUtil;
import domain.enums.DifiicultyLevel;
import domain.enums.Memoried;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointDTO {
    @NonNull
    private String name;
    @NonNull
    private String description;
    @NonNull
    private String category="未分类";
    private final LocalDate createDate= LocalDate.now();
    @NonNull
    private DifiicultyLevel difiicultyLevel= DifiicultyLevel.THREE;
    @NonNull
    private Memoried memoried=Memoried.NO;
    private List<AttachmentDTO> attachmentDTOS= ListUtil.empty();

}
