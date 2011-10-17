package com.rim.samples.device.smsdemo;

public class Debug {

    public void trace(String string) {
        System.out.println(Thread.currentThread().getName() + " " + string);
    }

}
