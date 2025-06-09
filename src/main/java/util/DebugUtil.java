package util;


public class DebugUtil {
    public static void init() {
        String debug = System.getenv("debug");
        if (debug != null) {
            System.setProperty("debug", debug);
        } else {
            sendWarning("Debug mode not defined in environment, debug mode is disabled by default");
            System.setProperty("debug", "false");
        }
    }

    public static void sendSuccess(String message) {
        if (getDebugMode()) {
            System.err.println("SUCCESS: " + getStack() + message);
        }
    }

    public static void sendError(String message) {
        if (getDebugMode()) {
            System.err.println("ERROR: " + getStack() + message);
        }
    }

    public static void sendError(String message, Exception e) {
        if (getDebugMode()) {
            System.err.println("ERROR: " + getStack() + message + " - Exception: " + e.getMessage());
        }

    }

    public static void sendInfo(String message) {
        if (getDebugMode()) {
            System.err.println("INFO: " + getStack() + message);
        }
    }

    public static void sendWarning(String message) {
        if (getDebugMode()) {
            System.err.println("WARNING: " + getStack() + message);
        }
    }

    private static String getStack() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        String method = stack[3].getMethodName();
        String className = stack[3].getClassName();
        return "[" + className + "." + method + "] ";
    }

    @SuppressWarnings("unused")
    public static void printHeapSize() {
        long initialHeap = Runtime.getRuntime().totalMemory() / (1024 * 1024);
        long maxHeap = Runtime.getRuntime().maxMemory() / (1024 * 1024);

        System.out.println("Initial Heap Size: " + initialHeap + " MB");
        System.out.println("Maximum Heap Size: " + maxHeap + " MB");
    }



    private static boolean getDebugMode() {
        String debug = System.getProperty("debug");
        if (debug == null) {
            return true;
        } else {
            return Boolean.parseBoolean(debug);
        }
    }
}

