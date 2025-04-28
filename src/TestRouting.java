public class TestRouting {

    public static void main(String[] args) {
        DBaccess dbaccess = new DBaccess(System.getenv("ROUTING_ENGINE_MYSQL_JDBC"));
        dbaccess.connect();

        if (dbaccess.conn != null) {
            DBService service = new DBService(dbaccess);

            RouteResult result = RouteFinder.findOptimalTrip(
                    41.9028, 12.4964,
                    41.8902, 12.4922,
                    service
            );

            if (result != null) {
                System.out.println(result);
            } else {
                System.out.println("❌ No route found.");
            }

        } else {
            System.out.println("❌ Failed to connect to database. Check credentials.");
        }


    }
}
