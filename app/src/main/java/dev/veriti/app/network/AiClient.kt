package dev.veriti.app.network

import dev.veriti.app.data.AppSettings
import dev.veriti.app.data.Message
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class AiReply(val text: String, val mood: String)
data class RemoteModel(val id: String, val created: Long = 0L)

class AiClient {
    fun complete(settings: AppSettings, history: List<Message>): AiReply {
        val endpoint = settings.baseUrl.trimEnd('/') + "/chat/completions"
        val connection = URL(endpoint).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 25_000
            connection.readTimeout = 120_000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            if (settings.apiKey.isNotBlank()) {
                connection.setRequestProperty("Authorization", "Bearer ${settings.apiKey.trim()}")
            }

            val messages = JSONArray().put(
                JSONObject().put("role", "system").put(
                    "content",
                    settings.systemPrompt + "\n\nЯзык интерфейса: русский. " +
                        "Если пользователь не перешёл на другой язык, отвечай на русском.\n" +
                        "Перед каждой репликой ставь ровно один технический тег: " +
                        "[mood:happy], [mood:angry], [mood:sad] или [mood:normal]. " +
                        "После тега сразу пиши ответ. Пользователь тег не увидит."
                )
            )
            history.takeLast(40).forEach {
                messages.put(JSONObject().put("role", it.role).put("content", it.content))
            }
            val body = JSONObject()
                .put("model", settings.model.trim())
                .put("messages", messages)
                .put("temperature", 0.7)
                .put("stream", false)
                .toString()
            connection.outputStream.bufferedWriter().use { it.write(body) }

            val code = connection.responseCode
            val response = (if (code in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                val detail = runCatching {
                    JSONObject(response).optJSONObject("error")?.optString("message")
                }.getOrNull().orEmpty()
                error("API $code${if (detail.isNotBlank()) ": $detail" else ""}")
            }
            val raw = JSONObject(response)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()
            val moodPattern = Regex("^\\s*\\[mood:(happy|angry|sad|normal)]\\s*", RegexOption.IGNORE_CASE)
            val mood = moodPattern.find(raw)?.groupValues?.getOrNull(1)?.lowercase() ?: "happy"
            return AiReply(text = raw.replaceFirst(moodPattern, "").trim(), mood = mood)
        } finally {
            connection.disconnect()
        }
    }

    fun listModels(settings: AppSettings): List<RemoteModel> {
        val connection = URL(settings.baseUrl.trimEnd('/') + "/models").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 20_000
            connection.readTimeout = 30_000
            if (settings.apiKey.isNotBlank()) {
                connection.setRequestProperty("Authorization", "Bearer ${settings.apiKey.trim()}")
            }
            val code = connection.responseCode
            val response = (if (code in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) error("API $code: список моделей недоступен")
            val data = JSONObject(response).optJSONArray("data") ?: JSONArray()
            val allModels = List(data.length()) { index ->
                val item = data.getJSONObject(index)
                RemoteModel(item.getString("id"), item.optLong("created", 0L))
            }.distinctBy { it.id }
            val nonChatMarkers = listOf("embedding", "whisper", "tts", "dall-e", "image", "moderation", "transcribe", "rerank")
            val chatModels = allModels.filter { model ->
                nonChatMarkers.none { marker -> model.id.contains(marker, ignoreCase = true) }
            }
            return (chatModels.ifEmpty { allModels })
                .sortedWith(compareByDescending<RemoteModel> { it.created }.thenByDescending { it.id })
        } finally {
            connection.disconnect()
        }
    }
}
