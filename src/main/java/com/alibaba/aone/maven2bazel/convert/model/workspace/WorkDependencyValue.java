package com.alibaba.aone.maven2bazel.convert.model.workspace;

import com.alibaba.aone.maven2bazel.convert.pattern.model.MavenDependencyModel;

import java.io.Serializable;

/**
 * WorkSpace的值
 */
public class WorkDependencyValue implements Serializable {
    /**
     * 版本
     */
    private String version;

    /**
     * 模块的名字
     */
    private String modulesName;

    /**
     * maven的依赖
     */
    private MavenDependencyModel mavenDependencyModel;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getModulesName() {
        return modulesName;
    }

    public void setModulesName(String modulesName) {
        this.modulesName = modulesName;
    }

    public MavenDependencyModel getMavenDependencyModel() {
        return mavenDependencyModel;
    }

    public void setMavenDependencyModel(MavenDependencyModel mavenDependencyModel) {
        this.mavenDependencyModel = mavenDependencyModel;
    }
}

