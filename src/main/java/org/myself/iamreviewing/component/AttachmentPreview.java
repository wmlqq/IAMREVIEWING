package org.myself.iamreviewing.component;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.TextAlignment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
     * 显示文本/代码文件预览
     * @param file 文件对象
     */
    private void showTextPreview(File file) {
        Label title = new Label("文本预览");
        title.setStyle("-fx-font-weight: bold;");
        
        TextArea textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.setEditable(false);
        textArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px;");
        VBox.setVgrow(textArea, Priority.ALWAYS);
        
        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        // 异步加载文本内容，避免大文件阻塞UI线程
        executor.submit(() -> {
            try {
                String content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
                textArea.setText(content);
            } catch (IOException e) {
                showError("读取文件内容失败: " + e.getMessage());
            }
        });
        
        this.getChildren().addAll(title, scrollPane);
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
        
        this.getChildren().addAll(title, scrollPane);
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
            javafx.scene.control.Button playBtn = new javafx.scene.control.Button("播放");
            playBtn.setOnAction(e -> {
                if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    mediaPlayer.pause();
                    playBtn.setText("播放");
                } else {
                    mediaPlayer.play();
                    playBtn.setText("暂停");
                }
            });
            
            javafx.scene.control.Button stopBtn = new javafx.scene.control.Button("停止");
            stopBtn.setOnAction(e -> {
                mediaPlayer.stop();
                playBtn.setText("播放");
            });
            
            VBox controls = new VBox(10);
            controls.setAlignment(Pos.CENTER);
            controls.getChildren().addAll(playBtn, stopBtn);
            
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
            javafx.scene.control.Button playBtn = new javafx.scene.control.Button("播放");
            playBtn.setOnAction(e -> {
                if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    mediaPlayer.pause();
                    playBtn.setText("播放");
                } else {
                    mediaPlayer.play();
                    playBtn.setText("暂停");
                }
            });
            
            javafx.scene.control.Button stopBtn = new javafx.scene.control.Button("停止");
            stopBtn.setOnAction(e -> {
                mediaPlayer.stop();
                playBtn.setText("播放");
            });
            
            VBox controls = new VBox(10);
            controls.setAlignment(Pos.CENTER);
            controls.getChildren().addAll(playBtn, stopBtn);
            
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