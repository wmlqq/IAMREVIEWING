package org.myself.iamreviewing.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum DifiicultyLevel {
    ONE(1,"简单"),
    TWO(2,"还行"),
    THREE(3,"中等"),
    FOUR(4,"困难"),
    FIVE(5,"放弃");


    @EnumValue
    private final Integer code;
    private final String desc;

    DifiicultyLevel(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
