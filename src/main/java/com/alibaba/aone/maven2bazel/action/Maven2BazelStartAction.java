package com.alibaba.aone.maven2bazel.action;

import com.alibaba.aone.maven2bazel.convert.utils.Dependencies2BazelUtil;
import com.alibaba.aone.maven2bazel.convert.utils.Pom2DependenciesUtil;
import com.alibaba.aone.maven2bazel.state.BazelSettings;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 启动按钮
 */
public class Maven2BazelStartAction extends AnAction {

    private BazelSettings bazelSettings = BazelSettings.getInstance();

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);

        String mavenHome = bazelSettings.getMavenHome();
        if (StringUtils.isBlank(mavenHome)) {
            notifyErrorMessage("mavenHome is not empty!");
            return;
        }

        String localRepository = bazelSettings.getLocalRepository();
        if (StringUtils.isBlank(localRepository)) {
            notifyErrorMessage("localRepository is not empty!");
            return;
        }
        String bazelJdkHome = bazelSettings.getBazelJdkHome();
        if (StringUtils.isBlank(bazelJdkHome)) {
            notifyErrorMessage("bazelJdkHome is not empty!");
            return;
        }
        String languageLevel = bazelSettings.getLanguageLevel();
        if (StringUtils.isBlank(languageLevel)) {
            notifyErrorMessage("languageLevel is not empty!");
            return;
        }
        String encoding = bazelSettings.getEncoding();
        if (StringUtils.isBlank(encoding)) {
            notifyErrorMessage("encoding is not empty!");
            return;
        }
        String bazelMavenRepositoryCache = bazelSettings.getBazelMavenRepositoryCache();
        if (StringUtils.isBlank(bazelMavenRepositoryCache)) {
            notifyErrorMessage("bazelMavenRepositoryCache is not empty!");
            return;
        }
        List<String> remoteRepositories = bazelSettings.getRemoteRepositories();
        if (CollectionUtils.isEmpty(remoteRepositories)) {
            notifyErrorMessage("remoteRepositories is not empty!");
            return;
        }
        String basePath = project.getBasePath();
        System.out.println("basePath:" + basePath);

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "maven to bazel", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                // start your process
                System.out.println("maven to bazel");
                indicator.setFraction(0.10);
                indicator.setText("pom convert to dependencies start");
                Pom2DependenciesUtil.pom2Dependencies(indicator, basePath, mavenHome);
                indicator.setText("pom convert to dependencies success");
                indicator.setFraction(0.20);
                indicator.setText("dependencies convert to bazel start");
                indicator.setFraction(0.21);
                try {
                    Dependencies2BazelUtil.dependency2Bazel(indicator, basePath);
                } catch (Exception e) {
                    e.printStackTrace();
                    notifyErrorMessage("pom to bazel error :" + e.getMessage());
                    indicator.setFraction(1.0);
                    indicator.setText("pom convert to dependencies failed");
                    return;
                }
            }
        });
    }

    /**
     * 显示错误信息
     *
     * @param errorMessage
     */
    private void notifyErrorMessage(String errorMessage) {
        NotificationGroup group =
            new NotificationGroup("maven2bazelNotifyMavenEmptyERORR", NotificationDisplayType.BALLOON, true);
        Notification notification = group.createNotification(StringUtils.trimToEmpty(errorMessage), MessageType.ERROR);
        Notifications.Bus.notify(notification);
    }
}
