package com.amm.acctbook.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ajts.androidmads.library.SQLiteToExcel;
import com.amm.acctbook.R;
import com.amm.acctbook.adapter.MainPageEntryAdapter;
import com.amm.acctbook.base.BaseActivity;
import com.amm.acctbook.base.BaseApplication;
import com.amm.acctbook.database.DatabaseOpenHelper;
import com.amm.acctbook.database.table.Entry;
import com.amm.acctbook.event.CategoryUpdateEvent;
import com.amm.acctbook.event.EntryUpdateEvent;
import com.amm.acctbook.util.SystemTool;

import org.simple.eventbus.Subscriber;

import java.io.File;
import java.io.IOException;
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
    @BindView(R.id.main_output_btn)
    Button btnOutput;

    private BaseApplication app;
    private List<Entry> entries;
    private MainPageEntryAdapter adapter;
    private float sum_cost;
    private ProgressDialog progressDialog;

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

    @Subscriber
    private void onCategoryUpdate(CategoryUpdateEvent event) {
        adapter.notifyDataSetChanged();
    }

    @OnClick({R.id.tv_add,R.id.main_output_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_add:
                Intent intent = new Intent(MainActivity.this, EntryAddActivity.class);
                intent.putExtra("pageType","add");
                startActivity(intent);
                break;
            case R.id.main_output_btn:
                output_excel();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Entry entry = entries.get(position);
        String info = TextUtils.isEmpty(entry.getInfo())?"无":entry.getInfo();
        String msg = "时间："+entry.getTime()
                +"\n金额："+entry.getAmount()
                +"\n备注："+info;
        new AlertDialog.Builder(this)
                .setTitle("详情")
                .setMessage(msg)
                .setPositiveButton("确定",null)
                .show();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        new AlertDialog.Builder(this)
                .setItems(new String[]{"修改","删除"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       if (which==0){
                           Intent intent = new Intent(MainActivity.this, EntryAddActivity.class);
                           intent.putExtra("pageType","edit");
                           intent.putExtra("entry",entries.get(position));
                           startActivity(intent);
                       }else if (which==1){
                           showDeleteItem(entries.get(position));
                       }
                    }
                })
                .show();
        return true;
    }

    private void showDeleteItem(final Entry entry){
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("确定删除该条目？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean isSuccess = app.getDbManager().removeEntry(entry);
                        Toast.makeText(MainActivity.this,(isSuccess?"删除成功":"删除失败"),Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("取消", null).show();
    }

    /**
     * 关闭progressDialog
     */
    private void dismissPDialog(){
        if (progressDialog != null){
            progressDialog.dismiss();
            progressDialog=null;
        }
    }

    /**
     * 导出db至excel
     */
    private void output_excel(){
        SQLiteToExcel sqliteToExcel = new SQLiteToExcel(MainActivity.this, DatabaseOpenHelper.DATABASE_NAME, SystemTool.getAppSavePath(MainActivity.this));
        String file_path = SystemTool.getAppSavePath(MainActivity.this)+"tb_test.xls";
        File file = new File(file_path);
        if (!file.exists()){
            File dir = new File(SystemTool.getAppSavePath(MainActivity.this));
            if (!dir.exists()){
                dir.mkdirs();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        sqliteToExcel.exportAllTables(file.getName(), new SQLiteToExcel.ExportListener() {
            @Override
            public void onStart() {
                dismissPDialog();
                progressDialog = ProgressDialog.show(MainActivity.this,null,"正在导出...");
            }
            @Override
            public void onCompleted(String filePath) {
                dismissPDialog();
                final File save_file = new File(filePath);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("提示")
                        .setMessage("导出成功至："+filePath)
                        .setPositiveButton("分享文件", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("*/*");
                                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(save_file));
                                startActivity(Intent.createChooser(intent,"选择分享工具"));
                            }
                        })
                        .setNegativeButton("我知道了",null)
                        .create().show();
            }
            @Override
            public void onError(Exception e) {
                dismissPDialog();
                Toast.makeText(MainActivity.this,"导出失败，请重试~",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }
}
