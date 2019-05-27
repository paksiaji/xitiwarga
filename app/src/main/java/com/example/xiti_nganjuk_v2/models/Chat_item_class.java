package com.example.xiti_nganjuk_v2.models;

public class Chat_item_class {
    private String messageId;
    private String messageText;
    private String messageTime;
    private String messageSender;
    private String messageReceiver;

    public Chat_item_class(String messageId, String messageText, String messageTime, String messageSender, String messageReceiver) {
        this.messageId = messageId;
        this.messageText = messageText;
        this.messageTime = messageTime;
        this.messageSender = messageSender;
        this.messageReceiver = messageReceiver;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(String messageTime) {
        this.messageTime = messageTime;
    }

    public String getMessageSender() {
        return messageSender;
    }

    public void setMessageSender(String messageSender) {
        this.messageSender = messageSender;
    }

    public String getMessageReceiver() {
        return messageReceiver;
    }

    public void setMessageReceiver(String messageReceiver) {
        this.messageReceiver = messageReceiver;
    }
}
