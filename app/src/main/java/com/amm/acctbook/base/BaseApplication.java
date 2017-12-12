package com.amm.acctbook.base;

import android.app.Application;
import android.support.annotation.NonNull;

import com.amm.acctbook.database.DatabaseManager;
import com.amm.acctbook.database.table.Category;
import com.amm.acctbook.database.table.Entry;
import com.amm.acctbook.event.CategoryChangedEvent;
import com.amm.acctbook.event.CategoryUpdateEvent;
import com.amm.acctbook.event.EntryChangedEvent;
import com.amm.acctbook.event.EntryUpdateEvent;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BaseApplication extends Application {
    private DatabaseManager dbManager;
    private List<Category> categories = new ArrayList<>();
    private List<Entry> entries = new ArrayList<>();
    private static BaseApplication instance;

    public BaseApplication() {
        super();
        instance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        dbManager = new DatabaseManager(this);
        initData();
    }

    @NonNull
    public static BaseApplication getInstance() {
        return instance;
    }

    private void initData() {
        queryCategory();
        queryEntry();
    }

    private void queryEntry() {
        if (!entries.isEmpty()){entries.clear();}
        entries.addAll(dbManager.queryAllEntry());
        EventBus.getDefault().post(new EntryUpdateEvent());
    }


    private void queryCategory() {
        if (!categories.isEmpty()){categories.clear();}
        categories.addAll(dbManager.queryAllCategory());
        EventBus.getDefault().post(new CategoryUpdateEvent());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        dbManager.close();
    }

    @Subscriber
    private void onEntryChanged(EntryChangedEvent event) {
        queryEntry();
    }

    @Subscriber
    private void onCategoryChanged(CategoryChangedEvent event) {
        queryCategory();
    }

    public DatabaseManager getDbManager() {
        return dbManager;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public List<Entry> getEntries() {
        return entries;
    }
}
