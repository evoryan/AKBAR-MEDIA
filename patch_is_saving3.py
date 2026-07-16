import re

with open("app/src/main/java/com/example/ui/screens/PembukuanScreen.kt", "r") as f:
    content = f.read()

target = """                                    fetchData()
                                    showAddDialog = false
                                } catch (e: Exception) {}
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryCyan, contentColor = Color.Black)
                    ) {
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }"""

rep = """                                    fetchData()
                                    showAddDialog = false
                                } catch (e: Exception) {
                                } finally {
                                    isSaving = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryCyan, contentColor = Color.Black),
                        enabled = !isSaving
                    ) {
                        Text(if (isSaving) "Menyimpan..." else "Simpan", fontWeight = FontWeight.Bold)
                    }"""

content = content.replace(target, rep)

with open("app/src/main/java/com/example/ui/screens/PembukuanScreen.kt", "w") as f:
    f.write(content)
