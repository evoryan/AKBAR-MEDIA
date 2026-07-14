import re

with open("app/src/main/java/com/example/ui/screens/DaftarAdminScreen.kt", "r") as f:
    content = f.read()

target = """                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        )
                    )
                    
                    Box {"""

rep = """                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        )
                    )
                    
                    var password by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password (kosongkan jika tidak diubah)") },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        )
                    )
                    
                    Box {"""
content = content.replace(target, rep)

target_add = """                                val newItem = AdminUser(
                                    id = "",
                                    name = name,
                                    username = username,
                                    role = selectedRole
                                )
                                ApiClient.apiService.addAdmin(newItem)"""

rep_add = """                                val newItem = AdminUser(
                                    id = "",
                                    name = name,
                                    username = username,
                                    role = selectedRole
                                )
                                // Send password as well if needed. Since AdminUser data class doesn't have password, we can create a temporary map or similar.
                                val adminMap = mapOf(
                                    "name" to name,
                                    "username" to username,
                                    "role" to selectedRole.name,
                                    "password" to password
                                )
                                ApiClient.apiService.addAdminMap(adminMap)"""
content = content.replace(target_add, rep_add)

target_edit = """                                adminList[index] = current.copy(name = name, username = username, role = selectedRole)
                            }
                        }
                    }"""

rep_edit = """                                val adminMap = mapOf(
                                    "name" to name,
                                    "username" to username,
                                    "role" to selectedRole.name,
                                    "password" to password
                                )
                                coroutineScope.launch {
                                    try {
                                        ApiClient.apiService.updateAdminMap(current.id, adminMap)
                                        adminList[index] = current.copy(name = name, username = username, role = selectedRole)
                                    } catch(e: Exception) {}
                                }
                            }
                        }
                    }"""
content = content.replace(target_edit, rep_edit)

with open("app/src/main/java/com/example/ui/screens/DaftarAdminScreen.kt", "w") as f:
    f.write(content)
