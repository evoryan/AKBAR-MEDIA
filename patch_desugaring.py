import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

target1 = """    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }"""
rep1 = """    compileOptions {
        coreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }"""

if target1 in content:
    content = content.replace(target1, rep1)
    if "coreLibraryDesugaring(" not in content:
        content = content.replace("dependencies {", "dependencies {\n  coreLibraryDesugaring(\"com.android.tools:desugar_jdk_libs:2.0.4\")")
    with open("app/build.gradle.kts", "w") as f:
        f.write(content)
    print("Patched build.gradle.kts")
else:
    print("Target not found")
