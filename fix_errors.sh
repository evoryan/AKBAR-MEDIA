#!/bin/bash
# Fix headerBg(...)
sed -i 's/containerColor = headerBg(0xFF11111A)/containerColor = Color(0xFF11111A)/g' app/src/main/java/com/example/ui/screens/*.kt
sed -i 's/containerColor = headerBg (isSelected)/containerColor = if (isSelected)/g' app/src/main/java/com/example/ui/screens/*.kt

# Fix Scaffold containerColor
sed -i 's/containerColor = headerBg,/containerColor = bgMain,/g' app/src/main/java/com/example/ui/screens/CustomersScreen.kt
sed -i 's/containerColor = headerBg/containerColor = bgMain/g' app/src/main/java/com/example/ui/screens/LoginScreen.kt
sed -i 's/containerColor = headerBg,/containerColor = bgMain,/g' app/src/main/java/com/example/ui/screens/ManageSecretsScreen.kt
sed -i 's/containerColor = headerBg,/containerColor = bgMain,/g' app/src/main/java/com/example/ui/screens/MikrotikScreen.kt
sed -i 's/containerColor = headerBg,/containerColor = bgMain,/g' app/src/main/java/com/example/ui/screens/PackagesScreen.kt
sed -i 's/containerColor = headerBg,/containerColor = bgMain,/g' app/src/main/java/com/example/ui/screens/PaymentScreen.kt
sed -i 's/containerColor = headerBg,/containerColor = bgMain,/g' app/src/main/java/com/example/ui/screens/PembayaranByAdminScreen.kt
sed -i 's/containerColor = headerBg,/containerColor = bgMain,/g' app/src/main/java/com/example/ui/screens/PembukuanScreen.kt
sed -i 's/containerColor = headerBg,/containerColor = bgMain,/g' app/src/main/java/com/example/ui/screens/RangkumanScreen.kt
sed -i 's/containerColor = headerBg,/containerColor = bgMain,/g' app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt
sed -i 's/containerColor = headerBg,/containerColor = bgMain,/g' app/src/main/java/com/example/ui/screens/UangDiAdminScreen.kt

