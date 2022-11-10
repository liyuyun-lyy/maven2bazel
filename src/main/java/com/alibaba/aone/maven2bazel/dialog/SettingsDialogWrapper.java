package com.alibaba.aone.maven2bazel.dialog;

import com.alibaba.aone.maven2bazel.enums.Constants;
import com.alibaba.aone.maven2bazel.listener.accessor.ExtendTextComponentAccessor;
import com.alibaba.aone.maven2bazel.model.RemoteRepositoryModel;
import com.alibaba.aone.maven2bazel.state.BazelSettings;
import com.alibaba.fastjson.JSON;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.panel.ComponentPanelBuilder;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.EditorComboBox;
import com.intellij.ui.EditorComboWithBrowseButton;
import com.intellij.ui.SideBorder;
import com.intellij.ui.SingleSelectionModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UI;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class SettingsDialogWrapper extends DialogWrapper {
    private Project project;

    private EditorComboWithBrowseButton mavenEditorComboWithBrowseButton;

    private EditorComboWithBrowseButton localRepositoryEditorComboWithBrowseButton;

    private EditorComboBox bazelJdkEditCombox;

    private EditorComboBox bazelMavenRepositoryCacheEditCombox;

    private BazelSettings bazelSettings = BazelSettings.getInstance();
    EditorComboBox languageLevelComboBox = null;

    EditorComboBox encodingComboBox = null;

    private Map<String, RemoteRepositoryModel> url2RemoteRepositoryModelMap = new LinkedHashMap<>();

    private JList jList;

    public SettingsDialogWrapper(Project project) {
        super(true); // use current window as parent
        this.project = project;

        setTitle("Bazel2Maven Basic Config");
        init();
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel() {
        boolean chooseFiles = false;
        boolean chooseFolders = true;
        boolean chooseJars = false;
        boolean chooseJarsAsFiles = false;
        boolean chooseJarContents = false;
        boolean chooseMultiple = false;

        FileChooserDescriptor fileChooserDescriptor =
            new FileChooserDescriptor(chooseFiles, chooseFolders, chooseJars, chooseJarsAsFiles, chooseJarContents,
                chooseMultiple);

        //文件选择框的宽度
        int browseButtonWidth = 460;

        //文件选择框的高度
        int browseButtonHeight = 30;

        //maven
        mavenEditorComboWithBrowseButton =
            new EditorComboWithBrowseButton(null, "", project, "BAZEL_MAVEN_RECENTS_KEY");
        mavenEditorComboWithBrowseButton.addBrowseFolderListener(null, null, project, fileChooserDescriptor,
            ExtendTextComponentAccessor.getEditorComboBoxAccessor());
        mavenEditorComboWithBrowseButton.setPreferredSize(new Dimension(browseButtonWidth, browseButtonHeight));

        //获取mavenHome与历史记录
        getMavenHomeAndHistories();

        //localRepository
        localRepositoryEditorComboWithBrowseButton =
            new EditorComboWithBrowseButton(null, "", project, "BAZEL_LOCAL_REPOSITORY_RECENTS_KEY");
        localRepositoryEditorComboWithBrowseButton.addBrowseFolderListener(null, null, project, fileChooserDescriptor,
            ExtendTextComponentAccessor.getEditorComboBoxAccessor());
        localRepositoryEditorComboWithBrowseButton.setPreferredSize(
            new Dimension(browseButtonWidth, browseButtonHeight));

        //获取localRepository与历史记录
        getLocalRepositoryAndHistories();

        //bazel-jdk本地目录
        bazelJdkEditCombox = new EditorComboBox("bazelJDK");
        bazelJdkEditCombox.setPreferredSize(new Dimension(browseButtonWidth, browseButtonHeight));

        //获取BazelJDKHome与历史记录
        getBazelJdkHomeAndHistories();

        //JDK编码水平
        languageLevelComboBox = new EditorComboBox("Level");
        for (int i = 5; i < 18; i++) {
            languageLevelComboBox.addItem("" + i);
        }
        //默认为JDK8
        String languageLevel = bazelSettings.getLanguageLevel();
        if (StringUtils.isNotBlank(languageLevel)) {
            languageLevelComboBox.setSelectedItem(StringUtils.trimToEmpty(languageLevel));
        } else {
            languageLevelComboBox.setSelectedItem("8");
        }

        //编码
        encodingComboBox = new EditorComboBox("Encoding");
        encodingComboBox.addItem("GBK");
        encodingComboBox.addItem("ISO-8859-1");
        encodingComboBox.addItem("US-ASCII");
        encodingComboBox.addItem("UTF-8");
        encodingComboBox.addItem("UTF-16");
        encodingComboBox.addItem("UTF-16BE");
        encodingComboBox.addItem("UTF-16LE");

        //默认为UTF-8
        String encoding = bazelSettings.getEncoding();
        if (StringUtils.isNotBlank(encoding)) {
            encodingComboBox.setSelectedItem(StringUtils.trimToEmpty(encoding));
        } else {
            encodingComboBox.setSelectedItem("UTF-8");
        }

        //bazel-maven的本地仓库缓存
        bazelMavenRepositoryCacheEditCombox = new EditorComboBox("bazelMavenRepositoryCache");
        bazelMavenRepositoryCacheEditCombox.setPreferredSize(new Dimension(browseButtonWidth, browseButtonHeight));

        //获取bazel的maven仓库与历史记录
        getBazelMavenRepositoryCacheAndHistories();

        CollectionListModel dataModel = new CollectionListModel();
        List<String> remoteRepositories = bazelSettings.getRemoteRepositories();
        if (CollectionUtils.isNotEmpty(remoteRepositories)) {
            for (String modelJSON : remoteRepositories) {
                try {

                    if (StringUtils.isBlank(modelJSON)) {
                        continue;
                    }
                    RemoteRepositoryModel remoteRepositoryModel =
                        JSON.parseObject(modelJSON, RemoteRepositoryModel.class);
                    if (remoteRepositoryModel == null) {
                        continue;
                    }
                    String url = remoteRepositoryModel.getUrl();
                    if (StringUtils.isBlank(url)) {
                        continue;
                    }
                    dataModel.add(StringUtils.trimToEmpty(url));
                    url2RemoteRepositoryModelMap.put(url, remoteRepositoryModel);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        jList = new JBList(dataModel);// 创建指定列名和数据的表格
        jList.setSelectionModel(new SingleSelectionModel());

        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(jList);
        decorator.setAddAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                adjustModelData();
                //弹出Table框
                System.out.println("===add====");
                RemoteRepositoriesDialogWrapper remoteRepositoriesDialogWrapper =
                    new RemoteRepositoriesDialogWrapper(project, Constants.RemoteRepositoriesTypeEnums.ADD, jList,
                        url2RemoteRepositoryModelMap);
                if (remoteRepositoriesDialogWrapper.showAndGet()) {
                    // user pressed OK
                    RemoteRepositoryModel remoteRepositoryModel =
                        remoteRepositoriesDialogWrapper.getRemoteRepositoryModel();
                    if (remoteRepositoryModel != null) {
                        System.out.println(JSON.toJSONString(remoteRepositoryModel));
                        dataModel.add(remoteRepositoryModel.getUrl());
                        url2RemoteRepositoryModelMap.put(remoteRepositoryModel.getUrl(), remoteRepositoryModel);
                    }
                }
            }
        });

        decorator.setEditAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                //弹出Table框
                System.out.println("===edit====");
                adjustModelData();

                RemoteRepositoriesDialogWrapper remoteRepositoriesDialogWrapper =
                    new RemoteRepositoriesDialogWrapper(project, Constants.RemoteRepositoriesTypeEnums.EDIT, jList,
                        url2RemoteRepositoryModelMap);
                if (remoteRepositoriesDialogWrapper.showAndGet()) {
                    // user pressed OK
                    //若编辑后的url与之前的不同则删除之前的url
                    Map<String, RemoteRepositoryModel> url2RemoteRepositoryModelMap2 = new LinkedHashMap<>();
                    url2RemoteRepositoryModelMap2.putAll(url2RemoteRepositoryModelMap);

                    Set<Map.Entry<String, RemoteRepositoryModel>> entries = url2RemoteRepositoryModelMap2.entrySet();
                    Iterator<Map.Entry<String, RemoteRepositoryModel>> iterator = entries.iterator();

                    dataModel.removeAll();
                    url2RemoteRepositoryModelMap.clear();
                    while (iterator.hasNext()) {
                        Map.Entry<String, RemoteRepositoryModel> next1 = iterator.next();
                        String key = next1.getKey();

                        if (StringUtils.isBlank(key)) {
                            continue;
                        }
                        RemoteRepositoryModel remoteRepositoryModel = next1.getValue();
                        if (remoteRepositoryModel == null) {
                            continue;
                        }

                        //若是之前的url
                        if (StringUtils.equals(StringUtils.trimToEmpty(remoteRepositoryModel.getUrl()),
                            StringUtils.trimToEmpty(remoteRepositoriesDialogWrapper.getSelectUrl()))) {
                            RemoteRepositoryModel newRemoteRepositoryModel =
                                remoteRepositoriesDialogWrapper.getRemoteRepositoryModel();
                            dataModel.add(newRemoteRepositoryModel.getUrl());
                            url2RemoteRepositoryModelMap.put(newRemoteRepositoryModel.getUrl(),
                                newRemoteRepositoryModel);
                        } else {
                            dataModel.add(remoteRepositoryModel.getUrl());
                            url2RemoteRepositoryModelMap.put(remoteRepositoryModel.getUrl(), remoteRepositoryModel);
                        }
                    }
                }
            }
        });

        decorator.setRemoveAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                System.out.println("===remove====");
                adjustModelData();

                //获取选择项，删除
                Object selectedValue = jList.getSelectedValue();
                if (selectedValue == null) {
                    return;
                }
                dataModel.removeAll();
                Set<Map.Entry<String, RemoteRepositoryModel>> entries = url2RemoteRepositoryModelMap.entrySet();
                Iterator<Map.Entry<String, RemoteRepositoryModel>> iterator = entries.iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, RemoteRepositoryModel> next1 = iterator.next();
                    String key = next1.getKey();

                    if (StringUtils.isBlank(key)) {
                        continue;
                    }
                    //若是之前的url
                    if (StringUtils.equals(StringUtils.trimToEmpty(key),
                        StringUtils.trimToEmpty(selectedValue.toString()))) {
                        iterator.remove();
                    } else {
                        dataModel.add(key);
                    }
                }
            }
        });

        JBScrollPane pane = new JBScrollPane(decorator.createPanel());
        pane.setPreferredSize(JBUI.size(0, 170));
        pane.putClientProperty(UIUtil.KEEP_BORDER_SIDES, SideBorder.ALL);
        ComponentPanelBuilder componentPanelBuilder =
            UI.PanelFactory.panel(pane).withLabel("RemoteRepositories").resizeY(true);
        JPanel p = UI.PanelFactory.grid().add(
                UI.PanelFactory.panel(mavenEditorComboWithBrowseButton).withLabel("MavenHome")
                    .withComment("Default: /usr/local/Cellar/maven/3.8.3/libexec")).add(
                UI.PanelFactory.panel(localRepositoryEditorComboWithBrowseButton).withLabel("LocalRepository")
                    .withComment("/Users/liyuyun/.m2/repository"))

            .add(UI.PanelFactory.panel(languageLevelComboBox).withLabel("JDK_Level:").withComment("Default: 8")
                .moveCommentRight()).add(
                UI.PanelFactory.panel(encodingComboBox).withLabel("Encoding:").withComment("Default: UTF-8")
                    .moveCommentRight())

            .add(UI.PanelFactory.panel(bazelJdkEditCombox).withLabel("Bazel_JDK_Home")
                .withComment("/Library/Java/JavaVirtualMachines/jdk-17.0.5.jdk/Contents/Home")).add(
                UI.PanelFactory.panel(bazelMavenRepositoryCacheEditCombox).withLabel("Bazel_Maven_Cache")
                    .withComment("/tmp/repository")).add(componentPanelBuilder).createPanel();

        return p;
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();

        //保存mavenHome与历史记录
        saveMavenHomeAndHistories();

        //保存localRepository与历史记录
        saveLocalRepositoryAndHistories();

        //保存Bazel的JDKHome与历史记录
        saveBazelJdkHomeAndHistories();

        //保存bazel的maven仓库与历史记录
        saveBazelMavenRepositoryCacheAndHistories();

        adjustModelData();

        //保存远程仓库数据
        if ((url2RemoteRepositoryModelMap != null) && (!url2RemoteRepositoryModelMap.isEmpty())) {
            List<String> remoteRepositories = new ArrayList<>();
            for (String url : url2RemoteRepositoryModelMap.keySet()) {
                if (StringUtils.isBlank(url)) {
                    continue;
                }
                RemoteRepositoryModel remoteRepositoryModel = url2RemoteRepositoryModelMap.get(url);
                if (remoteRepositoryModel == null) {
                    continue;
                }
                remoteRepositories.add(JSON.toJSONString(remoteRepositoryModel));
            }
            bazelSettings.setRemoteRepositories(remoteRepositories);
        }

        //保存JDK水平
        String languageLevel = languageLevelComboBox.getText();
        if (StringUtils.isNotBlank(languageLevel)) {
            bazelSettings.setLanguageLevel(StringUtils.trimToEmpty(languageLevel));
        }
        //编码水平
        String encoding = encodingComboBox.getText();
        if (StringUtils.isNotBlank(encoding)) {
            bazelSettings.setEncoding(StringUtils.trimToEmpty(encoding));
        }
    }

    // 保存localRepository与历史记录
    private void saveLocalRepositoryAndHistories() {
        String localRepository = localRepositoryEditorComboWithBrowseButton.getText();
        //mavenHome 不存在是否保存 不应该保存
        localRepository = StringUtils.trimToEmpty(localRepository);
        if (StringUtils.isNotBlank(localRepository)) {
            bazelSettings.setLocalRepository(localRepository);
        }

        //获取mavenHistory添加当前mavenHome至history
        Set<String> localRepositoryHistorys = bazelSettings.getLocalRepositoryHistories();
        if (CollectionUtils.isEmpty(localRepositoryHistorys)) {
            localRepositoryHistorys = new TreeSet<>();
        }
        if (StringUtils.isNotBlank(localRepository)) {
            localRepositoryHistorys.add(localRepository);
        }
        bazelSettings.setLocalRepositoryHistories(localRepositoryHistorys);
    }

    /**
     * 获取mavenHome与历史记录
     */
    private void getMavenHomeAndHistories() {
        //获取mavenHome配置
        String mavenHome = bazelSettings.getMavenHome();
        if (StringUtils.isNotBlank(mavenHome)) {
            mavenEditorComboWithBrowseButton.setText(StringUtils.trimToEmpty(mavenHome));
        }

        //获取maven历史记录 设置
        Set<String> mavenHomeHistories = bazelSettings.getMavenHomeHistories();
        String[] histories = getHistories(mavenHomeHistories, mavenHome);
        if (ArrayUtils.isNotEmpty(histories)) {
            mavenEditorComboWithBrowseButton.setHistory(histories);
        }
    }

    /**
     * 获取localRepository与历史记录
     */
    private void getLocalRepositoryAndHistories() {
        //获取mavenHome配置
        String localRepository = bazelSettings.getLocalRepository();
        if (StringUtils.isNotBlank(localRepository)) {
            localRepositoryEditorComboWithBrowseButton.setText(StringUtils.trimToEmpty(localRepository));
        }

        //获取maven历史记录 设置
        Set<String> localRepositoryHistories = bazelSettings.getLocalRepositoryHistories();
        String[] histories = getHistories(localRepositoryHistories, localRepository);
        if (ArrayUtils.isNotEmpty(histories)) {
            localRepositoryEditorComboWithBrowseButton.setHistory(histories);
        }
    }

    /**
     * 获取bazelJdkHome与历史记录
     */
    private void getBazelJdkHomeAndHistories() {
        //获取bazelJdkHome配置
        String bazelJdkHome = bazelSettings.getBazelJdkHome();
        if (StringUtils.isNotBlank(bazelJdkHome)) {
            bazelJdkEditCombox.setText(StringUtils.trimToEmpty(bazelJdkHome));
        }

        //获取maven历史记录 设置
        Set<String> bazelJdkHomeHistories = bazelSettings.getBazelJdkHomeHistories();
        String[] histories = getHistories(bazelJdkHomeHistories, bazelJdkHome);
        if (ArrayUtils.isNotEmpty(histories)) {
            bazelJdkEditCombox.setHistory(histories);
        }
    }

    /**
     * 获取bazel的maven仓库缓存与历史记录
     */
    private void getBazelMavenRepositoryCacheAndHistories() {
        String bazelMavenRepositoryCache = bazelSettings.getBazelMavenRepositoryCache();
        if (StringUtils.isNotBlank(bazelMavenRepositoryCache)) {
            bazelMavenRepositoryCacheEditCombox.setText(StringUtils.trimToEmpty(bazelMavenRepositoryCache));
        }

        Set<String> bazelMavenRepositoryCacheHistories = bazelSettings.getBazelMavenRepositoryCacheHistories();
        String[] histories = getHistories(bazelMavenRepositoryCacheHistories, bazelMavenRepositoryCache);
        if (ArrayUtils.isNotEmpty(histories)) {
            bazelMavenRepositoryCacheEditCombox.setHistory(histories);
        }
    }

    // 保存mavenHome与历史记录
    private void saveMavenHomeAndHistories() {
        String mavenHome = mavenEditorComboWithBrowseButton.getText();
        //mavenHome 不存在是否保存 不应该保存
        mavenHome = StringUtils.trimToEmpty(mavenHome);
        if (StringUtils.isNotBlank(mavenHome)) {
            bazelSettings.setMavenHome(mavenHome);
        }

        //获取mavenHistory添加当前mavenHome至history
        Set<String> mavenHomeHistorys = bazelSettings.getMavenHomeHistories();
        if (CollectionUtils.isEmpty(mavenHomeHistorys)) {
            mavenHomeHistorys = new TreeSet<>();
        }
        if (StringUtils.isNotBlank(mavenHome)) {
            mavenHomeHistorys.add(mavenHome);
        }
        bazelSettings.setMavenHomeHistories(mavenHomeHistorys);
    }

    // 保存bazel的jdkHome与历史记录
    private void saveBazelJdkHomeAndHistories() {
        String bazelJdkHome = bazelJdkEditCombox.getText();
        //mavenHome 不存在是否保存 不应该保存
        bazelJdkHome = StringUtils.trimToEmpty(bazelJdkHome);
        if (StringUtils.isNotBlank(bazelJdkHome)) {
            bazelSettings.setBazelJdkHome(bazelJdkHome);
        }

        //获取bazelJdkHistory添加当前mavenHome至history
        Set<String> bazelJdkHomeHistories = bazelSettings.getBazelJdkHomeHistories();
        if (CollectionUtils.isEmpty(bazelJdkHomeHistories)) {
            bazelJdkHomeHistories = new TreeSet<>();
        }
        if (StringUtils.isNotBlank(bazelJdkHome)) {
            bazelJdkHomeHistories.add(bazelJdkHome);
        }
        bazelSettings.setBazelJdkHomeHistories(bazelJdkHomeHistories);
    }

    // 保存bazel的Maven仓库与历史记录 bazelMavenRepositoryCacheEditorCombox
    private void saveBazelMavenRepositoryCacheAndHistories() {
        String bazelMavenRepositoryCache = bazelMavenRepositoryCacheEditCombox.getText();
        //mavenHome 不存在是否保存 不应该保存
        bazelMavenRepositoryCache = StringUtils.trimToEmpty(bazelMavenRepositoryCache);
        if (StringUtils.isNotBlank(bazelMavenRepositoryCache)) {
            bazelSettings.setBazelMavenRepositoryCache(bazelMavenRepositoryCache);
        }

        //获取bazelJdkHistory添加当前mavenHome至history
        Set<String> bazelMavenRepositoryCacheHistories = bazelSettings.getBazelMavenRepositoryCacheHistories();
        if (CollectionUtils.isEmpty(bazelMavenRepositoryCacheHistories)) {
            bazelMavenRepositoryCacheHistories = new TreeSet<>();
        }
        if (StringUtils.isNotBlank(bazelMavenRepositoryCache)) {
            bazelMavenRepositoryCacheHistories.add(bazelMavenRepositoryCache);
        }
        bazelSettings.setBazelMavenRepositoryCacheHistories(bazelMavenRepositoryCacheHistories);
    }

    /**
     * 调整数据
     */
    private void adjustModelData() {
        if (this.url2RemoteRepositoryModelMap == null || this.url2RemoteRepositoryModelMap.size() == 0) {
            return;
        }
        ListModel model = this.jList.getModel();
        int size = model.getSize();
        Map<String, RemoteRepositoryModel> url2RemoteRepositoryModelMap2 = new LinkedHashMap<>();
        url2RemoteRepositoryModelMap2.putAll(this.url2RemoteRepositoryModelMap);

        this.url2RemoteRepositoryModelMap.clear();
        for (int i = 0; i < size; i++) {
            Object elementAt = model.getElementAt(i);
            if (elementAt == null) {
                continue;
            }
            String url = elementAt.toString();
            if (StringUtils.isBlank(url)) {
                continue;
            }
            url2RemoteRepositoryModelMap.put(url, url2RemoteRepositoryModelMap2.get(url));
        }
    }

    /**
     * 得到历史记录
     *
     * @param set
     * @param key
     * @return
     */
    private String[] getHistories(Set<String> set, String key) {
        if (CollectionUtils.isEmpty(set)) {
            return new String[0];
        }
        int size = set.size();

        String[] historys = new String[size];
        int i = 1;
        for (String str : set) {
            if (StringUtils.equals(str, key)) {
                historys[0] = str;
            } else {
                historys[i++] = str;
            }
        }
        return historys;
    }
}
