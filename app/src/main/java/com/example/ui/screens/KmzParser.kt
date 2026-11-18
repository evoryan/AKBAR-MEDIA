package com.example.ui.screens

import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

data class ParsedKmzResult(
    val documentName: String,
    val featureCount: Int,
    val featuresJson: String
)

data class KmzFeatureClickInfo(
    val layerName: String,
    val featureName: String,
    val folderName: String,
    val description: String,
    val lat: Double?,
    val lng: Double?
)

private data class KmlStyleInfo(
    val lineColor: String = "#38BDF8",
    val lineOpacity: Double = 0.9,
    val lineWidth: Double = 3.0,
    val polyColor: String = "#38BDF8",
    val polyOpacity: Double = 0.25,
    val iconColor: String = "#F59E0B"
)

object KmzParser {

    fun parseBytes(bytes: ByteArray, fileName: String): ParsedKmzResult {
        val kmlDocs = mutableListOf<String>()

        // Detect ZIP/KMZ magic header PK\x03\x04
        val isZip = bytes.size >= 4 &&
            bytes[0] == 0x50.toByte() &&
            bytes[1] == 0x4B.toByte() &&
            (bytes[2] == 0x03.toByte() || bytes[2] == 0x05.toByte())

        if (isZip) {
            ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory && entry.name.lowercase().endsWith(".kml")) {
                        val out = ByteArrayOutputStream()
                        val buffer = ByteArray(8192)
                        var len: Int
                        while (zis.read(buffer).also { len = it } != -1) {
                            out.write(buffer, 0, len)
                        }
                        kmlDocs.add(out.toString(Charsets.UTF_8.name()))
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        } else {
            kmlDocs.add(String(bytes, Charsets.UTF_8))
        }

        if (kmlDocs.isEmpty()) {
            throw IllegalArgumentException("File KMZ tidak berisi dokumen KML yang valid.")
        }

        val allFeatures = JSONArray()
        var resolvedDocName = fileName.substringBeforeLast('.').ifBlank { "Layer KMZ" }

        kmlDocs.forEachIndexed { idx, kmlXml ->
            val parsedDoc = parseSingleKml(kmlXml)
            if (idx == 0 && parsedDoc.first.isNotBlank()) {
                resolvedDocName = parsedDoc.first
            }
            for (i in 0 until parsedDoc.second.length()) {
                allFeatures.put(parsedDoc.second.getJSONObject(i))
            }
        }

        return ParsedKmzResult(
            documentName = resolvedDocName,
            featureCount = allFeatures.length(),
            featuresJson = allFeatures.toString()
        )
    }

    private fun parseSingleKml(xmlText: String): Pair<String, JSONArray> {
        val cleanedXml = xmlText.trim().removePrefix("\uFEFF")
        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = false
            runCatching { setFeature("http://apache.org/xml/features/disallow-doctype-decl", true) }
        }
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(ByteArrayInputStream(cleanedXml.toByteArray(Charsets.UTF_8)))
        doc.documentElement.normalize()

        val styleLookup = mutableMapOf<String, KmlStyleInfo>()
        val styleMapRefs = mutableMapOf<String, String>()

        // Collect all <Style id="...">
        val styleNodes = doc.getElementsByTagName("Style")
        for (i in 0 until styleNodes.length) {
            val el = styleNodes.item(i) as? Element ?: continue
            val id = el.getAttribute("id").trim()
            if (id.isNotBlank()) {
                styleLookup["#$id"] = parseStyleElement(el)
                styleLookup[id] = styleLookup["#$id"]!!
            }
        }

        // Collect <StyleMap id="...">
        val styleMapNodes = doc.getElementsByTagName("StyleMap")
        for (i in 0 until styleMapNodes.length) {
            val el = styleMapNodes.item(i) as? Element ?: continue
            val id = el.getAttribute("id").trim()
            if (id.isNotBlank()) {
                val pairs = el.getElementsByTagName("Pair")
                var chosenUrl = ""
                for (j in 0 until pairs.length) {
                    val pairEl = pairs.item(j) as? Element ?: continue
                    val keyText = directChildText(pairEl, "key").lowercase()
                    val targetUrl = directChildText(pairEl, "styleUrl")
                    if (keyText == "normal" && targetUrl.isNotBlank()) {
                        chosenUrl = targetUrl
                        break
                    } else if (chosenUrl.isBlank() && targetUrl.isNotBlank()) {
                        chosenUrl = targetUrl
                    }
                }
                if (chosenUrl.isNotBlank()) {
                    styleMapRefs["#$id"] = chosenUrl
                    styleMapRefs[id] = chosenUrl
                }
            }
        }

        var docTitle = ""
        val docElements = doc.getElementsByTagName("Document")
        if (docElements.length > 0) {
            val firstDoc = docElements.item(0) as? Element
            if (firstDoc != null) {
                docTitle = directChildText(firstDoc, "name")
            }
        }

        val features = JSONArray()
        walkNode(doc.documentElement, "", styleLookup, styleMapRefs, features)
        return docTitle to features
    }

    private fun walkNode(
        element: Element,
        currentFolder: String,
        styles: Map<String, KmlStyleInfo>,
        styleMaps: Map<String, String>,
        outFeatures: JSONArray
    ) {
        val tag = element.tagName
        val nextFolder = when (tag) {
            "Folder", "Document" -> {
                val folderName = directChildText(element, "name")
                when {
                    folderName.isNotBlank() && currentFolder.isNotBlank() && tag == "Folder" ->
                        "$currentFolder / $folderName"
                    folderName.isNotBlank() -> folderName
                    else -> currentFolder
                }
            }
            else -> currentFolder
        }

        if (tag == "Placemark") {
            extractPlacemark(element, currentFolder, styles, styleMaps, outFeatures)
            return
        }

        val children = element.childNodes
        for (i in 0 until children.length) {
            val child = children.item(i)
            if (child.nodeType == Node.ELEMENT_NODE) {
                walkNode(child as Element, nextFolder, styles, styleMaps, outFeatures)
            }
        }
    }

    private fun extractPlacemark(
        placemark: Element,
        folderName: String,
        styles: Map<String, KmlStyleInfo>,
        styleMaps: Map<String, String>,
        outFeatures: JSONArray
    ) {
        val name = directChildText(placemark, "name").ifBlank { "Titik KMZ" }
        val rawDesc = directChildText(placemark, "description")
        val desc = stripHtmlTags(rawDesc)
        val styleUrl = directChildText(placemark, "styleUrl")
        val resolvedStyleUrl = styleMaps[styleUrl] ?: styleUrl
        val inlineStyleEl = directChildElement(placemark, "Style")
        val baseStyle = styles[resolvedStyleUrl] ?: styles[resolvedStyleUrl.removePrefix("#")] ?: KmlStyleInfo()
        val style = if (inlineStyleEl != null) parseStyleElement(inlineStyleEl, baseStyle) else baseStyle

        extractGeometries(placemark, name, desc, folderName, style, outFeatures)
    }

    private fun extractGeometries(
        container: Element,
        name: String,
        desc: String,
        folderName: String,
        style: KmlStyleInfo,
        outFeatures: JSONArray
    ) {
        val children = container.childNodes
        for (i in 0 until children.length) {
            val node = children.item(i)
            if (node.nodeType != Node.ELEMENT_NODE) continue
            val el = node as Element
            when (el.tagName) {
                "Point" -> {
                    val coordText = firstDescendantText(el, "coordinates")
                    val pts = parseCoordinateTuples(coordText)
                    if (pts.isNotEmpty()) {
                        val p = pts.first()
                        outFeatures.put(
                            JSONObject().apply {
                                put("kind", "point")
                                put("name", name)
                                put("description", desc)
                                put("folder", folderName)
                                put("color", style.iconColor)
                                put("coords", JSONArray().put(p.first).put(p.second))
                            }
                        )
                    }
                }
                "LineString" -> {
                    val coordText = firstDescendantText(el, "coordinates")
                    val pts = parseCoordinateTuples(coordText)
                    if (pts.size >= 2) {
                        val arr = JSONArray()
                        pts.forEach { (lat, lng) ->
                            arr.put(JSONArray().put(lat).put(lng))
                        }
                        outFeatures.put(
                            JSONObject().apply {
                                put("kind", "line")
                                put("name", name)
                                put("description", desc)
                                put("folder", folderName)
                                put("color", style.lineColor)
                                put("opacity", style.lineOpacity)
                                put("weight", style.lineWidth.coerceIn(1.5, 8.0))
                                put("coords", arr)
                            }
                        )
                    }
                }
                "Polygon" -> {
                    val outerBoundary = directChildElement(el, "outerBoundaryIs") ?: el
                    val coordText = firstDescendantText(outerBoundary, "coordinates")
                    val pts = parseCoordinateTuples(coordText)
                    if (pts.size >= 3) {
                        val arr = JSONArray()
                        pts.forEach { (lat, lng) ->
                            arr.put(JSONArray().put(lat).put(lng))
                        }
                        outFeatures.put(
                            JSONObject().apply {
                                put("kind", "polygon")
                                put("name", name)
                                put("description", desc)
                                put("folder", folderName)
                                put("color", style.lineColor)
                                put("fillColor", style.polyColor)
                                put("fillOpacity", style.polyOpacity)
                                put("weight", style.lineWidth.coerceIn(1.0, 6.0))
                                put("coords", arr)
                            }
                        )
                    }
                }
                "MultiGeometry" -> {
                    extractGeometries(el, name, desc, folderName, style, outFeatures)
                }
            }
        }
    }

    private fun parseCoordinateTuples(raw: String): List<Pair<Double, Double>> {
        if (raw.isBlank()) return emptyList()
        val result = mutableListOf<Pair<Double, Double>>()
        val tokens = raw.trim().split(Regex("""\s+"""))
        for (tok in tokens) {
            val parts = tok.split(',')
            if (parts.size >= 2) {
                val lng = parts[0].trim().toDoubleOrNull()
                val lat = parts[1].trim().toDoubleOrNull()
                if (lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0) {
                    result.add(lat to lng)
                }
            }
        }
        return result
    }

    private fun parseStyleElement(styleEl: Element, fallback: KmlStyleInfo = KmlStyleInfo()): KmlStyleInfo {
        var lineColor = fallback.lineColor
        var lineOpacity = fallback.lineOpacity
        var lineWidth = fallback.lineWidth
        var polyColor = fallback.polyColor
        var polyOpacity = fallback.polyOpacity
        var iconColor = fallback.iconColor

        directChildElement(styleEl, "LineStyle")?.let { ls ->
            val rawCol = directChildText(ls, "color")
            parseKmlColor(rawCol)?.let { (hex, alpha) ->
                lineColor = hex
                lineOpacity = alpha.coerceIn(0.25, 1.0)
            }
            directChildText(ls, "width").toDoubleOrNull()?.let { w ->
                if (w > 0) lineWidth = w
            }
        }

        directChildElement(styleEl, "PolyStyle")?.let { ps ->
            val rawCol = directChildText(ps, "color")
            parseKmlColor(rawCol)?.let { (hex, alpha) ->
                polyColor = hex
                polyOpacity = alpha.coerceIn(0.1, 0.75)
            }
        }

        directChildElement(styleEl, "IconStyle")?.let { isEl ->
            val rawCol = directChildText(isEl, "color")
            parseKmlColor(rawCol)?.let { (hex, _) ->
                iconColor = hex
            }
        }

        return KmlStyleInfo(
            lineColor = lineColor,
            lineOpacity = lineOpacity,
            lineWidth = lineWidth,
            polyColor = polyColor,
            polyOpacity = polyOpacity,
            iconColor = iconColor
        )
    }

    /**
     * KML colors are 8-hex-digit aabbggrr (alpha, blue, green, red).
     */
    private fun parseKmlColor(kmlColor: String): Pair<String, Double>? {
        val clean = kmlColor.trim().removePrefix("#")
        if (clean.length != 8) return null
        return try {
            val aa = clean.substring(0, 2).toInt(16) / 255.0
            val bb = clean.substring(2, 4)
            val gg = clean.substring(4, 6)
            val rr = clean.substring(6, 8)
            "#$rr$gg$bb" to aa
        } catch (_: Exception) {
            null
        }
    }

    private fun directChildElement(parent: Element, tagName: String): Element? {
        val children = parent.childNodes
        for (i in 0 until children.length) {
            val n = children.item(i)
            if (n.nodeType == Node.ELEMENT_NODE && (n as Element).tagName == tagName) {
                return n
            }
        }
        return null
    }

    private fun directChildText(parent: Element, tagName: String): String {
        return directChildElement(parent, tagName)?.textContent?.trim().orEmpty()
    }

    private fun firstDescendantText(parent: Element, tagName: String): String {
        val nodes = parent.getElementsByTagName(tagName)
        if (nodes.length == 0) return ""
        return nodes.item(0)?.textContent?.trim().orEmpty()
    }

    private fun stripHtmlTags(raw: String): String {
        if (raw.isBlank()) return ""
        return raw
            .replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("(?i)</p>|</tr>|</div>"), "\n")
            .replace(Regex("<[^>]+>"), " ")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace(Regex("[ \\t]+"), " ")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }
}
