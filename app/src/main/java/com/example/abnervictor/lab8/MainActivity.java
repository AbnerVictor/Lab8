package com.example.abnervictor.lab8;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private View item_list;
    private View new_item;
    private Button addItems;//信息列表界面的确定按钮
    private Button addNewItems;//增加信息界面的确定按钮
    private Button cancel;
    private ListView Itemlist;//信息列表
    private SimpleAdapter simpleAdapter;
    private EditText inputName;
    private EditText inputBirthday;
    private EditText inputPresent;
    private List<Map<String,Object>> listItems = new ArrayList<>();//用于向列表中填充列表项
    private DataBase dataBase;

    private LayoutInflater layoutInflater;
    private View dialogView;
    private TextView editName;
    private EditText editBirthday;
    private EditText editPresent;
    private TextView phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        dataBase = new DataBase(MainActivity.this);//创建or打开数据库
        InitList();//初始化列表
        SetListeners();
    }

    private void findView(){
        item_list = findViewById(R.id.item_list);
        new_item = findViewById(R.id.new_item);
        addItems = findViewById(R.id.addItems);
        addNewItems = findViewById(R.id.addNewItems);
        cancel = findViewById(R.id.cancel);
        Itemlist = findViewById(R.id.Itemlist);
        inputName = findViewById(R.id.inputName);
        inputBirthday = findViewById(R.id.inputBirthday);
        inputPresent = findViewById(R.id.inputPresent);
        layoutInflater = LayoutInflater.from(MainActivity.this);
        dialogView = layoutInflater.inflate(R.layout.edit_dialog,null);
        editName = dialogView.findViewById(R.id.editName);
        editBirthday = dialogView.findViewById(R.id.editBirthday);
        editPresent = dialogView.findViewById(R.id.editPresent);
        phone = dialogView.findViewById(R.id.phone);
    }

    private void InitList(){
        listItems = dataBase.getAllItems();
        //从数据库中获取信息放到listItems内
        simpleAdapter = new SimpleAdapter(this,listItems,R.layout.list_item, new String[]{"name","birthday","present"},new int[]{R.id.name,R.id.birthday,R.id.present});
        Itemlist.setAdapter(simpleAdapter);
    }//应用启动时初始化列表

    private void refreshList(){
        listItems.clear();
        listItems.addAll(dataBase.getAllItems());
        simpleAdapter.notifyDataSetChanged();
    }//刷新列表

    private void SetListeners(){
        addItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                item_list.setVisibility(View.GONE);
                new_item.setVisibility(View.VISIBLE);
                inputName.setText("");
                inputBirthday.setText("");
                inputPresent.setText("");
                inputName.setError(null,null);
            }
        });//跳转到添加条目界面
        addNewItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean legal = true;
                if (inputName.getText().toString().length() == 0){
                    legal = false;
                    inputName.setError("姓名不能为空！");
                }
                else if(!dataBase.isLegalName(inputName.getText().toString())){
                    legal = false;
                    inputName.setError("姓名不能重复！");
                }
                if (legal){
                    inputName.setError(null,null);
                    Toast.makeText(MainActivity.this,"保存成功",Toast.LENGTH_SHORT).show();
                    //
                    dataBase.insert(inputName.getText().toString(),inputBirthday.getText().toString(),inputPresent.getText().toString());
                    //
                    refreshList();//刷新列表
                    new Handler().postDelayed(new Runnable(){
                        public void run() {
                            //execute the task
                            item_list.setVisibility(View.VISIBLE);
                            new_item.setVisibility(View.GONE);
                        }
                    }, 500);//延时0.5秒执行
                }
            }
        });//点击添加信息，检查输入合法性，如果输入合法，保存到数据库并回到主界面
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                item_list.setVisibility(View.VISIBLE);
                new_item.setVisibility(View.GONE);
            }
        });
        Itemlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                String name = (String) listItems.get(pos).get("name");
                editName.setText(name);
                editBirthday.setText((String) listItems.get(pos).get("birthday"));
                editPresent.setText((String) listItems.get(pos).get("present"));
                //查询通讯录
                phone.setText("无");
                //The specified child already has a parent. You must call removeView() on the child's parent first
                ViewGroup parent = (ViewGroup) dialogView.getParent();
                if(parent != null) parent.removeView(dialogView);
                //
                AlertDialog.Builder EditAlertDialog = new AlertDialog.Builder(MainActivity.this);
                EditAlertDialog.setView(dialogView);
                EditAlertDialog.setTitle("修改信息");
                EditAlertDialog.setPositiveButton("保存修改", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dataBase.update(editName.getText().toString(),editBirthday.getText().toString(),editPresent.getText().toString());
                        refreshList();
                    }
                });
                EditAlertDialog.setNegativeButton("放弃修改",null);
                EditAlertDialog.create().show();
            }
        });//点击列表项

        Itemlist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int pos, long l) {
                final String name  = (String) listItems.get(pos).get("name");
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setMessage("是否要移除\""+ name +"\"的信息?");
                alertDialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dataBase.delete(name);
                        refreshList();
                        //listItems.remove(pos);
                        //simpleAdapter.notifyDataSetChanged();
                    }
                });
                alertDialog.setNegativeButton("否",null);
                alertDialog.create().show();
                return true;
            }
        });

    }

}
