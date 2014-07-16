package twitter_search.twitter_api_usage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Mandy Roick on 14.07.2014.
 */
public class DBManager {
    private Connection connection;

    public void connect() {
        try {
            connection = DriverManager.getConnection(
                            "jdbc:postgresql://isfet.hpi.uni-potsdam.de:5432/max?searchpath=mandy_masterarbeit",
                            "roick", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
