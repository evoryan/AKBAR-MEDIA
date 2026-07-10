svg_content = """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    
    <!-- Background is handled in ic_launcher_background.xml -->

    <!-- Center everything in 66dp safe zone (21 to 87) -->
    <!-- The logo: thick strokes -->
    
    <path
        android:fillColor="#000000"
        android:pathData="M 33 27 
                          L 43 27 
                          L 73 57 
                          L 63 57 Z" />
                          
    <path
        android:fillColor="#000000"
        android:pathData="M 33 77 
                          L 73 37 
                          L 83 47 
                          L 43 87 Z" />
                          
    <circle
        android:cx="75"
        android:cy="25"
        android:radius="6"
        android:fillColor="#000000" />
</vector>
"""
with open('app/src/main/res/drawable/ic_launcher_foreground.xml', 'w') as f:
    f.write(svg_content)

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
with open('app/src/main/res/drawable/ic_launcher_background.xml', 'w') as f:
    f.write(bg_content)
