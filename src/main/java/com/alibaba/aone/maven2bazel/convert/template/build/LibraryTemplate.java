package com.alibaba.aone.maven2bazel.convert.template.build;

import com.alibaba.aone.maven2bazel.convert.global.Global;
import com.alibaba.aone.maven2bazel.convert.pattern.model.MavenDependencyModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;

public class LibraryTemplate {

    /**
     * 获取build文件的内容
     *
     * @param libraryName
     * @param javaSourceList
     * @param customSourceList
     * @return
     */
    public static String getBuildString(MavenDependencyModel lombokDependency, String libraryName,
        List<String> javaSourceList, List<String> customSourceList, Set<String> protobufSet, Set<String> mavenSet,
        String mainClass, boolean isWar) {
        StringBuilder sb = new StringBuilder();

        //加载需要的基础组件
        //判断proto的文件是否为空
        boolean protobufListEmpty = CollectionUtils.isEmpty(protobufSet);
        if (protobufListEmpty) {
            sb.append(
                "load(\"@rules_java//java:defs.bzl\", \"java_binary\", \"java_library\", \"java_runtime\", \"java_test\")");
        } else {
            sb.append(
                "load(\"@rules_java//java:defs.bzl\", \"java_binary\", \"java_library\", \"java_proto_library\", \"java_runtime\", \"java_test\")");
            sb.append("\n");
            sb.append("load(\"@rules_proto//proto:defs.bzl\", \"proto_lang_toolchain\", \"proto_library\")");
        }

        //若是springboot启动类则加载springboot插件
        if (StringUtils.isNotBlank(mainClass)) {
            sb.append("\n").append("load(\"@rules_spring//springboot:springboot.bzl\", \"springboot\")");

            //mavenSet增加"@maven//:org_springframework_boot_spring_boot_loader",
            mavenSet.add("\"@maven//:org_springframework_boot_spring_boot_loader\",");
        }

        //判断是否是war
        if (isWar) {
            sb.append("\n");
            sb.append("load(\"@io_bazel_rules_java_war//java_war:defs.bzl\", \"java_war\", \"war\")");
        }

        sb.append("\n\n");

        //使类库可见
        sb.append("package(default_visibility = [\"//visibility:public\"])");
        sb.append("\n\n");

        //是否加载lombook
        if (lombokDependency != null) {
            sb.append("java_plugin(\n" + "    name = \"lombok_edge_basic_plugin\",\n" + "    generates_api = True,\n"
                + "    processor_class = \"lombok.launch.AnnotationProcessorHider$AnnotationProcessor\",\n"
                + "    visibility = [\"//visibility:public\"],\n" + "    deps = [\"@lombok_edge//jar\"],\n" + ")\n\n"
                + "java_library(\n" + "    name = \"lombok_basic_edge\",\n"
                + "    exported_plugins = [\":lombok_edge_basic_plugin\"],\n"
                + "    visibility = [\"//visibility:public\"],\n" + "    exports = [\"@lombok_edge//jar\"],\n" + ")");
            sb.append("\n\n");
        }

        //判断是否是proto文件，若是则加入基本的protobuf的引入
        //protobuf文件编译为java源文件之后的名字
        String protobufCompileName = "";
        if (!protobufListEmpty) {
            //添加protobuf的proto文件
            sb.append("proto_library(").append("\n");
            sb.append(Global.FOUR_BLANK_CHARACTER);
            String protobufLibraryName = StringUtils.replace(libraryName, "-", "_") + "_proto";

            sb.append("name = \"").append(protobufLibraryName).append("\"").append(",").append("\n");

            //加载源文件
            sb.append(Global.FOUR_BLANK_CHARACTER);
            sb.append("srcs = [");
            sb.append(getBasicListString(protobufSet));

            sb.append("],").append("\n");
            sb.append(")").append("\n");

            sb.append("\n");
            //加载proto的编译之后的文件
            //添加基本类库
            sb.append("java_proto_library(").append("\n");
            sb.append(Global.FOUR_BLANK_CHARACTER);
            protobufCompileName = protobufLibraryName + "_compile";

            sb.append("name = \"").append(protobufCompileName).append("\"").append(",").append("\n");

            //加载源文件
            sb.append(Global.FOUR_BLANK_CHARACTER);
            sb.append("deps = [");
            sb.append('\n');
            sb.append(Global.FOUR_BLANK_CHARACTER);
            sb.append(Global.FOUR_BLANK_CHARACTER);
            sb.append("\":" + protobufLibraryName + "\",");
            sb.append('\n');
            sb.append(Global.FOUR_BLANK_CHARACTER);

            sb.append("],").append("\n");
            sb.append(")").append("\n");
            sb.append("\n");
        }

        //添加基本类库
        //是否是war包
        if (isWar) {
            sb.append("java_war(").append("\n");
        } else {
            sb.append("java_library(").append("\n");
        }

        sb.append(Global.FOUR_BLANK_CHARACTER);

        //不是springboot启动类
        if (StringUtils.isBlank(mainClass)) {
            sb.append("name = \"").append(libraryName).append("\"").append(",").append("\n");
        } else {
            sb.append("name = \"").append(libraryName).append("-library").append("\"").append(",").append("\n");
        }

        //加载源文件
        sb.append(Global.FOUR_BLANK_CHARACTER);
        if (isWar) {
            sb.append("java_srcs = glob([");
        } else {
            sb.append("srcs = glob([");
        }

        sb.append(getBasicListString(javaSourceList));

        sb.append("]),").append("\n");

        //加载资源文件
        sb.append(Global.FOUR_BLANK_CHARACTER);
        sb.append("resources = glob([");
        sb.append(getBasicListString(customSourceList));

        sb.append("]),").append("\n");

        //若是war包 加载 wab_app_dir
        if (isWar) {
            sb.append(Global.FOUR_BLANK_CHARACTER);
            sb.append("web_app_dir = \"src/main/webapp\"").append(",").append("\n");
        }

        //加载maven依赖
        sb.append(Global.FOUR_BLANK_CHARACTER);
        sb.append("deps = [");
        if (!protobufListEmpty) {
            sb.append("\n").append(Global.FOUR_BLANK_CHARACTER).append(Global.FOUR_BLANK_CHARACTER).append("\"")
                .append(":").append(protobufCompileName).append("\"").append(",");
        }
        if (lombokDependency != null) {
            sb.append("\n").append(Global.FOUR_BLANK_CHARACTER).append(Global.FOUR_BLANK_CHARACTER).append("\"")
                .append(":").append("lombok_basic_edge").append("\"").append(",");
        }
        sb.append(getMavenListString(mavenSet));

        sb.append(")");

        if (StringUtils.isNotBlank(mainClass)) {
            sb.append("\n");
            sb.append("\n");
            //添加基本类库
            sb.append("springboot(").append("\n");
            sb.append(Global.FOUR_BLANK_CHARACTER);
            sb.append("name = \"").append(libraryName).append("\"").append(",").append("\n");
            //加载源文件
            sb.append(Global.FOUR_BLANK_CHARACTER);
            sb.append("boot_app_class = ").append("\"").append(mainClass).append("\"").append(",").append("\n");

            sb.append(Global.FOUR_BLANK_CHARACTER).append("java_library = ").append("\"").append(":")
                .append(libraryName).append("-library").append("\"").append(",").append("\n");

            sb.append(")");
        }

        //判断是否是war包
        if (isWar) {

        }

        sb.append("\n");
        return sb.toString();
    }

    /**
     * 生成基本的编译内容
     *
     * @param set
     * @return
     */
    private static String getBasicListString(Set<String> set) {
        StringBuilder sb = new StringBuilder();
        for (String source : set) {
            sb.append("\n").append(Global.FOUR_BLANK_CHARACTER).append(Global.FOUR_BLANK_CHARACTER).append("\"")
                .append(source).append("\"").append(",");
        }
        sb.append("\n");
        sb.append(Global.FOUR_BLANK_CHARACTER);
        return sb.toString();
    }

    /**
     * 生成基本的编译内容
     *
     * @param list
     * @return
     */
    private static String getBasicListString(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String source : list) {
            sb.append("\n").append(Global.FOUR_BLANK_CHARACTER).append(Global.FOUR_BLANK_CHARACTER).append("\"")
                .append(source).append("\"").append(",");
        }
        sb.append("\n");
        sb.append(Global.FOUR_BLANK_CHARACTER);
        return sb.toString();
    }

    /**
     * 生成maven的文件
     *
     * @param set
     * @return
     */
    private static String getMavenListString(Set<String> set) {
        StringBuilder sb = new StringBuilder();
        for (String source : set) {
            sb.append("\n").append(Global.FOUR_BLANK_CHARACTER).append(Global.FOUR_BLANK_CHARACTER).append(source);
        }
        sb.append("\n");
        sb.append(Global.FOUR_BLANK_CHARACTER);
        sb.append("],").append("\n");
        return sb.toString();
    }
}
