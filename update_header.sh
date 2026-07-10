#!/bin/bash
for file in app/src/main/java/com/example/ui/screens/*.kt; do
    # Ensure headerBg = Color(0xFF1F0216) exists
    if ! grep -q "val headerBg =" "$file"; then
        sed -i 's/val bgMain = .*/&\n    val headerBg = Color(0xFF1F0216)/' "$file"
    else
        sed -i 's/val headerBg = .*/val headerBg = Color(0xFF1F0216)/' "$file"
    fi
    
    # Update TopAppBarDefaults.topAppBarColors(containerColor = ...)
    sed -i 's/containerColor = cardBg/containerColor = headerBg/g' "$file"
    sed -i 's/containerColor = bgMain/containerColor = headerBg/g' "$file"
    sed -i 's/containerColor = Color.Transparent/containerColor = headerBg/g' "$file"
    sed -i 's/Modifier.background(Color(0xFF8B008B))/Modifier.background(headerBg)/g' "$file"
done
