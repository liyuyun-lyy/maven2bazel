An idea plugin to tansfer maven project to bazel. 
Plugin url: https://plugins.jetbrains.com/plugin/17940-maven2bazel

The main idea is to use maven to generate dependency:tree.
Then parse the tree from each module and generate corresponding BUILD files.

Inspired by https://gitee.com/liuzhenhuan/maven2bazel. The original repository uses IDEA 2019 and this repository has upgraded to 2022
