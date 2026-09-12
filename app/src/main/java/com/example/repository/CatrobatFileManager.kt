package com.example.repository

import android.content.Context
import android.net.Uri
import com.example.model.Brick
import com.example.model.BrickOp
import com.example.model.CostumeLook
import com.example.model.ProgramObject
import com.example.model.Project
import com.example.model.Script
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object CatrobatFileManager {

  const val EXTENSION_PCP = ".pcp"
  const val EXTENSION_CATROBAT = ".catrobat"

  /**
   * Serializes a Project into a JSON string conforming to PocketCodePlus format.
   */
  fun projectToJson(project: Project): String {
    val root = JSONObject()
    root.put("format", "PocketCodePlus_v2")
    root.put("id", project.id)
    root.put("title", project.title)
    root.put("description", project.description)
    root.put("createdAt", project.createdAt)
    root.put("backgroundHex", project.backgroundHex)
    root.put("isPreset", project.isPreset)

    val varsObj = JSONObject()
    project.variables.forEach { (k, v) -> varsObj.put(k, v) }
    root.put("variables", varsObj)

    val objArray = JSONArray()
    project.objects.forEach { actor ->
      val actorObj = JSONObject()
      actorObj.put("id", actor.id)
      actorObj.put("name", actor.name)
      actorObj.put("isBackground", actor.isBackground)
      actorObj.put("initialX", actor.initialX)
      actorObj.put("initialY", actor.initialY)
      actorObj.put("initialRotation", actor.initialRotation)
      actorObj.put("initialSize", actor.initialSize)
      actorObj.put("currentLookIndex", actor.currentLookIndex)

      val looksArray = JSONArray()
      actor.looks.forEach { look ->
        val lObj = JSONObject()
        lObj.put("id", look.id)
        lObj.put("name", look.name)
        lObj.put("emojiOrIcon", look.emojiOrIcon)
        lObj.put("tintHex", look.tintHex)
        looksArray.put(lObj)
      }
      actorObj.put("looks", looksArray)

      val scriptsArray = JSONArray()
      actor.scripts.forEach { script ->
        val sObj = JSONObject()
        sObj.put("id", script.id)
        sObj.put("header", brickToJson(script.header))
        val bArray = JSONArray()
        script.bricks.forEach { b -> bArray.put(brickToJson(b)) }
        sObj.put("bricks", bArray)
        scriptsArray.put(sObj)
      }
      actorObj.put("scripts", scriptsArray)
      objArray.put(actorObj)
    }
    root.put("objects", objArray)
    return root.toString(2)
  }

  private fun brickToJson(brick: Brick): JSONObject {
    val b = JSONObject()
    b.put("id", brick.id)
    b.put("op", brick.op.name)
    b.put("paramString1", brick.paramString1)
    b.put("paramString2", brick.paramString2)
    b.put("paramString3", brick.paramString3)
    b.put("paramNum1", brick.paramNum1)
    b.put("paramNum2", brick.paramNum2)
    if (brick.childBricks.isNotEmpty()) {
      val cArray = JSONArray()
      brick.childBricks.forEach { cArray.put(brickToJson(it)) }
      b.put("childBricks", cArray)
    }
    return b
  }

  /**
   * Exports a Project as a `.pcp` Zip archive bytes.
   */
  fun exportToPcpZip(project: Project): ByteArray {
    val baos = ByteArrayOutputStream()
    ZipOutputStream(baos).use { zos ->
      // 1. project.json
      val codeJson = projectToJson(project)
      zos.putNextEntry(ZipEntry("project.json"))
      zos.write(codeJson.toByteArray(StandardCharsets.UTF_8))
      zos.closeEntry()

      // 2. manifest.json
      val manifest = JSONObject().apply {
        put("application", "PocketCodePlus")
        put("version", "2.0")
        put("compatibleWithCatrobat", true)
        put("projectTitle", project.title)
        put("objectCount", project.objects.size)
        put("timestamp", System.currentTimeMillis())
      }
      zos.putNextEntry(ZipEntry("manifest.json"))
      zos.write(manifest.toString(2).toByteArray(StandardCharsets.UTF_8))
      zos.closeEntry()
    }
    return baos.toByteArray()
  }

  /**
   * Exports a Project as a `.catrobat` Zip archive bytes (with code.xml & permissions).
   */
  fun exportToCatrobatZip(project: Project): ByteArray {
    val baos = ByteArrayOutputStream()
    ZipOutputStream(baos).use { zos ->
      // 1. code.json
      val codeJson = projectToJson(project)
      zos.putNextEntry(ZipEntry("code.json"))
      zos.write(codeJson.toByteArray(StandardCharsets.UTF_8))
      zos.closeEntry()

      // 2. Catrobat code.xml representation
      val xmlContent = buildCatrobatXml(project)
      zos.putNextEntry(ZipEntry("code.xml"))
      zos.write(xmlContent.toByteArray(StandardCharsets.UTF_8))
      zos.closeEntry()

      // 3. Catrobat description.txt
      zos.putNextEntry(ZipEntry("description.txt"))
      zos.write(project.description.toByteArray(StandardCharsets.UTF_8))
      zos.closeEntry()
    }
    return baos.toByteArray()
  }

  private fun buildCatrobatXml(project: Project): String {
    val sb = StringBuilder()
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
    sb.append("<program>\n")
    sb.append("  <header>\n")
    sb.append("    <applicationName>PocketCodePlus</applicationName>\n")
    sb.append("    <applicationVersion>2.0</applicationVersion>\n")
    sb.append("    <catrobatLanguageVersion>0.999</catrobatLanguageVersion>\n")
    sb.append("    <programName>").append(escapeXml(project.title)).append("</programName>\n")
    sb.append("    <description>").append(escapeXml(project.description)).append("</description>\n")
    sb.append("  </header>\n")
    sb.append("  <objectList>\n")
    project.objects.forEach { obj ->
      sb.append("    <object name=\"").append(escapeXml(obj.name)).append("\">\n")
      sb.append("      <scriptList>\n")
      obj.scripts.forEach { s ->
        sb.append("        <script type=\"").append(s.header.op.name).append("\">\n")
        s.bricks.forEach { b ->
          sb.append("          <brick type=\"").append(b.op.name).append("\">\n")
          sb.append("            <p1>").append(escapeXml(b.paramString1)).append("</p1>\n")
          sb.append("            <n1>").append(b.paramNum1).append("</n1>\n")
          sb.append("          </brick>\n")
        }
        sb.append("        </script>\n")
      }
      sb.append("      </scriptList>\n")
      sb.append("    </object>\n")
    }
    sb.append("  </objectList>\n")
    sb.append("</program>\n")
    return sb.toString()
  }

  private fun escapeXml(str: String): String {
    return str.replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
  }

  /**
   * Parses project from JSON string.
   */
  fun parseProjectJson(jsonStr: String): Project {
    val root = JSONObject(jsonStr)
    val title = root.optString("title", "Imported Project")
    val desc = root.optString("description", "")
    val bg = root.optString("backgroundHex", "#0F172A")

    val vars = mutableMapOf<String, Float>()
    if (root.has("variables")) {
      val vObj = root.getJSONObject("variables")
      vObj.keys().forEach { k -> vars[k] = vObj.optDouble(k, 0.0).toFloat() }
    }

    val objects = mutableListOf<ProgramObject>()
    if (root.has("objects")) {
      val arr = root.getJSONArray("objects")
      for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        val looks = mutableListOf<CostumeLook>()
        if (o.has("looks")) {
          val lArr = o.getJSONArray("looks")
          for (j in 0 until lArr.length()) {
            val l = lArr.getJSONObject(j)
            looks.add(
              CostumeLook(
                id = l.optString("id", java.util.UUID.randomUUID().toString()),
                name = l.optString("name", "Look ${j + 1}"),
                emojiOrIcon = l.optString("emojiOrIcon", "🐱"),
                tintHex = l.optString("tintHex", "#FF9800")
              )
            )
          }
        }
        if (looks.isEmpty()) {
          looks.add(CostumeLook(name = "Default", emojiOrIcon = "🐱"))
        }

        val scripts = mutableListOf<Script>()
        if (o.has("scripts")) {
          val sArr = o.getJSONArray("scripts")
          for (k in 0 until sArr.length()) {
            val s = sArr.getJSONObject(k)
            val headerBrick = if (s.has("header")) parseBrick(s.getJSONObject("header")) else Brick(op = BrickOp.WHEN_SCENE_STARTS)
            val bricks = mutableListOf<Brick>()
            if (s.has("bricks")) {
              val bArr = s.getJSONArray("bricks")
              for (m in 0 until bArr.length()) {
                bricks.add(parseBrick(bArr.getJSONObject(m)))
              }
            }
            scripts.add(Script(id = s.optString("id", java.util.UUID.randomUUID().toString()), header = headerBrick, bricks = bricks))
          }
        }

        objects.add(
          ProgramObject(
            id = o.optString("id", java.util.UUID.randomUUID().toString()),
            name = o.optString("name", "Actor"),
            isBackground = o.optBoolean("isBackground", false),
            looks = looks,
            currentLookIndex = o.optInt("currentLookIndex", 0),
            initialX = o.optDouble("initialX", 0.0).toFloat(),
            initialY = o.optDouble("initialY", 0.0).toFloat(),
            initialRotation = o.optDouble("initialRotation", 0.0).toFloat(),
            initialSize = o.optDouble("initialSize", 100.0).toFloat(),
            scripts = scripts
          )
        )
      }
    }

    return Project(
      id = java.util.UUID.randomUUID().toString(),
      title = title,
      description = desc,
      backgroundHex = bg,
      objects = objects,
      variables = vars,
      isPreset = false
    )
  }

  private fun parseBrick(b: JSONObject): Brick {
    val opName = b.optString("op", "PLACE_AT_XY")
    val op = try { BrickOp.valueOf(opName) } catch (_: Exception) { BrickOp.PLACE_AT_XY }
    val childBricks = mutableListOf<Brick>()
    if (b.has("childBricks")) {
      val cArr = b.getJSONArray("childBricks")
      for (i in 0 until cArr.length()) {
        childBricks.add(parseBrick(cArr.getJSONObject(i)))
      }
    }
    return Brick(
      id = b.optString("id", java.util.UUID.randomUUID().toString()),
      op = op,
      paramString1 = b.optString("paramString1", ""),
      paramString2 = b.optString("paramString2", ""),
      paramString3 = b.optString("paramString3", ""),
      paramNum1 = b.optDouble("paramNum1", 0.0).toFloat(),
      paramNum2 = b.optDouble("paramNum2", 0.0).toFloat(),
      childBricks = childBricks
    )
  }

  /**
   * Imports a project from zip bytes (`.pcp` or `.catrobat`).
   */
  fun importZip(bytes: ByteArray): Project? {
    try {
      val zis = ZipInputStream(ByteArrayInputStream(bytes))
      var entry = zis.nextEntry
      var jsonStr: String? = null

      while (entry != null) {
        val name = entry.name.lowercase()
        if (name == "project.json" || name == "code.json" || name.endsWith(".json")) {
          jsonStr = zis.bufferedReader(StandardCharsets.UTF_8).readText()
          break
        }
        entry = zis.nextEntry
      }

      if (jsonStr != null) {
        return parseProjectJson(jsonStr)
      }
    } catch (_: Exception) {}
    return null
  }

  /**
   * Saves export bytes to app cache/files dir and returns the absolute path.
   */
  fun saveExportFile(context: Context, project: Project, isPcp: Boolean): File {
    val ext = if (isPcp) EXTENSION_PCP else EXTENSION_CATROBAT
    val safeTitle = project.title.replace(Regex("[^a-zA-Z0-9_]"), "_").take(24)
    val fileName = "$safeTitle$ext"
    val file = File(context.cacheDir, fileName)
    val bytes = if (isPcp) exportToPcpZip(project) else exportToCatrobatZip(project)
    FileOutputStream(file).use { it.write(bytes) }
    return file
  }

  fun exportToPcp(context: Context, project: Project): ByteArray = exportToPcpZip(project)
  fun exportToCatrobat(context: Context, project: Project): ByteArray = exportToCatrobatZip(project)
  fun importFromPcp(context: Context, bytes: ByteArray): Project? = importZip(bytes)
  fun importFromCatrobat(context: Context, bytes: ByteArray): Project? = importZip(bytes)
}
