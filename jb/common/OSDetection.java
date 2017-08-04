package jb.common;

public class OSDetection {

    private static String OS = System.getProperty("os.name").toLowerCase();

    public static void main(String[] args) {

        System.out.println(OS);

        if (isWindows()) {
            System.out.println("This is Windows");
        } else if (isMac()) {
            System.out.println("This is Mac");
        } else if (isUnix()) {
            System.out.println("This is Unix or Linux");
        } else if (isSolaris()) {
            System.out.println("This is Solaris");
        } else {
            System.out.println("Your OS is not support!!");
        }
    }

    public static boolean isWindows() {

        return OS.contains("win");

    }

    public static boolean isMac() {

        return OS.contains("mac");

    }

    public static boolean isUnix() {

        return OS.contains("nix") || OS.contains("nux") || OS.contains("aix");

    }

    public static boolean isSolaris() {

        return OS.contains("sunos");
    }

}
