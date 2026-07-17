import re

with open('app/src/main/java/com/example/ui/screens/OdcScreen.kt', 'r') as f:
    odc = f.read()

odc_old = """                items(odcList) { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(cardBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {"""
odc_new = """                items(odcList) { item ->
                    var isExpanded by remember { mutableStateOf(false) }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(cardBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                            .clickable { isExpanded = !isExpanded }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {"""

odc = odc.replace(odc_old, odc_new)

odc_old2 = """                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFFF003C))
                                }
                                }
                            }
                        }
                    }"""

odc_new2 = """                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFFF003C))
                                }
                                }
                            }
                        }
                        if (isExpanded) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = cardBorder)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Redaman In", color = textSecondary, fontSize = 14.sp)
                                    Text(item.redamanIn.takeIf { it.isNotEmpty() } ?: "-", color = textMain, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Redaman Out", color = textSecondary, fontSize = 14.sp)
                                    Text(item.redamanOut.takeIf { it.isNotEmpty() } ?: "-", color = textMain, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }"""

odc = odc.replace(odc_old2, odc_new2)

with open('app/src/main/java/com/example/ui/screens/OdcScreen.kt', 'w') as f:
    f.write(odc)

with open('app/src/main/java/com/example/ui/screens/OdpScreen.kt', 'r') as f:
    odp = f.read()

odp_old = """                items(odpList) { item ->
                    val odc = odcList.find { it.id == item.odcId }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(cardBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {"""

odp_new = """                items(odpList) { item ->
                    val odc = odcList.find { it.id == item.odcId }
                    var isExpanded by remember { mutableStateOf(false) }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(cardBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                            .clickable { isExpanded = !isExpanded }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {"""

odp = odp.replace(odp_old, odp_new)

odp_old2 = """                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFFF003C))
                                }
                                }
                            }
                        }
                    }"""

odp_new2 = """                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFFF003C))
                                }
                                }
                            }
                        }
                        if (isExpanded) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = cardBorder)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Redaman In", color = textSecondary, fontSize = 14.sp)
                                    Text(item.redamanIn.takeIf { it.isNotEmpty() } ?: "-", color = textMain, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Redaman Out", color = textSecondary, fontSize = 14.sp)
                                    Text(item.redamanOut.takeIf { it.isNotEmpty() } ?: "-", color = textMain, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }"""

odp = odp.replace(odp_old2, odp_new2)

with open('app/src/main/java/com/example/ui/screens/OdpScreen.kt', 'w') as f:
    f.write(odp)

