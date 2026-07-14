import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

content = content.replace(
    'applicationId = "com.aistudio.akbarmedia.xztyqw"', 
    'applicationId = "com.akbarmedia.bill"'
)

if 'implementation(libs.firebase.messaging)' not in content:
    content = content.replace(
        'implementation(libs.firebase.ai)', 
        'implementation(libs.firebase.ai)\n  implementation(libs.firebase.messaging)'
    )

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
