package com.example.abnervictor.lab8;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by abnervictor on 2017/12/7.
 */

public class DataBase extends SQLiteOpenHelper {
    private static final String DB_NAME = "Contacts.db";
    private static final String TABLE_NAME = "Contacts";
    private static final int DB_VERSION = 1;
    private SQLiteDatabase db;

    public DataBase(Context context){
        super(context,DB_NAME,null,DB_VERSION);
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建表Contacts，如果数据库已经存在，SQLiteOpenHelper不会调用onCreate方法
        createDataBase(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //构造函数会判断版本号，如果传入的版本号高于当前版本号时，会调用这个函数更新数据库和版本号
    }

    private void createDataBase(SQLiteDatabase db){
        String CREATE_TABLE = "create table " + TABLE_NAME
                + " (_id integer primary key , " + "name text , "
                + "birthday text , "
                + "present text);";
        db.execSQL(CREATE_TABLE);
    }//创建表Contacts

    public void insert(String name, String birthday, String present) {
        db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("birthday", birthday);
        values.put("present", present);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public void update(String name, String birthday, String present) {
        db = getWritableDatabase();
        String updateSQL = "update " + TABLE_NAME + " set birthday = '" + birthday + "' , present = '"+ present + "' where name = '" + name +"';";
        db.execSQL(updateSQL);
        db.close();
    }

    public void delete(String name){
        db = getWritableDatabase();
//        String whereClause = "**** = ?"; // 主键列名 = ?
//        String[] whereArgs = {name}; // 主键的值
//        db.delete(TABLE_NAME, whereClause, whereArgs);
        String deleteSQL = "delete from " + TABLE_NAME + " where name = '" + name +"';";
        db.execSQL(deleteSQL);
        db.close();
    }

    public List<Map<String,Object>> getAllItems(){
        db = getWritableDatabase();
        String sql = "select * from " + TABLE_NAME;
        Cursor cursor = db.rawQuery(sql,null);
        List<Map<String,Object>> AllItems = new ArrayList<>();
        if (cursor.moveToFirst()){
            do{
                Map<String,Object> item = new LinkedHashMap<>();
                item.put("name",cursor.getString(cursor.getColumnIndex("name")));
                item.put("birthday",cursor.getString(cursor.getColumnIndex("birthday")));
                item.put("present",cursor.getString(cursor.getColumnIndex("present")));
                //从数据库中查询信息
                AllItems.add(item);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return AllItems;
    }

    public String[] getInfoWithName(String name){
        db = getWritableDatabase();
        String sql = "select * from " + TABLE_NAME + " where name = '" + name +"';";
        Cursor cursor = db.rawQuery(sql,null);
        if (cursor.moveToFirst()){
            String[] info = {cursor.getString(cursor.getColumnIndex("birthday")),cursor.getString(cursor.getColumnIndex("name"))};
            cursor.close();
            return info;
        }
        else {
            cursor.close();
            return null;
        }
    }//利用名字查询出其它信息

    public boolean isLegalName(String name){
        db = getWritableDatabase();
        String sql = "select name from " + TABLE_NAME + " where name = '" + name +"';";
        Cursor cursor = db.rawQuery(sql,null);
        if (cursor.moveToFirst()){
            cursor.close();
            return false;
        }
        else {
            cursor.close();
            return true;
        }
    }//查询名字是否已经存在

}
