package org.myself.iamreviewing.component.previewer;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

import java.io.File;

/**
 * 视频文件预览器
 */
public class VideoPreviewer implements Previewer {

    @Override
    public void showPreview(File file, VBox parentContainer) {
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

            parentContainer.getChildren().addAll(title, scrollPane, controls);

            // 清理资源
            mediaPlayer.setOnEndOfMedia(() -> {
                playBtn.setText("播放");
            });

        } catch (Exception e) {
            showError("加载视频失败: " + e.getMessage(), parentContainer);
        }
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