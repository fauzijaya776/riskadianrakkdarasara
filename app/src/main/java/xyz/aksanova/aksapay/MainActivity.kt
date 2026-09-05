package xyz.aksanova.aksapay

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var serverUrl: EditText
    private lateinit var secretKey: EditText
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        serverUrl = findViewById(R.id.serverUrl)
        secretKey = findViewById(R.id.secretKey)
        status = findViewById(R.id.status)

        val prefs = Sender.getPrefs(this)
        serverUrl.setText(prefs.getString("server_url", ""))
        secretKey.setText(prefs.getString("secret_key", ""))

        findViewById<Button>(R.id.saveBtn).setOnClickListener {
            prefs.edit()
                .putString("server_url", serverUrl.text.toString().trim())
                .putString("secret_key", secretKey.text.toString().trim())
                .apply()
            Toast.makeText(this, "Pengaturan disimpan", Toast.LENGTH_SHORT).show()
            updateStatus()
        }

        findViewById<Button>(R.id.testBtn).setOnClickListener {
            if (!Sender.isConfigured(this)) {
                Toast.makeText(this, "Simpan URL & Secret dulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Sender.send(
                this,
                "xyz.aksanova.aksapay.test",
                "Uji Coba",
                "Notifikasi uji: Anda menerima Rp1 dari AksaPay"
            ) { ok, info ->
                status.text = if (ok) "Uji berhasil: $info" else "Uji gagal: $info"
            }
        }

        findViewById<Button>(R.id.permBtn).setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun isListenerEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(packageName)
    }

    private fun updateStatus() {
        val configured = Sender.isConfigured(this)
        val listener = isListenerEnabled()
        status.text = buildString {
            append("Konfigurasi: ")
            append(if (configured) "OK" else "belum")
            append("\nAkses notifikasi: ")
            append(if (listener) "AKTIF" else "belum diizinkan")
            if (configured && listener) append("\n\nSiap. Notifikasi pembayaran akan diteruskan otomatis.")
        }
    }
}
