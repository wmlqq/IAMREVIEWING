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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.embed.swing.SwingFXUtils;

/**
 * PDF文件预览器
 */
public class PdfPreviewer implements Previewer {

    // 使用CachedThreadPool，根据需要创建线程，避免请求排队
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    // 页面缓存，提高重复查看性能
    private final Map<Integer, Image> pageCache = new HashMap<>();
    // 缓存DPI设置，平衡质量和性能
    private static final float PREVIEW_DPI = 1.5f;
    private static final float ENLARGE_DPI = 2.0f;

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
            int pageCount = 0;
            
            try {
                // 只加载一次PDF文档，提高性能
                document = PDDocument.load(file);
                pageCount = document.getNumberOfPages();
                PDFRenderer pdfRenderer = new PDFRenderer(document);

                // 创建VBox来存放所有PDF页面
                VBox pagesVBox = new VBox(10);
                pagesVBox.setAlignment(Pos.CENTER);

                // 只渲染前三页，提高加载速度
                int pagesToRender = Math.min(pageCount, 3);
                for (int pageNum = 0; pageNum < pagesToRender; pageNum++) {
                    try {
                        // 渲染页面
                        BufferedImage bufferedImage = pdfRenderer.renderImage(pageNum, PREVIEW_DPI);
                        Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                        
                        // 缓存已渲染的页面
                        pageCache.put(pageNum, image);

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

                        final VBox finalPageContainer = pageContainer;
                        Platform.runLater(() -> pagesVBox.getChildren().add(finalPageContainer));
                    } catch (IOException e) {
                        // 单页渲染失败，显示错误信息但继续渲染其他页
                        String errorMsg = "第 " + (pageNum + 1) + " 页 - 加载失败: " + e.getMessage();
                        Label errorLabel = new Label(errorMsg);
                        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        errorLabel.setWrapText(true);
                        errorLabel.setAlignment(Pos.CENTER);
                        errorLabel.setStyle("-fx-padding: 20px; -fx-background-color: #fff5f5; -fx-border-color: #ffcccc; -fx-border-radius: 5px;");
                        
                        final Label finalErrorLabel = errorLabel;
                        Platform.runLater(() -> pagesVBox.getChildren().add(finalErrorLabel));
                    }
                }

                // 如果有更多页，显示提示
                if (pageCount > pagesToRender) {
                    Label morePagesLabel = new Label("... 还有 " + (pageCount - pagesToRender) + " 页未显示，点击放大查看完整内容");
                    morePagesLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
                    morePagesLabel.setAlignment(Pos.CENTER);
                    Platform.runLater(() -> pagesVBox.getChildren().add(morePagesLabel));
                }

                // 创建滚动面板来显示所有页面
                ScrollPane scrollPane = new ScrollPane(pagesVBox);
                scrollPane.setFitToWidth(true);
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                scrollPane.setStyle("-fx-background-color: #f0f0f0;");
                VBox.setVgrow(scrollPane, Priority.ALWAYS);

                // 保存页面总数用于放大查看
                final int totalPages = pageCount;
                final File finalFile = file;

                // 添加控制按钮
                HBox controls = new HBox(10);
                controls.setAlignment(Pos.CENTER_LEFT);

                // 添加放大查看按钮
                Button enlargeBtn = new Button("放大查看");
                enlargeBtn.setOnAction(e -> showEnlargeView(finalFile, totalPages));

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
                // 关闭文档资源
                if (document != null) {
                    try {
                        document.close();
                    } catch (IOException ex) {
                        // 忽略关闭异常
                    }
                }
            }
        });

        parentContainer.getChildren().addAll(title, pdfContent);
    }

    /**
     * 显示放大查看窗口
     */
    private void showEnlargeView(File file, int totalPages) {
        Stage stage = new Stage();
        stage.setTitle("PDF放大查看 - " + file.getName());

        // 为放大视图创建容器
        VBox largePagesVBox = new VBox(10);
        largePagesVBox.setAlignment(Pos.CENTER);
        largePagesVBox.setStyle("-fx-background-color: #f0f0f0;");

        // 添加加载中提示
        Label loadingLabel = new Label("正在加载所有页面...");
        largePagesVBox.getChildren().add(loadingLabel);

        // 异步加载所有页面，提高响应速度
        executor.submit(() -> {
            PDDocument document = null;
            try {
                // 重新打开文档用于放大查看
                document = PDDocument.load(file);
                PDFRenderer pdfRenderer = new PDFRenderer(document);

                // 清除旧的加载提示
                Platform.runLater(() -> largePagesVBox.getChildren().clear());

                // 渲染所有页面，使用更高的DPI
                for (int pageNum = 0; pageNum < totalPages; pageNum++) {
                    try {
                        // 渲染页面，使用更高的DPI
                        BufferedImage bufferedImage = pdfRenderer.renderImage(pageNum, ENLARGE_DPI);
                        Image image = SwingFXUtils.toFXImage(bufferedImage, null);

                        // 创建final变量用于lambda表达式
                        final int finalPageNum = pageNum;
                        Platform.runLater(() -> {
                            ImageView imageView = new ImageView(image);
                            imageView.setPreserveRatio(true);
                            imageView.setFitWidth(850); // 更大的显示宽度

                            Label pageLabel = new Label("第 " + (finalPageNum + 1) + " 页");
                            pageLabel.setStyle("-fx-font-weight: bold;");
                            pageLabel.setAlignment(Pos.CENTER);

                            VBox pageContainer = new VBox(5, pageLabel, imageView);
                            pageContainer.setAlignment(Pos.CENTER);
                            pageContainer.setStyle("-fx-padding: 10px; -fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5px;");

                            largePagesVBox.getChildren().add(pageContainer);
                        });
                    } catch (IOException ex) {
                        // 创建final变量用于lambda表达式
                        final int finalPageNum = pageNum;
                        Platform.runLater(() -> {
                            Label errorPageLabel = new Label("第 " + (finalPageNum + 1) + " 页 - 加载失败");
                            errorPageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            errorPageLabel.setAlignment(Pos.CENTER);
                            errorPageLabel.setStyle("-fx-padding: 20px; -fx-background-color: #fff5f5; -fx-border-color: #ffcccc; -fx-border-radius: 5px;");
                            largePagesVBox.getChildren().add(errorPageLabel);
                        });
                    }
                }
            } catch (Exception ex) {
                String errorMsg = "放大查看失败: " + ex.getMessage();
                Platform.runLater(() -> {
                    largePagesVBox.getChildren().clear();
                    Label errorLabel = new Label(errorMsg);
                    errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    errorLabel.setAlignment(Pos.CENTER);
                    largePagesVBox.getChildren().add(errorLabel);
                });
            } finally {
                if (document != null) {
                    try {
                        document.close();
                    } catch (IOException ex) {
                        // 忽略关闭异常
                    }
                }
            }
        });

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