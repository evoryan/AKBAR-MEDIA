with open('app/src/main/java/com/example/ui/navigation/NavGraph.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if "onLogout = {" in line:
        new_lines.append("                    onNavigateToCompanySettings = { navController.navigate(CompanySettingsRoute) },\n")
    new_lines.append(line)

with open('app/src/main/java/com/example/ui/navigation/NavGraph.kt', 'w') as f:
    f.writelines(new_lines)
