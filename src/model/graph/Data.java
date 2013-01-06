package model.graph;

/**
 * The Data class acts as an storage of an additional data in vertices or edges.
 */
public class Data {

    public static final int BOOLEAN = 0;
    public static final int BYTE = 1;
    public static final int INTEGER = 2;
    public static final int LONG = 3;
    public static final int FLOAT = 4;
    public static final int DOUBLE = 5;
    public static final int OTHER = 6;

    private Object value = null;

    private int type = -1;

    public Data() {}

    public Data(Object value) {

        this.value = value;

        if ("java.lang.Integer".equals(value.getClass().getName())) {
            this.type = Data.INTEGER;
        } else if ("java.lang.Boolean".equals(value.getClass().getName())) {
            this.type = Data.BOOLEAN;
        } else if ("java.lang.Byte".equals(value.getClass().getName())) {
            this.type = Data.BYTE;
        } else if ("java.lang.Long".equals(value.getClass().getName())) {
            this.type = Data.LONG;
        } else if ("java.lang.Float".equals(value.getClass().getName())) {
            this.type = Data.FLOAT;
        } else if ("java.lang.Double".equals(value.getClass().getName())) {
            this.type = Data.DOUBLE;
        } else {
            this.type = Data.OTHER;
        }
    }

    public Data(Object value, int type) {
        this.value = value;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
    	
        if (type == -1 && value != null) {
            if ("java.lang.Integer".equals(value.getClass().getName())) {
                this.type = Data.INTEGER;
            } else if ("java.lang.Boolean".equals(value.getClass().getName())) {
                this.type = Data.BOOLEAN;
            } else if ("java.lang.Byte".equals(value.getClass().getName())) {
                this.type = Data.BYTE;
            } else if ("java.lang.Long".equals(value.getClass().getName())) {
                this.type = Data.LONG;
            } else if ("java.lang.Float".equals(value.getClass().getName())) {
                this.type = Data.FLOAT;
            } else if ("java.lang.Double".equals(value.getClass().getName())) {
                this.type = Data.DOUBLE;
            } else {
                this.type = Data.OTHER;
            }
        }
        this.value = value;
    }
    
    public String toString() {
    	return value.toString();
    }
    
}
