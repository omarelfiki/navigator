package util;

import router.Node;

import java.util.List;

public class DebugUtil {

    @SuppressWarnings("unused")
    public static void printHeapSize() {
        long initialHeap = Runtime.getRuntime().totalMemory() / (1024 * 1024);
        long maxHeap = Runtime.getRuntime().maxMemory() / (1024 * 1024);

        System.out.println("Initial Heap Size: " + initialHeap + " MB");
        System.out.println("Maximum Heap Size: " + maxHeap + " MB");
    }

    @SuppressWarnings("unused")
    public static void printNode(Node node) {
        System.out.println(node.toString());
    }

    @SuppressWarnings("unused")
    public static void printPath(List<Node> path) {
        for (Node node : path) {
            System.out.println(node.toString());
        }
    }

    @SuppressWarnings("unused")
    public static boolean getDebugMode() {
        String debug = System.getProperty("debug");
        if (debug == null) {
            return true;
        } else {
            return Boolean.parseBoolean(debug);
        }
    }

    @SuppressWarnings("unused")
    public static String getOS() {
        String os = System.getProperty("os.name").toLowerCase();
        System.out.println("Operating System: " + os);
        return os;
    }
}
