package org.myself.iamreviewing.component;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 详情页附件查看器
 * 专门用于知识点详情页的附件展示，按照用户要求的布局样式显示附件内容
 */
public class DetailAttachmentViewer extends VBox {

    // 语法高亮规则（参考CodePreviewer）
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
        private record LanguageSyntax(Set<String> keywords, String singleLineComment, String multiLineCommentStart,
                                      String multiLineCommentEnd) {

    }
    
    public DetailAttachmentViewer() {
        this.setSpacing(10);
        this.setStyle("-fx-padding: 0px;");
    }
    
    /**
     * 显示文本文件内容
     */
    public void showTextFile(File file) {
        Platform.runLater(() -> {
            try {
                // 读取文件内容
                String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                
                // 创建文本显示区域
                TextFlow textFlow = new TextFlow();
                textFlow.setStyle(
                        "-fx-padding: 5px; " +
                        "-fx-font-family: 'Microsoft YaHei', 'SimSun', serif; " +
                        "-fx-font-size: 14px;"
                );
                
                // 普通文本，直接显示
                Text text = new Text(content);
                textFlow.getChildren().add(text);
                
                // 创建滚动面板
                ScrollPane scrollPane = new ScrollPane(textFlow);
                scrollPane.setFitToWidth(true);
                scrollPane.setFitToHeight(true);
                scrollPane.setStyle("-fx-background-color: transparent; -fx-border: none;");
                scrollPane.setPrefHeight(200);
                VBox.setVgrow(scrollPane, Priority.ALWAYS);
                
                this.getChildren().add(scrollPane);
            } catch (IOException e) {
                showError("读取文件失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 显示代码文件内容，支持语法高亮
     */
    public void showCodeFile(File file) {
        Platform.runLater(() -> {
            try {
                // 读取文件内容
                String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                String fileExtension = getFileExtension(file.getName());
                
                // 创建代码渲染区域
                TextFlow textFlow = new TextFlow();
                textFlow.setStyle(
                        "-fx-background-color: #1e1e1e; " +
                        "-fx-padding: 10px; " +
                        "-fx-font-family: 'Consolas', 'Monaco', 'Courier New', monospace; " +
                        "-fx-font-size: 14px;"
                );
                
                // 语法高亮渲染
                syntaxHighlight(textFlow, content, fileExtension);
                
                // 创建滚动面板
                ScrollPane scrollPane = new ScrollPane(textFlow);
                scrollPane.setFitToWidth(true);
                scrollPane.setFitToHeight(true);
                scrollPane.setStyle("-fx-background-color: transparent; -fx-border: none;");
                scrollPane.setPrefHeight(300);
                VBox.setVgrow(scrollPane, Priority.ALWAYS);
                
                this.getChildren().add(scrollPane);
            } catch (IOException e) {
                showError("读取代码文件失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 显示图片文件
     */
    public void showImageFile(File file) {
        Platform.runLater(() -> {
            try {
                ImageView imageView = new ImageView();
                imageView.setPreserveRatio(true);
                
                Image image = new Image(file.toURI().toString());
                imageView.setImage(image);
                
                // 根据图片实际尺寸动态调整显示大小
                double imageWidth = image.getWidth();
                double imageHeight = image.getHeight();
                
                // 计算合适的显示尺寸，最大宽度800，最大高度500
                double displayWidth, displayHeight;
                if (imageWidth > imageHeight) {
                    // 宽图
                    displayWidth = Math.min(imageWidth, 800);
                    displayHeight = (displayWidth / imageWidth) * imageHeight;
                } else {
                    // 高图或方图
                    displayHeight = Math.min(imageHeight, 500);
                    displayWidth = (displayHeight / imageHeight) * imageWidth;
                }
                
                imageView.setFitWidth(displayWidth);
                imageView.setFitHeight(displayHeight);
                
                this.getChildren().add(imageView);
            } catch (Exception e) {
                showError("加载图片失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 显示视频文件
     */
    public void showVideoFile(File file) {
        Platform.runLater(() -> {
            try {
                Media media = new Media(file.toURI().toString());
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                ScrollPane scrollPane = getScrollPane(mediaPlayer);
                VBox.setVgrow(scrollPane, Priority.ALWAYS);
                
                this.getChildren().add(scrollPane);
            } catch (Exception e) {
                showError("加载视频失败: " + e.getMessage());
            }
        });
    }

    private static ScrollPane getScrollPane(MediaPlayer mediaPlayer) {
        MediaView mediaView = new MediaView(mediaPlayer);

        mediaView.setPreserveRatio(true);
        mediaView.setFitWidth(800);
        mediaView.setFitHeight(500);

        // 创建滚动面板
        ScrollPane scrollPane = new ScrollPane(mediaView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border: none;");
        return scrollPane;
    }

    /**
     * 显示特殊文件（使用嵌套框）
     */
    public void showSpecialFile(File file, String fileType) {
        Platform.runLater(() -> {
            // 特殊文件类型，使用嵌套框
            VBox specialContainer = new VBox();
            specialContainer.setStyle(
                    "-fx-background-color: #f9f9f9; " +
                    "-fx-border: 1px solid #e0e0e0; " +
                    "-fx-border-radius: 4px; " +
                    "-fx-padding: 10px; " +
                    "-fx-margin-bottom: 15px;"
            );
            
            // 使用现有的AttachmentPreview处理特殊文件
            AttachmentPreview preview = new AttachmentPreview();
            preview.showPreview(file.getAbsolutePath(), fileType);
            
            specialContainer.getChildren().add(preview);
            this.getChildren().add(specialContainer);
        });
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
            if (syntax.multiLineCommentStart() != null && syntax.multiLineCommentEnd() != null) {
                int commentStart = line.indexOf(syntax.multiLineCommentStart());
                if (commentStart != -1) {
                    int commentEnd = line.indexOf(syntax.multiLineCommentEnd(), commentStart + syntax.multiLineCommentStart().length());
                    processLineSegments(line.substring(0, commentStart), syntax, segments);
                    if (commentEnd != -1) {
                        // 处理注释前的内容
                        // 添加注释
                        segments.add(new TextSegment(
                                line.substring(commentStart, commentEnd + syntax.multiLineCommentEnd().length()),
                                Color.GREEN
                        ));
                        // 处理注释后的内容
                        processLineSegments(line.substring(commentEnd + syntax.multiLineCommentEnd().length()), syntax, segments);
                    } else {
                        // 多行注释开始，没有结束
                        segments.add(new TextSegment(
                                line.substring(commentStart),
                                Color.GREEN
                        ));
                    }
                } else {
                    // 处理单行注释
                    int singleCommentStart = line.indexOf(syntax.singleLineComment());
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
                int singleCommentStart = line.indexOf(syntax.singleLineComment());
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
                Text text = new Text(segment.text());
                text.setFill(segment.color());
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
            else if (matcher.group(1) != null && syntax.keywords().contains(matcher.group(1))) {
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
     * 显示错误信息
     */
    private void showError(String message) {
        Platform.runLater(() -> {
            Label errorLabel = new Label(message);
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            errorLabel.setWrapText(true);
            errorLabel.setAlignment(Pos.CENTER);
            
            this.getChildren().add(errorLabel);
        });
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
         * 文本片段类
         */
        private record TextSegment(String text, Color color) {
    }
}