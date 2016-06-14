package com.example.android.wifidirect.discovery;

/**
 * Created by Naser on 4/19/2016.
 */
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serializer {

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(obj);
        Log.d("Serializer","Serilizer byte size: " + b.toByteArray().length);
        return b.toByteArray();

    }

    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream b = new ByteArrayInputStream(bytes);
        ObjectInputStream o = new ObjectInputStream(b);
        Log.d("Serializer","deSerilizer byte size: " + bytes.length);
        return o.readObject();
    }

}