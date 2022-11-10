package com.alibaba.aone.maven2bazel.action;

import com.alibaba.aone.maven2bazel.dialog.SettingsDialogWrapper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;

public class SettingsAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        new SettingsDialogWrapper(project).showAndGet();
    }
}
