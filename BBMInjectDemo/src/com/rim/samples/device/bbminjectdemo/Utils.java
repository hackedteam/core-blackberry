package com.rim.samples.device.bbminjectdemo;

import net.rim.blackberry.api.menuitem.ApplicationMenuItemRepository;

public class Utils {
    
    public static void addMenu(long id){
        long a=ApplicationMenuItemRepository.MENUITEM_SMS_VIEW;
    }
    
    public static void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {

        }
    }
    
    public static String byteArrayToHex(final byte[] data) {
        return byteArrayToHex(data, 0, data.length);
    }

    public static String byteArrayToHex(final byte[] data, final int offset,
            final int length) {
        final StringBuffer buf = new StringBuffer();
        for (int i = offset; i < offset + length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int twohalfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9)) {
                    buf.append((char) ('0' + halfbyte));
                } else {
                    buf.append((char) ('a' + (halfbyte - 10)));
                }
                halfbyte = data[i] & 0x0F;
            } while (twohalfs++ < 1);
        }
        return buf.toString();
    }
}
