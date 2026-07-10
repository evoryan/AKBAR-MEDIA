bg_content = """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M0,0h108v108h-108z" />
</vector>
"""

fg_content = """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    
    <!-- Long / line -->
    <path
        android:strokeColor="#000000"
        android:strokeWidth="18"
        android:strokeLineCap="round"
        android:pathData="M 32,76 L 76,32" />

    <!-- Top-left \ line -->
    <path
        android:strokeColor="#000000"
        android:strokeWidth="18"
        android:strokeLineCap="round"
        android:pathData="M 26,38 L 40,52" />

    <!-- Bottom-right \ line -->
    <path
        android:strokeColor="#000000"
        android:strokeWidth="18"
        android:strokeLineCap="round"
        android:pathData="M 68,56 L 82,70" />
        
    <!-- Dot -->
    <path
        android:fillColor="#000000"
        android:pathData="M 76,20 a 8,8 0 1,0 16,0 a 8,8 0 1,0 -16,0" />
        
</vector>
"""

with open('app/src/main/res/drawable/ic_launcher_foreground.xml', 'w') as f:
    f.write(fg_content)

with open('app/src/main/res/drawable/ic_launcher_background.xml', 'w') as f:
    f.write(bg_content)
