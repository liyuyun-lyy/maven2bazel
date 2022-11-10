package com.alibaba.aone.maven2bazel.convert.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class PathUtils {
    /**
     * 得到某个目录下的所有文件名称
     *
     * @param directoryPath
     * @param fileSuffix    文件后缀
     * @return
     */
    public static String[] getAllFilePathInDirectory(String directoryPath, final String fileSuffix) {
        try {
            File file = new File(directoryPath);
            if (!file.exists()) {
                System.out.println("getAllFilePathInDirectory file:{} is not exists" + file.getAbsoluteFile());
                return null;
            }
            if (!file.isDirectory()) {
                System.out.println("getAllFilePathInDirectory file:{} is not a directory" + file.getAbsoluteFile());
                return null;
            }
            //查询当前目录下的所有文件
            String[] allFilePathInDirectoryDirect = getAllFilePathInDirectoryDirect(directoryPath, fileSuffix);

            List<String> resultList = new ArrayList<String>();
            if (allFilePathInDirectoryDirect != null && allFilePathInDirectoryDirect.length != 0) {
                for (String name2 : allFilePathInDirectoryDirect) {
                    resultList.add(name2);
                }
            }

            //查询当前下的文件夹
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (dir.isDirectory()) {
                        return true;
                    }
                    return false;
                }
            };
            String[] list = file.list(filter);
            //得到子目录文件
            if (list == null || list.length == 0) {
                return allFilePathInDirectoryDirect;
            }

            for (String name : list) {
                String[] pathList = getAllFilePathInDirectory(directoryPath + "/" + name, fileSuffix);
                if (pathList == null || pathList.length == 0) {
                    continue;
                }
                for (String name2 : pathList) {
                    resultList.add(name + "/" + name2);
                }
            }
            if (resultList == null || resultList.size() == 0) {
                return null;
            }
            String[] resultArray = new String[resultList.size()];
            for (int i = 0, size = resultList.size(); i < size; i++) {
                resultArray[i] = resultList.get(i);
            }
            return resultArray;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 得到某个目录下的所有文件名称
     *
     * @param directoryPath
     * @param fileSuffix    文件后缀
     * @return
     */
    public static String[] getAllFilePathInDirectoryDirect(String directoryPath, final String fileSuffix) {
        try {
            File file = new File(directoryPath);
            if (!file.exists()) {
                System.out.println("getAllFilePathInDirectory file:{} is not exists" + file.getAbsoluteFile());
                return null;
            }
            if (!file.isDirectory()) {
                System.out.println("getAllFilePathInDirectory file:{} is not a directory" + file.getAbsoluteFile());
                return null;
            }
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (name.endsWith(fileSuffix)) {
                        return true;
                    }
                    return false;
                }
            };
            String[] fileNames = file.list(filter);
            if (fileNames == null || fileNames.length == 0) {
                System.out.println("getAllFilePathInDirectory file:{} fileNamelist is empty" + file.getAbsoluteFile());
                return null;
            }
            return fileNames;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
