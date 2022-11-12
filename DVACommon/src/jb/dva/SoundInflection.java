package jb.dva;

public class SoundInflection
{
    public static final int NONE = 0;
    public static final int BEGINNING = 1;
    public static final int REGULAR = 2;
    public static final int ENDING = 3;

    public static String getNameForInflection(int inflection) {
        switch (inflection) {
        case NONE: return "None";
        case BEGINNING: return "Beginning";
        case REGULAR: return "Regular";
        case ENDING: return "Ending";
        default: return null;
        }
    }

    public static int getInflectionForName(String name) {
        switch (name.trim()) {
            case "None":
                return NONE;
            case "Beginning":
                return BEGINNING;
            case "Regular":
                return REGULAR;
            case "Ending":
                return ENDING;
            default:
                return -1;
        }
    }
}