/*
* File Input.java
*
* Copyright (C) 2010 Remco Bouckaert remco@cs.auckland.ac.nz
*
* This file is part of BEAST2.
* See the NOTICE file distributed with this work for additional
* information regarding copyright ownership and licensing.
*
* BEAST is free software; you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
*  BEAST is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with BEAST; if not, write to the
* Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
* Boston, MA  02110-1301  USA
*/
package beast.core;

//import sun.reflect.generics.reflectiveObjects.WildcardTypeImpl;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents input of a Plugin class.
 * Inputs connect Plugins with outputs of other Plugins,
 * e.g. a Logger can get the result it needs to log from a
 * Plugin that actually performs a calculation.
 */
public class Input<T> {
    /**
     * input name, used for identification when getting/setting values of a plug-in *
     */
    String name = "";

    /**
     * short description of the function of this particular input *
     */
    String tipText = "";

    /**
     * value represented by this input
     */
    T value;

    /**
     * Type of T, automatically determined when setting a new value.
     * Used for type checking.
     */
    Class<?> theClass;

    /**
     * validation rules *
     */
    public enum Validate {
        OPTIONAL, REQUIRED, XOR
    }

    ;
    Validate rule = Validate.OPTIONAL;
    /**
     * used only if validation rule is XOR *
     */
    Input<?> other;
    public T defaultValue;

    /**
     * constructors *
     */
    public Input() {
    }

    public Input(String sName, String sTipText) {
        name = sName;
        tipText = sTipText;
        value = null;
    } // c'tor

    public Input(String sName, String sTipText, T startValue) {
        name = sName;
        tipText = sTipText;
        value = startValue;
        defaultValue = startValue;
    } // c'tor

    /**
     * constructor for REQUIRED rules for array-inputs *
     */
    public Input(String sName, String sTipText, T startValue, Validate rule) {
        name = sName;
        tipText = sTipText;
        value = startValue;
        defaultValue = startValue;
        if (rule != Validate.REQUIRED) {
            System.err.println("Programmer error: input rule should be REQUIRED for this Input constructor");
        }
        this.rule = rule;
    } // c'tor

    /**
     * constructor for REQUIRED rules *
     */
    public Input(String sName, String sTipText, Validate rule) {
        name = sName;
        tipText = sTipText;
        value = null;
        if (rule != Validate.REQUIRED) {
            System.err.println("Programmer error: input rule should be REQUIRED for this Input constructor");
        }
        this.rule = rule;
    } // c'tor

    /**
     * constructor for XOR rules *
     */
    public Input(String sName, String sTipText, Validate rule, Input<?> other) {
        name = sName;
        tipText = sTipText;
        value = null;
        if (rule != Validate.XOR) {
            System.err.println("Programmer error: input rule should be XOR for this Input constructor");
        }
        this.rule = rule;
        this.other = other;
        this.other.other = this;
        this.other.rule = rule;
    } // c'tor

    /**
     * various setters and getters *
     */
    public String getName() {
        return name;
    }

    public String getTipText() {
        return tipText;
    }

    public T get() {
        return value;
    }

    public Class<?> type() {
        return theClass;
    }

    public Validate getRule() {
        return rule;
    }

    public Input<?> getOther() {
        return other;
    }

    /**
     * Sets value to this input.
     * If class is not determined yet, first determine class of declaration of
     * this input so that we can do type checking.
     * If value is of type String, try to parse the value if this input is
     * Integer, Double or Boolean.
     * If this input is a List, instead of setting this value, the value is
     * added to the vector.
     * Otherwise, m_value is assigned to value.
     */
    @SuppressWarnings("unchecked")
    public void setValue(Object value, Plugin plugin) throws Exception {
        if (theClass == null) {
            determineClass(plugin);
        }
        if (value instanceof String) {
            setStringValue((String) value);
        } else if (this.value != null && this.value instanceof List<?>) {
            if (theClass.isAssignableFrom(value.getClass())) {
                // don't insert duplicates
                List vector = (List) this.value;
                for (Object o : vector) {
                    if (o.equals(value)) {
                        return;
                    }
                }
                vector.add(value);
            } else {
                throw new Exception("Input 101: type mismatch");
            }

        } else {
            if (theClass.isAssignableFrom(value.getClass())) {
                this.value = (T) value;
            } else {
                throw new Exception("Input 102: type mismatch");
            }
        }
    }


    /**
     * Determine class through introspection,
     * This sets the m_class member of Input<T> to the actual value of T.
     * If T is a vector, i.e. Input<List<S>>, the actual value of S
     * is assigned instead *
     */
    void determineClass(Plugin plugin) {
        try {
            Field[] fields = plugin.getClass().getFields();
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].getType().isAssignableFrom(Input.class)) {
                    Input<?> input = (Input<?>) fields[i].get(plugin);
                    if (input == this) {
                        Type t = fields[i].getGenericType();
                        Type[] genericTypes = ((ParameterizedType) t).getActualTypeArguments();
                        if (value != null && value instanceof ArrayList<?>) {
                            Type[] genericTypes2 = ((ParameterizedType) genericTypes[0]).getActualTypeArguments();

//                            if (genericTypes2[0] instanceof sun.reflect.generics.reflectiveObjects.WildcardTypeImpl) {
//                                WildcardTypeImpl wcti = (WildcardTypeImpl) genericTypes2[0];
//                                theClass = wcti.getClass();
//                            } else
                            theClass = (Class<?>) genericTypes2[0];
                        } else {


                            try {
                                Class<?> genericType = (Class<?>) genericTypes[0];
                                theClass = genericType;
                            } catch (Exception e) {
                                System.err.println(plugin.getClass().getName() + " " + plugin.getID() + " failed. Possibly template or abstract Plugin used???");
                                System.err.println("class is " + plugin.getClass());
                                e.printStackTrace(System.err);
                                System.exit(0);
                            }
                        }
                        i = fields.length;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // determineClass

    /**
     * Try to parse value of string into Integer, Double or Boolean,
     * or it this types differs, just assign as string.
     */
    @SuppressWarnings("unchecked")
    private void setStringValue(String sValue) throws Exception {
        // figure out the type of T and create object based on T=Integer, T=Double, T=Boolean, T=String
        if (theClass.equals(Integer.class)) {
            value = (T) new Integer(sValue);
            return;
        }
        if (theClass.equals(Double.class)) {
            value = (T) new Double(sValue);
            return;
        }
        if (theClass.equals(Boolean.class)) {
            String sValue2 = sValue.toLowerCase();
            if (sValue2.equals("yes") || sValue2.equals("no") || sValue2.equals("true") || sValue2.equals("false")) {
                value = (T) new Boolean(sValue != null && sValue.equals("yes") || sValue.equals("true"));
                return;
            }
        }
        // settle for a string
        if (theClass.isAssignableFrom(sValue.getClass())) {
            value = (T) sValue;
        } else {
            throw new Exception("Input 103: type mismatch");
        }
    } // setStringValue

    /**
     * validate input according to validation rule *
     */
    public void validate() throws Exception {
        switch (rule) {
            case OPTIONAL:
                // noting to do
                break;
            case REQUIRED:
                if (get() == null) {
                    throw new Exception("Input '" + getName() + "' must be specified.");
                }
                if (get() instanceof List<?>) {
                    if (((List<?>) get()).size() == 0) {
                        throw new Exception("At least one input of name '" + getName() + "' must be specified.");
                    }
                }
                break;
            case XOR:
                if (get() == null) {
                    if (other.get() == null) {
                        throw new Exception("Either input '" + getName() + "' or '" + other.getName() + "' needs to be specified");
                    }
                } else {
                    if (other.get() != null) {
                        throw new Exception("Only one of input '" + getName() + "' and '" + other.getName() + "' must be specified (not both)");
                    }
                }
                // noting to do
                break;
        }
    } // validate

} // class Input
