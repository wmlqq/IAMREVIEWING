package org.myself.iamreviewing.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.layout.Priority;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import lombok.Getter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.myself.iamreviewing.domain.enums.FileType;
import org.myself.iamreviewing.domain.vo.AttachmentVO;
import org.myself.iamreviewing.domain.vo.PointVO;
import org.myself.iamreviewing.service.AttachmentService;
import org.myself.iamreviewing.service.PointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.File;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
public class PointDetailController {

    @Autowired
    private PointService pointService;

    @Autowired
    private AttachmentService attachmentService;

    // 当前知识点ID
    private Long pointId;
    // 当前窗口
    private Stage currentStage;
    // 所有知识点列表，用于切换
    private List<PointVO> allPoints;
    // 当前知识点在列表中的索引
    private int currentPointIndex;
    
    // 语法高亮规则
    private static final Map<String, LanguageSyntax> LANGUAGE_SYNTAX_MAP = new HashMap<>();
    
    static {
        // 初始化语言语法规则
        initLanguageSyntax();
    }
    
    /**
     * 初始化语言语法规则
     */
    private static void initLanguageSyntax() {
        // C语言关键字
        Set<String> cKeywords = new HashSet<>(Arrays.asList(
                "auto", "break", "case", "char", "const", "continue", "default", "do",
                "double", "else", "enum", "extern", "float", "for", "goto", "if",
                "int", "long", "register", "return", "short", "signed", "sizeof", "static",
                "struct", "switch", "typedef", "union", "unsigned", "void", "volatile", "while"
        ));
        
        // C++语言关键字（包含C关键字）
        Set<String> cppKeywords = new HashSet<>(cKeywords);
        cppKeywords.addAll(Arrays.asList(
                "alignas", "alignof", "and", "and_eq", "asm", "atomic_cancel", "atomic_commit",
                "atomic_noexcept", "auto", "bitand", "bitor", "bool", "break", "case", "catch",
                "char", "char8_t", "char16_t", "char32_t", "class", "compl", "concept", "const",
                "consteval", "constexpr", "constinit", "const_cast", "continue", "co_await", "co_return",
                "co_yield", "decltype", "default", "delete", "do", "double", "dynamic_cast", "else",
                "enum", "explicit", "export", "extern", "false", "float", "for", "friend",
                "goto", "if", "inline", "int", "long", "mutable", "namespace", "new",
                "noexcept", "not", "not_eq", "nullptr", "operator", "or", "or_eq", "private",
                "protected", "public", "reflexpr", "register", "reinterpret_cast", "requires", "return",
                "short", "signed", "sizeof", "static", "static_assert", "static_cast", "struct", "switch",
                "synchronized", "template", "this", "thread_local", "throw", "true", "try", "typedef",
                "typeid", "typename", "union", "unsigned", "using", "virtual", "void", "volatile",
                "wchar_t", "while", "xor", "xor_eq"
        ));
        
        // Java语言关键字
        Set<String> javaKeywords = new HashSet<>(Arrays.asList(
                "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
                "class", "const", "continue", "default", "do", "double", "else", "enum",
                "extends", "final", "finally", "float", "for", "goto", "if", "implements",
                "import", "instanceof", "int", "interface", "long", "native", "new", "package",
                "private", "protected", "public", "return", "short", "static", "strictfp", "super",
                "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void",
                "volatile", "while", "true", "false", "null"
        ));
        
        // Python语言关键字
        Set<String> pythonKeywords = new HashSet<>(Arrays.asList(
                "False", "None", "True", "and", "as", "assert", "async", "await", "break",
                "class", "continue", "def", "del", "elif", "else", "except", "finally", "for",
                "from", "global", "if", "import", "in", "is", "lambda", "nonlocal", "not",
                "or", "pass", "raise", "return", "try", "while", "with", "yield"
        ));
        
        // 初始化语言语法映射
        LANGUAGE_SYNTAX_MAP.put(".c", new LanguageSyntax(cKeywords, "//", "/*", "*/"));
        LANGUAGE_SYNTAX_MAP.put(".h", new LanguageSyntax(cKeywords, "//", "/*", "*/"));
        LANGUAGE_SYNTAX_MAP.put(".cpp", new LanguageSyntax(cppKeywords, "//", "/*", "*/"));
        LANGUAGE_SYNTAX_MAP.put(".cxx", new LanguageSyntax(cppKeywords, "//", "/*", "*/"));
        LANGUAGE_SYNTAX_MAP.put(".cc", new LanguageSyntax(cppKeywords, "//", "/*", "*/"));
        LANGUAGE_SYNTAX_MAP.put(".java", new LanguageSyntax(javaKeywords, "//", "/*", "*/"));
        LANGUAGE_SYNTAX_MAP.put(".py", new LanguageSyntax(pythonKeywords, "#", null, null));
        LANGUAGE_SYNTAX_MAP.put(".pyw", new LanguageSyntax(pythonKeywords, "#", null, null));
    }
    
    /**
     * 语言语法规则类
     */
    @Getter
    private static class LanguageSyntax {
        private final Set<String> keywords;
        private final String singleLineComment;
        private final String multiLineCommentStart;
        private final String multiLineCommentEnd;
        
        public LanguageSyntax(Set<String> keywords, String singleLineComment, 
                           String multiLineCommentStart, String multiLineCommentEnd) {
            this.keywords = keywords;
            this.singleLineComment = singleLineComment;
            this.multiLineCommentStart = multiLineCommentStart;
            this.multiLineCommentEnd = multiLineCommentEnd;
        }

    }
    
    /**
     * 文本片段类
     */
    @Getter
    private static class TextSegment {
        private final String text;
        private final Color color;
        
        public TextSegment(String text, Color color) {
            this.text = text;
            this.color = color;
        }

    }

    // FXML组件
    @FXML
    private Button backBtn;
    @FXML
    private Button prevBtn;
    @FXML
    private Button nextBtn;
    @FXML
    private Label pointTitle;
    @FXML
    private Label detailName;
    @FXML
    private Label detailCategory;
    @FXML
    private Label detailDifficulty;
    @FXML
    private Label detailMemoried;
    @FXML
    private Label detailCreateDate;
    @FXML
    private TextArea detailDesc;
    @FXML
    private VBox allAttachments;

    /**
     * 初始化方法
     */
    @FXML
    public void initialize() {
        // 设置返回按钮事件
        backBtn.setOnAction(event -> closeWindow());
        // 设置切换按钮事件
        prevBtn.setOnAction(event -> showPrevPoint());
        nextBtn.setOnAction(event -> showNextPoint());
    }

    /**
     * 设置知识点ID并加载数据
     */
    public void setPointId(Long pointId) {
        this.pointId = pointId;
        // 加载所有知识点，用于切换
        allPoints = pointService.getAllPoints();
        // 找到当前知识点在列表中的索引
        for (int i = 0; i < allPoints.size(); i++) {
            if (allPoints.get(i).getId().equals(pointId)) {
                currentPointIndex = i;
                break;
            }
        }
        // 加载数据
        loadPointData();
        loadAllAttachments();
        // 更新切换按钮状态
        updateNavigationButtons();
    }

    /**
     * 设置当前窗口
     */
    public void setStage(Stage stage) {
        this.currentStage = stage;
    }

    /**
     * 加载知识点数据
     */
    private void loadPointData() {
        PointVO point = pointService.getPointById(pointId);
        if (point != null) {
            pointTitle.setText(point.getName());
            detailName.setText(point.getName());
            detailCategory.setText(point.getCategory());
            detailDifficulty.setText(point.getDifficultyLevel().getDesc());
            detailMemoried.setText(point.getMemoried().getDesc());
            detailCreateDate.setText(point.getCreateDate().toString());
            detailDesc.setText(point.getDescription());
        }
    }

    /**
     * 加载所有附件并直接展示内容
     */
    private void loadAllAttachments() {
        allAttachments.getChildren().clear();
        
        List<AttachmentVO> attachments = attachmentService.getAttachmentsByPointId(pointId);
        
        if (attachments.isEmpty()) {
            Label noAttachLabel = new Label("暂无附件");
            noAttachLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #718096; -fx-padding: 20px 0;");
            allAttachments.getChildren().add(noAttachLabel);
            return;
        }
        
        // 按类型排序：文本、代码、图片、音频、视频
        List<AttachmentVO> sortedAttachments = new ArrayList<>();
        
        // 添加文本附件
        sortedAttachments.addAll(attachments.stream()
                .filter(attach -> attach.getFileType() == FileType.TEXT)
                .collect(Collectors.toList()));
        
        // 添加代码附件
        sortedAttachments.addAll(attachments.stream()
                .filter(attach -> attach.getFileType() == FileType.CODE)
                .collect(Collectors.toList()));
        
        // 添加图片附件
        sortedAttachments.addAll(attachments.stream()
                .filter(attach -> attach.getFileType() == FileType.IMAGE)
                .collect(Collectors.toList()));
        
        // 添加音频附件
        sortedAttachments.addAll(attachments.stream()
                .filter(attach -> attach.getFileType() == FileType.AUDIO)
                .collect(Collectors.toList()));
        
        // 添加视频附件
        sortedAttachments.addAll(attachments.stream()
                .filter(attach -> attach.getFileType() == FileType.VIDEO)
                .collect(Collectors.toList()));
        
        // 添加附件类型标题
        addSectionTitle("附件内容");
        
        // 展示所有附件
        for (int i = 0; i < sortedAttachments.size(); i++) {
            AttachmentVO attachment = sortedAttachments.get(i);
            
            // 添加文件名
            Label filenameLabel = new Label(String.format("%s %s", 
                    getAttachmentIcon(attachment.getFileType()), 
                    attachment.getFilename()));
            filenameLabel.setStyle(
                    "-fx-font-size: 16px; " +
                    "-fx-font-weight: 600; " +
                    "-fx-text-fill: #2d3748; " +
                    "-fx-margin-top: 24px; " +
                    "-fx-margin-bottom: 8px;"
            );
            allAttachments.getChildren().add(filenameLabel);
            
            // 附件内容
            try {
                String filename = attachment.getFilename();
                String filepath = attachment.getFilepath();
                File file = new File(filepath);
                
                // 完全根据文件扩展名确定处理方法
            String fileExtension = getFileExtension(filename).toLowerCase();
            
            // 根据扩展名处理文件
            if (fileExtension.equals(".pdf")) {
                // PDF文件
                displayPdfFile(file, allAttachments);
            } else if (fileExtension.equals(".docx")) {
                // DOCX文件
                displayDocxFile(file, allAttachments);
            } else if (fileExtension.equals(".md") || fileExtension.equals(".txt") || fileExtension.equals(".text")) {
                // 文本文件
                displayTextFile(file, allAttachments);
            } else if (fileExtension.equals(".java") || fileExtension.equals(".py") || fileExtension.equals(".c") || 
                      fileExtension.equals(".cpp") || fileExtension.equals(".h") || fileExtension.equals(".hpp")) {
                // 代码文件
                displayCodeFile(file, allAttachments);
            } else if (fileExtension.equals(".jpg") || fileExtension.equals(".jpeg") || fileExtension.equals(".png") || 
                      fileExtension.equals(".gif") || fileExtension.equals(".bmp")) {
                // 图片文件
                displayImageFile(file, allAttachments);
            } else if (fileExtension.equals(".mp3") || fileExtension.equals(".wav") || fileExtension.equals(".ogg")) {
                // 音频文件
                createAudioPlayer(file, allAttachments);
            } else if (fileExtension.equals(".mp4") || fileExtension.equals(".avi") || fileExtension.equals(".mov") || 
                      fileExtension.equals(".wmv")) {
                // 视频文件
                createVideoPlayer(file, allAttachments);
            } else {
                // 其他文件类型，显示基本信息
                Label infoLabel = new Label("文件类型：" + fileExtension + "，大小：" + formatFileSize(file.length()));
                infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #718096; -fx-padding: 10px 0;");
                allAttachments.getChildren().add(infoLabel);
            }
                
                // 最后一个附件不需要添加空行
                if (i < sortedAttachments.size() - 1) {
                    // 添加空行分隔不同附件
                    Region spacer = new Region();
                    spacer.setPrefHeight(20);
                    allAttachments.getChildren().add(spacer);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                Label errorLabel = new Label("加载文件失败: " + e.getMessage());
                errorLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-size: 14px; -fx-padding: 10px 0;");
                allAttachments.getChildren().add(errorLabel);
            }
        }
    }
    
    /**
     * 添加章节标题
     */
    private void addSectionTitle(String title) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-font-size: 20px; " +
                "-fx-font-weight: 600; " +
                "-fx-text-fill: #2d3748; " +
                "-fx-margin-top: 30px; " +
                "-fx-margin-bottom: 16px;"
        );
        allAttachments.getChildren().add(titleLabel);
    }
    
    /**
     * 样式化文本内容
     */
    private void styleTextContent(ObservableList<Node> children) {
        for (Node node : children) {
            if (node instanceof TextArea) {
                TextArea textArea = (TextArea) node;
                textArea.setStyle(
                        "-fx-background-color: transparent; " +
                        "-fx-border: none; " +
                        "-fx-font-size: 15px; " +
                        "-fx-line-spacing: 1.6; " +
                        "-fx-text-fill: #4a5568; " +
                        "-fx-padding: 0; " +
                        "-fx-wrap-text: true;"
                );
            }
        }
    }
    
    /**
     * 样式化代码内容
     */
    private void styleCodeContent(ObservableList<Node> children) {
        for (Node node : children) {
            if (node instanceof TextArea) {
                TextArea textArea = (TextArea) node;
                textArea.setStyle(
                        "-fx-background-color: #2d3748; " +
                        "-fx-border-radius: 6px; " +
                        "-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                        "-fx-font-size: 14px; " +
                        "-fx-text-fill: #e2e8f0; " +
                        "-fx-padding: 16px; " +
                        "-fx-wrap-text: true; " +
                        "-fx-control-inner-background: #2d3748;"
                );
            }
        }
    }
    
    /**
     * 样式化图片内容
     */
    private void styleImageContent(ObservableList<Node> children) {
        for (Node node : children) {
            if (node instanceof ImageView) {
                ImageView imageView = (ImageView) node;
                imageView.setStyle(
                        "-fx-border-radius: 6px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);"
                );
            }
        }
    }
    
    /**
     * 创建音频播放器
     */
    private void createAudioPlayer(File file, VBox parentContainer) {
        try {
            Media media = new Media(file.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);

            // 音频控制按钮
            Button playBtn = new Button("播放");
            playBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #2d3748; -fx-padding: 6px 12px; -fx-border-radius: 4px;");
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
            stopBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #2d3748; -fx-padding: 6px 12px; -fx-border-radius: 4px;");
            stopBtn.setOnAction(e -> {
                mediaPlayer.stop();
                playBtn.setText("播放");
            });

            // 进度条
            Slider progressSlider = new Slider(0, 1, 0);
            progressSlider.setShowTickLabels(false);
            progressSlider.setShowTickMarks(false);
            progressSlider.setStyle("-fx-pref-height: 8px; -fx-background-color: #e2e8f0;");

            // 时间显示标签
            Label timeLabel = new Label("00:00 / 00:00");
            timeLabel.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; -fx-font-size: 12px; -fx-text-fill: #718096;");

            // 当媒体时长可用时更新进度条最大值和时间标签
            media.durationProperty().addListener((obs, oldDuration, newDuration) -> {
                progressSlider.setMax(newDuration.toSeconds());
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
            controls.setStyle("-fx-padding: 10px; -fx-background-color: #f7fafc; -fx-border-radius: 6px;");
            HBox.setHgrow(progressSlider, Priority.ALWAYS);

            parentContainer.getChildren().add(controls);

            // 清理资源
            mediaPlayer.setOnEndOfMedia(() -> {
                playBtn.setText("播放");
            });

        } catch (Exception e) {
            Label errorLabel = new Label("加载音频失败: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-size: 14px; -fx-padding: 10px 0;");
            parentContainer.getChildren().add(errorLabel);
        }
    }
    
    /**
     * 创建视频播放器
     */
    private void createVideoPlayer(File file, VBox parentContainer) {
        try {
            Media media = new Media(file.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            MediaView mediaView = new MediaView(mediaPlayer);

            mediaView.setPreserveRatio(true);
            mediaView.setFitWidth(800);
            mediaView.setFitHeight(450);
            mediaView.setStyle("-fx-border-radius: 6px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");

            // 视频控制按钮
            Button playBtn = new Button("播放");
            playBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #2d3748; -fx-padding: 6px 12px; -fx-border-radius: 4px;");
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
            stopBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #2d3748; -fx-padding: 6px 12px; -fx-border-radius: 4px;");
            stopBtn.setOnAction(e -> {
                mediaPlayer.stop();
                playBtn.setText("播放");
            });

            // 进度条
            Slider progressSlider = new Slider(0, 1, 0);
            progressSlider.setShowTickLabels(false);
            progressSlider.setShowTickMarks(false);
            progressSlider.setStyle("-fx-pref-height: 8px; -fx-background-color: #e2e8f0;");

            // 时间显示标签
            Label timeLabel = new Label("00:00 / 00:00");
            timeLabel.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; -fx-font-size: 12px; -fx-text-fill: #718096;");

            // 当媒体时长可用时更新进度条最大值和时间标签
            media.durationProperty().addListener((obs, oldDuration, newDuration) -> {
                progressSlider.setMax(newDuration.toSeconds());
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

            // 工具栏
            HBox controls = new HBox(10, playBtn, stopBtn, progressSlider, timeLabel);
            controls.setAlignment(javafx.geometry.Pos.CENTER);
            controls.setStyle("-fx-padding: 15px; -fx-background-color: #f7fafc; -fx-border-radius: 6px;");
            HBox.setHgrow(progressSlider, Priority.ALWAYS);

            // 视频容器
            VBox videoContainer = new VBox(10, mediaView, controls);
            videoContainer.setAlignment(javafx.geometry.Pos.CENTER);
            videoContainer.setStyle("-fx-background-color: #f7fafc; -fx-padding: 20px; -fx-border-radius: 6px;");

            parentContainer.getChildren().add(videoContainer);

            // 清理资源
            mediaPlayer.setOnEndOfMedia(() -> {
                playBtn.setText("播放");
            });

        } catch (Exception e) {
            Label errorLabel = new Label("加载视频失败: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-size: 14px; -fx-padding: 10px 0;");
            parentContainer.getChildren().add(errorLabel);
        }
    }
    
    /**
     * 显示文本文件
     */
    private void displayTextFile(File file, VBox parentContainer) {
        try {
            // 读取文件内容
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            
            // 使用带放大功能的文本显示组件
            createTextWithEnlargeButton(content, "TEXT", parentContainer, file.getName());
        } catch (IOException e) {
            Label errorLabel = new Label("读取文件失败: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-size: 14px; -fx-padding: 10px 0;");
            parentContainer.getChildren().add(errorLabel);
        }
    }
    
    /**
     * 显示代码文件
     */
    private void displayCodeFile(File file, VBox parentContainer) {
        try {
            // 读取文件内容
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            
            // 使用带放大功能的代码显示组件，支持语法高亮
            createTextWithEnlargeButton(content, "CODE", parentContainer, file.getName());
        } catch (IOException e) {
            Label errorLabel = new Label("读取代码文件失败: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-size: 14px; -fx-padding: 10px 0;");
            parentContainer.getChildren().add(errorLabel);
        }
    }
    
    /**
     * 显示图片文件
     */
    private void displayImageFile(File file, VBox parentContainer) {
        try {
            Image image = new Image(file.toURI().toString());
            ImageView imageView = new ImageView(image);
            
            imageView.setPreserveRatio(true);
            
            // 计算合适的显示尺寸
            double imageWidth = image.getWidth();
            double imageHeight = image.getHeight();
            double maxWidth = 800;
            double maxHeight = 600;
            
            if (imageWidth > maxWidth || imageHeight > maxHeight) {
                double widthRatio = maxWidth / imageWidth;
                double heightRatio = maxHeight / imageHeight;
                double scaleRatio = Math.min(widthRatio, heightRatio);
                
                imageView.setFitWidth(imageWidth * scaleRatio);
                imageView.setFitHeight(imageHeight * scaleRatio);
            } else {
                imageView.setFitWidth(imageWidth);
                imageView.setFitHeight(imageHeight);
            }
            
            imageView.setStyle(
                    "-fx-border-radius: 6px; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);"
            );
            
            // 添加图片到容器
            parentContainer.getChildren().add(imageView);
        } catch (Exception e) {
            Label errorLabel = new Label("加载图片失败: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-size: 14px; -fx-padding: 10px 0;");
            parentContainer.getChildren().add(errorLabel);
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex).toLowerCase();
    }
    
    /**
     * 语法高亮渲染
     */
    private void syntaxHighlight(TextFlow textFlow, String content, String fileExtension) {
        LanguageSyntax syntax = LANGUAGE_SYNTAX_MAP.getOrDefault(fileExtension, 
                new LanguageSyntax(Collections.emptySet(), "//", "/*", "*/"));
        
        // 简单的语法高亮实现
        String[] lines = content.split("\\n");
        
        for (String line : lines) {
            List<TextSegment> segments = new ArrayList<>();
            
            // 处理多行注释（这里简化处理，只处理单行内的注释）
            if (syntax.getMultiLineCommentStart() != null && syntax.getMultiLineCommentEnd() != null) {
                int commentStart = line.indexOf(syntax.getMultiLineCommentStart());
                if (commentStart != -1) {
                    int commentEnd = line.indexOf(syntax.getMultiLineCommentEnd(), commentStart + syntax.getMultiLineCommentStart().length());
                    if (commentEnd != -1) {
                        // 处理注释前的内容
                        processLineSegments(line.substring(0, commentStart), syntax, segments);
                        // 添加注释
                        segments.add(new TextSegment(
                                line.substring(commentStart, commentEnd + syntax.getMultiLineCommentEnd().length()),
                                Color.GREEN
                        ));
                        // 处理注释后的内容
                        processLineSegments(line.substring(commentEnd + syntax.getMultiLineCommentEnd().length()), syntax, segments);
                    } else {
                        // 多行注释开始，没有结束
                        processLineSegments(line.substring(0, commentStart), syntax, segments);
                        segments.add(new TextSegment(
                                line.substring(commentStart),
                                Color.GREEN
                        ));
                    }
                } else {
                    // 处理单行注释
                    int singleCommentStart = line.indexOf(syntax.getSingleLineComment());
                    if (singleCommentStart != -1) {
                        // 处理注释前的内容
                        processLineSegments(line.substring(0, singleCommentStart), syntax, segments);
                        // 添加单行注释
                        segments.add(new TextSegment(
                                line.substring(singleCommentStart),
                                Color.GREEN
                        ));
                    } else {
                        // 没有注释，处理整行
                        processLineSegments(line, syntax, segments);
                    }
                }
            } else {
                // 只有单行注释的语言（如Python）
                int singleCommentStart = line.indexOf(syntax.getSingleLineComment());
                if (singleCommentStart != -1) {
                    // 处理注释前的内容
                    processLineSegments(line.substring(0, singleCommentStart), syntax, segments);
                    // 添加单行注释
                    segments.add(new TextSegment(
                            line.substring(singleCommentStart),
                            Color.GREEN
                    ));
                } else {
                    // 没有注释，处理整行
                    processLineSegments(line, syntax, segments);
                }
            }
            
            // 将分段添加到TextFlow
            for (TextSegment segment : segments) {
                Text text = new Text(segment.getText());
                text.setFill(segment.getColor());
                textFlow.getChildren().add(text);
            }
            
            // 添加换行符
            textFlow.getChildren().add(new Text("\n"));
        }
    }
    
    /**
     * 处理行内语法元素
     */
    private void processLineSegments(String line, LanguageSyntax syntax, List<TextSegment> segments) {
        // 正则表达式匹配规则
        String pattern = "\\s+|\\b(\\w+)\\b|([\"'])(.*?)\\2|([0-9]+\\.?[0-9]*)|([+\\-*/%=<>!&|^~\\[\\]{}().,;:])";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(line);
        
        while (matcher.find()) {
            String match = matcher.group();
            
            // 空格
            if (match.matches("\\s+")) {
                segments.add(new TextSegment(match, Color.WHITE));
            }
            // 关键字
            else if (matcher.group(1) != null && syntax.getKeywords().contains(matcher.group(1))) {
                segments.add(new TextSegment(match, Color.BLUE));
            }
            // 字符串
            else if (matcher.group(2) != null) {
                segments.add(new TextSegment(match, Color.ORANGE));
            }
            // 数字
            else if (matcher.group(4) != null) {
                segments.add(new TextSegment(match, Color.PURPLE));
            }
            // 运算符和标点符号
            else if (matcher.group(5) != null) {
                segments.add(new TextSegment(match, Color.WHITE));
            }
            // 标识符
            else {
                segments.add(new TextSegment(match, Color.WHITE));
            }
        }
    }
    
    /**
     * 显示PDF文件
     */
    private void displayPdfFile(File file, VBox parentContainer) {
        System.out.println("开始处理PDF文件: " + file.getAbsolutePath());
        
        // 检查文件是否存在
        if (!file.exists()) {
            System.out.println("PDF文件不存在: " + file.getAbsolutePath());
            Label errorLabel = new Label("PDF文件不存在: " + file.getName());
            errorLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-size: 14px; -fx-padding: 10px 0;");
            parentContainer.getChildren().add(errorLabel);
            return;
        }
        
        // 创建PDF内容容器
        VBox pdfContainer = new VBox();
        pdfContainer.setStyle("-fx-background-color: #f7fafc; -fx-padding: 15px; -fx-border-radius: 6px;");
        
        // 添加加载中提示
        Label loadingLabel = new Label("正在加载PDF文件...");
        loadingLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 14px; -fx-padding: 20px 0;");
        loadingLabel.setAlignment(Pos.CENTER);
        pdfContainer.getChildren().add(loadingLabel);
        
        parentContainer.getChildren().add(pdfContainer);
        
        // 异步加载PDF文件，避免阻塞UI线程
        new Thread(() -> {
            try {
                System.out.println("开始加载PDF文件: " + file.getName());
                // 使用PDFBox加载PDF文件
                PDDocument document = PDDocument.load(file);
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                int pageCount = document.getNumberOfPages();
                
                System.out.println("PDF文件页数: " + pageCount);
                
                // 创建页面容器
                VBox pagesVBox = new VBox(10);
                pagesVBox.setAlignment(Pos.CENTER);
                pagesVBox.setStyle("-fx-padding: 10px;");
                
                // 只渲染前三页，提高性能
                int pagesToRender = Math.min(pageCount, 3);
                for (int pageNum = 0; pageNum < pagesToRender; pageNum++) {
                    try {
                        System.out.println("渲染PDF第 " + (pageNum + 1) + " 页");
                        // 渲染页面
                        BufferedImage bufferedImage = pdfRenderer.renderImage(pageNum, 1.5f);
                        Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                        
                        ImageView imageView = new ImageView(image);
                        imageView.setPreserveRatio(true);
                        // 调整宽度，适配更宽的窗口
                        imageView.setFitWidth(1000);
                        
                        // 添加页码
                        Label pageLabel = new Label("第 " + (pageNum + 1) + " 页");
                        pageLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #4a5568; -fx-margin-bottom: 5px;");
                        
                        VBox pageContainer = new VBox(5, pageLabel, imageView);
                        pageContainer.setAlignment(Pos.CENTER);
                        pageContainer.setStyle("-fx-background-color: white; -fx-padding: 10px; -fx-border-radius: 4px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");
                        
                        // 在JavaFX应用线程中添加页面
                        javafx.application.Platform.runLater(() -> {
                            pagesVBox.getChildren().add(pageContainer);
                        });
                    } catch (Exception e) {
                        // 单页渲染失败，继续渲染其他页
                        System.err.println("渲染PDF第 " + (pageNum + 1) + " 页失败: " + e.getMessage());
                    }
                }
                
                // 如果有更多页，显示提示
                if (pageCount > pagesToRender) {
                    Label morePagesLabel = new Label("... 还有 " + (pageCount - pagesToRender) + " 页未显示");
                    morePagesLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #718096; -fx-margin-top: 5px;");
                    morePagesLabel.setAlignment(Pos.CENTER);
                    
                    javafx.application.Platform.runLater(() -> {
                        pagesVBox.getChildren().add(morePagesLabel);
                    });
                }
                
                // 创建滚动面板
                ScrollPane scrollPane = new ScrollPane();
                scrollPane.setFitToWidth(true);
                scrollPane.setStyle("-fx-background-color: #f7fafc; -fx-border: none;");
                scrollPane.setContent(pagesVBox);
                
                // 添加控制栏
                HBox controlBox = new HBox();
                controlBox.setAlignment(Pos.CENTER_RIGHT);
                controlBox.setStyle("-fx-padding: 0 5px 5px 0;");
                
                // 添加放大查看按钮
                Button enlargeBtn = new Button("🔍");
                enlargeBtn.setStyle(
                        "-fx-background-color: transparent; " +
                        "-fx-font-size: 16px; " +
                        "-fx-text-fill: #4a5568; " +
                        "-fx-padding: 4px 8px; " +
                        "-fx-border: none; " +
                        "-fx-cursor: hand;"
                );
                enlargeBtn.setTooltip(new Tooltip("放大查看"));
                enlargeBtn.setOnAction(e -> {
                    showEnlargePdfView(file, pageCount);
                });
                
                // 添加页码信息
                Label pageInfoLabel = new Label("共 " + pageCount + " 页");
                pageInfoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #718096; -fx-margin-right: 10px;");
                
                controlBox.getChildren().addAll(pageInfoLabel, enlargeBtn);
                
                // 在JavaFX应用线程中更新UI
                javafx.application.Platform.runLater(() -> {
                    // 清除加载提示
                    pdfContainer.getChildren().clear();
                    
                    pdfContainer.getChildren().addAll(controlBox, scrollPane);
                });
                
                // 关闭文档
                document.close();
                System.out.println("PDF文件加载完成");
            } catch (Exception e) {
                final String errorMsg = "读取PDF文件失败: " + e.getMessage();
                System.err.println(errorMsg);
                e.printStackTrace();
                
                // 在JavaFX应用线程中显示错误信息
                javafx.application.Platform.runLater(() -> {
                    // 清除加载提示
                    pdfContainer.getChildren().clear();
                    
                    Label errorLabel = new Label(errorMsg);
                    errorLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-size: 14px; -fx-padding: 10px 0;");
                    pdfContainer.getChildren().add(errorLabel);
                });
            }
        }).start();
    }
    
    /**
     * 放大查看PDF
     */
    private void showEnlargePdfView(File file, int pageCount) {
        Stage stage = new Stage();
        stage.setTitle("PDF放大查看 - " + file.getName());
        
        VBox root = new VBox(10);
        root.setStyle("-fx-background-color: #f7fafc; -fx-padding: 20px;");
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f7fafc; -fx-border: none;");
        
        VBox pagesVBox = new VBox(15);
        pagesVBox.setAlignment(Pos.CENTER);
        pagesVBox.setStyle("-fx-padding: 10px;");
        
        try {
            PDDocument document = PDDocument.load(file);
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            
            for (int pageNum = 0; pageNum < pageCount; pageNum++) {
                BufferedImage bufferedImage = pdfRenderer.renderImage(pageNum, 2.0f);
                Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                
                ImageView imageView = new ImageView(image);
                imageView.setPreserveRatio(true);
                imageView.setFitWidth(850);
                
                Label pageLabel = new Label("第 " + (pageNum + 1) + " 页");
                pageLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #2d3748; -fx-margin-bottom: 5px;");
                
                VBox pageContainer = new VBox(5, pageLabel, imageView);
                pageContainer.setAlignment(Pos.CENTER);
                pageContainer.setStyle("-fx-background-color: white; -fx-padding: 15px; -fx-border-radius: 6px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3);");
                
                pagesVBox.getChildren().add(pageContainer);
            }
            
            document.close();
        } catch (IOException e) {
            Label errorLabel = new Label("加载PDF文件失败: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-size: 16px; -fx-padding: 20px;");
            pagesVBox.getChildren().add(errorLabel);
        }
        
        scrollPane.setContent(pagesVBox);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        root.getChildren().add(scrollPane);
        stage.setScene(new Scene(root, 900, 700));
        stage.show();
    }
    
    /**
     * 显示DOCX文件
     */
    private void displayDocxFile(File file, VBox parentContainer) {
        try {
            System.out.println("开始处理DOCX文件: " + file.getAbsolutePath());
            
            // 检查文件是否存在
            if (!file.exists()) {
                System.out.println("DOCX文件不存在: " + file.getAbsolutePath());
                Label errorLabel = new Label("DOCX文件不存在: " + file.getName());
                errorLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-size: 14px; -fx-padding: 10px 0;");
                parentContainer.getChildren().add(errorLabel);
                return;
            }
            
            // 使用POI加载DOCX文件
            XWPFDocument document = new XWPFDocument(Files.newInputStream(file.toPath()));
            
            // 提取文本内容
            StringBuilder content = new StringBuilder();
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            System.out.println("DOCX文件段落数量: " + paragraphs.size());
            
            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText();
                if (!text.isEmpty()) {
                    System.out.println("DOCX段落内容: " + text);
                    content.append(text).append("\n\n");
                }
            }
            
            document.close();
            
            // 如果提取到内容，显示文本
            if (content.length() > 0) {
                System.out.println("成功提取DOCX内容，长度: " + content.length());
                // 创建带放大功能的文本显示组件
                createTextWithEnlargeButton(content.toString(), "DOCX", parentContainer, file.getName());
            } else {
                // 没有提取到文本，显示提示信息
                System.out.println("DOCX文件内容为空或无法提取");
                Label infoLabel = new Label("DOCX文件内容为空或无法提取");
                infoLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 14px; -fx-padding: 15px 0;");
                parentContainer.getChildren().add(infoLabel);
            }
        } catch (Exception e) {
            // 捕获所有异常，确保UI不会崩溃
            System.out.println("读取DOCX文件失败: " + e.getMessage());
            e.printStackTrace();
            Label errorLabel = new Label("读取DOCX文件失败: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: #e53e3e; -fx-font-size: 14px; -fx-padding: 10px 0;");
            parentContainer.getChildren().add(errorLabel);
        }
    }
    
    /**
     * 创建带放大按钮的文本显示组件
     */
    private void createTextWithEnlargeButton(String content, String fileType, VBox parentContainer, String fileName) {
        // 创建容器
        VBox container = new VBox();
        container.setStyle("-fx-background-color: #f7fafc; -fx-border-radius: 6px;");
        
        // 创建控制栏
        HBox controlBox = new HBox();
        controlBox.setAlignment(Pos.CENTER_RIGHT);
        controlBox.setStyle("-fx-padding: 0 5px 5px 0;");
        
        // 添加放大查看按钮
        Button enlargeBtn = new Button("🔍");
        enlargeBtn.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-font-size: 16px; " +
                "-fx-text-fill: #4a5568; " +
                "-fx-padding: 4px 8px; " +
                "-fx-border: none; " +
                "-fx-cursor: hand;"
        );
        enlargeBtn.setTooltip(new Tooltip("放大查看"));
        enlargeBtn.setOnAction(e -> {
            showEnlargeText(content, fileType, fileName);
        });
        
        controlBox.getChildren().add(enlargeBtn);
        
        if ("CODE".equals(fileType)) {
            // 代码文件使用TextFlow显示语法高亮
            TextFlow textFlow = new TextFlow();
            textFlow.setStyle(
                    "-fx-background-color: #2d3748; " +
                    "-fx-padding: 15px; " +
                    "-fx-font-family: 'Consolas', 'Monaco', 'Courier New', monospace; " +
                    "-fx-font-size: 14px;"
            );
            textFlow.setPrefHeight(300);
            
            // 应用语法高亮
            syntaxHighlight(textFlow, content, getFileExtension(fileName));
            
            // 创建滚动面板
            ScrollPane scrollPane = new ScrollPane(textFlow);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: #f7fafc; -fx-border: none;");
            
            container.getChildren().addAll(controlBox, scrollPane);
        } else {
            // 普通文本文件使用TextArea显示
            TextArea textArea = new TextArea(content);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefHeight(300);
            
            // 设置样式
            String baseStyle = 
                    "-fx-border: none; " +
                    "-fx-padding: 15px; " +
                    "-fx-wrap-text: true;";
            
            textArea.setStyle(
                    "-fx-background-color: white; " +
                    "-fx-font-family: 'Microsoft YaHei', 'SimSun', serif; " +
                    "-fx-font-size: 16px; " +
                    "-fx-line-spacing: 1.5; " +
                    "-fx-text-fill: #4a5568; " +
                    baseStyle
            );
            
            container.getChildren().addAll(controlBox, textArea);
        }
        
        VBox.setVgrow(container.getChildren().get(1), Priority.ALWAYS);
        parentContainer.getChildren().add(container);
    }
    
    /**
     * 放大查看文本
     */
    private void showEnlargeText(String content, String fileType, String fileName) {
        Stage stage = new Stage();
        stage.setTitle("放大查看");
        
        VBox root = new VBox(10);
        root.setStyle("-fx-background-color: #f7fafc; -fx-padding: 20px;");
        
        if ("CODE".equals(fileType)) {
            // 代码文件使用TextFlow显示语法高亮
            TextFlow textFlow = new TextFlow();
            textFlow.setStyle(
                    "-fx-background-color: #2d3748; " +
                    "-fx-padding: 20px; " +
                    "-fx-font-family: 'Consolas', 'Monaco', 'Courier New', monospace; " +
                    "-fx-font-size: 16px;"
            );
            
            // 应用语法高亮
            syntaxHighlight(textFlow, content, getFileExtension(fileName));
            
            ScrollPane scrollPane = new ScrollPane(textFlow);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-border: none;");
            
            VBox.setVgrow(scrollPane, Priority.ALWAYS);
            root.getChildren().add(scrollPane);
        } else {
            // 普通文本文件使用TextArea显示
            TextArea textArea = new TextArea(content);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            
            textArea.setStyle(
                    "-fx-background-color: white; " +
                    "-fx-font-family: 'Microsoft YaHei', 'SimSun', serif; " +
                    "-fx-font-size: 18px; " +
                    "-fx-line-spacing: 1.5; " +
                    "-fx-text-fill: #4a5568; " +
                    "-fx-padding: 20px;"
            );
            
            ScrollPane scrollPane = new ScrollPane(textArea);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-border: none;");
            
            VBox.setVgrow(scrollPane, Priority.ALWAYS);
            root.getChildren().add(scrollPane);
        }
        
        stage.setScene(new Scene(root, 900, 700));
        stage.show();
    }
    
    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", (double) size / 1024);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", (double) size / (1024 * 1024));
        } else {
            return String.format("%.1f GB", (double) size / (1024 * 1024 * 1024));
        }
    }

    /**
     * 显示上一个知识点
     */
    private void showPrevPoint() {
        if (currentPointIndex > 0) {
            currentPointIndex--;
            pointId = allPoints.get(currentPointIndex).getId();
            loadPointData();
            loadAllAttachments();
            updateNavigationButtons();
        }
    }

    /**
     * 显示下一个知识点
     */
    private void showNextPoint() {
        if (currentPointIndex < allPoints.size() - 1) {
            currentPointIndex++;
            pointId = allPoints.get(currentPointIndex).getId();
            loadPointData();
            loadAllAttachments();
            updateNavigationButtons();
        }
    }

    /**
     * 更新导航按钮状态
     */
    private void updateNavigationButtons() {
        prevBtn.setDisable(currentPointIndex == 0);
        nextBtn.setDisable(currentPointIndex == allPoints.size() - 1);
    }

    /**
     * 根据文件类型获取图标
     */
    private String getAttachmentIcon(FileType fileType) {
        switch (fileType) {
            case TEXT:
                return "📄";
            case CODE:
                return "💻";
            case IMAGE:
                return "🖼️";
            case AUDIO:
                return "🎵";
            case VIDEO:
                return "🎬";
            default:
                return "📎";
        }
    }

    /**
     * 关闭当前窗口，返回主界面
     */
    private void closeWindow() {
        if (currentStage != null) {
            currentStage.close();
        }
    }
}
