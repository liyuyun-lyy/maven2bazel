package com.alibaba.aone.maven2bazel.convert.utils;

import cn.hutool.core.io.FileUtil;
import com.alibaba.aone.maven2bazel.convert.model.ProjectStruct;
import com.alibaba.aone.maven2bazel.convert.model.workspace.WorkDependencyKey;
import com.alibaba.aone.maven2bazel.convert.model.workspace.WorkDependencyValue;
import com.alibaba.aone.maven2bazel.convert.pattern.DependencyPattern;
import com.alibaba.aone.maven2bazel.convert.pattern.model.MavenDependencyModel;
import com.alibaba.aone.maven2bazel.convert.template.bazelrc.BazelrcTemplate;
import com.alibaba.aone.maven2bazel.convert.template.build.LibraryTemplate;
import com.alibaba.aone.maven2bazel.convert.template.workspace.WorkspaceTemplate;
import com.alibaba.aone.maven2bazel.enums.Constants;
import com.alibaba.aone.maven2bazel.state.BazelSettings;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.google.protobuf.ServiceException;
import com.intellij.openapi.progress.ProgressIndicator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Dependencies2BazelUtil {
    /**
     * workspace的每一项依赖对应的映射
     */
    private static Multimap<WorkDependencyKey, WorkDependencyValue> WORK_KEY_2_VALUE_TREE_MULTIMAP =
        LinkedHashMultimap.create();

    /**
     * 重复的Maven前缀->依赖列表
     */
    private static Multimap<String, MavenDependencyModel> REPEATED_MAVEN_PREFIX_2_MAVEN_DEPENDENCY_MODEL_MAP =
        LinkedHashMultimap.create();

    private static BazelSettings bazelSettings = BazelSettings.getInstance();

    public static void dependency2Bazel(ProgressIndicator indicator, String pomDirectory) throws ServiceException {
        Map<String, Model> projectName2ModelMap = getProjectName2ModelMap(pomDirectory);
        //循环遍历得到包的类型
        //判断是否有war包
        boolean isExistWar = false;
        //是否存在SpringBoot
        boolean isExistSpringBoot = false;
        if (MapUtils.isNotEmpty(projectName2ModelMap)) {
            for (String projectName : projectName2ModelMap.keySet()) {
                Model model = projectName2ModelMap.get(projectName);

                if (model == null) {
                    continue;
                }
                String springBootMainClass = ModulesUtil.getSpringbootStartMainClassName(model);
                if (StringUtils.isNotBlank(springBootMainClass)) {
                    isExistSpringBoot = true;
                }
            }
        }

        ProjectStruct projectStruct = getProjectStruct(indicator, projectName2ModelMap, pomDirectory, null);
        System.out.println("=====================");
        //递归遍历ProjectStruct 生成WORKSPACE与BUILD文件
        // (groupId,artifactId) ->(modulesName,version,MavenDependencyModel)
        //遍历所有groupId
        if (WORK_KEY_2_VALUE_TREE_MULTIMAP != null && WORK_KEY_2_VALUE_TREE_MULTIMAP.size() > 0) {
            for (WorkDependencyKey workDependencyKey : WORK_KEY_2_VALUE_TREE_MULTIMAP.keySet()) {
                Collection<WorkDependencyValue> workDependencyValues =
                    WORK_KEY_2_VALUE_TREE_MULTIMAP.get(workDependencyKey);

                //检测版本是否相同
                String version = "";
                boolean isSame = true;
                Iterator<WorkDependencyValue> iterator1 = workDependencyValues.iterator();
                while (iterator1.hasNext()) {
                    WorkDependencyValue next = iterator1.next();
                    if (StringUtils.isBlank(version)) {
                        String version1 = next.getVersion();
                        version = StringUtils.trimToEmpty(version1);
                    } else {
                        String version1 = StringUtils.trimToEmpty(next.getVersion());
                        if (!StringUtils.equals(version1, version)) {
                            isSame = false;
                            break;
                        }
                    }
                }
                System.out.println("=========is not Same:" + JSON.toJSONString(workDependencyValues));
                Iterator<WorkDependencyValue> iterator = workDependencyValues.iterator();
                while (iterator.hasNext()) {
                    WorkDependencyValue next = iterator.next();
                    MavenDependencyModel mavenDependencyModel2 = next.getMavenDependencyModel();
                    if (mavenDependencyModel2 != null) {
                        //检测版本是否相同
                        if (!isSame) { //若不相同
                            String mavenPrefix =
                                "maven_" + StringUtils.replace(StringUtils.trimToEmpty(next.getModulesName()), "-",
                                    "_");
                            mavenDependencyModel2.setMavenPrefix(mavenPrefix);
                            REPEATED_MAVEN_PREFIX_2_MAVEN_DEPENDENCY_MODEL_MAP.put(mavenPrefix, mavenDependencyModel2);
                            ProgressIndicatorPrintUtil.println(indicator, "get maven prefix:" + mavenPrefix, 0.30);
                        } else {
                            String mavenPrefix = "maven";
                            mavenDependencyModel2.setMavenPrefix(mavenPrefix);
                            REPEATED_MAVEN_PREFIX_2_MAVEN_DEPENDENCY_MODEL_MAP.put(mavenPrefix, mavenDependencyModel2);
                            ProgressIndicatorPrintUtil.println(indicator, "get maven prefix:" + mavenPrefix, 0.30);
                            break;
                        }
                    }
                }

            }
        }
        Multimap<String, String> mavenPrefix2WorkspaceMap = TreeMultimap.create();
        //判断是否有lombok
        MavenDependencyModel lombokDependency = null;
        if (REPEATED_MAVEN_PREFIX_2_MAVEN_DEPENDENCY_MODEL_MAP != null
            && REPEATED_MAVEN_PREFIX_2_MAVEN_DEPENDENCY_MODEL_MAP.size() > 0) {
            for (String mavenPrefix : REPEATED_MAVEN_PREFIX_2_MAVEN_DEPENDENCY_MODEL_MAP.keySet()) {
                Collection<MavenDependencyModel> mavenDependencyModels =
                    REPEATED_MAVEN_PREFIX_2_MAVEN_DEPENDENCY_MODEL_MAP.get(mavenPrefix);
                String mavenRealPrefix = "";
                if (StringUtils.isBlank(mavenPrefix)) {
                    mavenRealPrefix = "maven";
                } else {
                    mavenRealPrefix = mavenPrefix;

                }
                for (MavenDependencyModel mavenDependencyModel : mavenDependencyModels) {
                    //生成workspace文件
                    String workSpace = DependencyPattern.getWorkSpace(projectName2ModelMap, mavenDependencyModel);
                    //                    System.out.println(workSpace);
                    mavenPrefix2WorkspaceMap.put(mavenRealPrefix, workSpace);
                    if (StringUtils.equals(mavenDependencyModel.getGroupId(), "org.projectlombok")
                        && StringUtils.equals(mavenDependencyModel.getArtifactId(), "lombok")) {
                        lombokDependency = mavenDependencyModel;
                    }
                }
            }
            //
            ProgressIndicatorPrintUtil.println(indicator, "get workspace start ", 0.35);
            String workspaceString = WorkspaceTemplate.getWorkspaceString(lombokDependency, projectStruct.getName(),
                mavenPrefix2WorkspaceMap, isExistSpringBoot, isExistWar);
            ProgressIndicatorPrintUtil.println(indicator, "get workspace success ", 0.56);

            //            System.out.println(workspaceString);
            //写入WORKSPACE文件
            ProgressIndicatorPrintUtil.println(indicator, "get generateWorkspaceFile start ", 0.59);
            generateWorkspaceFile(pomDirectory, workspaceString);
            ProgressIndicatorPrintUtil.println(indicator, "get generateWorkspaceFile success ", 0.60);

            //生成BUILD文件
            ProgressIndicatorPrintUtil.println(indicator, "get generateBuildFile start ", 0.60);
            generateBuildFile(projectStruct, projectName2ModelMap);
            ProgressIndicatorPrintUtil.println(indicator, "get generateBuildFile success ", 0.90);
        }
    }

    private static Map<String, Model> getProjectName2ModelMap(String pomDirectory) {
        try {
            String pomPath = pomDirectory + "/pom.xml";

            //获取当前Model
            FileInputStream fis = new FileInputStream(new File(pomPath));
            MavenXpp3Reader modlusReader = new MavenXpp3Reader();
            Model model = modlusReader.read(fis);
            if (model == null) {
                return null;
            }
            //名称
            String projectName = getProjectName(model);
            if (StringUtils.isBlank(projectName)) {
                return null;
            }
            //获取BUILD列表
            Map<String, Model> childName2ModelMap = new HashMap<>();
            childName2ModelMap.put(projectName, model);
            //获取子模块
            List<String> moduleNames = model.getModules();
            if (CollectionUtils.isEmpty(moduleNames)) {
                return childName2ModelMap;
            }
            for (String moduleName : moduleNames) {
                if (StringUtils.isBlank(moduleName)) {
                    continue;
                }
                String childModuleDirectory = pomDirectory + "/" + moduleName;
                Map<String, Model> childName2ModelMap1 = getProjectName2ModelMap(childModuleDirectory);
                if (MapUtils.isEmpty(childName2ModelMap1)) {
                    continue;
                }
                for (String name : childName2ModelMap1.keySet()) {
                    Model model1 = childName2ModelMap1.get(name);
                    if (StringUtils.isBlank(model1.getGroupId())) {
                        model1.setGroupId(model.getGroupId());
                    }
                }
                childName2ModelMap.putAll(childName2ModelMap1);
            }
            return childName2ModelMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取当前model的名称
     * 取 ArtifactId 不再取name作为名称，因为有些名称为汉字
     *
     * @param model
     * @return
     */
    private static String getProjectName(Model model) {
        return StringUtils.trimToEmpty(model.getArtifactId());
    }

    /**
     * 获取项目结构
     *
     * @param pomDirectory
     * @param parentFullTreeNames 父级所有的目录树，中间用,分割
     * @return
     */
    private static ProjectStruct getProjectStruct(ProgressIndicator indicator, Map<String, Model> projectName2ModelMap,
        String pomDirectory, String parentFullTreeNames) {

        try {
            String pomPath = pomDirectory + "/pom.xml";

            //获取当前Model
            FileInputStream fis = new FileInputStream(new File(pomPath));
            MavenXpp3Reader modlusReader = new MavenXpp3Reader();
            Model model = modlusReader.read(fis);
            ProjectStruct projectStruct = new ProjectStruct();
            if (model == null) {
                return null;
            }
            projectStruct.setModel(model);

            //所有的父目录树,中间用","分隔
            projectStruct.setDirectoryPath(pomDirectory);
            if (StringUtils.isNotBlank(parentFullTreeNames)) {
                projectStruct.setParentFullTreeNames(StringUtils.trimToEmpty(parentFullTreeNames));
            }

            //名称
            String projectName = getProjectName(model);
            projectStruct.setName(projectName);
            ProgressIndicatorPrintUtil.println(indicator, "get project" + projectName + " struct", 0.28);
            //行数->BUILD依赖的问题
            //若是pom文件则不需要查询
            Map<Integer, MavenDependencyModel> lineNumber2MavenDependencyModelMap = null;
            String packaging = model.getPackaging();
            if (StringUtils.isBlank(packaging)) {
                packaging = Constants.PackingTypeEnums.JAR.value;
            }
            if (!StringUtils.equalsIgnoreCase(StringUtils.trimToEmpty(packaging),
                StringUtils.trimToEmpty(Constants.PackingTypeEnums.POM.value))) {
                lineNumber2MavenDependencyModelMap =
                    DependencyPattern.getLineNumber2MavenDependencyModelMap(indicator, projectName2ModelMap,
                        pomDirectory);
            }
            if (MapUtils.isNotEmpty(lineNumber2MavenDependencyModelMap)) {
                projectStruct.setLineNumber2DependencyMap(lineNumber2MavenDependencyModelMap);

                for (Integer i : lineNumber2MavenDependencyModelMap.keySet()) {

                    MavenDependencyModel mavenDependencyModel = lineNumber2MavenDependencyModelMap.get(i);
                    if (mavenDependencyModel == null) {
                        continue;
                    }

                    WorkDependencyKey workDependencyKey = new WorkDependencyKey();
                    workDependencyKey.setGroupId(StringUtils.trimToEmpty(mavenDependencyModel.getGroupId()));
                    workDependencyKey.setArtifactId(StringUtils.trimToEmpty(mavenDependencyModel.getArtifactId()));

                    WorkDependencyValue workDependencyValue = new WorkDependencyValue();
                    workDependencyValue.setModulesName(StringUtils.trimToEmpty(projectName));
                    workDependencyValue.setVersion(StringUtils.trimToEmpty(mavenDependencyModel.getVersion()));
                    workDependencyValue.setMavenDependencyModel(mavenDependencyModel);
                    //获取依赖数据
                    WORK_KEY_2_VALUE_TREE_MULTIMAP.put(workDependencyKey, workDependencyValue);
                    if (StringUtils.equals(mavenDependencyModel.getGroupId(), "org.projectlombok")
                        && StringUtils.equals(mavenDependencyModel.getArtifactId(), "lombok")) {
                        projectStruct.setLombokDependency(mavenDependencyModel);
                    }

                }
            }

            //获取子模块
            List<String> moduleNames = model.getModules();

            if (CollectionUtils.isEmpty(moduleNames)) {
                return projectStruct;
            }
            List<ProjectStruct> childrenProjectList = new ArrayList<>();
            for (String moduleName : moduleNames) {
                String childModuleDirectory = pomDirectory + "/" + moduleName;
                if (StringUtils.isBlank(moduleName)) {
                    continue;
                }
                String nowTreeNames = "";
                if (StringUtils.isBlank(parentFullTreeNames)) {
                    nowTreeNames = StringUtils.trimToEmpty(moduleName);
                } else {
                    nowTreeNames =
                        StringUtils.trimToEmpty(parentFullTreeNames) + "," + StringUtils.trimToEmpty(moduleName);
                }
                ProjectStruct childProjectStruct =
                    getProjectStruct(indicator, projectName2ModelMap, childModuleDirectory, nowTreeNames);
                if (childProjectStruct == null) {
                    continue;
                }
                childrenProjectList.add(childProjectStruct);
            }
            if (CollectionUtils.isNotEmpty(childrenProjectList)) {
                projectStruct.setChildren(childrenProjectList);
            }
            return projectStruct;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 生成workspace文件
     *
     * @param pomBaseDirectory
     * @param workspaceString
     */
    private static void generateWorkspaceFile(String pomBaseDirectory, String workspaceString) {
        //生成或覆盖WORKSPACE文件
        String workspaceFilePath = pomBaseDirectory + "/WORKSPACE";
        File workspaceFile = new File(workspaceFilePath);
        boolean existWorksapceFile = FileUtil.exist(workspaceFile);
        if (existWorksapceFile) {
            workspaceFilePath = pomBaseDirectory + "/WORKSPACE_" + UUIDUtil.getUUID();
        }
        File newWorkspaceFile = FileUtil.newFile(workspaceFilePath);

        FileUtil.writeUtf8String(workspaceString, newWorkspaceFile);

        if (existWorksapceFile) {
            boolean equals = FileUtil.contentEqualsIgnoreEOL(workspaceFile, newWorkspaceFile, Charset.forName("UTF-8"));
            System.out.println("equals:" + equals);
            //若相等则删除newWorkspaceFile文件
            if (equals) {
                FileUtil.del(newWorkspaceFile);
            } else {
                //不相等则删除之前的BUILD文件，将新文件重命名为BUILD文件
                if (FileUtil.exist(workspaceFile)) {
                    FileUtil.del(workspaceFile);
                }
                FileUtil.rename(newWorkspaceFile, "WORKSPACE", true);
            }
        }
    }

    //生成BUILD文件
    private static void generateBuildFile(ProjectStruct projectStruct, Map<String, Model> projectName2ModelMap)
        throws ServiceException {
        //判断是不是根目录
        String parentFullTreeNames = projectStruct.getParentFullTreeNames();
        String directoryPath = projectStruct.getDirectoryPath();
        String projectName = projectStruct.getName();
        if (StringUtils.isBlank(parentFullTreeNames)) {
            //1.生成.bazelrc文件
            //TODO 默认使用JDK8,UTF-8进行编译，后面可以根据配置进行更新 绝对路径 by liuzhenhuan 20211119
            generateBazelrcFile(directoryPath);

            //生成根目录下的BUILD文件
            //            generateEmptyBuildFile(directoryPath);
        }
        //生成BUILD文件
        Model model = projectStruct.getModel();
        String packaging = model.getPackaging();
        //POM文件生成空的BUILD文件
        if (StringUtils.equalsIgnoreCase(StringUtils.trimToEmpty(packaging),
            StringUtils.trimToEmpty(Constants.PackingTypeEnums.POM.value))) {
            //生成空的BUILD文件
            generateEmptyBuildFile(directoryPath);
        } else if (StringUtils.isBlank(packaging) || (StringUtils.equalsIgnoreCase(StringUtils.trimToEmpty(packaging),
            StringUtils.trimToEmpty(Constants.PackingTypeEnums.JAR.value))) || (StringUtils.equalsIgnoreCase(
            StringUtils.trimToEmpty(packaging), StringUtils.trimToEmpty(Constants.PackingTypeEnums.WAR.value)))) {
            //为空即为jar包
            //生成BUILD文件
            //生成临时文件 对比新生成的文件与原文件的区别，若一致则不需要覆盖，
            String builFilePath = directoryPath + "/BUILD";
            File buildFile = new File(builFilePath);
            boolean isExsit = false;
            if (FileUtil.exist(buildFile)) {
                //若存在则生成新的文件
                builFilePath = directoryPath + "/BUILD_" + UUIDUtil.getUUID();
                isExsit = true;
            }
            File newBuildFile = FileUtil.newFile(builFilePath);

            //写入BUILD文件
            //判断是否为引用的类库,还是springboot启动类
            String pomFile = directoryPath + "/pom.xml";

            List<String> javaResources = ModulesUtil.getJavaResources(pomFile);

            List<String> customResources = ModulesUtil.getCustomResources(pomFile);

            //是引用类库
            Set<String> protobufSet = null;
            boolean containProtobuf = ModulesUtil.isContainProtobuf(pomFile);
            if (containProtobuf) {
                protobufSet = ModulesUtil.getProtobufList(pomFile);
            }
            //springboot的启动类
            String mainClass = ModulesUtil.getSpringbootStartMainClassName(pomFile);
            MavenDependencyModel lombokDependency = projectStruct.getLombokDependency();

            //获取war包
            boolean isWar = false;
            if ((StringUtils.equalsIgnoreCase(StringUtils.trimToEmpty(packaging),
                StringUtils.trimToEmpty(Constants.PackingTypeEnums.WAR.value)))) {
                isWar = true;
            }

            Set<String> buildSet = new TreeSet<>();
            Map<Integer, MavenDependencyModel> lineNumber2DependencyMap = projectStruct.getLineNumber2DependencyMap();
            if (MapUtils.isNotEmpty(lineNumber2DependencyMap))
                for (Integer line : lineNumber2DependencyMap.keySet()) {
                    MavenDependencyModel mavenDependencyModel = lineNumber2DependencyMap.get(line);
                    if (mavenDependencyModel == null) {
                        continue;
                    }
                    String build = getBuild(projectName2ModelMap, mavenDependencyModel);
                    if (StringUtils.isNotBlank(build)) {
                        buildSet.add(StringUtils.trimToEmpty(build));
                    }
                }

            //判断是否是war包

            String buildFileContent =
                LibraryTemplate.getBuildString(lombokDependency, projectName, javaResources, customResources,
                    protobufSet, buildSet, mainClass, isWar);
            System.out.println("buildFileContent:\n" + buildFileContent);
            //写入BUILD文件
            FileUtil.writeUtf8String(buildFileContent, newBuildFile);
            if (isExsit) {
                boolean equals = FileUtil.contentEqualsIgnoreEOL(buildFile, newBuildFile, Charset.forName("UTF-8"));
                System.out.println("equals:" + equals);
                //若相等则删除newBuildFile文件
                if (equals) {
                    FileUtil.del(newBuildFile);
                } else {
                    //不相等则删除之前的BUILD文件，将新文件重命名为BUILD文件
                    if (FileUtil.exist(buildFile)) {
                        FileUtil.del(buildFile);
                    }
                    FileUtil.rename(newBuildFile, "BUILD", true);
                }
            }
        } else if ((StringUtils.equalsIgnoreCase(StringUtils.trimToEmpty(packaging),
            StringUtils.trimToEmpty(Constants.PackingTypeEnums.WAR.value)))) {
            //war包

        }

        //查看子项目
        List<ProjectStruct> children = projectStruct.getChildren();
        if (CollectionUtils.isNotEmpty(children)) {
            for (ProjectStruct childProjectStruct : children) {

                generateBuildFile(childProjectStruct, projectName2ModelMap);
            }
        }

    }

    private static boolean generateBazelrcFile(String pomBaseDirectory) throws ServiceException {
        //生成.bazelrc文件
        String bazelrcFilePath = pomBaseDirectory + "/.bazelrc";

        boolean bazelrcFileExsit = false;
        File bazelrcFile = new File(bazelrcFilePath);

        if (FileUtil.exist(bazelrcFile)) {
            bazelrcFilePath = pomBaseDirectory + "/.bazelrc" + "_" + UUIDUtil.getUUID();
            bazelrcFileExsit = true;
        }
        File newBazelrcFile = FileUtil.newFile(bazelrcFilePath);

        String jdkHome = bazelSettings.getBazelJdkHome();
        String bazelCacheDirectory = bazelSettings.getBazelMavenRepositoryCache();
        String bazelrcString = BazelrcTemplate.getBazelrcString(jdkHome, bazelCacheDirectory);
        if (StringUtils.isBlank(bazelrcString)) {
            throw new ServiceException("generate .bazelrc fail");
        }
        FileUtil.writeUtf8String(bazelrcString, newBazelrcFile);
        //不存在 则不需要覆盖
        if (!bazelrcFileExsit) {
            return true;
        }
        //比较是否相等
        boolean equals = FileUtil.contentEqualsIgnoreEOL(bazelrcFile, newBazelrcFile, Charset.forName("UTF-8"));
        //若相等则删除newBuildFile文件
        if (equals) {
            FileUtil.del(newBazelrcFile);
            return true;
        }
        //不相等则删除之前的BUILD文件，将新文件重命名为BUILD文件
        FileUtil.rename(newBazelrcFile, ".bazelrc", true);
        return true;
    }

    //生成空的BUILD文件
    private static void generateEmptyBuildFile(String directoryPath) {
        //生成根目录下的BUILD文件
        String rootBuildFilePath = directoryPath + "/BUILD";
        if (!FileUtil.exist(rootBuildFilePath)) {
            FileUtil.newFile(rootBuildFilePath);
            FileUtil.writeUtf8String("", rootBuildFilePath);
        }
    }

    private static String getBuild(Map<String, Model> childName2ModelMap, MavenDependencyModel mavenDependencyModel) {

        String scope = mavenDependencyModel.getScope();

        String groupId = mavenDependencyModel.getGroupId();
        String artifactId = mavenDependencyModel.getArtifactId();

        String mavenPrefix = mavenDependencyModel.getMavenPrefix();
        if (StringUtils.isBlank(mavenPrefix)) {
            mavenPrefix = "maven";
        } else {
            mavenPrefix = StringUtils.trimToEmpty(mavenPrefix);
        }

        if (StringUtils.equals(scope, "runtime")) {
            return StringUtils.EMPTY;
        }
        if (MapUtils.isNotEmpty(childName2ModelMap)) {
            //若groupId与artifactId相等 则直接返回路径
            for (String name : childName2ModelMap.keySet()) {
                Model model = childName2ModelMap.get(name);
                if (StringUtils.equals(StringUtils.trim(model.getGroupId()), StringUtils.trim(groupId))
                    && StringUtils.equals(StringUtils.trim(model.getArtifactId()), StringUtils.trim(artifactId))) {
                    return "\"//" + name + "\",";
                }
            }
        }

        // groupId 的. -都使用_替代
        String groupId2 = StringUtils.trim(groupId);
        groupId2 = StringUtils.replace(groupId2, ".", "_");
        groupId2 = StringUtils.replace(groupId2, "-", "_");

        // artifactId 的. -都使用_替代
        String artifactId2 = StringUtils.trim(artifactId);
        artifactId2 = StringUtils.replace(artifactId2, ".", "_");
        artifactId2 = StringUtils.replace(artifactId2, "-", "_");

        StringBuilder sb = new StringBuilder();
        sb.append("\"@" + mavenPrefix + "//:").append(groupId2).append("_").append(artifactId2).append("\"")
            .append(",");
        return sb.toString();
    }
}
