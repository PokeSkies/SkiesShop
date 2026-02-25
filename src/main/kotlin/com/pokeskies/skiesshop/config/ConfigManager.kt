package com.pokeskies.skiesshop.config

import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.pokeskies.skiesshop.SkiesShop
import com.pokeskies.skiesshop.data.entry.ShopEntry
import com.pokeskies.skiesshop.utils.Utils
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.stream.Collectors

object ConfigManager {
    private var assetPackage = "assets/${SkiesShop.MOD_ID}"

    private val presetType = object : TypeToken<Map<String, ShopEntry>>() {}.type

    lateinit var CONFIG: MainConfig
    var SHOPS: MutableMap<String, ShopConfig> = mutableMapOf()
    var CONFIRM_MENUS: MutableMap<String, ConfirmMenuConfig> = mutableMapOf()

    var PRESETS: MutableMap<String, ShopEntry> = mutableMapOf()

    fun load() {
        // Load defaulted configs if they do not exist
        copyDefaults()

        // Load all files
        CONFIG = loadFile("config.json", MainConfig())

        // Load presets first so shops can reference them
        loadPresets()
        loadShops()
        loadConfirmMenus()
    }

    private fun copyDefaults() {
        val classLoader = SkiesShop::class.java.classLoader

        SkiesShop.INSTANCE.configDir.mkdirs()

        attemptDefaultFileCopy(classLoader, "config.json")
        attemptDefaultDirectoryCopy(classLoader, "shops")
        attemptDefaultDirectoryCopy(classLoader, "confirm_menus")
        attemptDefaultDirectoryCopy(classLoader, "presets")
    }

    fun <T : Any> loadFile(filename: String, default: T, create: Boolean = false): T {
        val file = File(SkiesShop.INSTANCE.configDir, filename)
        var value: T = default
        try {
            Files.createDirectories(SkiesShop.INSTANCE.configDir.toPath())
            if (file.exists()) {
                FileReader(file).use { reader ->
                    val jsonReader = JsonReader(reader)
                    value = SkiesShop.INSTANCE.gsonPretty.fromJson(jsonReader, default::class.java)
                }
            } else if (create) {
                Files.createFile(file.toPath())
                FileWriter(file).use { fileWriter ->
                    fileWriter.write(SkiesShop.INSTANCE.gsonPretty.toJson(default))
                    fileWriter.flush()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return value
    }

    fun <T> saveFile(filename: String, `object`: T): Boolean {
        val dir = SkiesShop.INSTANCE.configDir
        val file = File(dir, filename)
        try {
            FileWriter(file).use { fileWriter ->
                fileWriter.write(SkiesShop.INSTANCE.gsonPretty.toJson(`object`))
                fileWriter.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    private fun attemptDefaultFileCopy(classLoader: ClassLoader, fileName: String) {
        val file = SkiesShop.INSTANCE.configDir.resolve(fileName)
        if (!file.exists()) {
            try {
                val stream = classLoader.getResourceAsStream("${assetPackage}/$fileName")
                    ?: throw NullPointerException("File not found $fileName")

                Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } catch (e: Exception) {
                Utils.printError("Failed to copy the default file '$fileName': $e")
            }
        }
    }

    private fun attemptDefaultDirectoryCopy(classLoader: ClassLoader, directoryName: String) {
        val directory = SkiesShop.INSTANCE.configDir.resolve(directoryName)
        if (!directory.exists()) {
            directory.mkdirs()
            try {
                val sourceUrl = classLoader.getResource("${assetPackage}/$directoryName")
                    ?: throw NullPointerException("Directory not found $directoryName")
                val sourcePath = Paths.get(sourceUrl.toURI())

                Files.walk(sourcePath).use { stream ->
                    stream.filter { Files.isRegularFile(it) }
                        .forEach { sourceFile ->
                            val destinationFile = directory.resolve(sourcePath.relativize(sourceFile).toString())
                            Files.copy(sourceFile, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                        }
                }
            } catch (e: Exception) {
                Utils.printError("Failed to copy the default directory '$directoryName': " + e.message)
            }
        }
    }

    private fun loadShops() {
        SHOPS = mutableMapOf()

        val dir = SkiesShop.INSTANCE.configDir.resolve("shops")
        if (dir.exists() && dir.isDirectory) {
            val files = Files.walk(dir.toPath())
                .filter { path: Path -> Files.isRegularFile(path) }
                .map { it.toFile() }
                .collect(Collectors.toList())
            if (files != null) {
                SkiesShop.LOGGER.info("Found ${files.size} Shop files: ${files.map { it.name }}")
                val enabledFiles = mutableListOf<String>()
                for (file in files) {
                    val fileName = file.name
                    if (file.isFile && fileName.contains(".json")) {
                        val id = fileName.substring(0, fileName.lastIndexOf(".json"))
                        val jsonReader = JsonReader(InputStreamReader(FileInputStream(file), Charsets.UTF_8))
                        try {
                            val config = SkiesShop.INSTANCE.gsonPretty.fromJson(JsonParser.parseReader(jsonReader), ShopConfig::class.java)
                            config.id = id
                            SHOPS[id] = config
                            enabledFiles.add(fileName)
                        } catch (ex: Exception) {
                            Utils.printError("Error while trying to parse the shop $fileName!")
                            ex.printStackTrace()
                        }
                    } else {
                        Utils.printError("File $fileName is either not a file or is not a .json file!")
                    }
                }
                Utils.printInfo("Successfully read and loaded the following enabled shop files: $enabledFiles")
            }
        } else {
            Utils.printError("The 'skins' directory either does not exist or is not a directory!")
        }
    }

    private fun loadConfirmMenus() {
        CONFIRM_MENUS = mutableMapOf()

        val dir = SkiesShop.INSTANCE.configDir.resolve("confirm_menus")
        if (dir.exists() && dir.isDirectory) {
            val files = Files.walk(dir.toPath())
                .filter { path: Path -> Files.isRegularFile(path) }
                .map { it.toFile() }
                .collect(Collectors.toList())
            if (files != null) {
                SkiesShop.LOGGER.info("Found ${files.size} Confirm Menu files: ${files.map { it.name }}")
                val enabledFiles = mutableListOf<String>()
                for (file in files) {
                    val fileName = file.name
                    if (file.isFile && fileName.contains(".json")) {
                        val id = fileName.substring(0, fileName.lastIndexOf(".json"))
                        val jsonReader = JsonReader(InputStreamReader(FileInputStream(file), Charsets.UTF_8))
                        try {
                            val config = SkiesShop.INSTANCE.gsonPretty.fromJson(JsonParser.parseReader(jsonReader), ConfirmMenuConfig::class.java)
                            config.id = id
                            CONFIRM_MENUS[id] = config
                            enabledFiles.add(fileName)
                        } catch (ex: Exception) {
                            Utils.printError("Error while trying to parse the Confirm Menu $fileName!")
                            ex.printStackTrace()
                        }
                    } else {
                        Utils.printError("File $fileName is either not a file or is not a .json file!")
                    }
                }
                Utils.printInfo("Successfully read and loaded the following Confirm Menu files: $enabledFiles")
            }
        } else {
            Utils.printError("The 'confirm_menus' directory either does not exist or is not a directory!")
        }
    }

    private fun loadPresets() {
        PRESETS.clear()

        val dir = SkiesShop.INSTANCE.configDir.resolve("presets")
        if (dir.exists() && dir.isDirectory) {
            val filePaths = Files.walk(dir.toPath())
                .filter { p: Path -> p.toString().endsWith(".json") }
                .collect(Collectors.toList())

            for (filePath in filePaths) {
                val file = filePath.toFile()
                if (file.isFile) {
                    val jsonReader = JsonReader(InputStreamReader(FileInputStream(file), Charsets.UTF_8))
                    try {
                        SkiesShop.LOGGER.info("Attempting to read Preset file ${file.name}...")

                        val map: Map<String, ShopEntry>? = SkiesShop.INSTANCE.gson.fromJson(JsonParser.parseReader(jsonReader), presetType)

                        map?.forEach { (key, entry) ->
                            try {
                                entry.id = key
                                entry.isPreset = false
                                PRESETS[key] = entry
                            } catch (e: Exception) {
                                Utils.printError("Failed to load preset entry '$key' from file '${file.name}': ${e.message}")
                            }
                        }

                        Utils.printInfo("Successfully read and loaded the Preset file ${file.name}!")
                    } catch (ex: Exception) {
                        Utils.printError("Error while trying to parse the file ${file.name} as a Preset!")
                        ex.printStackTrace()
                    }
                }
            }
        } else {
            Utils.printError("The `presets` directory either does not exist or is not a directory!")
        }
    }
}
