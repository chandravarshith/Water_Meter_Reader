package com.example.watermeterreader;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseManager extends SQLiteOpenHelper {
    public DatabaseManager(Context context) {
        super(context, "Meter_data.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create Table Meter_details(Meter_ID INTEGER primary key, Meter_Reading REAL, Date_time TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop Table if exists Meter_details");
        onCreate(db);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Boolean saveMeterData(int id, Float reading){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        contentValues.put("Meter_ID",id);
        contentValues.put("Meter_Reading",reading);
        contentValues.put("Date_time",dtf.format(now));


        long res = db.insert("Meter_details",null,contentValues);

        if(res == -1) return false;
        else return true;
    }

    public Boolean deleteMeterData (int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from Meter_details where Meter_ID= ?", new String[] {String.valueOf(id)});
        if (cursor.getCount() > 0) {
            long result = db.delete("Meter_details", "Meter_ID=?", new String[]{String.valueOf(id)});
            if (result == -1) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }

    }

    public Cursor viewData(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from Meter_details",null);
        return cursor;
    }


}
