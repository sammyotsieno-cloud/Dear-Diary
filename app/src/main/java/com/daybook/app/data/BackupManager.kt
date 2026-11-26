package com.daybook.app.data

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime

/** Saves everything to one JSON file and restores it. */
class BackupManager(context: Context) {

    private val resolver = context.applicationContext.contentResolver
    private val db = DaybookDatabase.get(context)
    private val dao = db.dao()

    /** Writes the backup to [uri]. Returns how many items were saved. */
    suspend fun export(uri: Uri): Int = withContext(Dispatchers.IO) {
        val diary = dao.getAllDiary()
        val money = dao.getAllMoney()

        val diaryJson = JSONArray()
        diary.forEach {
            diaryJson.put(
                JSONObject()
                    .put("date", it.date)
                    .put("note", it.note)
                    .put("mood", it.mood)
            )
        }
        val moneyJson = JSONArray()
        money.forEach {
            moneyJson.put(
                JSONObject()
                    .put("date", it.date)
                    .put("amountCents", it.amountCents)
                    .put("isExpense", it.isExpense)
                    .put("category", it.category)
                    .put("label", it.label)
            )
        }

        val root = JSONObject()
            .put("app", "daybook")
            .put("version", 1)
            .put("exportedAt", LocalDateTime.now().toString())
            .put("diary", diaryJson)
            .put("money", moneyJson)

        val stream = resolver.openOutputStream(uri, "wt")
            ?: throw IllegalStateException("Could not open the file")
        stream.use { it.write(root.toString(2).toByteArray(Charsets.UTF_8)) }

        diary.size + money.size
    }

    /** Replaces ALL current data with the backup in [uri]. Returns how many items were restored. */
    suspend fun import(uri: Uri): Int = withContext(Dispatchers.IO) {
        val stream = resolver.openInputStream(uri)
            ?: throw IllegalStateException("Could not open the file")
        val text = stream.use { it.readBytes().toString(Charsets.UTF_8) }

        val root = JSONObject(text)
        require(root.optString("app") == "daybook") { "This is not a Daybook backup file" }

        val diaryArray = root.getJSONArray("diary")
        val diary = (0 until diaryArray.length()).map { i ->
            val o = diaryArray.getJSONObject(i)
            LocalDate.parse(o.getString("date")) // rejects bad dates
            DiaryEntry(
                date = o.getString("date"),
                note = o.optString("note"),
                mood = o.optString("mood")
            )
        }

        val moneyArray = root.getJSONArray("money")
        val money = (0 until moneyArray.length()).map { i ->
            val o = moneyArray.getJSONObject(i)
            LocalDate.parse(o.getString("date"))
            MoneyEntry(
                id = 0,
                date = o.getString("date"),
                amountCents = o.getLong("amountCents"),
                isExpense = o.getBoolean("isExpense"),
                category = o.getString("category"),
                label = o.optString("label")
            )
        }

        // All or nothing: if anything fails, the old data stays.
        db.withTransaction {
            dao.clearDiary()
            dao.clearMoney()
            dao.insertDiaryAll(diary)
            dao.insertMoneyAll(money)
        }

        diary.size + money.size
    }
}
