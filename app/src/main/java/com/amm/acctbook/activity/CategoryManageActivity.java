package com.amm.acctbook.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.amm.acctbook.R;
import com.amm.acctbook.adapter.CategoryManageCategoryAdapter;
import com.amm.acctbook.base.BaseActivity;
import com.amm.acctbook.base.BaseApplication;
import com.amm.acctbook.database.table.Category;
import com.amm.acctbook.event.CategoryUpdateEvent;
import org.simple.eventbus.Subscriber;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CategoryManageActivity extends BaseActivity {
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_add_category)
    TextView tvAddCategory;
    @BindView(R.id.lv_category)
    ListView lvCategory;

    private BaseApplication app;
    private List<Category> categories = new ArrayList<>();
    private CategoryManageCategoryAdapter adapter;

    @Override
    protected int getLayoutResource() {
        return R.layout.category_manage_layout;
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
    }

    private void initView() {
        adapter = new CategoryManageCategoryAdapter(this, categories);
        lvCategory.setAdapter(adapter);
        lvCategory.setOnItemClickListener(onCategoryItemClick);
        lvCategory.setOnItemLongClickListener(onCategoryItemLongClick);
    }

    @Subscriber
    private void onCategoryUpdate(CategoryUpdateEvent event) {
        adapter.notifyDataSetChanged();
    }

    @OnClick({R.id.iv_back, R.id.tv_add_category})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.tv_add_category:
                showAddDialog();
                break;
        }
    }

    private AdapterView.OnItemClickListener onCategoryItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            new AlertDialog.Builder(CategoryManageActivity.this)
                    .setTitle("详情")
                    .setMessage("分类:" + adapter.getItem(position).getName() + "\n备注:" + adapter.getItem(position).getInfo())
                    .setPositiveButton("确定",null)
                    .show();
        }
    };

    private AdapterView.OnItemLongClickListener onCategoryItemLongClick = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
            new AlertDialog.Builder(CategoryManageActivity.this)
                    .setItems(new String[]{"修改","删除"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which==0){
                                showEditDialog(adapter.getItem(position));
                            }else if (which==1){
                                showDeleteItem(adapter.getItem(position));
                            }
                        }
                    })
                    .show();
            return true;
        }
    };

    /**
     * 显示添加弹框
     */
    private void showAddDialog(){
        View view = LayoutInflater.from(CategoryManageActivity.this).inflate(R.layout.add_category_dialog, null);
        final EditText etName = (EditText) view.findViewById(R.id.et_name);
        final EditText etInfo = (EditText) view.findViewById(R.id.et_info);
        new AlertDialog.Builder(CategoryManageActivity.this)
                .setTitle("添加分类")
                .setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface iDialog, int which) {
                        if (etName.getText().toString().isEmpty()) {
                            Toast.makeText(CategoryManageActivity.this, "分类名不能为空", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        boolean isSuccess = ((BaseApplication) CategoryManageActivity.this.getApplicationContext())
                                .getDbManager()
                                .addCategory(new Category(etName.getText().toString(), etInfo.getText().toString(), Category.TYPE_COST));
                        Toast.makeText(CategoryManageActivity.this,(isSuccess?"添加成功":"添加失败"),Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("取消",null).create().show();
    }

    /**
     * 显示修改弹框
     * @param category
     */
    private void showEditDialog(final Category category){
        View view = LayoutInflater.from(CategoryManageActivity.this).inflate(R.layout.add_category_dialog, null);
        final EditText etName = (EditText) view.findViewById(R.id.et_name);
        final EditText etInfo = (EditText) view.findViewById(R.id.et_info);
        etName.setText(category.getName());
        etInfo.setText(category.getInfo());
        new AlertDialog.Builder(CategoryManageActivity.this)
                .setTitle("修改分类")
                .setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface iDialog, int which) {
                        if (etName.getText().toString().isEmpty()) {
                            Toast.makeText(CategoryManageActivity.this, "分类名不能为空", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        boolean isSuccess = ((BaseApplication) CategoryManageActivity.this.getApplicationContext())
                                .getDbManager()
                                .updateCategory(new Category(category.getId(),etName.getText().toString(), etInfo.getText().toString(), Category.TYPE_COST));
                        Toast.makeText(CategoryManageActivity.this,(isSuccess?"修改成功":"修改失败"),Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("取消",null).create().show();
    }

    /**
     * 删除条目
     * @param category
     */
    private void showDeleteItem(final Category category){
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("确定删除分类吗，删除后该分类下所有流水的分类将标记为“无类别”？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean isSuccess = app.getDbManager().removeCategory(category);;
                        Toast.makeText(CategoryManageActivity.this,(isSuccess?"删除成功":"删除失败"),Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("取消", null).show();
    }
}
