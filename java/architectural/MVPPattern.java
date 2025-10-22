package architectural;

import java.util.*;

/**
 * MVP Pattern (Model-View-Presenter)
 * ===================================
 * 
 * Intent:
 * Separates presentation logic from business logic by introducing a Presenter
 * that handles all UI logic and acts as the middle-man between Model and View.
 * The View is passive and delegates all user actions to the Presenter.
 * 
 * Also Known As:
 * - Passive View
 * - Supervising Controller (variant)
 * 
 * Motivation:
 * - Improve testability by making View passive
 * - Decouple View from Model completely
 * - Make UI logic testable without UI framework
 * - Enable View to be easily replaceable
 * 
 * Applicability:
 * - Desktop applications with complex UI logic
 * - Android applications (was recommended pattern before MVVM)
 * - When you need to test UI logic without UI framework
 * - When View technology changes frequently
 * 
 * Structure:
 * View (interface) <- ConcreteView implements
 * Presenter -> View (interface)
 * Presenter -> Model
 * 
 * Participants:
 * - Model: Business logic and data
 * - View: Passive UI that delegates to Presenter
 * - Presenter: Contains UI logic, updates View
 * 
 * Key Differences from MVC:
 * 1. View is completely passive (no logic)
 * 2. Presenter talks to View through interface
 * 3. One-to-one Presenter-View relationship
 * 4. Presenter updates View explicitly
 */

// Example 1: Login System with MVP
// Model - contains business logic
class LoginModel {
    private Map<String, String> users = new HashMap<>();
    
    public LoginModel() {
        // Simulate user database
        users.put("admin", "admin123");
        users.put("user", "pass123");
    }
    
    public boolean authenticate(String username, String password) {
        String storedPassword = users.get(username);
        return storedPassword != null && storedPassword.equals(password);
    }
    
    public boolean userExists(String username) {
        return users.containsKey(username);
    }
}

// View interface - completely passive
interface LoginView {
    String getUsername();
    String getPassword();
    void showSuccess(String message);
    void showError(String message);
    void showLoading(boolean loading);
    void clearForm();
}

// Presenter - contains all UI logic
class LoginPresenter {
    private final LoginView view;
    private final LoginModel model;
    
    public LoginPresenter(LoginView view, LoginModel model) {
        this.view = view;
        this.model = model;
    }
    
    public void handleLogin() {
        String username = view.getUsername();
        String password = view.getPassword();
        
        // Validation logic in Presenter
        if (username == null || username.trim().isEmpty()) {
            view.showError("Username cannot be empty");
            return;
        }
        
        if (password == null || password.isEmpty()) {
            view.showError("Password cannot be empty");
            return;
        }
        
        view.showLoading(true);
        
        // Business logic delegation to Model
        if (model.authenticate(username, password)) {
            view.showLoading(false);
            view.showSuccess("Welcome, " + username + "!");
            view.clearForm();
        } else {
            view.showLoading(false);
            if (model.userExists(username)) {
                view.showError("Invalid password");
            } else {
                view.showError("User not found");
            }
        }
    }
}

// Concrete View implementation
class ConsoleLoginView implements LoginView {
    private String username;
    private String password;
    private LoginPresenter presenter;
    
    public void setPresenter(LoginPresenter presenter) {
        this.presenter = presenter;
    }
    
    public void simulateUserInput(String username, String password) {
        this.username = username;
        this.password = password;
        presenter.handleLogin();
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public void showSuccess(String message) {
        System.out.println("✓ SUCCESS: " + message);
    }
    
    @Override
    public void showError(String message) {
        System.out.println("✗ ERROR: " + message);
    }
    
    @Override
    public void showLoading(boolean loading) {
        if (loading) {
            System.out.println("[*] Loading...");
        }
    }
    
    @Override
    public void clearForm() {
        this.username = null;
        this.password = null;
        System.out.println("Form cleared");
    }
}

// Example 2: Task List Application
// Model
class TaskListModel {
    private List<Task> tasks = new ArrayList<>();
    private int nextId = 1;
    
    public void addTask(String title, String description) {
        tasks.add(new Task(nextId++, title, description, false));
    }
    
    public void completeTask(int taskId) {
        tasks.stream()
             .filter(t -> t.getId() == taskId)
             .findFirst()
             .ifPresent(t -> t.setCompleted(true));
    }
    
    public void deleteTask(int taskId) {
        tasks.removeIf(t -> t.getId() == taskId);
    }
    
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }
    
    public List<Task> getPendingTasks() {
        List<Task> pending = new ArrayList<>();
        for (Task task : tasks) {
            if (!task.isCompleted()) {
                pending.add(task);
            }
        }
        return pending;
    }
    
    static class Task {
        private int id;
        private String title;
        private String description;
        private boolean completed;
        
        public Task(int id, String title, String description, boolean completed) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.completed = completed;
        }
        
        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
    }
}

// View interface
interface TaskListView {
    void displayTasks(List<TaskListModel.Task> tasks);
    void displayTaskAdded(String message);
    void displayTaskCompleted(int taskId);
    void displayTaskDeleted(int taskId);
    void displayError(String error);
}

// Presenter
class TaskListPresenter {
    private final TaskListView view;
    private final TaskListModel model;
    
    public TaskListPresenter(TaskListView view, TaskListModel model) {
        this.view = view;
        this.model = model;
    }
    
    public void loadTasks() {
        view.displayTasks(model.getAllTasks());
    }
    
    public void loadPendingTasks() {
        view.displayTasks(model.getPendingTasks());
    }
    
    public void addTask(String title, String description) {
        if (title == null || title.trim().isEmpty()) {
            view.displayError("Task title cannot be empty");
            return;
        }
        
        model.addTask(title, description);
        view.displayTaskAdded("Task added: " + title);
        loadTasks(); // Refresh view
    }
    
    public void completeTask(int taskId) {
        model.completeTask(taskId);
        view.displayTaskCompleted(taskId);
        loadTasks(); // Refresh view
    }
    
    public void deleteTask(int taskId) {
        model.deleteTask(taskId);
        view.displayTaskDeleted(taskId);
        loadTasks(); // Refresh view
    }
}

// Concrete View
class ConsoleTaskListView implements TaskListView {
    @Override
    public void displayTasks(List<TaskListModel.Task> tasks) {
        System.out.println("\n=== Task List ===");
        if (tasks.isEmpty()) {
            System.out.println("No tasks");
        } else {
            for (TaskListModel.Task task : tasks) {
                String status = task.isCompleted() ? "[✓]" : "[ ]";
                System.out.println(status + " #" + task.getId() + ": " + task.getTitle() + 
                                 " - " + task.getDescription());
            }
        }
    }
    
    @Override
    public void displayTaskAdded(String message) {
        System.out.println("✓ " + message);
    }
    
    @Override
    public void displayTaskCompleted(int taskId) {
        System.out.println("✓ Task #" + taskId + " completed");
    }
    
    @Override
    public void displayTaskDeleted(int taskId) {
        System.out.println("✓ Task #" + taskId + " deleted");
    }
    
    @Override
    public void displayError(String error) {
        System.out.println("✗ ERROR: " + error);
    }
}

// Example 3: User Profile Editor
// Model
class UserProfileModel {
    private String name;
    private String email;
    private int age;
    
    public UserProfileModel(String name, String email, int age) {
        this.name = name;
        this.email = email;
        this.age = age;
    }
    
    public void updateProfile(String name, String email, int age) {
        this.name = name;
        this.email = email;
        this.age = age;
    }
    
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getAge() { return age; }
}

// View interface
interface UserProfileView {
    void showProfile(String name, String email, int age);
    void showValidationError(String field, String message);
    void showSaveSuccess();
}

// Presenter
class UserProfilePresenter {
    private final UserProfileView view;
    private final UserProfileModel model;
    
    public UserProfilePresenter(UserProfileView view, UserProfileModel model) {
        this.view = view;
        this.model = model;
    }
    
    public void loadProfile() {
        view.showProfile(model.getName(), model.getEmail(), model.getAge());
    }
    
    public void saveProfile(String name, String email, int age) {
        // Validation logic in Presenter
        if (name == null || name.trim().isEmpty()) {
            view.showValidationError("name", "Name cannot be empty");
            return;
        }
        
        if (email == null || !email.contains("@")) {
            view.showValidationError("email", "Invalid email address");
            return;
        }
        
        if (age < 0 || age > 150) {
            view.showValidationError("age", "Age must be between 0 and 150");
            return;
        }
        
        model.updateProfile(name, email, age);
        view.showSaveSuccess();
        loadProfile(); // Refresh view
    }
}

// Concrete View
class ConsoleUserProfileView implements UserProfileView {
    @Override
    public void showProfile(String name, String email, int age) {
        System.out.println("\n=== User Profile ===");
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("Age: " + age);
    }
    
    @Override
    public void showValidationError(String field, String message) {
        System.out.println("✗ Validation Error [" + field + "]: " + message);
    }
    
    @Override
    public void showSaveSuccess() {
        System.out.println("✓ Profile saved successfully!");
    }
}

/**
 * Demonstration of MVP Pattern
 */
public class MVPPattern {
    public static void main(String[] args) {
        demonstrateLogin();
        demonstrateTaskList();
        demonstrateUserProfile();
    }
    
    private static void demonstrateLogin() {
        System.out.println("=== MVP: Login System ===\n");
        
        LoginModel model = new LoginModel();
        ConsoleLoginView view = new ConsoleLoginView();
        LoginPresenter presenter = new LoginPresenter(view, model);
        view.setPresenter(presenter);
        
        // Test various scenarios
        System.out.println("Test 1: Valid login");
        view.simulateUserInput("admin", "admin123");
        
        System.out.println("\nTest 2: Invalid password");
        view.simulateUserInput("admin", "wrongpass");
        
        System.out.println("\nTest 3: Non-existent user");
        view.simulateUserInput("hacker", "hack123");
        
        System.out.println("\nTest 4: Empty username");
        view.simulateUserInput("", "pass");
        
        System.out.println("\nTest 5: Empty password");
        view.simulateUserInput("user", "");
    }
    
    private static void demonstrateTaskList() {
        System.out.println("\n\n=== MVP: Task List Application ===\n");
        
        TaskListModel model = new TaskListModel();
        ConsoleTaskListView view = new ConsoleTaskListView();
        TaskListPresenter presenter = new TaskListPresenter(view, model);
        
        // Add tasks
        presenter.addTask("Buy groceries", "Milk, bread, eggs");
        presenter.addTask("Write report", "Q4 financial report");
        presenter.addTask("Call dentist", "Schedule appointment");
        
        // Show all tasks
        presenter.loadTasks();
        
        // Complete a task
        presenter.completeTask(2);
        
        // Show pending tasks
        System.out.println("\n--- Pending Tasks Only ---");
        presenter.loadPendingTasks();
        
        // Delete a task
        presenter.deleteTask(1);
        presenter.loadTasks();
        
        // Try to add empty task
        presenter.addTask("", "This should fail");
    }
    
    private static void demonstrateUserProfile() {
        System.out.println("\n\n=== MVP: User Profile Editor ===\n");
        
        UserProfileModel model = new UserProfileModel("John Doe", "john@example.com", 30);
        ConsoleUserProfileView view = new ConsoleUserProfileView();
        UserProfilePresenter presenter = new UserProfilePresenter(view, model);
        
        // Load profile
        presenter.loadProfile();
        
        // Update with valid data
        System.out.println("\nUpdating profile...");
        presenter.saveProfile("Jane Smith", "jane@example.com", 28);
        
        // Try invalid email
        System.out.println("\nTrying invalid email...");
        presenter.saveProfile("Jane Smith", "invalid-email", 28);
        
        // Try invalid age
        System.out.println("\nTrying invalid age...");
        presenter.saveProfile("Jane Smith", "jane@example.com", 200);
        
        // Try empty name
        System.out.println("\nTrying empty name...");
        presenter.saveProfile("", "jane@example.com", 28);
    }
}
