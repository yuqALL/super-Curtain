package com.yuq.curtain.supercurtain.data;

import java.io.File;
import java.util.ArrayList;

public class ImMsgBean {

    public final static int CHAT_SENDER_OTHER= 0;
    public final static int CHAT_SENDER_ME = 1;

    public final static int CHAT_MSGTYPE_TEXT = 11;

    private String sender;
    private String recipient;
    private String id;
    private int msgType;
    private int senderType;
    private String time;
    private String name;
    private String content;

    private File file;
    private int fileLoadstye;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public int getSenderType() {
        return senderType;
    }

    public void setSenderType(int senderType) {
        this.senderType = senderType;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public int getFileLoadstye() {
        return fileLoadstye;
    }

    public void setFileLoadstye(int fileLoadstye) {
        this.fileLoadstye = fileLoadstye;
    }

    public static class CommentRequestData {
        private int count;

        private int result;

        private ArrayList<ImMsgBean> data;

        public int getCount() {
            return count;
        }

        public int getResult() {
            return result;
        }

        public ArrayList<ImMsgBean> getData() {
            return data;
        }
    }
}
