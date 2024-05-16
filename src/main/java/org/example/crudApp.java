package org.example;

import controllers.ConnectionPoolController;

import static spark.Spark.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;

public class crudApp {
    private static User user;

    public static void main(String[] args) {
        // Initialize the Spark application
        port(4567); // Set the port if not using the default (4567)

        // Route to get all users
        get("/users", (req, res) -> {
            // Set the response type to JSON
            res.type("application/json");
            // Call the method to get all users and convert the list to JSON
            return new Gson().toJson(getAllUsers());
        });

        // Route to get specific user
        get("/users/:id", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            return new Gson().toJson(getUser(id));
        });

        // Route to insert a user
        post("/users", (req, res) -> {
            User user = new Gson().fromJson(req.body(), User.class);
            int newId = 0;

            if (user == null) {
                res.status(400);
                return ("No user supplied");
            } else {
                newId = insertUser(user);
            }

            if (newId == 0) {
                res.status(400);
                return ("User not created");
            } else {
                res.status(200);
                String newUserURL = "/users/" + newId;
                res.header("Location", newUserURL);
                return "User created";
            }
        });

        // Route to update a user
        put("/users/:id", (req, res) -> {
            int userID = Integer.parseInt(req.params(":id"));
            User user = getUser(userID);

            User updatedUser;
            if (user == null) {
                res.status(404);
                return ("User Not Found");
            } else {
                String body = req.body();
                Gson gson = new Gson();
                updatedUser = gson.fromJson(body, User.class);
            }

            int rc = updateUser(updatedUser);
            if (rc != 0) {
                res.status(200);
                String userURL = "/users/" + userID;
                res.header("Location", userURL);
                return "Updated User";
            } else {
                res.status(404);
                return "User Not Found";
            }
        });

        // Initialize the delete route
        delete("/delete/:id", ((req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            if(deleteUser(id) == 0) {
                res.status(404);
                return "Not found";
            } else {
                res.status(200);
                return "OK";
            }
        }));
    }

    // Retrieve all users from the database
    private static List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection connection = ConnectionPoolController.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                // Assuming the User class has an appropriate constructor
                User user = new User(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("email"),
                        resultSet.getTimestamp("created_at")
                );
                userList.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }

    // Get user by ID
    private static User getUser(int id) {
        User user = null;
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection connection = ConnectionPoolController.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    user = new User(
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getString("email"),
                            resultSet.getTimestamp("created_at")
                    );
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    // Insert a user
    private static int insertUser(User user) {
        int recordsInserted = 0;
        String sql = "INSERT INTO users (name, email, created_at) VALUES (?, ?, ?)";
        int newId = 0;

        try (Connection connection = ConnectionPoolController.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setTimestamp(3, user.getCreatedAt());
            recordsInserted = statement.executeUpdate();

            if (recordsInserted > 0) {
                try(ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        newId = generatedKeys.getInt(1);
                    }
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return newId;
    }

    // Update user by ID
    private static int updateUser(User user) {
        // TODO: Possibly fix to where it actually updates by ID instead of passing it an entire user
        String sql = "UPDATE users SET name = ?, email = ?, created_at = ? WHERE id = ?";
        int recordsUpdated = 0;

        try (Connection connection = ConnectionPoolController.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setTimestamp(3, user.getCreatedAt());
            statement.setInt(4, user.getId());
            recordsUpdated = statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return recordsUpdated;
    }

    // Delete user with specified ID
    private static int deleteUser(int id) {
        int recordsDeleted = 0;
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection connection = ConnectionPoolController.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            recordsDeleted = statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return recordsDeleted;
    }
}

// Example User class
class User {
    private int id;
    private String name;
    private String email;
    private java.sql.Timestamp createdAt;

    public User(int id, String name, String email, java.sql.Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}