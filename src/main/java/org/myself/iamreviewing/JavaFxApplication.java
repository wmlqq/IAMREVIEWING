package org.myself.iamreviewing;

import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;
import org.myself.iamreviewing.service.AttachmentService;
import org.myself.iamreviewing.service.PointService;

public class JavaFxApplication extends Application {

    // Spring管理的Service（后续通过上下文获取）
    private PointService pointService;
    private AttachmentService attachmentService;

    @Override
    public void init() throws Exception {
        // 初始化阶段：从Spring上下文获取Bean（关键步骤）
        ConfigurableApplicationContext springContext = IamreviewingApplication.getSpringContext();
        this.pointService = springContext.getBean(PointService.class);
        this.attachmentService = springContext.getBean(AttachmentService.class);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

    }

    @Override
    public void stop() throws Exception {
        // 关闭阶段：销毁Spring上下文
        ConfigurableApplicationContext springContext = IamreviewingApplication.getSpringContext();
        springContext.close();
    }
}
