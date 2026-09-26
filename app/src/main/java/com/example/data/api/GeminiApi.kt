package com.example.data.api

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Generates crisis coordination response, situational analysis, or dynamic re-allocation advice.
     * Supports high thinking mode (gemini-3.1-pro-preview) and Google Search / Maps grounding.
     */
    suspend fun generateCrisisResponse(
        prompt: String,
        enableHighThinking: Boolean = false,
        enableSearchGrounding: Boolean = false,
        conversationHistory: List<Pair<String, String>> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "DEFAULT_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("API_KEY_NOT_CONFIGURED"))
        }

        // Model selection according to specifications:
        // Complex / High-thinking tasks: gemini-3.1-pro-preview
        // General / Search grounding: gemini-3.5-flash
        val model = if (enableHighThinking) "gemini-3.1-pro-preview" else "gemini-3.5-flash"
        val endpoint = "$BASE_URL$model:generateContent?key=$apiKey"

        try {
            val rootJson = JSONObject()

            // 1. System Instruction for Crisis Command
            val systemContent = JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", "You are SurakshaSathi Tactical AI - an elite emergency response and disaster resource coordinator. " +
                                "Provide clear, decisive, life-saving recommendations with strict priority triage (Critical, High, Medium). " +
                                "Format with concise tactical bullet points, resource numbers (water, rations, trauma teams), and GIS route safety notes.")
                    })
                })
            }
            rootJson.put("systemInstruction", systemContent)

            // 2. Contents / History
            val contentsArray = JSONArray()
            for ((role, text) in conversationHistory) {
                contentsArray.put(JSONObject().apply {
                    put("role", if (role == "user") "user" else "model")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", text) })
                    })
                })
            }
            // Add current turn
            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", prompt) })
                })
            })
            rootJson.put("contents", contentsArray)

            // 3. Generation Config (Thinking Config)
            val genConfig = JSONObject()
            if (enableHighThinking && model == "gemini-3.1-pro-preview") {
                val thinkingConfig = JSONObject().apply {
                    put("thinkingLevel", "HIGH")
                }
                genConfig.put("thinkingConfig", thinkingConfig)
            } else {
                genConfig.put("temperature", 0.3)
            }
            rootJson.put("generationConfig", genConfig)

            // 4. Grounding Tools
            if (enableSearchGrounding) {
                val toolsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("googleSearch", JSONObject())
                    })
                }
                rootJson.put("tools", toolsArray)
            }

            val requestBody = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: $responseBody"))
            }

            val responseJson = JSONObject(responseBody)
            val candidates = responseJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val sb = StringBuilder()
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        val text = part.optString("text", "")
                        sb.append(text)
                    }
                    return@withContext Result.success(sb.toString())
                }
            }

            Result.failure(Exception("Empty candidate response from Gemini API"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
