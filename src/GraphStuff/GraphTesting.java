public class GraphTesting {
    public static void main(String[] args) {
        DBaccess db = DBaccessProvider.getInstance();
        if (db == null) {
            System.err.println("DBaccessProvider returned null.");
            return;
        }

        db.connect(); // ✅ Ensure the DB is connected before using

        GraphBuilder builder = new GraphBuilder(db, 0.5); // 0.5 km walking radius
        LatLon start = new LatLon(41.9010, 12.5018); // Roma Termini
        LatLon end = new LatLon(41.8902, 12.4922);   // Colosseo
        String time = "08:30:00";

        Graph graph = builder.buildGraph(start, end, time);
        System.out.println("Graph built with " + graph.nodes.size() + " nodes.");
        System.out.println(graph);
        System.out.println();
    }
}
