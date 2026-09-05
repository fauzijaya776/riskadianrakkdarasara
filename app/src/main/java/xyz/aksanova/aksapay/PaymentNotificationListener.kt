package xyz.aksanova.aksapay

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class PaymentNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            val extras = sbn.notification.extras ?: return
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            val message = (bigText ?: text).trim()

            if (message.isBlank()) return

            // Hanya teruskan notifikasi yang kemungkinan berisi nominal pembayaran,
            // untuk mengurangi noise & menjaga privasi notifikasi lain.
            if (!message.contains("Rp", ignoreCase = true) &&
                !title.contains("Rp", ignoreCase = true)
            ) return

            Sender.send(applicationContext, sbn.packageName, title, message)
        } catch (_: Exception) {
            // abaikan error agar service tetap stabil
        }
    }
}
