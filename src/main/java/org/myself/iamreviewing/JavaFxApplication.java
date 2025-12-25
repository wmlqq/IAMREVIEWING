package org.myself.iamreviewing;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
        // 加载FXML文件
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        
        // 设置Spring上下文作为控制器工厂
        loader.setControllerFactory(IamreviewingApplication.getSpringContext()::getBean);
        
        // 加载根节点
        Parent root = loader.load();
        
        // 创建场景
        Scene scene = new Scene(root, 1200, 800);
        
        // 设置主舞台属性
        primaryStage.setTitle("I am Reviewing!!!");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(600);
        
        // 显示主舞台
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        // 关闭阶段：销毁Spring上下文
        ConfigurableApplicationContext springContext = IamreviewingApplication.getSpringContext();
        springContext.close();
    }
}
