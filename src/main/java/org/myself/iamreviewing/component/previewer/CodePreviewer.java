package org.myself.iamreviewing.component.previewer;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码文件预览器，支持语法高亮
 */
public class CodePreviewer implements Previewer {

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
        
        public Set<String> getKeywords() {
            return keywords;
        }
        
        public String getSingleLineComment() {
            return singleLineComment;
        }
        
        public String getMultiLineCommentStart() {
            return multiLineCommentStart;
        }
        
        public String getMultiLineCommentEnd() {
            return multiLineCommentEnd;
        }
    }

    @Override
    public void showPreview(File file, VBox parentContainer) {
        Label title = new Label("代码预览 - " + file.getName());
        title.setStyle("-fx-font-weight: bold;");

        // 在JavaFX应用线程中创建UI组件
        Platform.runLater(() -> {
            VBox codeContent = new VBox(10);
            codeContent.setAlignment(Pos.CENTER);
            codeContent.setStyle("-fx-padding: 10px;");

            // 添加加载中提示
            Label loadingLabel = new Label("正在加载代码文件...");
            codeContent.getChildren().add(loadingLabel);
            parentContainer.getChildren().addAll(title, codeContent);

            // 异步加载代码内容
            new Thread(() -> {
                try {
                    // 读取文件内容
                    String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                    String fileExtension = getFileExtension(file.getName());
                    
                    // 在JavaFX应用线程中更新UI
                    Platform.runLater(() -> {
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
                        
                        // 创建JavaFX滚动面板
                        ScrollPane fxScrollPane = new ScrollPane(textFlow);
                        fxScrollPane.setFitToWidth(true);
                        fxScrollPane.setFitToHeight(true);
                        fxScrollPane.setStyle("-fx-background-color: #f0f0f0;");
                        
                        // 创建放大查看按钮
                        Button enlargeBtn = new Button("放大查看");
                        enlargeBtn.setOnAction(e -> showEnlargeView(file, content, fileExtension));
                        
                        // 更新UI
                        codeContent.getChildren().clear();
                        codeContent.getChildren().addAll(enlargeBtn, fxScrollPane);
                        VBox.setVgrow(fxScrollPane, Priority.ALWAYS);
                    });
                } catch (IOException e) {
                    String errorMsg = "加载代码文件失败: " + e.getMessage();
                    System.err.println(errorMsg);
                    e.printStackTrace();
                    
                    Platform.runLater(() -> {
                        codeContent.getChildren().clear();
                        showError(errorMsg, codeContent);
                    });
                }
            }).start();
        });
    }
    
    /**
     * 显示放大查看窗口
     */
    private void showEnlargeView(File file, String content, String fileExtension) {
        Stage stage = new Stage();
        stage.setTitle("代码放大查看 - " + file.getName());
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        
        VBox largeVBox = new VBox(10);
        largeVBox.setStyle("-fx-padding: 10px;");
        
        // 在JavaFX应用线程中更新UI
        Platform.runLater(() -> {
            // 创建代码渲染区域
            TextFlow textFlow = new TextFlow();
            textFlow.setStyle(
                    "-fx-background-color: #1e1e1e; " +
                    "-fx-padding: 15px; " +
                    "-fx-font-family: 'Consolas', 'Monaco', 'Courier New', monospace; " +
                    "-fx-font-size: 16px;"
            );
            
            // 语法高亮渲染
            syntaxHighlight(textFlow, content, fileExtension);
            
            // 创建JavaFX滚动面板
            ScrollPane fxScrollPane = new ScrollPane(textFlow);
            fxScrollPane.setFitToWidth(true);
            fxScrollPane.setFitToHeight(true);
            VBox.setVgrow(fxScrollPane, Priority.ALWAYS);
            
            largeVBox.getChildren().add(fxScrollPane);
            
            stage.setScene(new javafx.scene.Scene(largeVBox, 900, 700));
            stage.show();
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
     * 文本片段类
     */
    private static class TextSegment {
        private final String text;
        private final Color color;
        
        public TextSegment(String text, Color color) {
            this.text = text;
            this.color = color;
        }
        
        public String getText() {
            return text;
        }
        
        public Color getColor() {
            return color;
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
     * 显示错误信息
     */
    private void showError(String message, VBox container) {
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        errorLabel.setWrapText(true);
        errorLabel.setAlignment(Pos.CENTER);
        
        container.getChildren().add(errorLabel);
    }
}