import re

# Fix OdcScreen
with open('app/src/main/java/com/example/ui/screens/OdcScreen.kt', 'r') as f:
    odc = f.read()

odc = odc.replace("""                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                    }
                    OutlinedTextField(
                        value = redamanIn,
                        onValueChange = { redamanIn = it },
                        label = { Text("Redaman In") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg, unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        )
                    )
                    OutlinedTextField(
                        value = redamanOut,
                        onValueChange = { redamanOut = it },
                        label = { Text("Redaman Out") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg, unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        )
                    )
                }""", """                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                    }
                }""")

odc = odc.replace("""                            }
                        }
                    }
                    OutlinedTextField(
                        value = redamanIn,
                        onValueChange = { redamanIn = it },
                        label = { Text("Redaman In") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg, unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        )
                    )
                    OutlinedTextField(
                        value = redamanOut,
                        onValueChange = { redamanOut = it },
                        label = { Text("Redaman Out") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg, unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        )
                    )
                }""", """                            }
                        }
                    }
                    OutlinedTextField(
                        value = redamanIn,
                        onValueChange = { redamanIn = it },
                        label = { Text("Redaman In") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg, unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        )
                    )
                    OutlinedTextField(
                        value = redamanOut,
                        onValueChange = { redamanOut = it },
                        label = { Text("Redaman Out") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg, unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        )
                    )
                }""")

with open('app/src/main/java/com/example/ui/screens/OdcScreen.kt', 'w') as f:
    f.write(odc)


# Fix OdpScreen
with open('app/src/main/java/com/example/ui/screens/OdpScreen.kt', 'r') as f:
    odp = f.read()

odp = odp.replace("""                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                    }
                    OutlinedTextField(
                        value = redamanIn,
                        onValueChange = { redamanIn = it },
                        label = { Text("Redaman In") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg, unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        )
                    )
                    OutlinedTextField(
                        value = redamanOut,
                        onValueChange = { redamanOut = it },
                        label = { Text("Redaman Out") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg, unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        )
                    )
                }""", """                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                    }
                }""")

with open('app/src/main/java/com/example/ui/screens/OdpScreen.kt', 'w') as f:
    f.write(odp)

# Fix JaringanScreen
with open('app/src/main/java/com/example/ui/screens/JaringanScreen.kt', 'r') as f:
    jar = f.read()

jar = jar.replace("val filteredOdp = odpList.filter { it.name.contains(searchQuery, true) || it.location?.contains(searchQuery, true) == true }", "val filteredOdp = odpList.filter { it.name.contains(searchQuery, true) }")

with open('app/src/main/java/com/example/ui/screens/JaringanScreen.kt', 'w') as f:
    f.write(jar)
