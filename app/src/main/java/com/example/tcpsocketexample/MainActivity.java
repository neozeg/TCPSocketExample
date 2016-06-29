package com.example.tcpsocketexample;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
public class MainActivity extends Activity {
    private final static String TAG = "MainActivity";

    private final static int OPEN_FILE_DIALOG_ID = 101;
    //view components;
    private Button mBtnT1,mBtnT2,mBtnT3,mBtnSend;
    private ListView mLvChatContent;
    private EditText mEtSendContent;

    private ChatListAdapter mChatListAdapter;
    private ArrayList<ChatEntity> mChatList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupViewComponents();
        startTCPserver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceivers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceivers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopTCPserver();
    }

    private void setupViewComponents(){
        mBtnT1 = (Button) findViewById(R.id.buttonTop1);
        mBtnT2 = (Button) findViewById(R.id.buttonTop2);
        mBtnT3 = (Button) findViewById(R.id.buttonTop3);
        mBtnSend = (Button) findViewById(R.id.buttonSend);
        mBtnSend.setOnClickListener(mBtnOCL);
        mBtnT1.setOnClickListener(mBtnOCL);
        mBtnT2.setOnClickListener(mBtnOCL);
        mBtnT3.setOnClickListener(mBtnOCL);
        mEtSendContent = (EditText)findViewById(R.id.editText);

        mLvChatContent = (ListView) findViewById(R.id.listViewChatContent);
        mChatList = new ArrayList<ChatEntity>();
        mChatListAdapter = new ChatListAdapter(getApplicationContext(),mChatList);
        mLvChatContent.setAdapter(mChatListAdapter);
    }

    private View.OnClickListener mBtnOCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == mBtnSend.getId()){
                sendChatMessage();
            }else if(v.getId() == mBtnT1.getId()){
                startTCPserver();
            }else if(v.getId() == mBtnT2.getId()){
                stopTCPserver();
            }else if(v.getId() == mBtnT3.getId()){
                showDialog(OPEN_FILE_DIALOG_ID);
            }
        }
    };

    private void sendChatMessage(){
        String msg = mEtSendContent.getText().toString();
        ChatEntity entity = new ChatEntity();
        entity.setMsgType(false);
        entity.setChatHostAddress("Host");
        entity.setChatContents(msg);
        mChatList.add(entity);
        mChatListAdapter.notifyDataSetChanged();
        //mTCPService.sendMsg(mTCPService.getClient(0),msg);
        for(int i=0;i<mTCPService.getClientCount();i++)mTCPService.sendMsg(mTCPService.getClient(i),msg);
        mEtSendContent.setText("");

    }
    private TCPSocketService mTCPService = null;
    private ServiceConnection mServiceCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mTCPService = ((TCPSocketService.LocalBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //mTCPService.unbindService(mServiceCon);

        }
    };
    private void startTCPserver(){
        Intent service = new Intent(MainActivity.this,TCPSocketService.class);
        bindService(service,mServiceCon,BIND_AUTO_CREATE);
    }
    private void stopTCPserver(){
        mTCPService = null;
        unbindService(mServiceCon);
    }

    private void registerReceivers(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(TCPSocketService.ACTION_TCP_INCOMING_MSG);
        registerReceiver(mReceiver,filter);
    }

    private void unregisterReceivers(){
        unregisterReceiver(mReceiver);
    }
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(TCPSocketService.ACTION_TCP_INCOMING_MSG)){
                String msg = intent.getStringExtra(TCPSocketService.EXTRA_TCP_MSG);
                String ip = intent.getStringExtra(TCPSocketService.EXTRA_TCP_HOST_IP);
                int port = intent.getIntExtra(TCPSocketService.EXTRA_TCP_HOST_PORT,0);
                ChatEntity entity = new ChatEntity();
                entity.setChatHostAddress(ip+":"+port);
                entity.setChatContents(msg);
                entity.setMsgType(true);
                mChatList.add(entity);
                mChatListAdapter.notifyDataSetChanged();
            }
        }
    };


    private byte[] readFile(String filepath){
        byte[] buffer = null;
        try {
            FileInputStream inputStream = new FileInputStream(filepath);
            int length = inputStream.available();
            buffer = new byte[length];
            inputStream.read(buffer);
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
        return buffer;
    }

    private void sendBufferedData(String filepath){
        final byte[] buffer;// = readFile(filepath);
        try {
            buffer = MyZipUtil.createZipBuffer(filepath);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mTCPService.sendData(mTCPService.getClient(0),buffer);
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////////
    //file dialog
    //////////////////////////////////////////////////////////////////////

    @Override
    protected Dialog onCreateDialog(int id) {
        if(id == OPEN_FILE_DIALOG_ID){
            Map<String,Integer> images = new HashMap<String, Integer>();
            images.put(OpenFileDialog.sParent,R.drawable.filedialog_folder_up);
            images.put(OpenFileDialog.sRoot,R.drawable.filedialog_root);
            images.put(OpenFileDialog.sFolder,R.drawable.filedialog_folder);
            images.put("wav",R.drawable.filedialog_wavfile);
            images.put(OpenFileDialog.sEmpty,R.drawable.filedialog_file);
            //images.put("bin",R.drawable.filedialog_file);
            Dialog dialog = OpenFileDialog.createDialog(id,this,"Open File",new CallbackBundle(){

                        @Override
                        public void callback(Bundle bundle) {
                            String filepath = bundle.getString(OpenFileDialog.EXTRA_STRING_PATH);
                            Log.v(TAG,"path= "+ filepath);
                            sendBufferedData(filepath);
                        }
                    },"",images
            );
            return dialog;
        }
        return null;
    }
}
