package com.example.clockedinprojectt9.models;

public class ChatSummary {
    private final Object target; // User or Event
    private final String lastMessage;
    private final long lastTimestamp;

    public ChatSummary(Object target, String lastMessage, long lastTimestamp) {
        this.target = target;
        this.lastMessage = lastMessage;
        this.lastTimestamp = lastTimestamp;
    }

    public Object getTarget() {
        return target;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public long getLastTimestamp() {
        return lastTimestamp;
    }
}
