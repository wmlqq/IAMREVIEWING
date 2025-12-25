package org.myself.iamreviewing.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.myself.iamreviewing.component.AttachmentPreview;
import org.myself.iamreviewing.domain.dto.AttachmentDTO;
import org.myself.iamreviewing.domain.dto.PointDTO;
import org.myself.iamreviewing.domain.enums.DifficultyLevel;
import org.myself.iamreviewing.domain.enums.FileType;
import org.myself.iamreviewing.domain.enums.Memoried;
import org.myself.iamreviewing.domain.vo.AttachmentVO;
import org.myself.iamreviewing.domain.vo.PointVO;
import org.myself.iamreviewing.service.AttachmentService;
import org.myself.iamreviewing.service.PointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.util.List;
import java.util.Optional;

@Controller
public class MainController {

    @Autowired
    private PointService pointService;

    @Autowired
    private AttachmentService attachmentService;

    // 当前选中的知识点
    private PointVO currentPoint;
    // 当前选中的附件
    private AttachmentVO currentAttachment;
    // 附件预览组件
    private AttachmentPreview attachmentPreview;

    // FXML组件
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> categoryFilter;
    @FXML
    private ListView<PointVO> pointListView;
    @FXML
    private Button deletePointBtn;
    @FXML
    private Button refreshBtn;
    @FXML
    private TextField pointNameField;
    @FXML
    private TextField pointCategoryField;
    @FXML
    private ComboBox<DifficultyLevel> difficultyCombo;
    @FXML
    private ComboBox<Memoried> memoriedCombo;
    @FXML
    private Label createDateLabel;
    @FXML
    private TextArea pointDescArea;
    @FXML
    private Button savePointBtn;
    @FXML
    private Button clearFormBtn;
    @FXML
    private ListView<AttachmentVO> attachmentListView;
    @FXML
    private Button uploadAttachBtn;
    @FXML
    private Button deleteAttachBtn;
    @FXML
    private BorderPane previewContainer;
    @FXML
    private VBox previewContent;
    @FXML
    private Label attachFilename;
    @FXML
    private Label attachType;
    @FXML
    private Label attachSize;
    @FXML
    private Label attachUploadTime;
    @FXML
    private Label attachPath;

    // 初始化方法
    @FXML
    public void initialize() {
        initializeControls();
        initializeAttachmentPreview();
        initializeData();
    }

    // 初始化控件
    private void initializeControls() {
        // 设置难度和掌握程度下拉框
        difficultyCombo.getItems().addAll(DifficultyLevel.values());
        memoriedCombo.getItems().addAll(Memoried.values());

        // 设置知识点列表的单元格工厂
        pointListView.setCellFactory(param -> new ListCell<PointVO>() {
            @Override
            protected void updateItem(PointVO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " - " + item.getCategory() + " (" + item.getDifficultyLevel().getDesc() + ")");
                }
            }
        });

        // 设置附件列表的单元格工厂
        attachmentListView.setCellFactory(param -> new ListCell<AttachmentVO>() {
            @Override
            protected void updateItem(AttachmentVO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getFilename() + " (" + item.getFileType().getDesc() + ")");
                }
            }
        });
    }

    // 初始化附件预览组件
    private void initializeAttachmentPreview() {
        attachmentPreview = new AttachmentPreview();
        previewContent.getChildren().clear();
        previewContent.getChildren().add(attachmentPreview);
    }

    // 初始化数据
    private void initializeData() {
        loadAllPoints();
        loadCategories();
    }

    // 加载所有知识点
    @FXML
    private void loadAllPoints() {
        // 调用service层方法加载所有知识点，返回PointVO列表
        List<PointVO> pointVOs = pointService.getAllPoints();
        pointListView.getItems().setAll(pointVOs);
    }

    // 加载所有分类
    private void loadCategories() {
        // 调用service层方法加载所有分类
        List<String> categories = pointService.getAllCategories();
        categoryFilter.getItems().clear();
        categoryFilter.getItems().add("全部");
        categoryFilter.getItems().addAll(categories);
        categoryFilter.getSelectionModel().selectFirst();
    }

    // 搜索知识点
    @FXML
    private void searchPoints() {
        String keyword = searchField.getText();
        List<PointVO> pointVOS = pointService.searchByKeyword(keyword);
        pointListView.getItems().setAll(pointVOS);
    }

    // 按分类筛选
    @FXML
    private void filterByCategory() {
        String category = categoryFilter.getValue();
        if ("全部".equals(category)) {
            loadAllPoints();
        } else {
            List<PointVO> points = pointService.getByCategory(category);
            pointListView.getItems().setAll(points);
        }
    }

    // 添加知识点
    @FXML
    private void addPoint() {
        clearForm();
        currentPoint = null;
        pointNameField.requestFocus();
    }

    // 保存知识点
    @FXML
    private void savePoint() {
        // 验证表单数据
        if (pointNameField.getText().isEmpty()) {
            showError("知识点名称不能为空");
            return;
        }

        // 创建或更新知识点对象
        PointDTO pointDTO = new PointDTO();

        pointDTO.setName(pointNameField.getText());
        pointDTO.setDescription(pointDescArea.getText());
        pointDTO.setCategory(pointCategoryField.getText());
        pointDTO.setDifficultyLevel(difficultyCombo.getValue()== null ? DifficultyLevel.THREE : difficultyCombo.getValue());
        pointDTO.setMemoried(memoriedCombo.getValue()== null ? Memoried.NO : memoriedCombo.getValue());

        PointVO savedPoint;
        // 调用service层方法保存或更新知识点
        if (currentPoint != null) {
            // 更新知识点
            savedPoint = pointService.updatePoint(currentPoint.getId(), pointDTO);
        } else {
            // 创建新知识点
            savedPoint = pointService.createPoint(pointDTO);
        }

        if (savedPoint != null) {
            showSuccess("知识点保存成功");
            loadAllPoints();
            // 更新当前选中的知识点
            currentPoint = savedPoint;
        } else {
            showError("知识点保存失败");
        }
    }

    // 删除知识点
    @FXML
    private void deletePoint() {
        if (currentPoint != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("确认删除");
            alert.setHeaderText("删除知识点");
            alert.setContentText("确定要删除这个知识点吗？删除后无法恢复。");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // 调用service层方法删除知识点
                boolean deleted = pointService.removeById(currentPoint.getId());
                if (deleted) {
                    showSuccess("知识点删除成功");
                    clearForm();
                    loadAllPoints();
                    currentPoint = null;
                } else {
                    showError("知识点删除失败");
                }
            }
        }
    }

    // 知识点列表选择变化
    @FXML
    private void onPointSelectionChanged() {
        currentPoint = pointListView.getSelectionModel().getSelectedItem();
        if (currentPoint != null) {
            // 填充表单数据
            pointNameField.setText(currentPoint.getName());
            pointDescArea.setText(currentPoint.getDescription());
            pointCategoryField.setText(currentPoint.getCategory());
            difficultyCombo.setValue(currentPoint.getDifficultyLevel());
            memoriedCombo.setValue(currentPoint.getMemoried());
            createDateLabel.setText(currentPoint.getCreateDate().toString());

            // 加载附件列表
            loadAttachments();

            // 启用删除按钮
            deletePointBtn.setDisable(false);
        } else {
            deletePointBtn.setDisable(true);
        }
    }

    // 上传附件
    @FXML
    private void uploadAttachment() {
        if (currentPoint == null) {
            showError("请先选择或创建一个知识点");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择附件文件");
        
        // 设置文件过滤器
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("文本文件", "*.txt", "*.md", "*.doc", "*.docx", "*.pdf"),
                new FileChooser.ExtensionFilter("图片文件", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("音频文件", "*.mp3", "*.wav", "*.ogg"),
                new FileChooser.ExtensionFilter("视频文件", "*.mp4", "*.avi", "*.mov", "*.wmv"),
                new FileChooser.ExtensionFilter("代码文件", "*.java", "*.c", "*.cpp", "*.py", "*.js", "*.html", "*.css"),
                new FileChooser.ExtensionFilter("所有支持的文件", "*.txt", "*.md", "*.doc", "*.docx", "*.pdf",
                        "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp",
                        "*.mp3", "*.wav", "*.ogg",
                        "*.mp4", "*.avi", "*.mov", "*.wmv",
                        "*.java", "*.c", "*.cpp", "*.py", "*.js", "*.html", "*.css")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            // 创建附件DTO对象
            AttachmentDTO attachmentDTO = new AttachmentDTO();
            attachmentDTO.setFilename(file.getName());
            attachmentDTO.setFilepath(file.getAbsolutePath());
            attachmentDTO.setFileSize(file.length());
            
            // 根据文件扩展名设置文件类型
            String extension = getFileExtension(file);
            FileType fileType = getFileTypeFromExtension(extension);
            attachmentDTO.setFileType(fileType);

            // 调用service层方法保存附件
            boolean saved = attachmentService.addAttachment(currentPoint.getId(), attachmentDTO);
            if (saved) {
                showSuccess("附件上传成功");
                loadAttachments();
            } else {
                showError("附件上传失败");
            }
        }
    }

    // 删除附件
    @FXML
    private void deleteAttachment() {
        if (currentAttachment != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("确认删除");
            alert.setHeaderText("删除附件");
            alert.setContentText("确定要删除这个附件吗？删除后无法恢复。");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // 调用service层方法删除附件
                boolean deleted = attachmentService.removeById(currentAttachment.getId());
                if (deleted) {
                    showSuccess("附件删除成功");
                    loadAttachments();
                    currentAttachment = null;
                } else {
                    showError("附件删除失败");
                }
            }
        }
    }

    // 附件列表选择变化
    @FXML
    private void onAttachmentSelectionChanged() {
        currentAttachment = attachmentListView.getSelectionModel().getSelectedItem();
        if (currentAttachment != null) {
            // 显示附件信息
            attachFilename.setText(currentAttachment.getFilename());
            attachType.setText(currentAttachment.getFileType().getDesc());
            attachSize.setText(formatFileSize(currentAttachment.getFileSize()));
            attachUploadTime.setText(currentAttachment.getUploadTime().toString());
            attachPath.setText(currentAttachment.getFilepath());

            // 使用AttachmentPreview组件预览附件
            attachmentPreview.showPreview(currentAttachment.getFilepath(), currentAttachment.getFileType().getDesc());

            // 启用删除按钮
            deleteAttachBtn.setDisable(false);
        } else {
            deleteAttachBtn.setDisable(true);
            // 清空预览区
            attachmentPreview.clear();
        }
    }

    // 预览附件
    @FXML
    private void previewAttachment() {
        if (currentAttachment != null) {
            // 使用AttachmentPreview组件预览附件
            attachmentPreview.showPreview(currentAttachment.getFilepath(), currentAttachment.getFileType().getDesc());
        }
    }

    // 加载附件列表
    private void loadAttachments() {
        if (currentPoint != null) {
            // 调用service层方法加载附件列表，返回AttachmentVO列表
            List<AttachmentVO> attachmentVOs = attachmentService.getAttachmentsByPointId(currentPoint.getId());
            attachmentListView.getItems().setAll(attachmentVOs);
        } else {
            attachmentListView.getItems().clear();
        }
    }

    // 清除表单数据
    @FXML
    private void clearForm() {
        pointNameField.clear();
        pointDescArea.clear();
        pointCategoryField.setText("未分类");
        difficultyCombo.setValue(DifficultyLevel.THREE);
        memoriedCombo.setValue(Memoried.NO);
        createDateLabel.setText("");

        // 清空附件相关内容
        attachmentListView.getItems().clear();
        currentAttachment = null;
        deleteAttachBtn.setDisable(true);
        attachmentPreview.clear();
        attachFilename.setText("");
        attachType.setText("");
        attachSize.setText("");
        attachUploadTime.setText("");
        attachPath.setText("");
    }

    // 辅助方法：获取文件扩展名
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf('.');
        if (lastIndexOf == -1) {
            return "";
        }
        return name.substring(lastIndexOf + 1).toLowerCase();
    }

    // 辅助方法：根据扩展名获取文件类型
    private FileType getFileTypeFromExtension(String extension) {
        // 文本文件
        if (extension.matches("txt|md|doc|docx|pdf")) {
            return FileType.TEXT;
        }
        // 图片文件
        if (extension.matches("jpg|jpeg|png|gif|bmp")) {
            return FileType.IMAGE;
        }
        // 视频文件
        if (extension.matches("mp4|avi|mov|wmv")) {
            return FileType.VIDEO;
        }
        // 音频文件
        if (extension.matches("mp3|wav|ogg")) {
            return FileType.AUDIO;
        }
        // 代码文件
        if (extension.matches("java|c|cpp|py|js|html|css")) {
            return FileType.CODE;
        }
        // 默认返回文本类型
        return FileType.TEXT;
    }

    // 辅助方法：格式化文件大小
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }

    // 辅助方法：显示错误信息
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // 辅助方法：显示成功信息
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("成功");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
