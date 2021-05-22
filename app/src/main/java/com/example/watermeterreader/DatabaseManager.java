package com.example.watermeterreader;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseManager extends SQLiteOpenHelper {
    public DatabaseManager(Context context) {
        super(context, "MeterData.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create Table MeterDetails(Meter_ID INTEGER primary key, Meter_Reading REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop Table if exists MeterDetails");
        onCreate(db);
    }

    public Boolean saveMeterData(int id, Float reading){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("Meter_ID",id);
        contentValues.put("Meter_Reading",reading);

        long res = db.insert("MeterDetails",null,contentValues);

        if(res == -1) return false;
        else return true;
    }

    public Boolean deleteMeterData (int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from MeterDetails where Meter_ID= ?", new String[] {String.valueOf(id)});
        if (cursor.getCount() > 0) {
            long result = db.delete("MeterDetails", "Meter_ID=?", new String[]{String.valueOf(id)});
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
        Cursor cursor = db.rawQuery("Select * from MeterDetails",null);
        return cursor;
    }


}
