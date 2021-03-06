package cn.edu.scut.phonebook;

import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.WriterException;
import com.yzq.zxinglibrary.encode.CodeCreator;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.util.Date;
import java.util.List;

public class ContactsPersonCardActivity extends AppCompatActivity {

    ContactsPerson person;
    private TextView NameTextView;
    private Context context = this;
    private RecyclerView contactsPersonPhoneNumListView;
    private LinearLayoutManager linearLayoutManager;
    private ContactsPersonPhoneNumListAdapter adapter;
    static ContactsDBHandle contactsDBHandle = new ContactsDBHandle();

    private Button Bit_Card; //二维码名片
    private Button EditContactsButton;
    private Button DeleteContactsButton;
    private Button CallLogBtn;
    private Button Collected; //星标联系人
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LitePal.getDatabase(); //初始化数据库
        setContentView(R.layout.activity_contacts_person_card);

        Intent intent = getIntent();
        person = (ContactsPerson)intent.getSerializableExtra("ContactsPerson");

        NameTextView = (TextView)findViewById(R.id.ContactsPersonNameTextView);

        NameTextView.setText(person.getName());

        //隐藏状态栏
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        initRecyclerViewParts();
        initButton();
        initButtonsClickEvents();


    }

    private void initRecyclerViewParts()
    {
        contactsPersonPhoneNumListView = (RecyclerView)findViewById(R.id.ContactsPersonPhoneNumsListView);
        linearLayoutManager = new LinearLayoutManager(this);
        contactsPersonPhoneNumListView.setLayoutManager(linearLayoutManager);
        adapter = new ContactsPersonPhoneNumListAdapter(this,person.getPhoneNumbers());

        contactsPersonPhoneNumListView.setAdapter(adapter);

    }
    private void initButton(){
        Bit_Card = (Button)findViewById(R.id.QRCodeShare_Btn);
        EditContactsButton = (Button)findViewById(R.id.EditContacts_Btn);
        DeleteContactsButton = (Button)findViewById(R.id.DeleteContacts_Btn);
        CallLogBtn = (Button)findViewById(R.id.ShowCallRecord_Btn);
        Collected = (Button)findViewById(R.id.Collected_Btn);
        contactsDBHandle.Create(ContactsPersonCardActivity.this);//创建数据表
        if(ContactsPersonCardActivity.contactsDBHandle.getimportance(ContactsPersonCardActivity.this.person.getName())){
            Collected.setText("已收藏");//判断是否重要
        };
    }


    private void initButtonsClickEvents(){

        //二维码按钮点击事件
        Bit_Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = null;// 这里是获取图片Bitmap，也可以传入其他参数到Dialog中
                String contentEtString = person.getVcard();
                try {
                    Bitmap logo = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                    bitmap = CodeCreator.createQRCode(contentEtString, 500, 500, logo);

                } catch (WriterException e) {
                    e.printStackTrace();
                }
                QRShowDialog.Builder dialogBuild = new QRShowDialog.Builder(ContactsPersonCardActivity.this);
                dialogBuild.setBitmap(bitmap);
                QRShowDialog dialog = dialogBuild.create();
                dialog.setCanceledOnTouchOutside(true);// 点击外部区域关闭
                dialog.show();
            }
        });

        EditContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactsPersonCardActivity.this, EditContactsActivity.class);
                intent.putExtra("ID", person.getID());
                intent.putExtra("name", person.getName());
                intent.putExtra("phone", person.getPhoneNumbers().get(0));

                if(!person.isEmailEmpty())
                {
                    intent.putExtra("email", person.getEmails().get(0));
                }
                startActivity(intent);
            }
        });

        DeleteContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(ContactsPersonCardActivity.this)
                        .setTitle("是否确认删除？")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                ContactsUtils.deleteContacts(ContactsPersonCardActivity.this, person.getID());

                                Toast.makeText(ContactsPersonCardActivity.this,"删除成功", Toast.LENGTH_SHORT).show();

                                finish();
                            }
                        }).show();
            }
        });

        CallLogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ContactsPersonCardActivity.this, CalllogSort.class);
                intent.putExtra("key", person.getName());
                intent.putExtra("isFromContactsCard", "true");
                startActivity(intent);
            }});
        Collected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                List<ContactsDB> contactsDBS = DataSupport.findAll(ContactsDB.class);
                for(ContactsDB contactsDB:contactsDBS){
                    Log.i("contant",contactsDB.getName());
                }*/
                ContactsPersonCardActivity.contactsDBHandle.switchimportance(ContactsPersonCardActivity.this.person.getName());
                if(ContactsPersonCardActivity.contactsDBHandle.getimportance(ContactsPersonCardActivity.this.person.getName())){
                    Collected.setText("已收藏");
                    //Log.i("collect","已收藏");
                }else{
                    Collected.setText("收藏");
                    //Log.i("collect","未收藏");
                }
                //Log.i("contant","有点到");
                //String tempname = contactsDBHandle.getDate("洪浩强").toString();
                //Toast.makeText(ContactsPersonCardActivity.this, contactsDBHandle.getDate("洪浩强").toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
