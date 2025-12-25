package org.myself.iamreviewing.component.previewer;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.embed.swing.SwingFXUtils;

/**
 * PDF文件预览器
 */
public class PdfPreviewer implements Previewer {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void showPreview(File file, VBox parentContainer) {
        Label title = new Label("PDF预览 - " + file.getName());
        title.setStyle("-fx-font-weight: bold;");

        VBox pdfContent = new VBox(10);
        pdfContent.setAlignment(Pos.CENTER);

        // 添加加载中提示
        Label loadingLabel = new Label("正在加载PDF文件...");
        pdfContent.getChildren().add(loadingLabel);

        // 异步加载PDF内容，避免阻塞UI线程
        executor.submit(() -> {
            PDDocument document = null;
            try {
                // 加载PDF文档
                document = PDDocument.load(file);
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                int pageCount = document.getNumberOfPages();

                // 创建VBox来存放所有PDF页面
                VBox pagesVBox = new VBox(10);
                pagesVBox.setAlignment(Pos.CENTER);

                // 渲染每一页
                for (int pageNum = 0; pageNum < pageCount; pageNum++) {
                    try {
                        // 渲染PDF页面到BufferedImage，使用较低的DPI以提高性能
                        BufferedImage bufferedImage = pdfRenderer.renderImage(pageNum, 1.0f); // 1.0倍缩放，平衡质量和性能
                        // 转换为JavaFX Image
                        Image image = SwingFXUtils.toFXImage(bufferedImage, null);

                        ImageView imageView = new ImageView(image);
                        imageView.setPreserveRatio(true);
                        imageView.setFitWidth(500); // 设置适合预览的宽度

                        // 为每一页添加页面标题
                        Label pageLabel = new Label("第 " + (pageNum + 1) + " 页");
                        pageLabel.setStyle("-fx-font-weight: bold;");
                        pageLabel.setAlignment(Pos.CENTER);

                        VBox pageContainer = new VBox(5, pageLabel, imageView);
                        pageContainer.setAlignment(Pos.CENTER);
                        pageContainer.setStyle("-fx-padding: 10px; -fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5px;");

                        pagesVBox.getChildren().add(pageContainer);
                    } catch (IOException e) {
                        // 单页渲染失败，显示错误信息但继续渲染其他页
                        Label errorPageLabel = new Label("第 " + (pageNum + 1) + " 页 - 加载失败");
                        errorPageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

                        Label errorMsgLabel = new Label("错误: " + e.getMessage());
                        errorMsgLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                        errorMsgLabel.setWrapText(true);

                        VBox errorContainer = new VBox(5, errorPageLabel, errorMsgLabel);
                        errorContainer.setAlignment(Pos.CENTER);
                        errorContainer.setStyle("-fx-padding: 20px; -fx-background-color: #fff5f5; -fx-border-color: #ffcccc; -fx-border-radius: 5px;");

                        pagesVBox.getChildren().add(errorContainer);
                    }
                }

                // 创建滚动面板来显示所有页面
                ScrollPane scrollPane = new ScrollPane(pagesVBox);
                scrollPane.setFitToWidth(true);
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                scrollPane.setStyle("-fx-background-color: #f0f0f0;");
                VBox.setVgrow(scrollPane, Priority.ALWAYS);

                // 添加控制按钮
                HBox controls = new HBox(10);
                controls.setAlignment(Pos.CENTER_LEFT);

                // 添加放大查看按钮
                Button enlargeBtn = new Button("放大查看");
                enlargeBtn.setOnAction(e -> {
                    Stage stage = new Stage();
                    stage.setTitle("PDF放大查看 - " + file.getName());

                    // 为放大视图重新创建页面，避免共享UI组件问题
                    VBox largePagesVBox = new VBox(10);
                    largePagesVBox.setAlignment(Pos.CENTER);

                    // 重新渲染所有页面，使用更高的DPI
                    for (int pageNum = 0; pageNum < pageCount; pageNum++) {
                        try {
                            BufferedImage bufferedImage = pdfRenderer.renderImage(pageNum, 1.5f); // 1.5倍缩放，更高质量
                            Image image = SwingFXUtils.toFXImage(bufferedImage, null);

                            ImageView imageView = new ImageView(image);
                            imageView.setPreserveRatio(true);
                            imageView.setFitWidth(800); // 更大的显示宽度

                            Label pageLabel = new Label("第 " + (pageNum + 1) + " 页");
                            pageLabel.setStyle("-fx-font-weight: bold;");
                            pageLabel.setAlignment(Pos.CENTER);

                            VBox pageContainer = new VBox(5, pageLabel, imageView);
                            pageContainer.setAlignment(Pos.CENTER);
                            pageContainer.setStyle("-fx-padding: 10px; -fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5px;");

                            largePagesVBox.getChildren().add(pageContainer);
                        } catch (IOException ex) {
                            Label errorPageLabel = new Label("第 " + (pageNum + 1) + " 页 - 加载失败");
                            errorPageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            largePagesVBox.getChildren().add(errorPageLabel);
                        }
                    }

                    ScrollPane largeScrollPane = new ScrollPane(largePagesVBox);
                    largeScrollPane.setFitToWidth(true);
                    largeScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                    largeScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                    largeScrollPane.setStyle("-fx-background-color: #f0f0f0;");

                    VBox largeVBox = new VBox(10, largeScrollPane);
                    largeVBox.setStyle("-fx-padding: 10px;");
                    VBox.setVgrow(largeScrollPane, Priority.ALWAYS);

                    stage.setScene(new javafx.scene.Scene(largeVBox, 900, 700));
                    stage.show();
                });

                // 添加页码信息
                Label pageInfoLabel = new Label("共 " + pageCount + " 页");
                pageInfoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

                controls.getChildren().addAll(enlargeBtn, pageInfoLabel);

                // 在UI线程中更新界面
                Platform.runLater(() -> {
                    pdfContent.getChildren().clear();
                    pdfContent.getChildren().addAll(controls, scrollPane);
                });

            } catch (Exception e) {
                // 处理PDF加载的所有异常
                String errorMsg = "加载PDF文件失败: " + e.getMessage();
                System.err.println(errorMsg);
                e.printStackTrace();

                Platform.runLater(() -> {
                    pdfContent.getChildren().clear();
                    showError(errorMsg, pdfContent);
                });
            } finally {
                // 确保文档关闭
                if (document != null) {
                    try {
                        document.close();
                    } catch (IOException e) {
                        // 忽略关闭异常
                    }
                }
            }
        });

        parentContainer.getChildren().addAll(title, pdfContent);
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