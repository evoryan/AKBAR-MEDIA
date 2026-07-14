with open("app/build.gradle.kts", "r") as f:
    content = f.read()

content = content.replace("implementation(libs.androidx.navigation.compose)", "implementation(libs.androidx.navigation.compose)\n  implementation(libs.opencsv)")

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
