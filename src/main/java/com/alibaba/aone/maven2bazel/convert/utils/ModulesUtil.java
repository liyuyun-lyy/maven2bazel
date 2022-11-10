package com.alibaba.aone.maven2bazel.convert.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Resource;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ModulesUtil {

    /**
     * 得到springboot的启动class类名
     *
     * @param pomFile
     */
    public static String getSpringbootStartMainClassName(String pomFile) {
        Model mavenModel = getMavenModel(pomFile);
        return getSpringbootStartMainClassName(mavenModel);
    }

    /**
     * 得到springboot的启动class类名
     *
     * @param model
     */
    public static String getSpringbootStartMainClassName(Model model) {
        Plugin plugin = getSpringbootStartMainPlugin(model);
        if (plugin == null) {
            return null;
        }
        Object configuration = plugin.getConfiguration();
        if (configuration == null) {
            return null;
        }
        String jsonString = JSON.toJSONString(configuration);
        if (StringUtils.isBlank(jsonString)) {
            return null;
        }
        JSONObject jsonObject = JSON.parseObject(jsonString);
        if (jsonObject == null) {
            return null;
        }

        JSONArray childrenJsonArray = jsonObject.getJSONArray("children");
        if (childrenJsonArray == null || childrenJsonArray.size() == 0) {
            return null;
        }

        for (int i = 0; i < childrenJsonArray.size(); i++) {
            JSONObject jsonObject2 = childrenJsonArray.getJSONObject(i);
            if (StringUtils.equals(StringUtils.trim(jsonObject2.getString("name")), "mainClass")) {
                return StringUtils.trim(jsonObject2.getString("value"));
            }
        }

        return null;
    }

    /**
     * 得到springboot的启动插件
     *
     * @param model
     */
    public static Plugin getSpringbootStartMainPlugin(Model model) {
        List<Plugin> plugins = getPlugins(model);
        if (CollectionUtils.isEmpty(plugins)) {
            return null;
        }
        for (Plugin plugin : plugins) {
            if (!StringUtils.equals(StringUtils.trim(plugin.getGroupId()), "org.springframework.boot")) {
                continue;
            }
            if (!StringUtils.equals(StringUtils.trim(plugin.getArtifactId()), "spring-boot-maven-plugin")) {
                continue;
            }
            return plugin;
        }
        return null;
    }

    /**
     * 获取maven的插件列表
     *
     * @param pomFile
     * @return
     */
    public static List<Plugin> getPlugins(Model pomFile) {
        try {
            Build build = getBuild(pomFile);
            ;
            if (build == null) {
                return null;
            }
            List<Plugin> plugins = build.getPlugins();
            return plugins;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取maven的build
     *
     * @param model
     * @return
     */
    public static Build getBuild(Model model) {
        try {
            Build build = model.getBuild();
            return build;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * TODO 暂时返回统一的src/main/java/下的文件
     * 得到Java的资源文件
     *
     * @param pomFile
     * @return
     */
    public static List<String> getJavaResources(String pomFile) {
        String javaResourceRoot = "src/main/java";
        List<String> resourceList = new ArrayList<String>();

        resourceList.add(javaResourceRoot + "/**/*.java");
        return resourceList;
    }

    /**
     * TODO 暂时返回统一的src/main/resources/下的文件
     * 得到Java的资源文件
     *
     * @param pomFile
     * @return
     */
    public static List<String> getCustomResources(String pomFile) {
        String resourceRoot = "src/main/resources";

        List<String> resourceList = new ArrayList<String>();
        List<Resource> resources = getResources(pomFile);
        if (CollectionUtils.isNotEmpty(resources)) {
            for (Resource resource : resources) {
                if (StringUtils.equals(resourceRoot, resource.getDirectory())) {
                    List<String> includes = resource.getIncludes();
                    for (String include : includes) {
                        resourceList.add(resourceRoot + "/" + include);
                    }
                }
            }
        }
        if (CollectionUtils.isEmpty(resourceList)) {
            resourceList.add(resourceRoot + "/**");
        }
        return resourceList;
    }

    /**
     * 获取maven的插件列表
     *
     * @param pomFile
     * @return
     */
    public static List<Resource> getResources(String pomFile) {
        try {
            Model mavenModel = getMavenModel(pomFile);
            return getResources(mavenModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取maven的model
     *
     * @param pomFile
     * @return
     */
    public static Model getMavenModel(String pomFile) {
        try {
            FileInputStream fis = new FileInputStream(new File(pomFile));
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(fis);

            return model;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取maven的插件列表
     *
     * @param model
     * @return
     */
    public static List<Resource> getResources(Model model) {
        try {
            Build build = getBuild(model);
            if (build == null) {
                return null;
            }
            List<Resource> resources = build.getResources();
            return resources;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * TODO 目前只是强制是否存在资源文件src/main/proto的声明
     * 后期会增加个proto文件的列表判断 by liuzhenhuan 20210924 add
     * 判断是否存在protobuf
     *
     * @param pomFile
     * @return
     */
    public static boolean isContainProtobuf(String pomFile) {
        String resourceRoot = "src/main/proto";
        List<Resource> resources = getResources(pomFile);
        if (CollectionUtils.isNotEmpty(resources)) {
            for (Resource resource : resources) {
                if (StringUtils.equals(resourceRoot, resource.getDirectory())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * TODO 目前只是强制是否存在资源文件src/main/proto的声明
     * 后期会增加个proto文件的列表判断 by liuzhenhuan 20210924 add
     * 判断是否存在protobuf
     *
     * @param pomFile
     * @return
     */
    public static Set<String> getProtobufList(String pomFile) {
        String resourceRoot = "src/main/proto";
        List<Resource> resources = getResources(pomFile);
        Set<String> set = new TreeSet<String>();

        if (CollectionUtils.isNotEmpty(resources)) {
            for (Resource resource : resources) {
                if (StringUtils.equals(resourceRoot, resource.getDirectory())) {
                    //TODO 获取文件夹下面的所有文件
                    String substringBefore = StringUtils.substringBefore(pomFile, "pom.xml");
                    String path = substringBefore + resourceRoot;
                    String[] allFilePathInDirectory = PathUtils.getAllFilePathInDirectory(path, ".proto");
                    if (ArrayUtils.isNotEmpty(allFilePathInDirectory)) {
                        for (String path2 : allFilePathInDirectory) {
                            set.add(resourceRoot + "/" + StringUtils.trim(path2));
                        }

                    }
                }
            }
        }
        return set;
    }
}
