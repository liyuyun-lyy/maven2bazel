package com.alibaba.aone.maven2bazel.convert.model;

import com.alibaba.aone.maven2bazel.convert.pattern.model.MavenDependencyModel;
import org.apache.maven.model.Model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 项目结构
 */
public class ProjectStruct implements Serializable {

    /**
     * 当前Model
     */
    private Model model;

    /**
     * 当前目录
     */
    private String directoryPath;

    /**
     * 当前名
     */
    private String name;

    /**
     * 所有的父目录树，用","分隔，根目录的父目录为空
     */
    private String parentFullTreeNames;

    /**
     * 行号->依赖映射
     */
    private Map<Integer, MavenDependencyModel> lineNumber2DependencyMap;

    /**
     * 子模块的Model
     */
    private List<ProjectStruct> children;

    /**
     * lombok插件
     */
    private MavenDependencyModel lombokDependency;

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public List<ProjectStruct> getChildren() {
        return children;
    }

    public void setChildren(List<ProjectStruct> children) {
        this.children = children;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Integer, MavenDependencyModel> getLineNumber2DependencyMap() {
        return lineNumber2DependencyMap;
    }

    public void setLineNumber2DependencyMap(Map<Integer, MavenDependencyModel> lineNumber2DependencyMap) {
        this.lineNumber2DependencyMap = lineNumber2DependencyMap;
    }

    public String getParentFullTreeNames() {
        return parentFullTreeNames;
    }

    public void setParentFullTreeNames(String parentFullTreeNames) {
        this.parentFullTreeNames = parentFullTreeNames;
    }

    public MavenDependencyModel getLombokDependency() {
        return lombokDependency;
    }

    public void setLombokDependency(MavenDependencyModel lombokDependency) {
        this.lombokDependency = lombokDependency;
    }
}

