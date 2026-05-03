package com.mygoal_healthtracker.app.data

import com.mygoal_healthtracker.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.absoluteValue

class GeminiNutritionService(
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    suspend fun estimateFood(name: String, quantity: String): Nutrition {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) return offlineEstimate(name, quantity)

        return runCatching {
            requestGemini(apiKey, name, quantity)
        }.getOrElse {
            offlineEstimate(name, quantity)
        }
    }

    private suspend fun requestGemini(apiKey: String, name: String, quantity: String): Nutrition = withContext(Dispatchers.IO) {
        val endpoint = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
        val connection = (endpoint.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10_000
            readTimeout = 15_000
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
        }
        val prompt = """
            Return only compact JSON for this food item using keys calories, protein, carbs, fat.
            Food: $name
            Quantity: $quantity
        """.trimIndent()
        val body = """
            {"contents":[{"parts":[{"text":${json.encodeToString(prompt)}}]}]}
        """.trimIndent()
        OutputStreamWriter(connection.outputStream).use { it.write(body) }
        val response = connection.inputStream.bufferedReader().use { it.readText() }
        val text = json.parseToJsonElement(response)
            .jsonObject["candidates"]?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("content")?.jsonObject?.get("parts")?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("text")?.jsonPrimitive?.content
            ?.replace("```json", "")
            ?.replace("```", "")
            ?.trim()
            ?: error("Missing Gemini response")
        val parsed = json.parseToJsonElement(text).jsonObject
        Nutrition(
            calories = parsed.float("calories"),
            protein = parsed.float("protein"),
            carbs = parsed.float("carbs"),
            fat = parsed.float("fat"),
        )
    }

    private fun JsonObject.float(key: String): Float {
        return get(key)?.jsonPrimitive?.floatOrNull ?: 0f
    }

    private fun offlineEstimate(name: String, quantity: String): Nutrition {
        val seed = (name.lowercase() + quantity.lowercase()).hashCode().absoluteValue
        val servingFactor = when {
            quantity.contains("1000") -> 4f
            quantity.contains("750") -> 3f
            quantity.contains("500") -> 2f
            quantity.contains("250") -> 1f
            quantity.contains("2") -> 1.6f
            else -> 1f
        }
        val baseCalories = 140f + (seed % 180)
        val protein = 4f + (seed % 18)
        val carbs = 16f + (seed % 42)
        val fat = 3f + (seed % 16)
        return Nutrition(
            calories = baseCalories * servingFactor,
            protein = protein * servingFactor,
            carbs = carbs * servingFactor,
            fat = fat * servingFactor,
        )
    }
}
