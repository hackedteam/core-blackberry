/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Parameter.java 
 * Created      : 26-mar-2010
 * *************************************************/
package com.ht.rcs.blackberry.params;

// TODO: Auto-generated Javadoc
/**
 * The Class Parameter.
 */
public class Parameter {

    /**
     * Factory.
     * 
     * @param ParamsId
     *            the params id
     * @param confParams
     *            the conf params
     * @return the parameter
     */
    public static Parameter Factory(int ParamsId, byte[] confParams) {
        Parameter parameter = new Parameter(ParamsId, confParams);
        return parameter;
    }

    /** The Parameter id. */
    public int ParameterId = -1;

    /** The Conf params. */
    byte[] ConfParams;

    /**
     * Instantiates a new parameter.
     * 
     * @param ParamsId
     *            the params id
     * @param confParams
     *            the conf params
     */
    public Parameter(int ParamsId, byte[] confParams) {
        this.ParameterId = ParamsId;
        this.ConfParams = confParams;
    }
}
