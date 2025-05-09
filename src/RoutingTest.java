import java.util.List;

public class RoutingTest {
    public static void main(String[] args) {
        AStarRouterV router = new AStarRouterV();
        DBaccess db = DBaccessProvider.getInstance();
        if (db == null) {
            System.err.println("DBaccessProvider returned null.");
            return;
        }

        List<Node> path = router.findFastestPath(42.0186,12.4989,42.0191,12.4911,"08:00:00");
        if (path != null) {
            System.out.println("Path found:");
            for (Node node : path) {
                System.out.println(node);
            }
        } else {
            System.out.println("No path found.");
        }
    }
}
