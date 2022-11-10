package com.alibaba.aone.maven2bazel.convert.utils;

import cn.hutool.core.lang.UUID;

public class UUIDUtil {
    public static String getUUID() {
        return UUID.randomUUID().toString(true);
    }
}
