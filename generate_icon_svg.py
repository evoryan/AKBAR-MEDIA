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

# Let's draw an X with a dot. 
# We'll use pathData with M, L, and stroke.
# Actually vector drawable paths don't support stroke-linecap="round" universally in all versions easily, 
# wait, Android VectorDrawable DOES support android:strokeLineCap="round" and android:strokeLineJoin="round"!
fg_content = """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    
    <!-- / line -->
    <path
        android:strokeColor="#000000"
        android:strokeWidth="16"
        android:strokeLineCap="round"
        android:pathData="M 32,76 L 76,32" />

    <!-- \ line top -->
    <path
        android:strokeColor="#000000"
        android:strokeWidth="16"
        android:strokeLineCap="round"
        android:pathData="M 32,32 L 46,46" />

    <!-- \ line bottom -->
    <path
        android:strokeColor="#000000"
        android:strokeWidth="16"
        android:strokeLineCap="round"
        android:pathData="M 62,62 L 76,76" />
        
    <!-- Dot -->
    <path
        android:fillColor="#E53935"
        android:pathData="M 85,24 A 6,6 0 1,1 85,23.9" />
        
</vector>
"""

with open('app/src/main/res/drawable/ic_launcher_foreground.xml', 'w') as f:
    f.write(fg_content)

with open('app/src/main/res/drawable/ic_launcher_background.xml', 'w') as f:
    f.write(bg_content)
