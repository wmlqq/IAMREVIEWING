package org.myself.iamreviewing.component.previewer;

import javafx.scene.layout.VBox;
import java.io.File;

/**
 * 文件预览器接口
 * 定义了所有文件预览器需要实现的方法
 */
public interface Previewer {
    /**
     * 显示文件预览
     * @param file 文件对象
     * @param parentContainer 父容器，预览内容将添加到这个容器中
     */
    void showPreview(File file, VBox parentContainer);
}