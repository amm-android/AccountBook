package com.amm.acctbook.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amm.acctbook.R;
import com.amm.acctbook.base.BaseApplication;
import com.amm.acctbook.database.DatabaseManager;
import com.amm.acctbook.database.table.Category;
import com.amm.acctbook.database.table.Entry;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainPageEntryAdapter extends BaseAdapter {

    private Context context;
    private List<Entry> entries;
    private DatabaseManager manager;

    public MainPageEntryAdapter(Context context, List<Entry> entries) {
        this.context = context;
        this.entries = entries;
        manager = ((BaseApplication) context.getApplicationContext()).getDbManager();
    }

    @Override
    public int getCount() {
        return entries.size();
    }

    @Override
    public Object getItem(int position) {
        return entries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.mainpage_entrylist_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Entry entry = (Entry) getItem(position);
        Category category = manager.queryCategoryByEntry(entry);
        holder.tvDate.setText(entry.getTime());
        holder.tvCategory.setText(category == null ? "无类别" : category.getName());
        holder.tvAmount.setText("－"+(new DecimalFormat(".00").format(entry.getAmount())).toString());
        if (position%2 == 1) convertView.setBackgroundColor(Color.parseColor("#0F000000"));
        else convertView.setBackgroundColor(Color.parseColor("#00000000"));
        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.tv_amount)
        TextView tvAmount;
        @BindView(R.id.tv_date)
        TextView tvDate;
        @BindView(R.id.tv_category)
        TextView tvCategory;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
