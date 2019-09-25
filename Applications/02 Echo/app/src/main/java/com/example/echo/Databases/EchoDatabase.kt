package com.example.echo.Databases

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class EchoDatabase: SQLiteOpenHelper{

    val DB_NAME = "FavouriteDatabase"
    val TABLE_NAME = "FavouriteTable"
    val COLUMN_ID = "SongId"
    val COLUMN_SONG_TITLE = "SongTitle"
    val COLUMN_SONG_ARTIST = "SongArtist"
    val COLUMN_SONG_PATH = "SongPath"


    override fun onCreate(sqliteDatabase: SQLiteDatabase?) {
        sqliteDatabase?.execSQL(
        "CREATE TABLE " + TABLE_NAME + "( " +
                COLUMN_ID + " INTEGER, " +
                COLUMN_SONG_ARTIST + " STRING, " +
                COLUMN_SONG_TITLE + " STRING, " +
                COLUMN_SONG_PATH + " STRING);")
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    constructor(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) : super(
        context,
        name,
        factory,
        version
    )
}