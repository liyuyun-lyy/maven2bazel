package com.alibaba.aone.maven2bazel.convert.template.bazelrc;

import com.alibaba.aone.maven2bazel.state.BazelSettings;
import org.apache.commons.lang3.StringUtils;

/**
 * build --define=ABSOLUTE_JAVABASE=/usr/local/jdk1.8.0_261
 * #build --define=ABSOLUTE_JAVABASE=/home/liuzhenhuan/jdk/jdk-15
 * build --javabase=@bazel_tools//tools/jdk:absolute_javabase
 * build --host_javabase=@bazel_tools//tools/jdk:absolute_javabase
 * build --java_toolchain=@bazel_tools//tools/jdk:toolchain_vanilla
 * build --host_java_toolchain=@bazel_tools//tools/jdk:toolchain_vanilla
 * <p>
 * # 使用jdk8进行编译
 * build --javacopt="-source 8 -target 8 -encoding UTF-8"
 * <p>
 * build --incompatible_restrict_string_escapes=false
 * <p>
 * # 自定义的maven插件本地插件存储目录
 * #build --repo_env=COURSIER_CACHE=/home/liuzhenhuan/bazel_cache
 * .bazelrc文件生成
 */
public class BazelrcTemplate {

    private static BazelSettings bazelSettings = BazelSettings.getInstance();

    public static String getBazelrcString(String jdkHome, String bazelCacheDirectory) {
        StringBuilder sb = new StringBuilder();
        sb.append("build --define=ABSOLUTE_JAVABASE=");
        if (StringUtils.isNotBlank(jdkHome)) {
            sb.append(StringUtils.trimToEmpty(jdkHome)).append("\n");
        }

        sb.append("build --javabase=@bazel_tools//tools/jdk:absolute_javabase\n");
        sb.append("build --host_javabase=@bazel_tools//tools/jdk:absolute_javabase\n");
        sb.append("build --java_toolchain=@bazel_tools//tools/jdk:toolchain_vanilla\n");
        sb.append("build --host_java_toolchain=@bazel_tools//tools/jdk:toolchain_vanilla\n");
        //build --javacopt="-source 8 -target 8 -encoding UTF-8"
        //使用jdk8进行编译
        String languageLevel = bazelSettings.getLanguageLevel();
        if (StringUtils.isBlank(languageLevel)) {
            languageLevel = "8";
        }
        languageLevel = StringUtils.trimToEmpty(languageLevel);

        //编码
        String encoding = bazelSettings.getEncoding();
        if (StringUtils.isBlank(encoding)) {
            encoding = "UTF-8";
        }
        encoding = StringUtils.trimToEmpty(encoding);

        sb.append("build --javacopt=\"-source ");
        sb.append(languageLevel);
        sb.append(" ");
        sb.append("-target ");
        sb.append(languageLevel);
        sb.append(" ");
        sb.append("-encoding ");
        sb.append(encoding);

        sb.append("\"");
        sb.append("\n");

        sb.append("build --incompatible_restrict_string_escapes=false");

        //自定义的maven插件本地插件存储目录
        if (StringUtils.isNotBlank(bazelCacheDirectory)) {
            sb.append("\n");
            sb.append("build --repo_env=COURSIER_CACHE=");
            sb.append(StringUtils.trimToEmpty(bazelCacheDirectory));
        }

        return sb.toString();
    }
}

