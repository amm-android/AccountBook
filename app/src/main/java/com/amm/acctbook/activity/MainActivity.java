package com.amm.acctbook.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amm.acctbook.R;
import com.amm.acctbook.adapter.MainPageEntryAdapter;
import com.amm.acctbook.base.BaseActivity;
import com.amm.acctbook.base.BaseApplication;
import com.amm.acctbook.database.table.Entry;
import com.amm.acctbook.event.EntryUpdateEvent;
import org.simple.eventbus.Subscriber;
import java.text.DecimalFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    @BindView(R.id.tv_cost)
    TextView tvCost;
    @BindView(R.id.tv_add)
    TextView tvAdd;
    @BindView(R.id.lv_recent_entry)
    ListView lvRecentEntry;

    private BaseApplication app;
    private List<Entry> entries;
    private MainPageEntryAdapter adapter;
    private float sum_cost;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

    @Override
    protected void initAfter() {
        ButterKnife.bind(this);
        registerEventBus();
        initData();
        initView();
    }

    private void initData() {
        app = BaseApplication.getInstance();
        entries = app.getEntries();
    }

    private void initView() {
        calculate();
        adapter = new MainPageEntryAdapter(this, entries);
        lvRecentEntry.setAdapter(adapter);
        lvRecentEntry.setOnItemClickListener(this);
        lvRecentEntry.setOnItemLongClickListener(this);
    }

    public void calculate() {
        sum_cost = 0;
        for (Entry entry : entries) {
            sum_cost += entry.getAmount();
        }
        tvCost.setText(String.valueOf(new DecimalFormat("0.00").format(sum_cost)));
    }

    @Subscriber
    private void onEntryUpdate(EntryUpdateEvent event) {
        adapter.notifyDataSetChanged();
        calculate();
    }

    @OnClick({R.id.tv_add})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_add:
                startActivity(new Intent(this, EntryAddActivity.class));
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Entry entry = entries.get(position);
        String info = entry.getInfo();
        if (null != info && !info.isEmpty())
            Toast.makeText(this, entry.getInfo(), Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "这条记录没有添加备注", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        new AlertDialog.Builder(this).setTitle("删除该条目？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        app.getDbManager().removeEntry(entries.get(position));
                    }
                }).setNegativeButton("取消", null).show();
        return true;
    }
}
