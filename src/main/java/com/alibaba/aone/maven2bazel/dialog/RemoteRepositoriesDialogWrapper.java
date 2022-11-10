package com.alibaba.aone.maven2bazel.dialog;

import com.alibaba.aone.maven2bazel.enums.Constants;
import com.alibaba.aone.maven2bazel.model.RemoteRepositoryModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.UI;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import java.util.Map;
import java.util.function.Supplier;

public class RemoteRepositoriesDialogWrapper extends DialogWrapper {

    private Project project;

    /**
     * bazel的远程maven仓库的对话框弹出类型 add-新加,edit编辑
     */
    private Constants.RemoteRepositoriesTypeEnums typeEnums;

    /**
     * 远程仓库配置信息
     */
    private RemoteRepositoryModel remoteRepositoryModel;

    /**
     * 选择框
     */
    private JList jList;

    /**
     * url输入框是否正常
     */
    private boolean urlTextSuccess = false;

    /**
     * url输入框
     */
    private JTextField urlTextField;

    /**
     * url的校验
     */
    private ComponentValidator urlComponentValidator;

    /**
     * 选择的url
     */
    private String selectUrl = "";

    private Map<String, RemoteRepositoryModel> url2RemoteRepositoryModelMap;

    public RemoteRepositoriesDialogWrapper(Project project, Constants.RemoteRepositoriesTypeEnums typeEnums,
        JList jList, Map<String, RemoteRepositoryModel> url2RemoteRepositoryModelMap) {
        super(true); // use current window as parent
        this.project = project;
        this.typeEnums = typeEnums;
        this.jList = jList;
        this.url2RemoteRepositoryModelMap = url2RemoteRepositoryModelMap;

        String title = "";
        if (StringUtils.equalsIgnoreCase(this.typeEnums.value, Constants.RemoteRepositoriesTypeEnums.ADD.value)) {
            title = "New";
        } else {
            title = "Edit";
            Object selectedValue = jList.getSelectedValue();
            System.out.println(selectedValue);
            if (selectedValue != null) {
                this.selectUrl = (String)selectedValue;
            }
        }
        setTitle(title);
        init();
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel() {
        getOKAction().setEnabled(false);
        int width = 30;
        //url的输入框
        urlTextField = new JTextField(width);
        validatorJTextFiled(urlTextField, "url");

        //若是编辑
        if (StringUtils.isNotBlank(selectUrl)) {
            if ((url2RemoteRepositoryModelMap != null) && (!url2RemoteRepositoryModelMap.isEmpty())) {
                for (String url : url2RemoteRepositoryModelMap.keySet()) {
                    if (StringUtils.isBlank(url)) {
                        continue;
                    }
                    RemoteRepositoryModel remoteRepositoryModel = url2RemoteRepositoryModelMap.get(url);
                    if (remoteRepositoryModel == null) {
                        continue;
                    }
                    if (StringUtils.equals(StringUtils.trimToEmpty(remoteRepositoryModel.getUrl()),
                        StringUtils.trimToEmpty(selectUrl))) {
                        urlTextField.setText(StringUtils.trimToEmpty(remoteRepositoryModel.getUrl()));
                        break;
                    }
                }
            }
        }

        JPanel p = UI.PanelFactory.grid().add(UI.PanelFactory.panel(urlTextField).withLabel("url:")).createPanel();
        return p;
    }

    /**
     * 校验输入框非空
     *
     * @param textField
     * @param name
     */
    private void validatorJTextFiled(JTextField textField, String name) {
        String MESSAGE = "The " + name + " is not empty!";
        ComponentValidator componentValidator =
            new ComponentValidator(project).withValidator(new Supplier<ValidationInfo>() {
                @Override
                public ValidationInfo get() {
                    String pt = textField.getText();
                    if (StringUtils.isBlank(pt)) {
                        return new ValidationInfo(MESSAGE, textField);
                    } else {
                        if (StringUtils.equalsIgnoreCase(name, "url")) {
                            urlTextSuccess = true;
                        }
                        if (urlTextSuccess) {
                            getOKAction().setEnabled(true);
                        }
                    }
                    return null;
                }
            }).installOn(textField);

        componentValidator.updateInfo(new ValidationInfo(MESSAGE, textField));
        if (StringUtils.equalsIgnoreCase(name, "url")) {
            urlComponentValidator = componentValidator;
        }

        textField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                ComponentValidator.getInstance(textField).ifPresent(v -> v.revalidate());
            }
        });
    }

    @Override
    protected void doOKAction() {

        //获取内容
        remoteRepositoryModel = new RemoteRepositoryModel();

        //url
        String url = urlTextField.getText();
        if (StringUtils.equalsIgnoreCase(this.typeEnums.value, Constants.RemoteRepositoriesTypeEnums.ADD.value) && (
            url2RemoteRepositoryModelMap != null) && (!url2RemoteRepositoryModelMap.isEmpty())) {
            for (String url1 : url2RemoteRepositoryModelMap.keySet()) {
                if (StringUtils.isBlank(url1)) {
                    continue;
                }
                RemoteRepositoryModel remoteRepositoryModel = url2RemoteRepositoryModelMap.get(url1);
                if (remoteRepositoryModel == null) {
                    continue;
                }

                String url2 = remoteRepositoryModel.getUrl();
                if (StringUtils.equalsIgnoreCase(StringUtils.trimToEmpty(url), StringUtils.trimToEmpty(url2))) {
                    urlComponentValidator.updateInfo(new ValidationInfo("The URL cannot be duplicated!", urlTextField));
                    return;
                }
            }
        } else if (StringUtils.equalsIgnoreCase(this.typeEnums.value, Constants.RemoteRepositoriesTypeEnums.EDIT.value)
            && (url2RemoteRepositoryModelMap != null) && (!url2RemoteRepositoryModelMap.isEmpty())) {
            for (String url1 : url2RemoteRepositoryModelMap.keySet()) {
                if (StringUtils.isBlank(url1)) {
                    continue;
                }
                RemoteRepositoryModel remoteRepositoryModel = url2RemoteRepositoryModelMap.get(url1);
                if (remoteRepositoryModel == null) {
                    continue;
                }

                String oldUrl = remoteRepositoryModel.getUrl();

                if (StringUtils.equals(StringUtils.trimToEmpty(oldUrl), StringUtils.trimToEmpty(selectUrl))) {
                    continue;
                }

                if (StringUtils.equalsIgnoreCase(StringUtils.trimToEmpty(url), StringUtils.trimToEmpty(oldUrl))) {
                    urlComponentValidator.updateInfo(new ValidationInfo("The URL cannot be duplicated!", urlTextField));
                    return;
                }
            }
        }
        remoteRepositoryModel.setUrl(StringUtils.trimToEmpty(url));
        super.doOKAction();
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Constants.RemoteRepositoriesTypeEnums getTypeEnums() {
        return typeEnums;
    }

    public void setTypeEnums(Constants.RemoteRepositoriesTypeEnums typeEnums) {
        this.typeEnums = typeEnums;
    }

    public RemoteRepositoryModel getRemoteRepositoryModel() {
        return remoteRepositoryModel;
    }

    public void setRemoteRepositoryModel(RemoteRepositoryModel remoteRepositoryModel) {
        this.remoteRepositoryModel = remoteRepositoryModel;
    }

    public JList getjList() {
        return jList;
    }

    public void setjList(JList jList) {
        this.jList = jList;
    }

    public boolean isUrlTextSuccess() {
        return urlTextSuccess;
    }

    public void setUrlTextSuccess(boolean urlTextSuccess) {
        this.urlTextSuccess = urlTextSuccess;
    }

    public JTextField getUrlTextField() {
        return urlTextField;
    }

    public void setUrlTextField(JTextField urlTextField) {
        this.urlTextField = urlTextField;
    }

    public ComponentValidator getUrlComponentValidator() {
        return urlComponentValidator;
    }

    public void setUrlComponentValidator(ComponentValidator urlComponentValidator) {
        this.urlComponentValidator = urlComponentValidator;
    }

    public String getSelectUrl() {
        return selectUrl;
    }

    public void setSelectUrl(String selectUrl) {
        this.selectUrl = selectUrl;
    }

    public Map<String, RemoteRepositoryModel> getUrl2RemoteRepositoryModelMap() {
        return url2RemoteRepositoryModelMap;
    }

    public void setUrl2RemoteRepositoryModelMap(Map<String, RemoteRepositoryModel> url2RemoteRepositoryModelMap) {
        this.url2RemoteRepositoryModelMap = url2RemoteRepositoryModelMap;
    }
}
