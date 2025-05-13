package util;

import java.util.List;

public class DebugUtli {
    public static void printHeapSize() {
        long initialHeap = Runtime.getRuntime().totalMemory() / (1024 * 1024);
        long maxHeap = Runtime.getRuntime().maxMemory() / (1024 * 1024);

        System.out.println("Initial Heap Size: " + initialHeap + " MB");
        System.out.println("Maximum Heap Size: " + maxHeap + " MB");
    }

    public static void printNode(Node node) {
        System.out.println(node.toString());
    }

    public static void printPath(List<Node> path) {
        for (Node node : path) {
            System.out.println(node.toString());
        }
    }

    public static boolean getDebugMode() {
        String debug = System.getProperty("debug");
        return debug != null && debug.equals("true");
    }
}
