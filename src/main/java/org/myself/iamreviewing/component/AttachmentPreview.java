package org.myself.iamreviewing.component;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.embed.swing.SwingFXUtils;

public class AttachmentPreview extends VBox {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    public AttachmentPreview() {
        this.setAlignment(Pos.CENTER);
        this.setSpacing(10);
        this.setPrefHeight(300);
        this.setStyle("-fx-padding: 10px;");
    }

    /**
     * 根据文件路径和类型显示预览
     * @param filePath 文件路径
     * @param fileType 文件类型描述
     */
    public void showPreview(String filePath, String fileType) {
        File file = new File(filePath);
        
        // 清空当前预览内容
        this.getChildren().clear();
        
        // 检查文件是否存在
        if (!file.exists()) {
            showError("附件文件不存在，请检查文件路径");
            return;
        }
        
        // 根据文件类型显示不同的预览
        switch (fileType) {
            case "文本":
                // 检查是否是PDF或DOCX文件
                if (filePath.toLowerCase().endsWith(".pdf")) {
                    showPdfPreview(file);
                } else if (filePath.toLowerCase().endsWith(".docx")) {
                    showDocxPreview(file);
                } else {
                    showTextPreview(file);
                }
                break;
            case "代码":
                showTextPreview(file);
                break;
            case "图片":
                showImagePreview(file);
                break;
            case "音频":
                showAudioPreview(file);
                break;
            case "视频":
                showVideoPreview(file);
                break;
            default:
                showError("不支持的文件类型预览");
        }
    }
    
    /**
     * 显示PDF文件预览
     * @param file 文件对象
     */
    private void showPdfPreview(File file) {
        Label title = new Label("PDF预览 - " + file.getName());
        title.setStyle("-fx-font-weight: bold;");
        
        VBox pdfContent = new VBox(10);
        pdfContent.setAlignment(Pos.CENTER);
        
        // 异步加载PDF内容，避免阻塞UI线程
        executor.submit(() -> {
            try (PDDocument document = PDDocument.load(file)) {
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                int pageCount = document.getNumberOfPages();
                
                // 创建VBox来存放所有PDF页面
                VBox pagesVBox = new VBox(10);
                pagesVBox.setAlignment(Pos.CENTER);
                
                // 渲染每一页
                for (int pageNum = 0; pageNum < pageCount; pageNum++) {
                    // 渲染PDF页面到BufferedImage
                    BufferedImage bufferedImage = pdfRenderer.renderImage(pageNum, 1.5f); // 1.5倍缩放
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
                }
                
                // 创建滚动面板来显示所有页面
                ScrollPane scrollPane = new ScrollPane(pagesVBox);
                scrollPane.setFitToWidth(true);
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                VBox.setVgrow(scrollPane, Priority.ALWAYS);
                
                // 添加放大查看按钮
                Button enlargeBtn = new Button("放大查看");
                enlargeBtn.setOnAction(e -> {
                    Stage stage = new Stage();
                    stage.setTitle("PDF放大查看 - " + file.getName());
                    
                    ScrollPane largeScrollPane = new ScrollPane(pagesVBox);
                    largeScrollPane.setFitToWidth(true);
                    largeScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                    largeScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                    
                    VBox largeVBox = new VBox(10, largeScrollPane);
                    largeVBox.setStyle("-fx-padding: 10px;");
                    VBox.setVgrow(largeScrollPane, Priority.ALWAYS);
                    
                    stage.setScene(new javafx.scene.Scene(largeVBox, 900, 700));
                    stage.show();
                });
                
                // 在UI线程中更新界面
                javafx.application.Platform.runLater(() -> {
                    pdfContent.getChildren().clear();
                    pdfContent.getChildren().addAll(enlargeBtn, scrollPane);
                });
                
            } catch (IOException e) {
                javafx.application.Platform.runLater(() -> {
                    showError("加载PDF文件失败: " + e.getMessage());
                });
            }
        });
        
        this.getChildren().addAll(title, pdfContent);
    }
    
    /**
     * 显示DOCX文件预览
     * @param file 文件对象
     */
    private void showDocxPreview(File file) {
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
                javafx.application.Platform.runLater(() -> {
                    docxContent.getChildren().clear();
                    docxContent.getChildren().addAll(enlargeBtn, scrollPane);
                });
                
            } catch (IOException e) {
                javafx.application.Platform.runLater(() -> {
                    showError("加载DOCX文件失败: " + e.getMessage());
                });
            }
        });
        
        this.getChildren().addAll(title, docxContent);
    }

    /**
     * 显示文本/代码文件预览
     * @param file 文件对象
     */
    private void showTextPreview(File file) {
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
                javafx.application.Platform.runLater(() -> {
                    textArea.setText(content);
                });
            } catch (IOException e) {
                showError("读取文件内容失败: " + e.getMessage());
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
                    showError("读取文件内容失败: " + ex.getMessage());
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
        
        this.getChildren().addAll(title, toolbar, scrollPane);
    }

    /**
     * 显示图片预览
     * @param file 文件对象
     */
    private void showImagePreview(File file) {
        Label title = new Label("图片预览 - " + file.getName());
        title.setStyle("-fx-font-weight: bold;");
        
        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        
        try {
            Image image = new Image(file.toURI().toString());
            imageView.setImage(image);
            
            // 根据图片实际尺寸动态调整显示大小
            double imageWidth = image.getWidth();
            double imageHeight = image.getHeight();
            
            // 计算合适的显示尺寸，最大宽度500，最大高度300
            double displayWidth, displayHeight;
            if (imageWidth > imageHeight) {
                // 宽图
                displayWidth = Math.min(imageWidth, 500);
                displayHeight = (displayWidth / imageWidth) * imageHeight;
            } else {
                // 高图或方图
                displayHeight = Math.min(imageHeight, 300);
                displayWidth = (displayHeight / imageHeight) * imageWidth;
            }
            
            imageView.setFitWidth(displayWidth);
            imageView.setFitHeight(displayHeight);
            
        } catch (Exception e) {
            showError("加载图片失败: " + e.getMessage());
            return;
        }
        
        ScrollPane scrollPane = new ScrollPane(imageView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: #f0f0f0;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        // 控制按钮
        Button enlargeBtn = new Button("放大查看");
        Button rotateBtn = new Button("旋转");
        
        // 旋转功能
        rotateBtn.setOnAction(e -> {
            imageView.setRotate(imageView.getRotate() + 90);
        });
        
        // 放大查看
        enlargeBtn.setOnAction(e -> {
            Stage stage = new Stage();
            stage.setTitle("放大查看 - " + file.getName());
            
            Image image = imageView.getImage();
            ImageView largeImageView = new ImageView(image);
            largeImageView.setPreserveRatio(true);
            
            // 根据图片实际尺寸设置初始窗口大小
            double imageWidth = image.getWidth();
            double imageHeight = image.getHeight();
            
            // 计算合适的窗口大小，最大宽度1000，最大高度800
            double windowWidth = Math.min(imageWidth + 40, 1000);
            double windowHeight = Math.min(imageHeight + 80, 800);
            
            // 设置图片显示大小，保持原始比例
            largeImageView.setFitWidth(imageWidth > 1000 ? 1000 : imageWidth);
            largeImageView.setFitHeight(imageHeight > 800 ? 800 : imageHeight);
            
            ScrollPane largeScrollPane = new ScrollPane(largeImageView);
            largeScrollPane.setFitToWidth(true);
            largeScrollPane.setFitToHeight(true);
            largeScrollPane.setStyle("-fx-background-color: #f0f0f0;");
            
            // 放大视图的旋转按钮
            Button largeRotateBtn = new Button("旋转");
            largeRotateBtn.setOnAction(ev -> {
                largeImageView.setRotate(largeImageView.getRotate() + 90);
            });
            
            // 添加缩放控制
            ComboBox<String> zoomCombo = new ComboBox<>();
            zoomCombo.getItems().addAll("50%", "75%", "100%", "125%", "150%", "200%", "适应窗口");
            zoomCombo.setValue("100%");
            
            zoomCombo.setOnAction(ev -> {
                String zoomValue = zoomCombo.getValue();
                if ("适应窗口".equals(zoomValue)) {
                    largeImageView.setFitWidth(windowWidth - 40);
                    largeImageView.setFitHeight(windowHeight - 80);
                } else {
                    double zoom = Double.parseDouble(zoomValue.replace("%", "")) / 100;
                    largeImageView.setFitWidth(image.getWidth() * zoom);
                    largeImageView.setFitHeight(image.getHeight() * zoom);
                }
            });
            
            // 窗口大小变化时，更新"适应窗口"选项的显示
            stage.widthProperty().addListener((obs, oldWidth, newWidth) -> {
                if ("适应窗口".equals(zoomCombo.getValue())) {
                    largeImageView.setFitWidth(newWidth.doubleValue() - 40);
                }
            });
            
            stage.heightProperty().addListener((obs, oldHeight, newHeight) -> {
                if ("适应窗口".equals(zoomCombo.getValue())) {
                    largeImageView.setFitHeight(newHeight.doubleValue() - 80);
                }
            });
            
            HBox largeControls = new HBox(10, largeRotateBtn, zoomCombo);
            largeControls.setAlignment(Pos.CENTER_LEFT);
            largeControls.setStyle("-fx-padding: 10px;");
            
            VBox largeVBox = new VBox(10, largeControls, largeScrollPane);
            largeVBox.setStyle("-fx-padding: 10px;");
            VBox.setVgrow(largeScrollPane, Priority.ALWAYS);
            
            stage.setScene(new javafx.scene.Scene(largeVBox, windowWidth, windowHeight));
            stage.show();
        });
        
        // 工具栏
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getChildren().addAll(enlargeBtn, rotateBtn);
        
        this.getChildren().addAll(title, toolbar, scrollPane);
    }

    /**
     * 显示音频预览
     * @param file 文件对象
     */
    private void showAudioPreview(File file) {
        Label title = new Label("音频预览 - " + file.getName());
        title.setStyle("-fx-font-weight: bold;");
        
        try {
            Media media = new Media(file.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            
            // 音频控制按钮
            Button playBtn = new Button("播放");
            playBtn.setOnAction(e -> {
                if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    mediaPlayer.pause();
                    playBtn.setText("播放");
                } else {
                    mediaPlayer.play();
                    playBtn.setText("暂停");
                }
            });
            
            Button stopBtn = new Button("停止");
            stopBtn.setOnAction(e -> {
                mediaPlayer.stop();
                playBtn.setText("播放");
            });
            
            // 进度条
            Slider progressSlider = new Slider(0, 1, 0);
            progressSlider.setShowTickLabels(false);
            progressSlider.setShowTickMarks(false);
            progressSlider.setStyle("-fx-pref-height: 10px;");
            
            // 时间显示标签
            Label timeLabel = new Label("00:00 / 00:00");
            timeLabel.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; -fx-font-size: 12px;");
            
            // 当媒体时长可用时更新进度条最大值和时间标签
            media.durationProperty().addListener((obs, oldDuration, newDuration) -> {
                progressSlider.setMax(newDuration.toSeconds());
                // 内联格式化时间
                int seconds = (int) Math.floor(newDuration.toSeconds());
                int minutes = seconds / 60;
                seconds %= 60;
                String formattedDuration = String.format("%02d:%02d", minutes, seconds);
                timeLabel.setText("00:00 / " + formattedDuration);
            });
            
            // 进度条更新
            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (!progressSlider.isValueChanging()) {
                    progressSlider.setValue(newTime.toSeconds());
                    // 内联格式化时间
                    int currentSeconds = (int) Math.floor(newTime.toSeconds());
                    int currentMinutes = currentSeconds / 60;
                    currentSeconds %= 60;
                    String formattedCurrent = String.format("%02d:%02d", currentMinutes, currentSeconds);
                    
                    int totalSeconds = (int) Math.floor(media.getDuration().toSeconds());
                    int totalMinutes = totalSeconds / 60;
                    totalSeconds %= 60;
                    String formattedTotal = String.format("%02d:%02d", totalMinutes, totalSeconds);
                    
                    timeLabel.setText(formattedCurrent + " / " + formattedTotal);
                }
            });
            
            // 进度条拖动
            progressSlider.setOnMousePressed(e -> {
                mediaPlayer.pause();
            });
            
            progressSlider.setOnMouseReleased(e -> {
                mediaPlayer.seek(javafx.util.Duration.seconds(progressSlider.getValue()));
                mediaPlayer.play();
                playBtn.setText("暂停");
            });
            
            // 进度条拖动过程中更新时间显示
            progressSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
                if (progressSlider.isValueChanging()) {
                    // 内联格式化时间
                    int currentSeconds = (int) Math.floor(newValue.doubleValue());
                    int currentMinutes = currentSeconds / 60;
                    currentSeconds %= 60;
                    String formattedCurrent = String.format("%02d:%02d", currentMinutes, currentSeconds);
                    
                    int totalSeconds = (int) Math.floor(media.getDuration().toSeconds());
                    int totalMinutes = totalSeconds / 60;
                    totalSeconds %= 60;
                    String formattedTotal = String.format("%02d:%02d", totalMinutes, totalSeconds);
                    
                    timeLabel.setText(formattedCurrent + " / " + formattedTotal);
                }
            });
            
            HBox controls = new HBox(10, playBtn, stopBtn, progressSlider, timeLabel);
            controls.setAlignment(Pos.CENTER);
            controls.setStyle("-fx-padding: 10px;");
            HBox.setHgrow(progressSlider, Priority.ALWAYS);
            
            this.getChildren().addAll(title, controls);
            
            // 清理资源
            mediaPlayer.setOnEndOfMedia(() -> {
                playBtn.setText("播放");
            });
            
        } catch (Exception e) {
            showError("加载音频失败: " + e.getMessage());
        }
    }

    /**
     * 显示视频预览
     * @param file 文件对象
     */
    private void showVideoPreview(File file) {
        Label title = new Label("视频预览 - " + file.getName());
        title.setStyle("-fx-font-weight: bold;");
        
        try {
            Media media = new Media(file.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            MediaView mediaView = new MediaView(mediaPlayer);
            
            mediaView.setPreserveRatio(true);
            mediaView.setFitWidth(500);
            mediaView.setFitHeight(300);
            
            // 视频控制按钮
            Button playBtn = new Button("播放");
            playBtn.setOnAction(e -> {
                if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    mediaPlayer.pause();
                    playBtn.setText("播放");
                } else {
                    mediaPlayer.play();
                    playBtn.setText("暂停");
                }
            });
            
            Button stopBtn = new Button("停止");
            stopBtn.setOnAction(e -> {
                mediaPlayer.stop();
                playBtn.setText("播放");
            });
            
            Button enlargeBtn = new Button("放大查看");
            Button rotateBtn = new Button("旋转");
            
            // 旋转功能
            rotateBtn.setOnAction(e -> {
                mediaView.setRotate(mediaView.getRotate() + 90);
            });
            
            // 进度条
            Slider progressSlider = new Slider(0, 1, 0);
            progressSlider.setShowTickLabels(false);
            progressSlider.setShowTickMarks(false);
            progressSlider.setStyle("-fx-pref-height: 10px;");
            
            // 时间显示标签
            Label timeLabel = new Label("00:00 / 00:00");
            timeLabel.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; -fx-font-size: 12px;");
            
            // 当媒体时长可用时更新进度条最大值和时间标签
            media.durationProperty().addListener((obs, oldDuration, newDuration) -> {
                progressSlider.setMax(newDuration.toSeconds());
                // 内联格式化时间
                int seconds = (int) Math.floor(newDuration.toSeconds());
                int minutes = seconds / 60;
                seconds %= 60;
                String formattedDuration = String.format("%02d:%02d", minutes, seconds);
                timeLabel.setText("00:00 / " + formattedDuration);
            });
            
            // 进度条更新
            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (!progressSlider.isValueChanging()) {
                    progressSlider.setValue(newTime.toSeconds());
                    // 内联格式化时间
                    int currentSeconds = (int) Math.floor(newTime.toSeconds());
                    int currentMinutes = currentSeconds / 60;
                    currentSeconds %= 60;
                    String formattedCurrent = String.format("%02d:%02d", currentMinutes, currentSeconds);
                    
                    int totalSeconds = (int) Math.floor(media.getDuration().toSeconds());
                    int totalMinutes = totalSeconds / 60;
                    totalSeconds %= 60;
                    String formattedTotal = String.format("%02d:%02d", totalMinutes, totalSeconds);
                    
                    timeLabel.setText(formattedCurrent + " / " + formattedTotal);
                }
            });
            
            // 进度条拖动
            progressSlider.setOnMousePressed(e -> {
                mediaPlayer.pause();
            });
            
            progressSlider.setOnMouseReleased(e -> {
                mediaPlayer.seek(javafx.util.Duration.seconds(progressSlider.getValue()));
                mediaPlayer.play();
                playBtn.setText("暂停");
            });
            
            // 进度条拖动过程中更新时间显示
            progressSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
                if (progressSlider.isValueChanging()) {
                    // 内联格式化时间
                    int currentSeconds = (int) Math.floor(newValue.doubleValue());
                    int currentMinutes = currentSeconds / 60;
                    currentSeconds %= 60;
                    String formattedCurrent = String.format("%02d:%02d", currentMinutes, currentSeconds);
                    
                    int totalSeconds = (int) Math.floor(media.getDuration().toSeconds());
                    int totalMinutes = totalSeconds / 60;
                    totalSeconds %= 60;
                    String formattedTotal = String.format("%02d:%02d", totalMinutes, totalSeconds);
                    
                    timeLabel.setText(formattedCurrent + " / " + formattedTotal);
                }
            });
            
            // 放大查看
            enlargeBtn.setOnAction(e -> {
                Stage stage = new Stage();
                stage.setTitle("放大查看 - " + file.getName());
                
                MediaPlayer largeMediaPlayer = new MediaPlayer(media);
                MediaView largeMediaView = new MediaView(largeMediaPlayer);
                largeMediaView.setPreserveRatio(true);
                
                // 动态调整视频尺寸以适应窗口
                stage.widthProperty().addListener((obs, oldWidth, newWidth) -> {
                    largeMediaView.setFitWidth(newWidth.doubleValue() - 40);
                    largeMediaView.setFitHeight((newWidth.doubleValue() - 40) * 9 / 16); // 16:9 比例
                });
                
                // 放大视图的控制按钮
                Button largePlayBtn = new Button("播放");
                largePlayBtn.setOnAction(ev -> {
                    if (largeMediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                        largeMediaPlayer.pause();
                        largePlayBtn.setText("播放");
                    } else {
                        largeMediaPlayer.play();
                        largePlayBtn.setText("暂停");
                    }
                });
                
                Button largeStopBtn = new Button("停止");
                largeStopBtn.setOnAction(ev -> {
                    largeMediaPlayer.stop();
                    largePlayBtn.setText("播放");
                });
                
                Button largeRotateBtn = new Button("旋转");
                largeRotateBtn.setOnAction(ev -> {
                    largeMediaView.setRotate(largeMediaView.getRotate() + 90);
                });
                
                // 放大视图的进度条
                Slider largeProgressSlider = new Slider(0, 1, 0);
                largeProgressSlider.setShowTickLabels(false);
                largeProgressSlider.setShowTickMarks(false);
                largeProgressSlider.setStyle("-fx-pref-height: 10px;");
                
                // 放大视图的时间显示
                Label largeTimeLabel = new Label("00:00 / 00:00");
                largeTimeLabel.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; -fx-font-size: 12px;");
                
                // 放大视图的进度条更新
                media.durationProperty().addListener((obs, oldDuration, newDuration) -> {
                    largeProgressSlider.setMax(newDuration.toSeconds());
                    // 内联格式化时间
                    int seconds = (int) Math.floor(newDuration.toSeconds());
                    int minutes = seconds / 60;
                    seconds %= 60;
                    String formattedDuration = String.format("%02d:%02d", minutes, seconds);
                    largeTimeLabel.setText("00:00 / " + formattedDuration);
                });
                
                largeMediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                    if (!largeProgressSlider.isValueChanging()) {
                        largeProgressSlider.setValue(newTime.toSeconds());
                        // 内联格式化时间
                        int currentSeconds = (int) Math.floor(newTime.toSeconds());
                        int currentMinutes = currentSeconds / 60;
                        currentSeconds %= 60;
                        String formattedCurrent = String.format("%02d:%02d", currentMinutes, currentSeconds);
                        
                        int totalSeconds = (int) Math.floor(media.getDuration().toSeconds());
                        int totalMinutes = totalSeconds / 60;
                        totalSeconds %= 60;
                        String formattedTotal = String.format("%02d:%02d", totalMinutes, totalSeconds);
                        
                        largeTimeLabel.setText(formattedCurrent + " / " + formattedTotal);
                    }
                });
                
                largeProgressSlider.setOnMousePressed(ev -> {
                    largeMediaPlayer.pause();
                });
                
                largeProgressSlider.setOnMouseReleased(ev -> {
                    largeMediaPlayer.seek(javafx.util.Duration.seconds(largeProgressSlider.getValue()));
                    largeMediaPlayer.play();
                    largePlayBtn.setText("暂停");
                });
                
                largeProgressSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
                    if (largeProgressSlider.isValueChanging()) {
                        // 内联格式化时间
                        int currentSeconds = (int) Math.floor(newValue.doubleValue());
                        int currentMinutes = currentSeconds / 60;
                        currentSeconds %= 60;
                        String formattedCurrent = String.format("%02d:%02d", currentMinutes, currentSeconds);
                        
                        int totalSeconds = (int) Math.floor(media.getDuration().toSeconds());
                        int totalMinutes = totalSeconds / 60;
                        totalSeconds %= 60;
                        String formattedTotal = String.format("%02d:%02d", totalMinutes, totalSeconds);
                        
                        largeTimeLabel.setText(formattedCurrent + " / " + formattedTotal);
                    }
                });
                
                HBox largeControls = new HBox(10, largePlayBtn, largeStopBtn, largeRotateBtn, largeProgressSlider, largeTimeLabel);
                largeControls.setAlignment(Pos.CENTER);
                largeControls.setStyle("-fx-padding: 10px;");
                HBox.setHgrow(largeProgressSlider, Priority.ALWAYS);
                
                VBox largeVBox = new VBox(10, largeMediaView, largeControls);
                largeVBox.setStyle("-fx-padding: 10px;");
                VBox.setVgrow(largeMediaView, Priority.ALWAYS);
                
                stage.setScene(new javafx.scene.Scene(largeVBox, 900, 600));
                stage.show();
            });
            
            // 工具栏
            HBox controls = new HBox(10, playBtn, stopBtn, rotateBtn, enlargeBtn, progressSlider, timeLabel);
            controls.setAlignment(Pos.CENTER);
            controls.setStyle("-fx-padding: 10px;");
            HBox.setHgrow(progressSlider, Priority.ALWAYS);
            
            ScrollPane scrollPane = new ScrollPane(mediaView);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setStyle("-fx-background-color: #000000;");
            VBox.setVgrow(scrollPane, Priority.ALWAYS);
            
            this.getChildren().addAll(title, scrollPane, controls);
            
            // 清理资源
            mediaPlayer.setOnEndOfMedia(() -> {
                playBtn.setText("播放");
            });
            
        } catch (Exception e) {
            showError("加载视频失败: " + e.getMessage());
        }
    }

    /**
     * 显示错误信息
     * @param message 错误信息
     */
    private void showError(String message) {
        this.getChildren().clear();
        
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        errorLabel.setWrapText(true);
        errorLabel.setTextAlignment(TextAlignment.CENTER);
        
        this.getChildren().add(errorLabel);
    }

    /**
     * 清空预览内容
     */
    public void clear() {
        this.getChildren().clear();
    }
}