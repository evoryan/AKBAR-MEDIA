import re

with open('app/src/main/AndroidManifest.xml', 'r') as f:
    content = f.read()

old_block = """        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.MyApplication">"""

new_block = """        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:windowSoftInputMode="adjustResize"
            android:theme="@style/Theme.MyApplication">"""

if old_block in content:
    content = content.replace(old_block, new_block)
    with open('app/src/main/AndroidManifest.xml', 'w') as f:
        f.write(content)
    print("Patched successfully")
else:
    print("Old block not found!")
