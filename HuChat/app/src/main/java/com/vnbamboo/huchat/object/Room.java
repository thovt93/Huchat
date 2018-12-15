package com.vnbamboo.huchat.object;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private String roomCode;
    private String name;
    private List<ChatMessage> chatHistory;
    private List<User> listMember;
    private transient Bitmap avatar;

    public Room( String roomCode, String name){
        this.roomCode = roomCode;
        this.name = name;
    }
    public Room(){
        roomCode = name = null;
        listMember = new ArrayList<>();
        chatHistory = new ArrayList<>();
    }


    public Room(Room a){
        this.roomCode = a.getRoomCode();
        this.listMember.addAll(a.getListMember());
        this.avatar = a.getAvatar();
    }


    public void setRoomCode( String roomCode ) {
        this.roomCode = roomCode;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public List<ChatMessage> getChatHistory() {
        return chatHistory;
    }

    public void setChatHistory( List<ChatMessage> chatHistory ) {
        this.chatHistory = chatHistory;
    }

    public ChatMessage getChatMessageAt(int i){
        return chatHistory.get(i);
    }

    public List<User> getListMember() {
        return listMember;
    }

    public void setListMember( List<User> listMember ) {
        this.listMember.addAll(listMember);
    }

    public User getUserAt(int i){
        return listMember.get(i);
    }

    public Bitmap getAvatar() {
        return avatar;
    }
    public void addMessage(ChatMessage chatMessage){
        this.chatHistory.add(chatMessage);
    }
    public void setAvatar( Bitmap avatar ) {
        this.avatar = avatar;
    }
}

