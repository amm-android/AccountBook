package com.amm.acctbook.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.amm.acctbook.R;
import com.amm.acctbook.adapter.EntryAddCategoryAdatper;
import com.amm.acctbook.base.BaseActivity;
import com.amm.acctbook.base.BaseApplication;
import com.amm.acctbook.database.table.Category;
import com.amm.acctbook.database.table.Entry;
import com.amm.acctbook.event.CategoryChangedEvent;
import com.amm.acctbook.event.CategoryUpdateEvent;
import com.amm.acctbook.event.EntryUpdateEvent;

import org.simple.eventbus.Subscriber;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 添加、修改Entry
 */
public class EntryAddActivity extends BaseActivity {
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_category_manage)
    TextView tvCategoryManage;
    @BindView(R.id.spinner_category)
    Spinner spinnerCategory;
    @BindView(R.id.et_amount)
    EditText etAmount;
    @BindView(R.id.tv_date)
    TextView tvDate;
    @BindView(R.id.tv_time)
    TextView tvTime;
    @BindView(R.id.et_info)
    EditText etInfo;
    @BindView(R.id.iv_add)
    ImageView ivAdd;

    private String pageType="add";//页面类型：add添加、edit修改
    private Entry entry;
    private BaseApplication app;
    private List<Category> categories = new ArrayList<>();
    private Calendar calendar;
    private EntryAddCategoryAdatper adapter;

    @Override
    protected int getLayoutResource() {
        return R.layout.entry_add_layout;
    }

    @Override
    protected void initAfter() {
        ButterKnife.bind(this);
        registerEventBus();
        initData();
        initView();
    }

    private void initData() {
        app = (BaseApplication) getApplication();
        categories = app.getCategories();
        calendar = Calendar.getInstance(Locale.CHINA);
        pageType = getIntent().getStringExtra("pageType");
        if (pageType.equals("edit")){
            entry = (Entry) getIntent().getSerializableExtra("entry");
        }
    }

    private void initView() {
        if(pageType.equals("add")){
            tvDate.setText(getDateString(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)));
            tvTime.setText(getTimeString(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));
        }else if (pageType.equals("edit")){
            String[] times = entry.getTime().split(" ");
            tvDate.setText(times[0]);
            tvTime.setText(times[1]);
            etAmount.setText(String.valueOf(entry.getAmount()));
            etInfo.setText(entry.getInfo());
        }
        initSpinnerView();
    }

    private void initSpinnerView(){
        if (adapter == null){
            adapter = new EntryAddCategoryAdatper(this, categories);
            spinnerCategory.setAdapter(adapter);
        }else {
            adapter.notifyDataSetChanged();
        }

        int select_index = 0;
        for (int i = 0; i < categories.size(); i++) {
            Category category = categories.get(i);
            if (entry != null && entry.getCategoryId()==category.getId()){
                select_index = i;
            }
        }
        spinnerCategory.setSelection(select_index);
    }

    @Subscriber
    private void onCategoryUpdate(CategoryUpdateEvent event) {
        initSpinnerView();
    }

    @OnClick({R.id.iv_back, R.id.tv_category_manage, R.id.tv_date, R.id.tv_time, R.id.iv_add})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.tv_category_manage:
                startActivity(new Intent(this, CategoryManageActivity.class));
                break;
            case R.id.tv_date:
                new DatePickerDialog(this, onDateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
                break;
            case R.id.tv_time:
                new TimePickerDialog(this, onTimeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
                break;
            case R.id.iv_add:
                String amount = etAmount.getText().toString();
                if (amount.isEmpty()) {
                    Toast.makeText(this, "请输入金额", Toast.LENGTH_SHORT).show();
                    break;
                } else if (amount.contains(".")) {
                    int position = amount.length() - amount.indexOf('.') - 1;
                    if (position > 2) {
                        Toast.makeText(this, "不能提交两位以上的小数", Toast.LENGTH_SHORT).show();
                        break;
                    } else if (position == 0) {
                        amount = amount.substring(0, amount.length() - 1);
                    }
                }
                Log.d("amount", amount);
                String time = tvDate.getText().toString() + " " + tvTime.getText().toString();
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                Category category_sel = (Category) spinnerCategory.getSelectedItem();
                boolean isSuccess = true;
                if(pageType.equals("add")){
                    isSuccess = app.getDbManager().addEntry(new Entry(category_sel.getId(), Float.valueOf(amount), time, calendar.getTimeInMillis(), etInfo.getText().toString().trim()));
                }else if (pageType.equals("edit")){
                    isSuccess = app.getDbManager().updateEntry(new Entry(entry.getId(),category_sel.getId(), Float.valueOf(amount), time, calendar.getTimeInMillis(), etInfo.getText().toString().trim()));
                }
                if (isSuccess){
                    Toast.makeText(EntryAddActivity.this,pageType+"成功",Toast.LENGTH_SHORT).show();
                    finish();
                }else {
                    Toast.makeText(EntryAddActivity.this,pageType+"失败",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            calendar.set(year, monthOfYear, dayOfMonth);
            tvDate.setText(getDateString(year, monthOfYear + 1, dayOfMonth));
        }
    };
    private TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            tvTime.setText(getTimeString(hourOfDay, minute));
        }
    };

    private CharSequence getDateString(int year, int month, int day) {
        return new StringBuilder().append(year).append("-").append(month < 10 ? "0" + month : month).append("-").append(day < 10 ? "0" + day : day);
    }

    private CharSequence getTimeString(int hour, int minute) {
        return new StringBuilder().append(hour < 10 ? "0" + hour : hour).append(":").append(minute < 10 ? "0" + minute : minute);
    }
}
