package com.example.abnervictor.lab8;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
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
    private TextView inputBirthday;
    private EditText inputPresent;
    private List<Map<String,Object>> listItems = new ArrayList<>();//用于向列表中填充列表项
    private DataBase dataBase;

    private LayoutInflater layoutInflater;
    private View dialogView;
    private TextView editName;
    private TextView editBirthday;
    private EditText editPresent;
    private TextView phone;

    private int year,month,day;

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

        java.util.Calendar cal = java.util.Calendar.getInstance();
        year=cal.get(java.util.Calendar.YEAR);       //获取年月日时分秒
        month=cal.get(java.util.Calendar.MONTH);   //获取到的月份是从0开始计数
        day=cal.get(java.util.Calendar.DAY_OF_MONTH);
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
                inputBirthday.setText(" 点击选择日期");
                inputPresent.setText("");
                inputName.setError(null,null);
            }
        });//跳转到添加条目界面
        inputBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog.OnDateSetListener listener=new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker arg0, int year, int month, int day) {
                        inputBirthday.setText(year+"-"+(++month)+"-"+day);      //将选择的日期显示到TextView中,因为之前获取month直接使用，所以不需要+1，这个地方需要显示，所以+1
                    }
                };
                DatePickerDialog dialog=new DatePickerDialog(MainActivity.this, 0,listener,year,month,day);//后边三个参数为显示dialog时默认的日期，月份从0开始，0-11对应1-12个月
                dialog.show();
            }
        });//选择日期的对话框
        editBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int yy,mm,dd;
                String date = editBirthday.getText().toString();
                String[] set = date.split("-");
                yy = Integer.parseInt(set[0]);
                mm = Integer.parseInt(set[1])-1;
                dd = Integer.parseInt(set[2]);
                //从日期字符串中获取年月日信息，用于初始化日期选择对话框
                DatePickerDialog.OnDateSetListener listener=new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker arg0, int year, int month, int day) {
                        editBirthday.setText(year+"-"+(++month)+"-"+day);      //将选择的日期显示到TextView中,因为之前获取month直接使用，所以不需要+1，这个地方需要显示，所以+1
                    }
                };
                DatePickerDialog dialog=new DatePickerDialog(MainActivity.this, 0,listener,yy,mm,dd);//后边三个参数为显示dialog时默认的日期，月份从0开始，0-11对应1-12个月
                dialog.show();
            }
        });//选择日期的对话框
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
                    String Birthday = inputBirthday.getText().toString();
                    Birthday = (Birthday.equals(" 点击选择日期")) ? "1926-08-17":Birthday;
                    dataBase.insert(inputName.getText().toString(),Birthday,inputPresent.getText().toString());
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
                phone.setText(getPhoneNum(name));
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

    private String getPhoneNum(String name){
        String PhoneNum = "无";
        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null,
                android.provider.ContactsContract.Contacts.DISPLAY_NAME + "='"+name+"'",null,null);
        if (cursor.getCount() > 0){
            String isHas = "0";
            String ContactID = "null";
            if (cursor.moveToFirst()){
                isHas = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));//是否有号码，至少有一条号码时，isHas为1，否则为0
                ContactID = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));//获取联系人ID
                Log.d("getPhoneNum", "ContactID of "+name+" is "+ContactID);
            }//获取查找结果中第一个人的联系人ID
            if (!ContactID.equals("null") && !isHas.equals("0")){
                Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + ContactID,null,null);
                if (cursorPhone.moveToFirst()){
                    PhoneNum = "";
                    int Numcnt = 0;
                    do{
                        PhoneNum += ((Numcnt == 0)?"":"、") + cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Numcnt++;
                    } while(cursorPhone.moveToNext());
                    Log.d("getPhoneNum", "PhoneNum of "+name+" is "+PhoneNum);
                }
                cursorPhone.close();
            }//如果能够获取到联系人ID时执行
        }
        cursor.close();
        return PhoneNum;
    }

}
