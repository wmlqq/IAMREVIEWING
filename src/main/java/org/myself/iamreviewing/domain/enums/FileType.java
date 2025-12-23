package org.myself.iamreviewing.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum FileType {
    TEXT(1,"文本"),
    IMAGE(2,"图片"),
    VIDEO(3,"视频"),
    AUDIO(4,"音频"),
    CODE(5,"代码");

    @EnumValue
    private final Integer code;
    private final String desc;
    FileType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
