
package storven;

import java.sql.*;


public class DbConnection {
    
    
    public static Connection ConnectionDB(){
        
        try{
            Class.forName("org.sqlite.JDBC");
            Connection con = DriverManager.getConnection("jdbc:sqlite:AccountsDB.db");
            System.out.println("Connection Succeeded");
            return con;
        }
        catch(Exception e){
            System.out.println("Connection Failed" + e);
            return null;
        }
    }
}
