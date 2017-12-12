package com.amm.acctbook.adapter;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.amm.acctbook.R;
import com.amm.acctbook.database.table.Category;

import java.util.List;

public class EntryAddCategoryAdatper extends ArrayAdapter<Category> {
    public EntryAddCategoryAdatper(Context context, List<Category> objects) {
        super(context, android.R.layout.simple_spinner_item, objects);
        setDropDownViewResource(R.layout.spinner_item_layout);
    }
}
