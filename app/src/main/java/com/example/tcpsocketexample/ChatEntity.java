package com.example.tcpsocketexample;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Administrator on 2016/6/27.
 */
public class ChatEntity {
    private final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private String mChatHostAddress;
    private String mChatHostName;
    private String mChatContents;
    private Date mChatDate;
    private boolean isInComingMsg;

    public ChatEntity(){
        mChatDate = Calendar.getInstance().getTime();
    }

    public void setChatHostAddress(String address){
        mChatHostAddress = address;
    }

    public String getChatHostAddress(){
        return mChatHostAddress;
    }

    public void setChatHostName (String name){
        mChatHostName = name;
    }

    public String getChatHostName(){
        return mChatHostName;
    }

    public void setChatContents(String contents){
        mChatContents = contents;
    }

    public String getChatContents(){
        return  mChatContents;
    }

    public void setMsgType(boolean type){
        isInComingMsg = type;
    }

    public boolean getMsgType(){
        return isInComingMsg;
    }

    public String getChatDate(){
        DateFormat format = new SimpleDateFormat(DATE_FORMAT);
        return format.format(mChatDate);
    }
}
