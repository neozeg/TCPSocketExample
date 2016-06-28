package com.example.tcpsocketexample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by Administrator on 2016/6/27.
 */
public class ChatListAdapter extends BaseAdapter {
    private final static  String TAG = "ChatListAdapter";
    private Context mContext;
    private List<ChatEntity> mChatList;
    private LayoutInflater mInflater;

    public ChatListAdapter(Context context,List<ChatEntity> list){
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mChatList = list;
    }

    public static interface IMsgViewType {
        int IMVT_COM_MSG = 0;
        int IMVT_TO_MSG = 1;
    }
    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if(mChatList.get(position).getMsgType())
            return IMsgViewType.IMVT_COM_MSG;
        else
            return IMsgViewType.IMVT_TO_MSG;

    }

    @Override
    public int getCount() {
        return mChatList.size();
    }

    @Override
    public Object getItem(int position) {
        return mChatList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        ChatEntity entity = mChatList.get(position);
        if(convertView == null){
            if(!entity.getMsgType()){
                convertView = mInflater.inflate(R.layout.chatting_item_msg_text_right,null);
            }else
                convertView = mInflater.inflate(R.layout.chatting_item_msg_text_left,null);
            holder = new ViewHolder();
            holder.mTvName = (TextView) convertView.findViewById(R.id.textViewSendName);
            holder.mTvDate = (TextView) convertView.findViewById(R.id.textViewSendTime);
            holder.mTvContent =  (TextView) convertView.findViewById(R.id.textViewSendMessage);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }
        holder.mTvName.setText(entity.getChatHostAddress());
        holder.mTvContent.setText(entity.getChatContents());
        holder.mTvDate.setText(entity.getChatDate());


        return convertView;
    }

    static class ViewHolder{
        //TextView mTvAddress;
        public TextView mTvName;
        public TextView mTvDate;
        public TextView mTvContent;
        public boolean isInComeMsg;
    }
}
