load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")
load("@bazel_tools//tools/build_defs/repo:jvm.bzl", "jvm_maven_import_external")
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

BAZEL_INTELLIJ_REV = "c187c1b7264e309793c7c00f96e72e03fef51bb3"

def load_plugin_deps():
    git_repository(
        name = "bazel_intellij",
        commit = BAZEL_INTELLIJ_REV,
        remote = "git@github.com:wix-playground/intellij.git",
    )

    jvm_maven_import_external(
        name = "auto_value",
        artifact = "com.google.auto.value:auto-value:1.6.2",
        artifact_sha256 = "edbe65a5c53e3d4f5cb10b055d4884ae7705a7cd697be4b2a5d8427761b8ba12",
        licenses = ["notice"],  # Apache 2.0
        server_urls = ["https://repo.maven.apache.org/maven2"],
    )

    jvm_maven_import_external(
        name = "auto_value_annotations",
        artifact = "com.google.auto.value:auto-value-annotations:1.6.2",
        artifact_sha256 = "b48b04ddba40e8ac33bf036f06fc43995fc5084bd94bdaace807ce27d3bea3fb",
        licenses = ["notice"],  # Apache 2.0
        server_urls = ["https://repo.maven.apache.org/maven2"],
    )

    http_archive(
        name = "intellij_ce_2019_2",
        build_file = "@bazel_intellij//intellij_platform_sdk:BUILD.idea192",
        sha256 = "fed481dfbd44a0717ab544c4c09c8bf5c037c50e39897551abeec81328cda9f7",
        url = "https://www.jetbrains.com/intellij-repository/releases/com/jetbrains/intellij/idea/ideaIC/2019.2.4/ideaIC-2019.2.4.zip",
    )

    http_archive(
        name = "intellij_ce_2019_3",
        build_file = "@bazel_intellij//intellij_platform_sdk:BUILD.idea193",
        sha256 = "fb347c3c681328d11e87846950e8c5af6ac2c8d6a7e56946d3a10e6121d322f9",
        url = "https://www.jetbrains.com/intellij-repository/releases/com/jetbrains/intellij/idea/ideaIC/2019.3.2/ideaIC-2019.3.2.zip",
    )

    # The plugin api for IntelliJ UE 2019.2. This is required to run UE-specific
    # integration tests.
    http_archive(
        name = "intellij_ue_2019_2",
        build_file = "@bazel_intellij//intellij_platform_sdk:BUILD.ue192",
        sha256 = "525467f3606c5e9ba79b502e4800d9b9c3be0366167c147e5baa531f74a5b43d",
        url = "https://www.jetbrains.com/intellij-repository/releases/com/jetbrains/intellij/idea/ideaIU/2019.2.3/ideaIU-2019.2.3.zip",
    )

    http_archive(
        name = "intellij_ue_2019_3",
        build_file = "@bazel_intellij//intellij_platform_sdk:BUILD.ue192",
        sha256 = "5bbaeaa580ae38622b8c6e9188551726f7d82fccde8ef1743cb141d902235e66",
        strip_prefix = "idea-IU-193.4386.10",
        url = "https://download.jetbrains.com/idea/ideaIU-193.4386.10.tar.gz",
    )

    # Python plugin for IntelliJ CE 2019.2. Required at compile-time for python-specific features.
    http_archive(
        name = "python_2019_2",
        build_file_content = "\n".join([
            "java_import(",
            "    name = 'python',",
            "    jars = ['python-ce/lib/python-ce.jar'],",
            "    visibility = ['//visibility:public'],",
            ")",
        ]),
        sha256 = "c0d970d4b8034fbe1a1c705a59e2d6321ec032ae38c65535493dc1ec5c8aeec5",
        url = "https://plugins.jetbrains.com/files/7322/66012/python-ce.zip",
    )

    http_archive(
        name = "python_2019_3",
        build_file_content = "\n".join([
            "java_import(",
            "    name = 'python',",
            "    jars = ['python-ce/lib/python-ce.jar'],",
            "    visibility = ['//visibility:public'],",
            ")",
        ]),
        sha256 = "21b2bd88c594bc58d8e8062c845be3bee965fc4dff2da9521158a6da3ab5b825",
        url = "https://plugins.jetbrains.com/files/7322/70397/python-ce.zip",
    )

    # Go plugin for IntelliJ UE. Required at compile-time for Bazel integration.
    http_archive(
        name = "go_2019_2",
        build_file_content = "\n".join([
            "java_import(",
            "    name = 'go',",
            "    jars = glob(['intellij-go/lib/*.jar']),",
            "    visibility = ['//visibility:public'],",
            ")",
        ]),
        sha256 = "c19195a5979a0d5361ab9d1e82beeeb40c6f9eebaae504d07790a9f3afee4478",
        url = "https://plugins.jetbrains.com/files/9568/68228/intellij-go-192.6603.23.335.zip",
    )

    http_archive(
        name = "go_2019_3",
        build_file_content = "\n".join([
            "java_import(",
            "    name = 'go',",
            "    jars = glob(['intellij-go/lib/*.jar']),",
            "    visibility = ['//visibility:public'],",
            ")",
        ]),
        sha256 = "b80126de5f2011e506943cbc1959f2af14d206a000812ee67ffef3c2d59380ef",
        url = "https://plugins.jetbrains.com/files/9568/70301/intellij-go-193.4386.1.538.zip",
    )

    jvm_maven_import_external(
        name = "error_prone_annotations",
        artifact = "com.google.errorprone:error_prone_annotations:2.3.0",
        artifact_sha256 = "524b43ea15ca97c68f10d5f417c4068dc88144b620d2203f0910441a769fd42f",
        licenses = ["notice"],  # Apache 2.0
        server_urls = ["https://repo.maven.apache.org/maven2"],
    )

    http_archive(
        name = "scala_2019_2",
        build_file = "//plugin_deps:BUILD.scala_plugin",
        sha256 = "b895f9d46f4cc01bd040e6319574fad85bdc287366d4073e130ef7df49199aaa",
        url = "https://plugins.jetbrains.com/files/1347/66462/scala-intellij-bin-2019.2.15.zip",
    )

    http_archive(
        name = "scala_2019_3",
        build_file = "//plugin_deps:BUILD.scala_plugin",
        sha256 = "b945bcb8bf4a029c42230893b41587d408101370a663e050302275919cf015f3",
        url = "https://plugins.jetbrains.com/files/1347/70287/scala-intellij-bin-2019.3.7.zip",
    )

    jvm_maven_import_external(
        name = "junit",
        artifact = "junit:junit:4.12",
        artifact_sha256 = "59721f0805e223d84b90677887d9ff567dc534d7c502ca903c0c2b17f05c116a",
        licenses = ["notice"],  # Common Public License 1.0
        server_urls = ["https://repo.maven.apache.org/maven2"],
    )

    jvm_maven_import_external(
        name = "truth",
        artifact = "com.google.truth:truth:0.42",
        artifact_sha256 = "dd652bdf0c4427c59848ac0340fd6b6d20c2cbfaa3c569a8366604dbcda5214c",
        licenses = ["notice"],  # Apache 2.0
        server_urls = ["https://repo.maven.apache.org/maven2"],
    )

    jvm_maven_import_external(
        name = "truth8",
        artifact = "com.google.truth.extensions:truth-java8-extension:0.42",
        artifact_sha256 = "cf9e095a6763bc33633b8844c3ebadffe3b082c81dd97a4d79b64ad88d305bc1",
        licenses = ["notice"],  # Apache 2.0
        server_urls = ["https://repo.maven.apache.org/maven2"],
    )

    jvm_maven_import_external(
        name = "mockito",
        artifact = "org.mockito:mockito-core:1.10.19",
        artifact_sha256 = "d5831ee4f71055800821a34a3051cf1ed5b3702f295ffebd50f65fb5d81a71b8",
        licenses = ["notice"],  # Apache 2.0
        server_urls = ["https://repo.maven.apache.org/maven2"],
    )

    jvm_maven_import_external(
        name = "objenesis",
        artifact = "org.objenesis:objenesis:1.3",
        artifact_sha256 = "dd4ef3d3091063a4fec578cbb2bbe6c1f921c00091ba2993dcd9afd25ff9444a",
        licenses = ["notice"],  # Apache 2.0
        server_urls = ["https://repo.maven.apache.org/maven2"],
    )
