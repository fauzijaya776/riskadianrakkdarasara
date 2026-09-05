package xyz.aksanova.aksapay

import android.content.Context
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object Sender {

    fun getPrefs(ctx: Context) = ctx.getSharedPreferences("aksapay", Context.MODE_PRIVATE)

    fun isConfigured(ctx: Context): Boolean {
        val p = getPrefs(ctx)
        val base = p.getString("server_url", "") ?: ""
        val secret = p.getString("secret_key", "") ?: ""
        return base.isNotBlank() && secret.isNotBlank()
    }

    fun send(
        ctx: Context,
        pkg: String,
        title: String,
        message: String,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val p = getPrefs(ctx)
        val base = (p.getString("server_url", "") ?: "").trim().trimEnd('/')
        val secret = (p.getString("secret_key", "") ?: "").trim()
        if (base.isBlank() || secret.isBlank()) {
            onResult(false, "Belum dikonfigurasi (URL/Secret kosong).")
            return
        }

        Thread {
            var ok = false
            var info: String
            try {
                val url = URL("$base/api/notify")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 10000
                conn.readTimeout = 10000
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("X-Secret-Key", secret)

                val body = JSONObject().apply {
                    put("package", pkg)
                    put("title", title)
                    put("message", message)
                }.toString()

                conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val resp = stream?.bufferedReader()?.use { it.readText() } ?: ""
                conn.disconnect()
                ok = code in 200..299
                info = "HTTP $code — $resp"
            } catch (e: Exception) {
                info = e.message ?: "error jaringan"
            }
            Handler(Looper.getMainLooper()).post { onResult(ok, info) }
        }.start()
    }
}
