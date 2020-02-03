# Wixperiments
## Features ##
- Fixes Scala attachment & navigation to Scala sources.
 
## Building yourself
Use Bazel:

     Pass the following build flags to bazel:
     --define=ij_product=intellij-2019.3 # or intellij-2019.2 for older Intellj IDEA
     --nocheck_visibility # to be able to use bazel plugin private rules
  
    `bazel build //:bazel_scala_ext_plugin_zip` builds plugin zip, which can be installed manually
        
    `//:plugin_dev` ia a run configuration target for IntelliJ. If specified bin project view, automatic 
     run configuration is created. Allows debugging.
    
    Import plugin development projectview: `intellij.projectview`
    
    [Configure and add Intellij SDK ](https://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/setting_up_environment.html)
 
