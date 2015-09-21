package com.clubkiwiserver.CVar;

/**
 * Created by x201 on 6/08/2015.
 */

enum VarType
{
    String,
    Double,
    Integer
}

public class CVar
{
    private Object value;
    private VarType type;

    public CVar(Object value)
    {
        this.value = value;

        if(value instanceof String)
            type = VarType.String;
        else if(value instanceof Double)
            type = VarType.Double;
        else if(value instanceof Integer)
            type = VarType.Integer;
        else
            throw new IllegalArgumentException("CVar must be string integer or double");
    }

    public Object getValue()
    {
        return value;
    }

    public void setValue(String value) throws NumberFormatException
    {
            if (type == VarType.String)
                this.value = value;
            else if (type == VarType.Double)
                this.value = Double.parseDouble(value);
            else if (type == VarType.Integer)
                this.value = Integer.parseInt(value);
            else
                throw new NumberFormatException("CVar must be set to string integer or double");
    }

    public VarType getType()
    {
        return type;
    }
}
