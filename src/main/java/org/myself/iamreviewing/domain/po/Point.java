package org.myself.iamreviewing.domain.po;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import org.myself.iamreviewing.domain.enums.DifiicultyLevel;
import org.myself.iamreviewing.domain.enums.Memoried;
import lombok.Data;
import lombok.NonNull;


import java.time.LocalDate;
import java.util.List;

@Data
public class Point {
    private Long id;
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
    @TableField(exist = false)
    private List<Attachment> attachments= ListUtil.empty();
}
