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
        Label title = new Label("PDF预览");
        title.setStyle("-fx-font-weight: bold;");
        
        // 由于PDF预览需要复杂的库支持，这里先显示基本信息和打开提示
        Label infoLabel = new Label("PDF文件预览需要外部支持，点击下方按钮打开文件");
        infoLabel.setWrapText(true);
        
        Button openBtn = new Button("打开PDF文件");
        openBtn.setOnAction(e -> {
            try {
                // 使用系统默认程序打开PDF文件
                java.awt.Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                showError("打开PDF文件失败: " + ex.getMessage());
            }
        });
        
        VBox controls = new VBox(10, infoLabel, openBtn);
        controls.setAlignment(Pos.CENTER);
        
        this.getChildren().addAll(title, controls);
    }
    
    /**
     * 显示DOCX文件预览
     * @param file 文件对象
     */
    private void showDocxPreview(File file) {
        Label title = new Label("DOCX预览");
        title.setStyle("-fx-font-weight: bold;");
        
        // 由于DOCX预览需要复杂的库支持，这里先显示基本信息和打开提示
        Label infoLabel = new Label("DOCX文件预览需要外部支持，点击下方按钮打开文件");
        infoLabel.setWrapText(true);
        
        Button openBtn = new Button("打开DOCX文件");
        openBtn.setOnAction(e -> {
            try {
                // 使用系统默认程序打开DOCX文件
                java.awt.Desktop.getDesktop().open(file);
            } catch (IOException ex) {
                showError("打开DOCX文件失败: " + ex.getMessage());
            }
        });
        
        VBox controls = new VBox(10, infoLabel, openBtn);
        controls.setAlignment(Pos.CENTER);
        
        this.getChildren().addAll(title, controls);
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
        Label title = new Label(previewType + "预览");
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
        
        TextArea textArea = new TextArea();
        textArea.setWrapText(false); // 代码文件不自动换行
        textArea.setEditable(false);
        // 优化代码预览样式
        textArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; -fx-font-size: 12px; -fx-background-color: #f5f5f5; -fx-text-fill: #333;");
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
                textArea.setText(content);
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
            
            TextArea largeTextArea = new TextArea(textArea.getText());
            largeTextArea.setWrapText(false);
            largeTextArea.setEditable(false);
            // 优化放大后的代码样式
            largeTextArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; -fx-font-size: 14px; -fx-background-color: #f5f5f5; -fx-text-fill: #333;");
            
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
            
            VBox largeVBox = new VBox(10, largeToolbar, largeScrollPane);
            largeVBox.setStyle("-fx-padding: 10px;");
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
        Label title = new Label("图片预览");
        title.setStyle("-fx-font-weight: bold;");
        
        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(400);
        imageView.setFitHeight(250);
        
        try {
            Image image = new Image(file.toURI().toString());
            imageView.setImage(image);
        } catch (Exception e) {
            showError("加载图片失败: " + e.getMessage());
            return;
        }
        
        ScrollPane scrollPane = new ScrollPane(imageView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
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
            
            ImageView largeImageView = new ImageView(imageView.getImage());
            largeImageView.setPreserveRatio(true);
            largeImageView.setFitWidth(800);
            largeImageView.setFitHeight(600);
            
            ScrollPane largeScrollPane = new ScrollPane(largeImageView);
            largeScrollPane.setFitToWidth(true);
            largeScrollPane.setFitToHeight(true);
            
            // 放大视图的旋转按钮
            Button largeRotateBtn = new Button("旋转");
            largeRotateBtn.setOnAction(ev -> {
                largeImageView.setRotate(largeImageView.getRotate() + 90);
            });
            
            VBox largeVBox = new VBox(10, largeRotateBtn, largeScrollPane);
            largeVBox.setStyle("-fx-padding: 10px;");
            VBox.setVgrow(largeScrollPane, Priority.ALWAYS);
            
            stage.setScene(new javafx.scene.Scene(largeVBox, 900, 700));
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
        Label title = new Label("音频预览");
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
            progressSlider.setShowTickLabels(true);
            progressSlider.setShowTickMarks(true);
            progressSlider.setMinorTickCount(5);
            
            // 进度条更新
            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (!progressSlider.isValueChanging()) {
                    progressSlider.setValue(newTime.toSeconds() / media.getDuration().toSeconds());
                }
            });
            
            // 进度条拖动
            progressSlider.setOnMouseReleased(e -> {
                mediaPlayer.seek(media.getDuration().multiply(progressSlider.getValue()));
            });
            
            HBox controls = new HBox(10, playBtn, stopBtn, progressSlider);
            controls.setAlignment(Pos.CENTER);
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
        Label title = new Label("视频预览");
        title.setStyle("-fx-font-weight: bold;");
        
        try {
            Media media = new Media(file.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            MediaView mediaView = new MediaView(mediaPlayer);
            
            mediaView.setPreserveRatio(true);
            mediaView.setFitWidth(400);
            mediaView.setFitHeight(250);
            
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
            progressSlider.setShowTickLabels(true);
            progressSlider.setShowTickMarks(true);
            progressSlider.setMinorTickCount(5);
            
            // 进度条更新
            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (!progressSlider.isValueChanging()) {
                    progressSlider.setValue(newTime.toSeconds() / media.getDuration().toSeconds());
                }
            });
            
            // 进度条拖动
            progressSlider.setOnMouseReleased(e -> {
                mediaPlayer.seek(media.getDuration().multiply(progressSlider.getValue()));
            });
            
            // 放大查看
            enlargeBtn.setOnAction(e -> {
                Stage stage = new Stage();
                stage.setTitle("放大查看 - " + file.getName());
                
                MediaPlayer largeMediaPlayer = new MediaPlayer(media);
                MediaView largeMediaView = new MediaView(largeMediaPlayer);
                largeMediaView.setPreserveRatio(true);
                largeMediaView.setFitWidth(800);
                largeMediaView.setFitHeight(600);
                
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
                largeProgressSlider.setShowTickLabels(true);
                largeProgressSlider.setShowTickMarks(true);
                largeProgressSlider.setMinorTickCount(5);
                
                largeMediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                    if (!largeProgressSlider.isValueChanging()) {
                        largeProgressSlider.setValue(newTime.toSeconds() / media.getDuration().toSeconds());
                    }
                });
                
                largeProgressSlider.setOnMouseReleased(ev -> {
                    largeMediaPlayer.seek(media.getDuration().multiply(largeProgressSlider.getValue()));
                });
                
                HBox largeControls = new HBox(10, largePlayBtn, largeStopBtn, largeRotateBtn, largeProgressSlider);
                largeControls.setAlignment(Pos.CENTER);
                HBox.setHgrow(largeProgressSlider, Priority.ALWAYS);
                
                VBox largeVBox = new VBox(10, largeMediaView, largeControls);
                largeVBox.setStyle("-fx-padding: 10px;");
                VBox.setVgrow(largeMediaView, Priority.ALWAYS);
                
                stage.setScene(new javafx.scene.Scene(largeVBox, 900, 700));
                stage.show();
            });
            
            // 工具栏
            HBox controls = new HBox(10, playBtn, stopBtn, rotateBtn, enlargeBtn, progressSlider);
            controls.setAlignment(Pos.CENTER);
            HBox.setHgrow(progressSlider, Priority.ALWAYS);
            
            ScrollPane scrollPane = new ScrollPane(mediaView);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
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