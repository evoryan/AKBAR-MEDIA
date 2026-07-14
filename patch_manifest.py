import re

with open("app/src/main/AndroidManifest.xml", "r") as f:
    content = f.read()

target = """        <activity
            android:name=".MainActivity\""""

rep = """        <service
            android:name=".MyFirebaseMessagingService"
            android:exported="true">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>

        <activity
            android:name=".MainActivity\""""

content = content.replace(target, rep)

with open("app/src/main/AndroidManifest.xml", "w") as f:
    f.write(content)
