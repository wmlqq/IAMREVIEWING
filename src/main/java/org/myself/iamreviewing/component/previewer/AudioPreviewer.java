package org.myself.iamreviewing.component.previewer;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

/**
 * 音频文件预览器
 */
public class AudioPreviewer implements Previewer {

    @Override
    public void showPreview(File file, VBox parentContainer) {
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
            progressSlider.setOnMousePressed(e -> mediaPlayer.pause());

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
            controls.setAlignment(javafx.geometry.Pos.CENTER);
            controls.setStyle("-fx-padding: 10px;");
            HBox.setHgrow(progressSlider, Priority.ALWAYS);

            parentContainer.getChildren().addAll(title, controls);

            // 清理资源
            mediaPlayer.setOnEndOfMedia(() -> playBtn.setText("播放"));

        } catch (Exception e) {
            showError("加载音频失败: " + e.getMessage(), parentContainer);
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
        errorLabel.setAlignment(javafx.geometry.Pos.CENTER);

        container.getChildren().add(errorLabel);
    }
}