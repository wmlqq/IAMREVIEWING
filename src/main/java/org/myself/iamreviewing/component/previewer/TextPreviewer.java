package org.myself.iamreviewing.component.previewer;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 文本/代码文件预览器
 */
public class TextPreviewer implements Previewer {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void showPreview(File file, VBox parentContainer) {
        String previewType = file.getName().endsWith(".java") || file.getName().endsWith(".c") || 
                             file.getName().endsWith(".cpp") || file.getName().endsWith(".py") || 
                             file.getName().endsWith(".js") || file.getName().endsWith(".html") || 
                             file.getName().endsWith(".css") ? "代码" : "文本";
        Label title = new Label(previewType + "预览 - " + file.getName());
        title.setStyle("-fx-font-weight: bold;");

        // 编码选择
        Label charsetLabel = new Label("编码: ");
        ComboBox<String> charsetCombo = new ComboBox<>();
        List<String> commonCharsets = Arrays.asList(
            "UTF-8", "GBK", "GB2312", "ISO-8859-1", "UTF-16", "UTF-32"
        );
        charsetCombo.getItems().addAll(commonCharsets);
        charsetCombo.setValue("UTF-8"); // 默认编码

        // 放大按钮
        Button enlargeBtn = new Button("放大查看");

        // 创建代码编辑器
        TextArea textArea = new TextArea();
        textArea.setWrapText(false); // 代码文件不自动换行
        textArea.setEditable(false);

        // 根据文件类型设置不同的样式
        if ("代码".equals(previewType)) {
            // 代码文件使用等宽字体和语法高亮样式
            textArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; -fx-font-size: 12px; -fx-background-color: #1e1e1e; -fx-text-fill: #d4d4d4; -fx-highlight-fill: #424242; -fx-highlight-text-fill: #ffffff;");
        } else {
            // 普通文本使用默认样式
            textArea.setStyle("-fx-font-family: 'Microsoft YaHei', 'SimSun', serif; -fx-font-size: 12px; -fx-background-color: #f9f9f9; -fx-text-fill: #333;");
        }

        textArea.setPrefRowCount(20);
        VBox.setVgrow(textArea, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // 加载文本内容的方法
        Runnable loadText = () -> {
            try {
                String charset = charsetCombo.getValue();
                byte[] bytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
                String content = new String(bytes, Charset.forName(charset));

                // 在UI线程中更新文本
                Platform.runLater(() -> {
                    textArea.setText(content);
                });
            } catch (IOException e) {
                showError("读取文件内容失败: " + e.getMessage(), parentContainer);
            }
        };

        // 异步加载文本内容，避免大文件阻塞UI线程
        executor.submit(loadText);

        // 编码切换事件
        charsetCombo.setOnAction(e -> loadText.run());

        // 放大查看事件
        enlargeBtn.setOnAction(e -> {
            Stage stage = new Stage();
            stage.setTitle("放大查看 - " + file.getName());

            TextArea largeTextArea = new TextArea();
            largeTextArea.setWrapText(false);
            largeTextArea.setEditable(false);

            // 放大视图的样式
            if ("代码".equals(previewType)) {
                largeTextArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; -fx-font-size: 14px; -fx-background-color: #1e1e1e; -fx-text-fill: #d4d4d4; -fx-highlight-fill: #424242; -fx-highlight-text-fill: #ffffff;");
            } else {
                largeTextArea.setStyle("-fx-font-family: 'Microsoft YaHei', 'SimSun', serif; -fx-font-size: 14px; -fx-background-color: #f9f9f9; -fx-text-fill: #333;");
            }

            // 加载内容到放大视图
            largeTextArea.setText(textArea.getText());

            ScrollPane largeScrollPane = new ScrollPane(largeTextArea);
            largeScrollPane.setFitToWidth(true);
            largeScrollPane.setFitToHeight(true);
            largeScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            largeScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            // 放大视图的编码选择
            Label largeCharsetLabel = new Label("编码: ");
            ComboBox<String> largeCharsetCombo = new ComboBox<>();
            largeCharsetCombo.getItems().addAll(commonCharsets);
            largeCharsetCombo.setValue(charsetCombo.getValue());

            // 放大视图的编码切换
            largeCharsetCombo.setOnAction(ev -> {
                try {
                    String charset = largeCharsetCombo.getValue();
                    byte[] bytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
                    String content = new String(bytes, Charset.forName(charset));
                    largeTextArea.setText(content);
                } catch (IOException ex) {
                    showError("读取文件内容失败: " + ex.getMessage(), new VBox());
                }
            });

            HBox largeToolbar = new HBox(10, largeCharsetLabel, largeCharsetCombo);
            largeToolbar.setAlignment(Pos.CENTER_LEFT);
            largeToolbar.setStyle("-fx-padding: 10px;");

            VBox largeVBox = new VBox(10, largeToolbar, largeScrollPane);
            largeVBox.setStyle("-fx-padding: 0 10px 10px 10px;");
            VBox.setVgrow(largeScrollPane, Priority.ALWAYS);

            stage.setScene(new javafx.scene.Scene(largeVBox, 1000, 800));
            stage.show();
        });

        // 工具栏
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getChildren().addAll(charsetLabel, charsetCombo, enlargeBtn);

        parentContainer.getChildren().addAll(title, toolbar, scrollPane);
    }

    /**
     * 显示错误信息
     * @param message 错误信息
     * @param container 容器，错误信息将添加到这个容器中
     */
    private void showError(String message, VBox container) {
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        errorLabel.setWrapText(true);
        errorLabel.setAlignment(Pos.CENTER);

        container.getChildren().add(errorLabel);
    }
}