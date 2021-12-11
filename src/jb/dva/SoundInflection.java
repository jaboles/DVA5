package jb.dva;

public class SoundInflection
{
    public static final int NONE = 0;
    public static final int RISING = 1;
    public static final int FALLING = 2;

    public static String getNameForInflection(int inflection) {
        switch (inflection) {
        case NONE: return "None";
        case RISING: return "Rising";
        case FALLING: return "Falling";
        default: return null;
        }
    }

    public static int getInflectionForName(String name) {
        if (name.trim().equals("None")) {
            return NONE;
        } else if (name.trim().equals("Rising")) {
            return RISING;
        } else if (name.trim().equals("Falling")) {
            return FALLING;
        } else {
            return -1;
        }
    }
}