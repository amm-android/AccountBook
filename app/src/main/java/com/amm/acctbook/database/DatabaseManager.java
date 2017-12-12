package com.amm.acctbook.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.amm.acctbook.database.table.Category;
import com.amm.acctbook.database.table.Entry;
import com.amm.acctbook.event.CategoryChangedEvent;
import com.amm.acctbook.event.EntryChangedEvent;
import com.amm.acctbook.event.EntryUpdateEvent;

import org.simple.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DatabaseManager {
    private SQLiteDatabase db;
    private DatabaseOpenHelper helper;
    private Context context;

    public DatabaseManager(Context context) {
        this.context = context;
        helper = new DatabaseOpenHelper(context);
        db = helper.getWritableDatabase();
    }

    /**
     * 增加类别
     *
     * @param category
     * @return 是否操作成功
     */
    public boolean addCategory(Category category) {
        boolean isSuccess;
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("name", category.getName());
            values.put("info", category.getInfo());
            values.put("type", category.getType());
            db.insert(DatabaseOpenHelper.TABLE_CATEGORY, null, values);
            db.setTransactionSuccessful();
            isSuccess = true;
            EventBus.getDefault().post(new CategoryChangedEvent());
        } catch (Exception e) {
            e.printStackTrace();
            isSuccess = false;
        } finally {
            db.endTransaction();
        }
        return isSuccess;
    }

    /**
     * 查询所有分类
     *
     * @return 分类List
     */
    public List<Category> queryAllCategory() {
        ArrayList<Category> categories = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from " + DatabaseOpenHelper.TABLE_CATEGORY, null);
        while (cursor.moveToNext()) {
            Category category = new Category(
                    cursor.getInt(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("name")),
                    cursor.getString(cursor.getColumnIndex("info")),
                    cursor.getInt(cursor.getColumnIndex("type")));
            categories.add(category);
        }
        cursor.close();
        return categories;
    }

    /**
     * 根据流水条目查询类别对象
     * @param entry
     * @return 是否操作成功
     */
    public Category queryCategoryByEntry(@NonNull Entry entry) {
        Cursor cursor = db.rawQuery("select * from " + DatabaseOpenHelper.TABLE_CATEGORY + " where id = ?", new String[]{String.valueOf(entry.getCategoryId())});
        Category category = null;
        if (cursor.moveToNext()) {
            category = new Category(
                    cursor.getInt(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("name")),
                    cursor.getString(cursor.getColumnIndex("info")),
                    cursor.getInt(cursor.getColumnIndex("type")));
        }
        cursor.close();
        return category;
    }

    /**
     * 修改类别
     *
     * @param category
     * @return 是否操作成功
     */
    public boolean updateCategory(Category category) {
        boolean isSuccess;
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("name", category.getName());
            values.put("info", category.getInfo());
            values.put("type", category.getType());
            db.update(DatabaseOpenHelper.TABLE_CATEGORY, values, "id = ?", new String[]{String.valueOf(category.getId())});
            db.setTransactionSuccessful();
            isSuccess = true;
            EventBus.getDefault().post(new CategoryChangedEvent());
        } catch (Exception e) {
            e.printStackTrace();
            isSuccess = false;
        } finally {
            db.endTransaction();
        }
        return isSuccess;
    }

    /**
     * 移除类别
     *
     * @param category
     * @return 是否操作成功
     */
    public boolean removeCategory(Category category) {
        boolean isSuccess;
        if (!updateEntriesInCategory(category)) {
            Toast.makeText(context, "更新分类下条目的类别失败", Toast.LENGTH_SHORT).show();
            return false;
        }
        db.beginTransaction();
        try {
            db.delete(DatabaseOpenHelper.TABLE_CATEGORY, "id = ?", new String[]{String.valueOf(category.getId())});
            db.setTransactionSuccessful();
            isSuccess = true;
            EventBus.getDefault().post(new CategoryChangedEvent());
        } catch (Exception e) {
            e.printStackTrace();
            isSuccess = false;
        } finally {
            db.endTransaction();
        }
        return isSuccess;
    }


    /**
     * 增加流水记录
     *
     * @param entry
     * @return 是否操作成功
     */
    public boolean addEntry(Entry entry) {
        boolean isSuccess;
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("category_id", entry.getCategoryId());
            values.put("amount", entry.getAmount());
            values.put("time", entry.getTime());
            values.put("timestamp", entry.getTimestamp());
            values.put("info", entry.getInfo());
            db.insert(DatabaseOpenHelper.TABLE_ENTRY, null, values);
            db.setTransactionSuccessful();
            isSuccess = true;
            EventBus.getDefault().post(new EntryChangedEvent());
            Log.d("添加流水", "成功");
        } catch (Exception e) {
            e.printStackTrace();
            isSuccess = false;
        } finally {
            db.endTransaction();
        }
        return isSuccess;
    }

    /**
     * 查询所有流水条目
     *
     * @return 流水List
     */
    public List<Entry> queryAllEntry() {
        ArrayList<Entry> entries = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from " + DatabaseOpenHelper.TABLE_ENTRY, null);
        while (cursor.moveToNext()) {
            Entry entry = new Entry(
                    cursor.getInt(cursor.getColumnIndex("id")),
                    cursor.getInt(cursor.getColumnIndex("category_id")),
                    cursor.getFloat(cursor.getColumnIndex("amount")),
                    cursor.getString(cursor.getColumnIndex("time")),
                    cursor.getLong(cursor.getColumnIndex("timestamp")),
                    cursor.getString(cursor.getColumnIndex("info")));
            entries.add(entry);
        }
        cursor.close();
        Collections.reverse(entries);
        return entries;
    }

    /**
     * 修改流水条目
     *
     * @param entry
     * @return 是否操作成功
     */
    public boolean updateEntry(Entry entry) {
        boolean isSuccess;
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("category_id", entry.getCategoryId());
            values.put("amount", entry.getAmount());
            values.put("time", entry.getTime());
            values.put("timestamp", entry.getTimestamp());
            values.put("info", entry.getInfo());
            db.update(DatabaseOpenHelper.TABLE_ENTRY, values, "id = ?", new String[]{String.valueOf(entry.getId())});
            db.setTransactionSuccessful();
            isSuccess = true;
            EventBus.getDefault().post(new EntryChangedEvent());
        } catch (Exception e) {
            e.printStackTrace();
            isSuccess = false;
        } finally {
            db.endTransaction();
        }
        return isSuccess;
    }

    /**
     * 移除流水条目
     *
     * @param entry
     * @return 是否操作成功
     */
    public boolean removeEntry(Entry entry) {
        boolean isSuccess;
        db.beginTransaction();
        try {
            db.delete(DatabaseOpenHelper.TABLE_ENTRY, "id = ?", new String[]{String.valueOf(entry.getId())});
            db.setTransactionSuccessful();
            isSuccess = true;
            Calendar calendar = Calendar.getInstance(Locale.CHINA);
            calendar.setTimeInMillis(entry.getTimestamp());
            EventBus.getDefault().post(new EntryChangedEvent());
        } catch (Exception e) {
            e.printStackTrace();
            isSuccess = false;
        } finally {
            db.endTransaction();
        }
        return isSuccess;
    }

    /**
     * 移除类别下的所有条目的类别信息
     *
     * @param category
     * @return 是否操作成功
     */
    public boolean updateEntriesInCategory(Category category) {
        boolean isSuccess;
        int count;
        db.beginTransaction();
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("category_id", -1);
            count = db.update(DatabaseOpenHelper.TABLE_ENTRY,contentValues, "category_id = ?", new String[]{String.valueOf(category.getId())});
            db.setTransactionSuccessful();
            isSuccess = true;
            Log.d("删除分类下条目的类别信息", "更新了 " + count + " 条记录");
            EventBus.getDefault().post(new EntryChangedEvent());
        } catch (Exception e) {
            e.printStackTrace();
            isSuccess = false;
        } finally {
            db.endTransaction();
        }
        return isSuccess;
    }

    public void close() {
        helper.close();
    }
}
