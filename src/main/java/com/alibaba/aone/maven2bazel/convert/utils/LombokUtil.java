package com.alibaba.aone.maven2bazel.convert.utils;

import com.alibaba.aone.maven2bazel.convert.model.LombokModel;

import java.util.HashMap;
import java.util.Map;

/**
 * lombok的工具类
 */
public class LombokUtil {

    private static Map<String, LombokModel> version2LombokModelMap = new HashMap<>();

    public static LombokModel getLombokModel(String version) {
        LombokModel lombokModel = version2LombokModelMap.get(version);
        if (lombokModel == null) {
            return version2LombokModelMap.get("1.18.22");
        }
        return lombokModel;
    }

    static {

        //1.18.14
        String url_1_18_14 = "https://repo1.maven.org/maven2/org/projectlombok/lombok/1.18.14/lombok-1.18.14.jar";
        String sha256_1_18_14 = "32b9e85b9d10bdf9287d16ff4ab1b21507bb9ebb5aebb8d78dddc8c9bbbd7d17";
        version2LombokModelMap.put("1.18.14", new LombokModel(url_1_18_14, sha256_1_18_14));

        //1.18.16
        String url_1_18_16 = "https://repo1.maven.org/maven2/org/projectlombok/lombok/1.18.16/lombok-1.18.16.jar";
        String sha256_1_18_16 = "7206cbbfd6efd5e85bceff29545633645650be58d58910a23b0d4835fbd15ed7";
        version2LombokModelMap.put("1.18.16", new LombokModel(url_1_18_16, sha256_1_18_16));

        //1.18.18
        String url_1_18_18 = "https://repo1.maven.org/maven2/org/projectlombok/lombok/1.18.18/lombok-1.18.18.jar";
        String sha256_1_18_18 = "601ec46206e0f9cac2c0583b3350e79f095419c395e991c761640f929038e9cc";
        version2LombokModelMap.put("1.18.18", new LombokModel(url_1_18_18, sha256_1_18_18));

        //1.18.20
        String url_1_18_20 = "https://repo1.maven.org/maven2/org/projectlombok/lombok/1.18.20/lombok-1.18.20.jar";
        String sha256_1_18_20 = "ce947be6c2fbe759fbbe8ef3b42b6825f814c98c8853f1013f2d9630cedf74b0";
        version2LombokModelMap.put("1.18.20", new LombokModel(url_1_18_20, sha256_1_18_20));

        //1.18.22
        String url_1_18_22 = "https://repo1.maven.org/maven2/org/projectlombok/lombok/1.18.22/lombok-1.18.22.jar";
        String sha256_1_18_22 = "ecef1581411d7a82cc04281667ee0bac5d7c0a5aae74cfc38430396c91c31831";
        version2LombokModelMap.put("1.18.22", new LombokModel(url_1_18_22, sha256_1_18_22));
    }
}

