package com.example.freyja

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FreyjaDbHelper(context: Context) : SQLiteOpenHelper(context, DB, null, VER) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE residents(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              apartment INTEGER UNIQUE NOT NULL,
              full_name TEXT NOT NULL,
              area REAL NOT NULL,
              entrance INTEGER NOT NULL,
              debt REAL NOT NULL,
              pin_hash TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE payments(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              resident_id INTEGER NOT NULL,
              date TEXT NOT NULL,
              amount REAL NOT NULL,
              note TEXT,
              FOREIGN KEY(resident_id) REFERENCES residents(id)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE messages(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              title TEXT NOT NULL,
              body TEXT NOT NULL,
              date TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE requests(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              resident_id INTEGER NOT NULL,
              topic TEXT NOT NULL,
              body TEXT NOT NULL,
              date TEXT NOT NULL,
              status TEXT NOT NULL,
              FOREIGN KEY(resident_id) REFERENCES residents(id)
            )
        """.trimIndent())

        db.execSQL("""
    CREATE TABLE polls(
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      title TEXT NOT NULL,
      body TEXT,
      deadline TEXT
    )
        """.trimIndent())

        db.execSQL("""
    CREATE TABLE poll_options(
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      poll_id INTEGER NOT NULL,
      text TEXT NOT NULL,
      FOREIGN KEY(poll_id) REFERENCES polls(id)
    )
        """.trimIndent())

        db.execSQL("""
    CREATE TABLE poll_votes(
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      poll_id INTEGER NOT NULL,
      resident_id INTEGER NOT NULL,
      option_id INTEGER NOT NULL,
      date TEXT NOT NULL,
      UNIQUE(poll_id, resident_id),
      FOREIGN KEY(poll_id) REFERENCES polls(id),
      FOREIGN KEY(resident_id) REFERENCES residents(id),
      FOREIGN KEY(option_id) REFERENCES poll_options(id)
    )
        """.trimIndent())


        seedDemo(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS requests")
        db.execSQL("DROP TABLE IF EXISTS messages")
        db.execSQL("DROP TABLE IF EXISTS payments")
        db.execSQL("DROP TABLE IF EXISTS residents")
        db.execSQL("DROP TABLE IF EXISTS poll_votes")
        db.execSQL("DROP TABLE IF EXISTS poll_options")
        db.execSQL("DROP TABLE IF EXISTS polls")
        onCreate(db)
    }

    // ---------- PIN helpers ----------
    private fun hashPin(pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(pin.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Перевірка входу: квартира + PIN
     * Повертає Resident, якщо квартира існує і PIN правильний.
     */
    fun checkPin(apartment: Int, pin: String): Resident? {
        readableDatabase.rawQuery(
            "SELECT * FROM residents WHERE apartment=?",
            arrayOf(apartment.toString())
        ).use { c ->
            if (!c.moveToFirst()) return null
            val stored = c.getString(c.getColumnIndexOrThrow("pin_hash"))
            if (stored != hashPin(pin)) return null

            return Resident(
                id = c.getLong(c.getColumnIndexOrThrow("id")),
                apartment = c.getInt(c.getColumnIndexOrThrow("apartment")),
                fullName = c.getString(c.getColumnIndexOrThrow("full_name")),
                area = c.getDouble(c.getColumnIndexOrThrow("area")),
                entrance = c.getInt(c.getColumnIndexOrThrow("entrance")),
                debt = c.getDouble(c.getColumnIndexOrThrow("debt"))
            )
        }
    }

    private fun seedDemo(db: SQLiteDatabase) {
        fun insResident(ap: Int, name: String, area: Double, ent: Int, debt: Double, pin: String) {
            val cv = ContentValues().apply {
                put("apartment", ap)
                put("full_name", name)
                put("area", area)
                put("entrance", ent)
                put("debt", debt)
                put("pin_hash", hashPin(pin))
            }
            db.insert("residents", null, cv)
        }

        insResident(306, "Ященко Богдан Віталійович", 54.3, 1, 1250.50, "1234")
        insResident(407, "Кушнерчук Іван Віталійович", 63.0, 2, 0.0, "0000")
        insResident(505, "Тахтаров Сергій Олександрович", 57.8, 3, 1300.50, "1737")

        db.insert("messages", null, ContentValues().apply {
            put("title", "Збори мешканців")
            put("body", "15.12 о 18:00 у дворі. Питання: тариф, ремонт даху.")
            put("date", "2025-12-15")
        })
        db.insert("messages", null, ContentValues().apply {
            put("title", "Аварійне відключення води")
            put("body", "З 10:00 до 14:00 буде відключено воду (ремонт).")
            put("date", "2025-12-16")
        })

        db.insert("payments", null, ContentValues().apply {
            put("resident_id", 1); put("date", "2025-11-01"); put("amount", 500.0); put("note", "Квартплата")
        })
        db.insert("payments", null, ContentValues().apply {
            put("resident_id", 1); put("date", "2025-12-01"); put("amount", 400.0); put("note", "Ремонтний фонд")
        })

        db.insert("requests", null, ContentValues().apply {
            put("resident_id", 1)
            put("topic", "Не працює лампа на поверсі")
            put("body", "Прошу замінити лампу на 3 поверсі біля ліфта.")
            put("date", "2025-12-10")
            put("status", "Надіслано")
        })
        // ---- DEMO голосування ----
        db.insert("polls", null, ContentValues().apply {
            put("title", "Ремонт ліфта")
            put("body", "Чи підтримуєте ремонт ліфта у 1 підʼїзді?")
            put("deadline", "2025-12-31")
        })

        db.insert("poll_options", null, ContentValues().apply {
            put("poll_id", 1)
            put("text", "Підтримую")
        })
        db.insert("poll_options", null, ContentValues().apply {
            put("poll_id", 1)
            put("text", "Не підтримую")
        })

    }

    // ---------- existing helpers ----------
    fun findResidentByApartment(apartment: Int): Resident? {
        readableDatabase.rawQuery(
            "SELECT * FROM residents WHERE apartment=?",
            arrayOf(apartment.toString())
        ).use { c ->
            if (!c.moveToFirst()) return null
            return Resident(
                id = c.getLong(c.getColumnIndexOrThrow("id")),
                apartment = c.getInt(c.getColumnIndexOrThrow("apartment")),
                fullName = c.getString(c.getColumnIndexOrThrow("full_name")),
                area = c.getDouble(c.getColumnIndexOrThrow("area")),
                entrance = c.getInt(c.getColumnIndexOrThrow("entrance")),
                debt = c.getDouble(c.getColumnIndexOrThrow("debt"))
            )
        }
    }

    fun getResidentById(residentId: Long): Resident? {
        readableDatabase.rawQuery(
            "SELECT * FROM residents WHERE id=?",
            arrayOf(residentId.toString())
        ).use { c ->
            if (!c.moveToFirst()) return null
            return Resident(
                id = c.getLong(c.getColumnIndexOrThrow("id")),
                apartment = c.getInt(c.getColumnIndexOrThrow("apartment")),
                fullName = c.getString(c.getColumnIndexOrThrow("full_name")),
                area = c.getDouble(c.getColumnIndexOrThrow("area")),
                entrance = c.getInt(c.getColumnIndexOrThrow("entrance")),
                debt = c.getDouble(c.getColumnIndexOrThrow("debt"))
            )
        }
    }

    fun getResidentDebt(residentId: Long): Double {
        readableDatabase.rawQuery(
            "SELECT debt FROM residents WHERE id=?",
            arrayOf(residentId.toString())
        ).use { c ->
            if (!c.moveToFirst()) return 0.0
            return c.getDouble(0)
        }
    }

    fun getPayments(residentId: Long): List<Payment> {
        val list = mutableListOf<Payment>()
        readableDatabase.rawQuery(
            "SELECT * FROM payments WHERE resident_id=? ORDER BY date DESC",
            arrayOf(residentId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                list.add(
                    Payment(
                        id = c.getLong(c.getColumnIndexOrThrow("id")),
                        residentId = c.getLong(c.getColumnIndexOrThrow("resident_id")),
                        date = c.getString(c.getColumnIndexOrThrow("date")),
                        amount = c.getDouble(c.getColumnIndexOrThrow("amount")),
                        note = c.getString(c.getColumnIndexOrThrow("note"))
                    )
                )
            }
        }
        return list
    }

    fun getMessages(): List<Message> {
        val list = mutableListOf<Message>()
        readableDatabase.rawQuery(
            "SELECT * FROM messages ORDER BY date DESC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                list.add(
                    Message(
                        id = c.getLong(c.getColumnIndexOrThrow("id")),
                        title = c.getString(c.getColumnIndexOrThrow("title")),
                        body = c.getString(c.getColumnIndexOrThrow("body")),
                        date = c.getString(c.getColumnIndexOrThrow("date"))
                    )
                )
            }
        }
        return list
    }

    fun addRequest(residentId: Long, topic: String, body: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())

        val cv = ContentValues().apply {
            put("resident_id", residentId)
            put("topic", topic)
            put("body", body)
            put("date", today)
            put("status", "Надіслано")
        }
        return writableDatabase.insert("requests", null, cv)
    }

    fun getRequests(residentId: Long): List<Request> {
        val list = mutableListOf<Request>()
        readableDatabase.rawQuery(
            "SELECT * FROM requests WHERE resident_id=? ORDER BY date DESC",
            arrayOf(residentId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                list.add(
                    Request(
                        id = c.getLong(c.getColumnIndexOrThrow("id")),
                        residentId = c.getLong(c.getColumnIndexOrThrow("resident_id")),
                        topic = c.getString(c.getColumnIndexOrThrow("topic")),
                        body = c.getString(c.getColumnIndexOrThrow("body")),
                        date = c.getString(c.getColumnIndexOrThrow("date")),
                        status = c.getString(c.getColumnIndexOrThrow("status"))
                    )
                )
            }
        }
        return list
    }

    fun makePayment(residentId: Long, amount: Double, note: String?): Boolean {
        if (residentId <= 0) return false
        if (amount <= 0.0) return false

        val db = writableDatabase
        db.beginTransaction()
        try {
            val currentDebt = getResidentDebt(residentId)

            // ✅ Дозволяємо переплату: борг може стати < 0 (аванс)
            val newDebt = currentDebt - amount

            val cvDebt = ContentValues().apply {
                put("debt", newDebt)
            }
            val rows = db.update("residents", cvDebt, "id=?", arrayOf(residentId.toString()))
            if (rows != 1) return false

            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val today = sdf.format(java.util.Date())

            val cvPay = ContentValues().apply {
                put("resident_id", residentId)
                put("date", today)
                put("amount", amount)
                put("note", note ?: "Оплата боргу ОСББ")
            }
            val id = db.insert("payments", null, cvPay)
            if (id <= 0) return false

            db.setTransactionSuccessful()
            return true
        } finally {
            db.endTransaction()
        }
    }

    fun getPolls(): List<Poll> {
        val list = mutableListOf<Poll>()
        readableDatabase.rawQuery(
            "SELECT * FROM polls ORDER BY id DESC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                list.add(
                    Poll(
                        id = c.getLong(c.getColumnIndexOrThrow("id")),
                        title = c.getString(c.getColumnIndexOrThrow("title")),
                        body = c.getString(c.getColumnIndexOrThrow("body")),
                        deadline = c.getString(c.getColumnIndexOrThrow("deadline"))
                    )
                )
            }
        }
        return list
    }

    fun getPollOptions(pollId: Long): List<PollOption> {
        val list = mutableListOf<PollOption>()
        readableDatabase.rawQuery(
            "SELECT * FROM poll_options WHERE poll_id=?",
            arrayOf(pollId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                list.add(
                    PollOption(
                        id = c.getLong(c.getColumnIndexOrThrow("id")),
                        pollId = pollId,
                        text = c.getString(c.getColumnIndexOrThrow("text"))
                    )
                )
            }
        }
        return list
    }

    fun hasVoted(pollId: Long, residentId: Long): Boolean {
        readableDatabase.rawQuery(
            "SELECT 1 FROM poll_votes WHERE poll_id=? AND resident_id=?",
            arrayOf(pollId.toString(), residentId.toString())
        ).use { c ->
            return c.moveToFirst()
        }
    }

    fun vote(pollId: Long, residentId: Long, optionId: Long): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())

        val cv = ContentValues().apply {
            put("poll_id", pollId)
            put("resident_id", residentId)
            put("option_id", optionId)
            put("date", today)
        }

        return writableDatabase.insert("poll_votes", null, cv) > 0
    }

    fun getPollResults(pollId: Long): List<PollResult> {
        val list = mutableListOf<PollResult>()
        readableDatabase.rawQuery(
            """
        SELECT o.text, COUNT(v.id) as cnt
        FROM poll_options o
        LEFT JOIN poll_votes v ON v.option_id = o.id
        WHERE o.poll_id=?
        GROUP BY o.id
        """.trimIndent(),
            arrayOf(pollId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                list.add(
                    PollResult(
                        optionText = c.getString(0),
                        votes = c.getInt(1)
                    )
                )
            }
        }
        return list
    }


    companion object {
        private const val DB = "freyja.db"
        private const val VER = 1
    }
}
