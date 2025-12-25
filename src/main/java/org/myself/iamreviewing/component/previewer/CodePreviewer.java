package org.myself.iamreviewing.component.previewer;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
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

    // 不同语言的关键词映射
    private static final Map<String, Set<String>> LANGUAGE_KEYWORDS = new HashMap<>();
    // 语言映射（扩展名 -> 语言名称）
    private static final Map<String, String> EXTENSION_LANGUAGE_MAP = new HashMap<>();
    // 语法高亮颜色配置
    private static final Color KEYWORD_COLOR = Color.rgb(255, 191, 0); // 黄色
    private static final Color STRING_COLOR = Color.rgb(139, 195, 74); // 绿色
    private static final Color COMMENT_COLOR = Color.rgb(128, 128, 128); // 灰色
    private static final Color NUMBER_COLOR = Color.rgb(247, 107, 107); // 红色
    private static final Color DEFAULT_COLOR = Color.WHITE;
    
    static {
        // 初始化语言关键词
        // C++ 关键词
        Set<String> cppKeywords = new HashSet<>(Arrays.asList(
            "alignas", "alignof", "and", "and_eq", "asm", "atomic_cancel", "atomic_commit", "atomic_noexcept",
            "auto", "bitand", "bitor", "bool", "break", "case", "catch", "char", "char8_t", "char16_t", "char32_t",
            "class", "compl", "concept", "const", "consteval", "constexpr", "constinit", "const_cast", "continue",
            "co_await", "co_return", "co_yield", "decltype", "default", "delete", "do", "double", "dynamic_cast",
            "else", "enum", "explicit", "export", "extern", "false", "float", "for", "friend", "goto", "if",
            "inline", "int", "long", "mutable", "namespace", "new", "noexcept", "not", "not_eq", "nullptr", "operator",
            "or", "or_eq", "private", "protected", "public", "reflexpr", "register", "reinterpret_cast", "requires",
            "return", "short", "signed", "sizeof", "static", "static_assert", "static_cast", "struct", "switch",
            "synchronized", "template", "this", "thread_local", "throw", "true", "try", "typedef", "typeid",
            "typename", "union", "unsigned", "using", "virtual", "void", "volatile", "wchar_t", "while", "xor", "xor_eq"
        ));
        
        // C 关键词
        Set<String> cKeywords = new HashSet<>(Arrays.asList(
            "auto", "break", "case", "char", "const", "continue", "default", "do", "double", "else", "enum",
            "extern", "float", "for", "goto", "if", "int", "long", "register", "return", "short", "signed",
            "sizeof", "static", "struct", "switch", "typedef", "union", "unsigned", "void", "volatile", "while"
        ));
        
        // Java 关键词
        Set<String> javaKeywords = new HashSet<>(Arrays.asList(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while"
        ));
        
        // Python 关键词
        Set<String> pythonKeywords = new HashSet<>(Arrays.asList(
            "False", "None", "True", "and", "as", "assert", "async", "await", "break", "class", "continue",
            "def", "del", "elif", "else", "except", "finally", "for", "from", "global", "if", "import",
            "in", "is", "lambda", "nonlocal", "not", "or", "pass", "raise", "return", "try", "while",
            "with", "yield"
        ));
        
        // 添加到语言关键词映射
        LANGUAGE_KEYWORDS.put("cpp", cppKeywords);
        LANGUAGE_KEYWORDS.put("c", cKeywords);
        LANGUAGE_KEYWORDS.put("java", javaKeywords);
        LANGUAGE_KEYWORDS.put("python", pythonKeywords);
        
        // 初始化扩展名到语言的映射
        EXTENSION_LANGUAGE_MAP.put(".cpp", "cpp");
        EXTENSION_LANGUAGE_MAP.put(".cxx", "cpp");
        EXTENSION_LANGUAGE_MAP.put(".cc", "cpp");
        EXTENSION_LANGUAGE_MAP.put(".c", "c");
        EXTENSION_LANGUAGE_MAP.put(".h", "c");
        EXTENSION_LANGUAGE_MAP.put(".java", "java");
        EXTENSION_LANGUAGE_MAP.put(".py", "python");
        EXTENSION_LANGUAGE_MAP.put(".pyw", "python");
    }

    @Override
    public void showPreview(File file, VBox parentContainer) {
        Label title = new Label("代码预览 - " + file.getName());
        title.setStyle("-fx-font-weight: bold;");

        VBox codeContent = new VBox(10);
        codeContent.setAlignment(Pos.CENTER);
        codeContent.setStyle("-fx-padding: 10px;");

        // 添加加载中提示
        Label loadingLabel = new Label("正在加载代码文件...");
        codeContent.getChildren().add(loadingLabel);

        // 异步加载代码内容
        new Thread(() -> {
            try {
                // 读取文件内容
                String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                String fileExtension = getFileExtension(file.getName());
                String language = EXTENSION_LANGUAGE_MAP.getOrDefault(fileExtension, "");
                
                // 创建带语法高亮的TextFlow
                TextFlow textFlow = createSyntaxHighlightedTextFlow(content, language);
                
                // 创建滚动面板
                ScrollPane scrollPane = new ScrollPane(textFlow);
                scrollPane.setFitToWidth(true);
                scrollPane.setFitToHeight(true);
                scrollPane.setStyle("-fx-background-color: #2b2b2b;");
                
                // 创建放大查看按钮
                Button enlargeBtn = new Button("放大查看");
                enlargeBtn.setOnAction(e -> showEnlargeView(file, content, language));
                
                // 更新UI
                Platform.runLater(() -> {
                    codeContent.getChildren().clear();
                    codeContent.getChildren().addAll(enlargeBtn, scrollPane);
                    VBox.setVgrow(scrollPane, Priority.ALWAYS);
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
        
        parentContainer.getChildren().addAll(title, codeContent);
    }
    
    /**
     * 创建带有语法高亮的TextFlow
     */
    private TextFlow createSyntaxHighlightedTextFlow(String content, String language) {
        TextFlow textFlow = new TextFlow();
        textFlow.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 10px;");
        
        // 获取当前语言的关键词集合
        Set<String> keywords = LANGUAGE_KEYWORDS.getOrDefault(language, Collections.emptySet());
        
        // 基本语法高亮规则
        // 1. 匹配单行注释
        // 2. 匹配字符串字面量
        // 3. 匹配数字
        // 4. 匹配关键词
        
        // 分割内容为行
        String[] lines = content.split("\\n");
        
        for (String line : lines) {
            // 处理单行
            List<Text> lineTexts = new ArrayList<>();
            
            // 简单的语法高亮实现
            int pos = 0;
            while (pos < line.length()) {
                // 检查是否是注释
                if (line.startsWith("//", pos) || (language.equals("python") && line.startsWith("#", pos))) {
                    // 这是注释
                    Text commentText = new Text(line.substring(pos) + "\n");
                    commentText.setFont(Font.font("Consolas", 14));
                    commentText.setFill(COMMENT_COLOR);
                    lineTexts.add(commentText);
                    break;
                }
                
                // 检查是否是字符串
                if (line.startsWith("\"", pos) || line.startsWith("'", pos)) {
                    char quote = line.charAt(pos);
                    int endPos = pos + 1;
                    boolean escaped = false;
                    
                    while (endPos < line.length()) {
                        if (line.charAt(endPos) == '\\' && !escaped) {
                            escaped = true;
                        } else if (line.charAt(endPos) == quote && !escaped) {
                            break;
                        } else {
                            escaped = false;
                        }
                        endPos++;
                    }
                    
                    if (endPos < line.length()) {
                        endPos++;
                    }
                    
                    Text stringText = new Text(line.substring(pos, endPos));
                    stringText.setFont(Font.font("Consolas", 14));
                    stringText.setFill(STRING_COLOR);
                    lineTexts.add(stringText);
                    pos = endPos;
                    continue;
                }
                
                // 检查是否是数字
                if (Character.isDigit(line.charAt(pos)) || (line.charAt(pos) == '.' && pos + 1 < line.length() && Character.isDigit(line.charAt(pos + 1)))) {
                    int endPos = pos + 1;
                    while (endPos < line.length()) {
                        char c = line.charAt(endPos);
                        if (!Character.isDigit(c) && c != '.') {
                            break;
                        }
                        endPos++;
                    }
                    
                    Text numberText = new Text(line.substring(pos, endPos));
                    numberText.setFont(Font.font("Consolas", 14));
                    numberText.setFill(NUMBER_COLOR);
                    lineTexts.add(numberText);
                    pos = endPos;
                    continue;
                }
                
                // 检查是否是关键词
                if (Character.isLetterOrDigit(line.charAt(pos)) || line.charAt(pos) == '_') {
                    int endPos = pos + 1;
                    while (endPos < line.length()) {
                        char c = line.charAt(endPos);
                        if (!Character.isLetterOrDigit(c) && c != '_') {
                            break;
                        }
                        endPos++;
                    }
                    
                    String word = line.substring(pos, endPos);
                    Text wordText = new Text(word);
                    wordText.setFont(Font.font("Consolas", 14));
                    
                    // 如果是关键词，设置关键词颜色
                    if (keywords.contains(word)) {
                        wordText.setFill(KEYWORD_COLOR);
                    } else {
                        wordText.setFill(DEFAULT_COLOR);
                    }
                    
                    lineTexts.add(wordText);
                    pos = endPos;
                    continue;
                }
                
                // 其他字符，使用默认颜色
                Text normalText = new Text(String.valueOf(line.charAt(pos)));
                normalText.setFont(Font.font("Consolas", 14));
                normalText.setFill(DEFAULT_COLOR);
                lineTexts.add(normalText);
                pos++;
            }
            
            // 添加换行符
            lineTexts.add(new Text("\n"));
            
            // 添加到TextFlow
            textFlow.getChildren().addAll(lineTexts);
        }
        
        return textFlow;
    }
    
    /**
     * 显示放大查看窗口
     */
    private void showEnlargeView(File file, String content, String language) {
        Stage stage = new Stage();
        stage.setTitle("代码放大查看 - " + file.getName());
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        
        VBox largeVBox = new VBox(10);
        largeVBox.setStyle("-fx-padding: 10px; -fx-background-color: #2b2b2b;");
        
        // 创建带语法高亮的TextFlow
        TextFlow textFlow = createSyntaxHighlightedTextFlow(content, language);
        
        // 调整字体大小为16px
        for (javafx.scene.Node node : textFlow.getChildren()) {
            if (node instanceof Text) {
                ((Text) node).setFont(Font.font("Consolas", 16));
            }
        }
        
        // 创建滚动面板
        ScrollPane scrollPane = new ScrollPane(textFlow);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: #2b2b2b;");
        
        largeVBox.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        stage.setScene(new javafx.scene.Scene(largeVBox, 900, 700, Color.web("#2b2b2b")));
        stage.show();
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