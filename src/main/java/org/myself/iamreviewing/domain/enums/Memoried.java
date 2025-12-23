package org.myself.iamreviewing.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum Memoried {
    YES(0,"会了"),
    MAYBE(1,"可能"),
    NO(2,"不会");

    @EnumValue
    private final Integer code;
    private final String desc;
    Memoried(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
