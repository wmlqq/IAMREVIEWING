package org.myself.iamreviewing.component.previewer;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * DOCX文件预览器
 */
public class DocxPreviewer implements Previewer {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void showPreview(File file, VBox parentContainer) {
        Label title = new Label("DOCX预览 - " + file.getName());
        title.setStyle("-fx-font-weight: bold;");

        VBox docxContent = new VBox(10);
        docxContent.setAlignment(Pos.CENTER);

        // 异步加载DOCX内容，避免阻塞UI线程
        executor.submit(() -> {
            try (XWPFDocument document = new XWPFDocument(java.nio.file.Files.newInputStream(file.toPath()))) {
                // 提取DOCX文本内容
                StringBuilder content = new StringBuilder();
                for (XWPFParagraph paragraph : document.getParagraphs()) {
                    String text = paragraph.getText();
                    if (!text.isEmpty()) {
                        content.append(text).append("\n\n");
                    }
                }

                // 创建文本区域显示内容
                TextArea textArea = new TextArea(content.toString());
                textArea.setWrapText(true);
                textArea.setEditable(false);
                textArea.setStyle("-fx-font-family: 'Microsoft YaHei', 'SimSun', serif; -fx-font-size: 12px; -fx-background-color: #f9f9f9;");

                // 创建滚动面板
                ScrollPane scrollPane = new ScrollPane(textArea);
                scrollPane.setFitToWidth(true);
                scrollPane.setFitToHeight(true);
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                VBox.setVgrow(scrollPane, Priority.ALWAYS);

                // 添加放大查看按钮
                Button enlargeBtn = new Button("放大查看");
                enlargeBtn.setOnAction(e -> {
                    Stage stage = new Stage();
                    stage.setTitle("DOCX放大查看 - " + file.getName());

                    TextArea largeTextArea = new TextArea(content.toString());
                    largeTextArea.setWrapText(true);
                    largeTextArea.setEditable(false);
                    largeTextArea.setStyle("-fx-font-family: 'Microsoft YaHei', 'SimSun', serif; -fx-font-size: 14px; -fx-background-color: #f9f9f9;");

                    ScrollPane largeScrollPane = new ScrollPane(largeTextArea);
                    largeScrollPane.setFitToWidth(true);
                    largeScrollPane.setFitToHeight(true);

                    VBox largeVBox = new VBox(10, largeScrollPane);
                    largeVBox.setStyle("-fx-padding: 20px;");
                    VBox.setVgrow(largeScrollPane, Priority.ALWAYS);

                    stage.setScene(new javafx.scene.Scene(largeVBox, 900, 700));
                    stage.show();
                });

                // 在UI线程中更新界面
                Platform.runLater(() -> {
                    docxContent.getChildren().clear();
                    docxContent.getChildren().addAll(enlargeBtn, scrollPane);
                });

            } catch (IOException e) {
                Platform.runLater(() -> {
                    docxContent.getChildren().clear();
                    showError("加载DOCX文件失败: " + e.getMessage(), docxContent);
                });
            }
        });

        parentContainer.getChildren().addAll(title, docxContent);
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