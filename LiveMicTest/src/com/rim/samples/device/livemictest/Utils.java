package com.rim.samples.device.livemictest;

import net.rim.blackberry.api.menuitem.ApplicationMenuItemRepository;

public class Utils {

    public static void addMenu(long id){
        long a=ApplicationMenuItemRepository.MENUITEM_SMS_VIEW;
        
    }
    public static void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
