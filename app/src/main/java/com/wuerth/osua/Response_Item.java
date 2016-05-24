package com.wuerth.osua;

public class Response_Item {
    public int statusCode;
    public String body;

    Response_Item(int statusCode, String body){
        this.statusCode = statusCode;
        this.body = body;
    }
}
