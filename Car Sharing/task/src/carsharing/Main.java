package carsharing;

import java.sql.*;
import java.util.Scanner;

public class Main {
    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL = "jdbc:h2:./src/carsharing/db/carsharing";

    //  Database credentials
    static final String USER = "";
    static final String PASS = "";

    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        try {
            // STEP 1: Register JDBC driver
            Class.forName(JDBC_DRIVER);

            // STEP 2: Open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            conn.setAutoCommit(true);

            // STEP 3: Execute a query to create/modify the COMPANY table
            stmt = conn.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS COMPANY " +
                         "(ID INTEGER AUTO_INCREMENT PRIMARY KEY, " +
                         " NAME VARCHAR(255) UNIQUE NOT NULL)";
            stmt.executeUpdate(sql);

            // Create CAR table
            String sql2 = "CREATE TABLE IF NOT EXISTS CAR " +
                          "(ID INT AUTO_INCREMENT PRIMARY KEY, " +
                          " NAME VARCHAR(255) UNIQUE NOT NULL, " +
                          " COMPANY_ID INT NOT NULL, " +
                          " CONSTRAINT FK_COMPANYID FOREIGN KEY (COMPANY_ID) " +
                          " REFERENCES COMPANY(ID))";
            stmt.executeUpdate(sql2);

            String sql3 = "CREATE TABLE IF NOT EXISTS CUSTOMER " +
                          "(ID INT AUTO_INCREMENT PRIMARY KEY, " +
                          "NAME VARCHAR(255) UNIQUE NOT NULL, " +
                          "RENTED_CAR_ID INT, " +
                          "FOREIGN KEY (RENTED_CAR_ID) REFERENCES CAR(ID))";
            stmt.executeUpdate(sql3);

            // Show the main menu and keep the program running
            Main app = new Main();
            app.showMainMenu(conn);

        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException se2) { }
            try {
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        System.out.println("Goodbye!");
    }

    public void showMainMenu(Connection conn) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("1. Log in as a manager");
            System.out.println("2. Log in as a customer");
            System.out.println("3. Create a customer");
            System.out.println("0. Exit");

            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                switch (choice) {
                    case 1:
                        showManagerMenu(conn, scanner);
                        break;
                    case 2:
                        loginAsCustomer(conn, scanner);
                        break;
                    case 3:
                        createCustomer(conn, scanner);
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } else {
                System.out.println("Please enter a valid number.");
                scanner.next();
            }
        }
    }

    public void showManagerMenu(Connection conn, Scanner scanner) {
        while (true) {
            System.out.println("1. Company list\n" +
                               "2. Create a company\n" +
                               "0. Back");

            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume the leftover newline character

                switch (choice) {
                    case 1:
                        listCompanies(conn, scanner);
                        break;
                    case 2:
                        createCompany(conn, scanner);
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } else {
                System.out.println("Please enter a valid number.");
                scanner.next();
            }
        }
    }

    public void listCompanies(Connection conn, Scanner scanner) {
        try (Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            String sql = "SELECT * FROM COMPANY ORDER BY ID";
            ResultSet rs = stmt.executeQuery(sql);

            if (!rs.isBeforeFirst()) {
                System.out.println("The company list is empty!");
            } else {
                System.out.println("Choose a company:");
                int index = 1;
                while (rs.next()) {
                    System.out.println(index + ". " + rs.getString("NAME"));
                    index++;
                }
                System.out.println("0. Back");

                int companyChoice = scanner.nextInt();
                if (companyChoice > 0 && companyChoice < index) {
                    rs.absolute(companyChoice);
                    showCompanyMenu(conn, scanner, rs.getInt("ID"), rs.getString("NAME"));
                }
            }
            rs.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public void showCompanyMenu(Connection conn, Scanner scanner, int companyId, String companyName) {
        while (true) {
            System.out.println("'" + companyName + "' company:");
            System.out.println("1. Car list");
            System.out.println("2. Create a car");
            System.out.println("0. Back");

            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume the leftover newline character

                switch (choice) {
                    case 1:
                        listCars(conn, companyId);
                        break;
                    case 2:
                        createCar(conn, scanner, companyId);
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } else {
                System.out.println("Please enter a valid number.");
                scanner.next();
            }
        }
    }

    public void listCars(Connection conn, int companyId) {
        try (Statement stmt = conn.createStatement()) {
            String sql = "SELECT * FROM CAR WHERE COMPANY_ID = " + companyId + " ORDER BY ID";
            ResultSet rs = stmt.executeQuery(sql);

            if (!rs.isBeforeFirst()) {
                System.out.println("The car list is empty!");
            } else {
                System.out.println("Car list:");
                int index = 1;
                while (rs.next()) {
                    System.out.println(index + ". " + rs.getString("NAME"));
                    index++;
                }
            }
            rs.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public void createCar(Connection conn, Scanner scanner, int companyId) {
        System.out.print("Enter the car name: ");
        String carName = scanner.nextLine();

        try {
            Statement stmt = conn.createStatement();
            String sql = "INSERT INTO CAR (NAME, COMPANY_ID) VALUES ('" + carName + "', " + companyId + ")";
            stmt.executeUpdate(sql);
            System.out.println("Car created successfully!");
            stmt.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public void createCompany(Connection conn, Scanner scanner) {
        System.out.print("Enter the company name: ");
        String companyName = scanner.nextLine();

        try {
            Statement stmt = conn.createStatement();
            String sql = "INSERT INTO COMPANY (NAME) VALUES ('" + companyName + "')";
            stmt.executeUpdate(sql);
            System.out.println("Company created successfully!");
            stmt.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public void loginAsCustomer(Connection conn, Scanner scanner) {
        try {
            String sql = "SELECT * FROM CUSTOMER ORDER BY ID";
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery(sql);

            if (!rs.isBeforeFirst()) {
                System.out.println("The customer list is empty!");
                return;
            }

            System.out.println("Customer list:");
            int index = 1;
            while (rs.next()) {
                System.out.println(index + ". " + rs.getString("NAME"));
                index++;
            }
            System.out.println("0. Back");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (choice > 0 && choice < index) {
                rs.absolute(choice);
                int customerId = rs.getInt("ID");
                String customerName = rs.getString("NAME");
                showCustomerMenu(conn, scanner, customerId, customerName);
            }

            rs.close();
            stmt.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public void createCustomer(Connection conn, Scanner scanner) {
        System.out.print("Enter the customer name: ");
        String customerName = scanner.nextLine();

        try {
            String sql = "INSERT INTO CUSTOMER (NAME) VALUES (?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, customerName);
            pstmt.executeUpdate();
            System.out.println("The customer was added!");
            pstmt.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public void showCustomerMenu(Connection conn, Scanner scanner, int customerId, String customerName) {
        while (true) {
            System.out.println("1. Rent a car");
            System.out.println("2. Return a rented car");
            System.out.println("3. My rented car");
            System.out.println("0. Back");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    rentCar(conn, scanner, customerId);
                    break;
                case 2:
                    returnRentedCar(conn, customerId);
                    break;
                case 3:
                    showRentedCar(conn, customerId);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    public void rentCar(Connection conn, Scanner scanner, int customerId) {
        try {
            // Check if customer already rented a car
            String checkSql = "SELECT RENTED_CAR_ID FROM CUSTOMER WHERE ID = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, customerId);
            ResultSet checkRs = checkStmt.executeQuery();
            checkRs.next();
            if (checkRs.getObject("RENTED_CAR_ID") != null) {
                System.out.println("You've already rented a car!");
                return;
            }
            checkRs.close();
            checkStmt.close();

            // Show company list
            String companySql = "SELECT * FROM COMPANY ORDER BY ID";
            Statement companyStmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet companyRs = companyStmt.executeQuery(companySql);

            if (!companyRs.isBeforeFirst()) {
                System.out.println("The company list is empty!");
                return;
            }

            System.out.println("Choose a company:");
            int index = 1;
            while (companyRs.next()) {
                System.out.println(index + ". " + companyRs.getString("NAME"));
                index++;
            }
            System.out.println("0. Back");

            int companyChoice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (companyChoice > 0 && companyChoice < index) {
                companyRs.absolute(companyChoice);
                int companyId = companyRs.getInt("ID");
                String companyName = companyRs.getString("NAME");

                // Show available cars
                String carSql = "SELECT * FROM CAR WHERE COMPANY_ID = ? AND ID NOT IN (SELECT RENTED_CAR_ID FROM CUSTOMER WHERE RENTED_CAR_ID IS NOT NULL) ORDER BY ID";
                PreparedStatement carStmt = conn.prepareStatement(carSql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                carStmt.setInt(1, companyId);
                ResultSet carRs = carStmt.executeQuery();

                if (!carRs.isBeforeFirst()) {
                    System.out.println("No available cars in the '" + companyName + "' company");
                    return;
                }

                System.out.println("Choose a car:");
                index = 1;
                while (carRs.next()) {
                    System.out.println(index + ". " + carRs.getString("NAME"));
                    index++;
                }
                System.out.println("0. Back");

                int carChoice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                if (carChoice > 0 && carChoice < index) {
                    carRs.absolute(carChoice);
                    int carId = carRs.getInt("ID");
                    String carName = carRs.getString("NAME");

                    // Rent the car
                    String rentSql = "UPDATE CUSTOMER SET RENTED_CAR_ID = ? WHERE ID = ?";
                    PreparedStatement rentStmt = conn.prepareStatement(rentSql);
                    rentStmt.setInt(1, carId);
                    rentStmt.setInt(2, customerId);
                    rentStmt.executeUpdate();

                    System.out.println("You rented '" + carName + "'");
                    rentStmt.close();
                }

                carRs.close();
                carStmt.close();
            }

            companyRs.close();
            companyStmt.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public void returnRentedCar(Connection conn, int customerId) {
        try {
            String checkSql = "SELECT RENTED_CAR_ID FROM CUSTOMER WHERE ID = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, customerId);
            ResultSet checkRs = checkStmt.executeQuery();
            checkRs.next();
            if (checkRs.getObject("RENTED_CAR_ID") == null) {
                System.out.println("You didn't rent a car!");
                return;
            }
            checkRs.close();
            checkStmt.close();

            String returnSql = "UPDATE CUSTOMER SET RENTED_CAR_ID = NULL WHERE ID = ?";
            PreparedStatement returnStmt = conn.prepareStatement(returnSql);
            returnStmt.setInt(1, customerId);
            returnStmt.executeUpdate();
            System.out.println("You've returned a rented car!");
            returnStmt.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public void showRentedCar(Connection conn, int customerId) {
        try {
            String sql = "SELECT CAR.NAME AS CAR_NAME, COMPANY.NAME AS COMPANY_NAME " +
                         "FROM CUSTOMER " +
                         "JOIN CAR ON CUSTOMER.RENTED_CAR_ID = CAR.ID " +
                         "JOIN COMPANY ON CAR.COMPANY_ID = COMPANY.ID " +
                         "WHERE CUSTOMER.ID = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("Your rented car:");
                System.out.println(rs.getString("CAR_NAME"));
                System.out.println("Company:");
                System.out.println(rs.getString("COMPANY_NAME"));
            } else {
                System.out.println("You didn't rent a car!");
            }

            rs.close();
            pstmt.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }
}