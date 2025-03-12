package com.websarva.wings.android.alarm

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Alarm::class, CalendarEvent::class], version = 5)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun calendarEventDao(): CalendarEventDao

    companion object {
        // MIGRATION from 1 to 2: CalendarEvent テーブル作成（day列）
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

        // MIGRATION from 2 to 3: CalendarEvent テーブルの列名変更（day -> date）
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `calendar_events_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `date` TEXT NOT NULL,
                        `schedule` TEXT NOT NULL
                    )
                """.trimIndent())
                database.execSQL("""
                    INSERT INTO calendar_events_new (id, date, schedule)
                    SELECT id, day, schedule FROM calendar_events
                """.trimIndent())
                database.execSQL("DROP TABLE calendar_events")
                database.execSQL("ALTER TABLE calendar_events_new RENAME TO calendar_events")
            }
        }

        // MIGRATION from 3 to 4: Alarm テーブルに enabled 列を追加（デフォルトは1＝true）
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE alarms ADD COLUMN enabled INTEGER NOT NULL DEFAULT 1")
            }
        }

        // MIGRATION from 4 to 5: Alarm テーブルに backgroundUri 列を追加（デフォルトは空文字列）
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE alarms ADD COLUMN backgroundUri TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
