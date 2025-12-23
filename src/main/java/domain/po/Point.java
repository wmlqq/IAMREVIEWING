package domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import domain.enums.DifiicultyLevel;
import domain.enums.Memoried;
import lombok.Data;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

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
    private Attachment[] attachment;


}
