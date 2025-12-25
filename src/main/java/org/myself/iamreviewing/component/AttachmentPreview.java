package org.myself.iamreviewing.component;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.myself.iamreviewing.component.previewer.*;

import java.io.File;

/**
 * 附件预览组件
 * 负责协调各种类型文件的预览，具体预览功能由各个独立的预览器实现
 */
public class AttachmentPreview extends VBox {
    
    // 各种类型的预览器
    private PdfPreviewer pdfPreviewer;
    private DocxPreviewer docxPreviewer;
    private TextPreviewer textPreviewer;
    private CodePreviewer codePreviewer;
    private ImagePreviewer imagePreviewer;
    private AudioPreviewer audioPreviewer;
    private VideoPreviewer videoPreviewer;
    
    public AttachmentPreview() {
        this.setAlignment(Pos.CENTER);
        this.setSpacing(10);
        this.setPrefHeight(300);
        this.setStyle("-fx-padding: 10px;");
        
        // 初始化预览器
        initPreviewers();
    }
    
    /**
     * 初始化各种类型的预览器
     */
    private void initPreviewers() {
        pdfPreviewer = new PdfPreviewer();
        docxPreviewer = new DocxPreviewer();
        textPreviewer = new TextPreviewer();
        codePreviewer = new CodePreviewer();
        imagePreviewer = new ImagePreviewer();
        audioPreviewer = new AudioPreviewer();
        videoPreviewer = new VideoPreviewer();
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
                    pdfPreviewer.showPreview(file, this);
                } else if (filePath.toLowerCase().endsWith(".docx")) {
                    docxPreviewer.showPreview(file, this);
                } else {
                    textPreviewer.showPreview(file, this);
                }
                break;
            case "代码":
                codePreviewer.showPreview(file, this);
                break;
            case "图片":
                imagePreviewer.showPreview(file, this);
                break;
            case "音频":
                audioPreviewer.showPreview(file, this);
                break;
            case "视频":
                videoPreviewer.showPreview(file, this);
                break;
            default:
                showError("不支持的文件类型预览");
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