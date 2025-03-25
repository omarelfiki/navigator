import java.sql.*;

public class GTFSaccess {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/postgres";
        String user = "postgres";
        String password = "6262";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("✅ Connected to PostgreSQL!");

            String query = "SELECT stop_name FROM stops WHERE stop_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, "5019");  // example stop_id
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String stopName = rs.getString("stop_name");
                    System.out.println("🛑 Stop Name: " + stopName);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}