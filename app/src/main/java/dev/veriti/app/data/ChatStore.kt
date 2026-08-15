package dev.veriti.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class ChatStore(context: Context) {
    private val prefs = context.getSharedPreferences("veriti_data", Context.MODE_PRIVATE)

    fun loadChats(): List<Chat> = runCatching {
        val array = JSONArray(prefs.getString("chats", "[]"))
        List(array.length()) { i ->
            val json = array.getJSONObject(i)
            val messagesJson = json.getJSONArray("messages")
            Chat(
                id = json.getLong("id"),
                title = json.getString("title"),
                updatedAt = json.getLong("updatedAt"),
                messages = List(messagesJson.length()) { j ->
                    val message = messagesJson.getJSONObject(j)
                    Message(
                        id = message.getLong("id"),
                        role = message.getString("role"),
                        content = message.getString("content"),
                        createdAt = message.getLong("createdAt")
                    )
                }
            )
        }.sortedByDescending { it.updatedAt }
    }.getOrDefault(emptyList())

    fun saveChats(chats: List<Chat>) {
        val array = JSONArray()
        chats.forEach { chat ->
            val messages = JSONArray()
            chat.messages.forEach { message ->
                messages.put(JSONObject().apply {
                    put("id", message.id); put("role", message.role)
                    put("content", message.content); put("createdAt", message.createdAt)
                })
            }
            array.put(JSONObject().apply {
                put("id", chat.id); put("title", chat.title)
                put("updatedAt", chat.updatedAt); put("messages", messages)
            })
        }
        prefs.edit().putString("chats", array.toString()).apply()
    }

    fun loadSettings(): AppSettings = AppSettings(
        providerName = prefs.getString("provider", "OpenAI") ?: "OpenAI",
        baseUrl = prefs.getString("baseUrl", "https://api.openai.com/v1") ?: "https://api.openai.com/v1",
        apiKey = prefs.getString("apiKey", "") ?: "",
        model = prefs.getString("model", "gpt-4.1-mini") ?: "gpt-4.1-mini",
        language = "ru",
        systemPrompt = AppSettings().systemPrompt
    )

    fun saveSettings(value: AppSettings) {
        prefs.edit()
            .putString("provider", value.providerName)
            .putString("baseUrl", value.baseUrl)
            .putString("apiKey", value.apiKey)
            .putString("model", value.model)
            .putString("language", value.language)
            .apply()
    }
}
