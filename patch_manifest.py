import re
with open("app/src/main/AndroidManifest.xml", "r") as f:
    content = f.read()

permissions = """    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />"""

content = content.replace('<uses-permission android:name="android.permission.INTERNET" />', permissions)

provider = """        </activity>
        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>
    </application>"""

content = content.replace("""        </activity>
    </application>""", provider)

with open("app/src/main/AndroidManifest.xml", "w") as f:
    f.write(content)
