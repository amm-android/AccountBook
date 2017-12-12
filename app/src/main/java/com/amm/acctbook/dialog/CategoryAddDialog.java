package com.amm.acctbook.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.amm.acctbook.R;
import com.amm.acctbook.base.BaseApplication;
import com.amm.acctbook.database.table.Category;

public class CategoryAddDialog {
    private static EditText etName;
    private static EditText etInfo;
    private static AlertDialog dialog;
    private static RadioGroup radioGroup;

    public static AlertDialog create(final Context context) {
        if (dialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View view = LayoutInflater.from(context).inflate(R.layout.add_category_dialog, null);
            etName = (EditText) view.findViewById(R.id.et_name);
            etInfo = (EditText) view.findViewById(R.id.et_info);
            builder.setTitle(R.string.category_add_dialog_title);
            builder.setView(view);
            builder.setPositiveButton("添加", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface iDialog, int which) {
                    dialog = null;
                    if (etName.getText().toString().isEmpty()) {
                        Toast.makeText(context, "分类名不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ((BaseApplication) context.getApplicationContext())
                            .getDbManager()
                            .addCategory(new Category(
                                    etName.getText().toString(),
                                    etInfo.getText().toString(), Category.TYPE_COST));
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface iDialog, int which) {
                    dialog = null;
                }
            });
            dialog = builder.create();
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog = null;
                }
            });
        }
        return dialog;
    }
}
