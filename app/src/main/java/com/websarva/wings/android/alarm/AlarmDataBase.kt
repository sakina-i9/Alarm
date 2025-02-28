package com.websarva.wings.android.alarm

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Alarm::class, CalendarEvent::class], version = 3)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun calendarEventDao(): CalendarEventDao

    companion object {
        // バージョン1から2へのマイグレーション：CalendarEvent テーブルを作成（列名は "day" とする）
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `calendar_events` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `day` TEXT NOT NULL,
                        `schedule` TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        // バージョン2から3へのマイグレーション：テーブルの列名を "day" から "date" に変更
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. 新しいテーブルを作成（列名を date に変更）
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `calendar_events_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `date` TEXT NOT NULL,
                        `schedule` TEXT NOT NULL
                    )
                """.trimIndent())
                // 2. 古いテーブルからデータをコピー（day 列を date にコピー）
                database.execSQL("""
                    INSERT INTO calendar_events_new (id, date, schedule)
                    SELECT id, day, schedule FROM calendar_events
                """.trimIndent())
                // 3. 古いテーブルを削除
                database.execSQL("DROP TABLE calendar_events")
                // 4. 新しいテーブルの名前を calendar_events に変更
                database.execSQL("ALTER TABLE calendar_events_new RENAME TO calendar_events")
            }
        }
    }
}
