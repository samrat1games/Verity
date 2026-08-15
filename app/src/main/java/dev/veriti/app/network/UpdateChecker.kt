package dev.veriti.app.network

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ReleaseUpdate(
    val name: String,
    val pageUrl: String,
    val apkUrl: String? = null,
    val apkName: String? = null
) {
    val hasApk: Boolean get() = !apkUrl.isNullOrBlank()
}

class UpdateChecker {
    fun check(): ReleaseUpdate {
        val connection = URL(RELEASE_API).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 20_000
            connection.readTimeout = 25_000
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("User-Agent", "VerityDroid/0.1.0")
            val code = connection.responseCode
            val body = (if (code in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) error("GitHub $code")
            val json = JSONObject(body)
            val assets = json.optJSONArray("assets")
            var apkUrl: String? = null
            var apkName: String? = null
            if (assets != null) {
                for (index in 0 until assets.length()) {
                    val asset = assets.getJSONObject(index)
                    val name = asset.optString("name")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkUrl = asset.optString("browser_download_url").takeIf { it.isNotBlank() }
                        apkName = name
                        break
                    }
                }
            }
            return ReleaseUpdate(
                name = json.optString("name", "Verity"),
                pageUrl = json.optString("html_url", RELEASE_PAGE),
                apkUrl = apkUrl,
                apkName = apkName
            )
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        const val RELEASE_PAGE = "https://github.com/samrat1games/Verity/releases/tag/Verity"
        private const val RELEASE_API = "https://api.github.com/repos/samrat1games/Verity/releases/tags/Verity"
    }
}
