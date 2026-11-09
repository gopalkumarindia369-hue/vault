package com.notivault.app.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.notivault.app.data.local.entity.NotificationEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtil {

    fun exportToJson(context: Context, notifications: List<NotificationEntity>): File {
        val jsonArray = JSONArray()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        for (item in notifications) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("packageName", item.packageName)
                put("appName", item.appName)
                put("title", item.title ?: "")
                put("text", item.text ?: "")
                put("bigText", item.bigText ?: "")
                put("subText", item.subText ?: "")
                put("timestamp", item.timestamp)
                put("formattedDate", dateFormat.format(Date(item.timestamp)))
                put("category", item.category ?: "")
                put("channelId", item.channelId ?: "")
                put("isIntercepted", item.isIntercepted)
                put("isOngoing", item.isOngoing)
            }
            jsonArray.put(obj)
        }

        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "notivault_export_${System.currentTimeMillis()}.json")
        file.writeText(jsonArray.toString(2))
        return file
    }

    fun exportToCsv(context: Context, notifications: List<NotificationEntity>): File {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val sb = StringBuilder()
        sb.append("ID,PackageName,AppName,Title,Text,SubText,Timestamp,Date,Category,Intercepted,Ongoing\n")

        for (item in notifications) {
            sb.append("${item.id},")
            sb.append(""${escapeCsv(item.packageName)}",")
            sb.append(""${escapeCsv(item.appName)}",")
            sb.append(""${escapeCsv(item.title ?: "")}",")
            sb.append(""${escapeCsv(item.text ?: "")}",")
            sb.append(""${escapeCsv(item.subText ?: "")}",")
            sb.append("${item.timestamp},")
            sb.append(""${dateFormat.format(Date(item.timestamp))}",")
            sb.append(""${escapeCsv(item.category ?: "")}",")
            sb.append("${item.isIntercepted},")
            sb.append("${item.isOngoing}\n")
        }

        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "notivault_export_${System.currentTimeMillis()}.csv")
        file.writeText(sb.toString())
        return file
    }

    private fun escapeCsv(str: String): String {
        return str.replace(""", """").replace("\n", " ").replace("\r", "")
    }

    fun shareExportedFile(context: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export Notifications"))
    }
}
