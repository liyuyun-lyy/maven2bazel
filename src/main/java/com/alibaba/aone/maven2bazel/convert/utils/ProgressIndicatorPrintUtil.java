package com.alibaba.aone.maven2bazel.convert.utils;

import com.intellij.openapi.progress.ProgressIndicator;
import org.apache.commons.lang3.StringUtils;

public class ProgressIndicatorPrintUtil {
    public static void println(ProgressIndicator indicator, String text) {
        println(indicator, text, null);
    }

    /**
     * 打印进度条
     *
     * @param indicator
     * @param text
     * @param fraction
     */
    public static void println(ProgressIndicator indicator, String text, Double fraction) {
        if (indicator == null) {
            return;
        }
        if (StringUtils.isNotBlank(text)) {
            System.out.println(StringUtils.trimToEmpty(text));
            indicator.setText(StringUtils.trimToEmpty(text));
        }
        if (fraction != null) {
            indicator.setFraction(fraction);
            System.out.println(fraction);
        }
    }
}
