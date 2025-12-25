package org.myself.iamreviewing.component.previewer;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;

/**
 * 图片文件预览器
 */
public class ImagePreviewer implements Previewer {

    @Override
    public void showPreview(File file, VBox parentContainer) {
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
            showError("加载图片失败: " + e.getMessage(), parentContainer);
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

        parentContainer.getChildren().addAll(title, toolbar, scrollPane);
    }

    /**
     * 显示错误信息
     * @param message 错误信息
     * @param container 容器，错误信息将添加到这个容器中
     */
    private void showError(String message, VBox container) {
        container.getChildren().clear();
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        errorLabel.setWrapText(true);
        errorLabel.setAlignment(Pos.CENTER);
        container.getChildren().add(errorLabel);
    }
}