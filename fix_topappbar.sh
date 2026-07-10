#!/bin/bash
for file in app/src/main/java/com/example/ui/screens/*.kt; do
    sed -i 's/containerColor = [a-zA-Z0-9_]*/containerColor = headerBg/g' "$file"
done
