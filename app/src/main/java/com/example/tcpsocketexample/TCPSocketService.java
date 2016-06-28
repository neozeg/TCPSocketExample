package com.example.tcpsocketexample;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016/6/27.
 */
public class TCPSocketService extends Service {
    private final static String TAG = "TCPSocketService";
    private final static int SERVER_PORT = 6664;

    public final static String ACTION_TCP_INCOMING_MSG = "action.tcp.incoming.msg";
    public final static String EXTRA_TCP_MSG = "extra.tcp.message";
    public final static String EXTRA_TCP_HOST_IP = "extra.tcp.host.ip";
    public final static String EXTRA_TCP_HOST_PORT = "extra.tcp.host.port";
    public final static String EXTRA_TCP_CLIENT = "extra.tcp.client";

    private boolean isServiceDestroy = false;
    ServerSocket mServerSocket = null;
    ArrayList<Socket> mClientList;

    @Override
    public void onCreate() {
        new Thread(new TcpServer()).start();
        Log.v(TAG,"onCreate()");
        mClientList = new ArrayList<Socket>();
        mClientList.clear();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        isServiceDestroy = true;
        try {
            mServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.v(TAG,"onDestroy()");
        super.onDestroy();
    }

    public class LocalBinder extends Binder{
        TCPSocketService getService(){
            return TCPSocketService.this;
        }
    }
    private LocalBinder mLocalBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mLocalBinder;
    }

    private class TcpServer implements Runnable{
        @Override
        public void run() {
            //ServerSocket serverSocket;
            try {
                mServerSocket = new ServerSocket(SERVER_PORT);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            while (!isServiceDestroy){
                try {
                    final Socket client = mServerSocket.accept();
                    mClientList.add(client);
                    new Thread(){
                        @Override
                        public void run() {
                            try {
                                responseClient(client);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public int getClientCount(){
        return  mClientList.size();
    }
    public Socket getClient(int pos){
        Socket client = mClientList.get(pos);
        return mClientList.get(pos);
    }
    public void sendData(Socket client,byte[] data){
        OutputStream out;
        int length = 1024;
        try {
            int i=0;
            out = client.getOutputStream();
            out.write(data);
            if(data.length<length){
                for(i=0;i<data.length/length;i++){
                    out.write(data,i*length,length);
                    out.flush();
                }
            }
            out.write(data,i*length,data.length-(i*length));
            out.flush();
            //out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendMsg(Socket client,String msg){
        PrintWriter out;
        try {
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())),true);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        out.println(msg);
        //out.close();

    }
    private void responseClient(Socket client) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(),"UTF-8"));
        //PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())),true);
        //out.println("hello");
        Log.i(TAG,client.getInetAddress().getHostAddress()+" connected");
        while(!isServiceDestroy){
            char[] cha = new char[1024];
            int len = in.read(cha);
            String str = new String(cha,0,len);
            Intent data = new Intent();
            data.setAction(ACTION_TCP_INCOMING_MSG);
            data.putExtra(EXTRA_TCP_MSG,str);
            data.putExtra(EXTRA_TCP_HOST_IP,client.getInetAddress().getHostAddress());
            data.putExtra(EXTRA_TCP_HOST_PORT,client.getPort());
            sendBroadcast(data);

            Log.i(TAG,"MSG: "+ str);
            if(TextUtils.isEmpty(str)){
                Log.i(TAG,"Client disconnected");
                break;
            }
        }
        //out.close();
        in.close();
        client.close();
    }

}
