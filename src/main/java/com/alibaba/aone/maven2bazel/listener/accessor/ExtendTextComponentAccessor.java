package com.alibaba.aone.maven2bazel.listener.accessor;

import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.ui.EditorComboBox;
import org.jetbrains.annotations.NotNull;

public class ExtendTextComponentAccessor {
    /**
     * The accessor that gets and changes whole text
     */
    public static TextComponentAccessor<EditorComboBox> getEditorComboBoxAccessor(){
        TextComponentAccessor<EditorComboBox> EDIT_COMBOBOX_WHOLE_TEXT = new TextComponentAccessor<EditorComboBox>() {
            @Override
            public String getText(EditorComboBox comboBox) {
                Object item = comboBox.getEditor().getItem();
                return item.toString();
            }

            @Override
            public void setText(EditorComboBox comboBox, @NotNull String text) {
                comboBox.getEditor().setItem(text);
            }
        };
        return EDIT_COMBOBOX_WHOLE_TEXT;
    }

}
