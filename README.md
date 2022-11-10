# maven2bazel
An idea IDE plugin to tansfer maven project to bazel.  
Plugin url: https://plugins.jetbrains.com/plugin/17940-maven2bazel

## Project Status: Experimental  
This is not an officially supported Google product (meaning, support and/or new releases may be limited.)

## Usage
1. In idea, click Tools -> Maven2Bazel -> Settings, all settings are requied to be set.
2. Click Tools -> Maven2Bazel -> maven -> bazel and wait for it to finish.
3. You should see WORKSPACE and BUILD file in root directy and each module.

## How the tool works
The main idea is to use maven to generate dependency:tree.
Then parse the tree from each module and generate corresponding BUILD files.

Inspired by https://gitee.com/liuzhenhuan/maven2bazel. The original repository uses IDEA 2019 and this repository has upgraded to 2022
