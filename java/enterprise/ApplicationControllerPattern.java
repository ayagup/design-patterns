package enterprise;

import java.util.*;
import java.util.function.BiFunction;

/**
 * Application Controller Pattern
 * 
 * Intent: Centralized point for handling screen navigation and
 * application flow logic. Separates flow control from presentation
 * and business logic.
 * 
 * Motivation:
 * Complex navigation rules should not be in presentation layer.
 * Handles "what screen comes next" decisions.
 * Reuses navigation logic across different UIs.
 * 
 * Applicability:
 * - Complex navigation flows (wizards, workflows)
 * - Multiple UI platforms sharing flow logic
 * - Dynamic screen transitions
 * - Business process orchestration
 */

/**
 * Example 1: Simple Application Controller
 * 
 * Manages screen flow based on actions
 */
class Screen {
    private final String name;
    private final Map<String, Object> data;
    
    public Screen(String name) {
        this.name = name;
        this.data = new HashMap<>();
    }
    
    public String getName() { return name; }
    
    public void setData(String key, Object value) {
        data.put(key, value);
    }
    
    public Object getData(String key) {
        return data.get(key);
    }
    
    public Map<String, Object> getAllData() {
        return new HashMap<>(data);
    }
}

class SimpleApplicationController {
    private final Map<String, Map<String, String>> flowMap; // screen -> action -> nextScreen
    
    public SimpleApplicationController() {
        this.flowMap = new HashMap<>();
        initializeFlows();
    }
    
    private void initializeFlows() {
        // Login flow
        Map<String, String> loginFlow = new HashMap<>();
        loginFlow.put("success", "dashboard");
        loginFlow.put("failure", "login");
        loginFlow.put("register", "registration");
        flowMap.put("login", loginFlow);
        
        // Dashboard flow
        Map<String, String> dashboardFlow = new HashMap<>();
        dashboardFlow.put("profile", "userProfile");
        dashboardFlow.put("settings", "settings");
        dashboardFlow.put("logout", "login");
        flowMap.put("dashboard", dashboardFlow);
        
        // Registration flow
        Map<String, String> registrationFlow = new HashMap<>();
        registrationFlow.put("submit", "login");
        registrationFlow.put("cancel", "login");
        flowMap.put("registration", registrationFlow);
        
        System.out.println("  [AppController] Flow map initialized");
    }
    
    public Screen navigate(String currentScreen, String action) {
        System.out.println("  [AppController] Navigate from '" + currentScreen + 
                         "' via action '" + action + "'");
        
        Map<String, String> actions = flowMap.get(currentScreen);
        if (actions != null) {
            String nextScreen = actions.get(action);
            if (nextScreen != null) {
                System.out.println("  [AppController] Next screen: " + nextScreen);
                return new Screen(nextScreen);
            }
        }
        
        System.out.println("  [AppController] No flow defined, staying on: " + currentScreen);
        return new Screen(currentScreen);
    }
}

/**
 * Example 2: Command-Based Application Controller
 * 
 * Uses Command pattern for dynamic flow logic
 */
interface NavigationCommand {
    Screen execute(Map<String, Object> context);
}

class LoginNavigationCommand implements NavigationCommand {
    @Override
    public Screen execute(Map<String, Object> context) {
        boolean authenticated = (boolean) context.getOrDefault("authenticated", false);
        
        if (authenticated) {
            System.out.println("    [NavCommand] Login successful -> dashboard");
            Screen screen = new Screen("dashboard");
            screen.setData("user", context.get("username"));
            return screen;
        } else {
            System.out.println("    [NavCommand] Login failed -> login");
            Screen screen = new Screen("login");
            screen.setData("error", "Invalid credentials");
            return screen;
        }
    }
}

class CheckoutNavigationCommand implements NavigationCommand {
    @Override
    public Screen execute(Map<String, Object> context) {
        int step = (int) context.getOrDefault("step", 1);
        
        String nextScreen = switch (step) {
            case 1 -> {
                System.out.println("    [NavCommand] Checkout step 1 -> shipping");
                yield "checkout/shipping";
            }
            case 2 -> {
                System.out.println("    [NavCommand] Checkout step 2 -> payment");
                yield "checkout/payment";
            }
            case 3 -> {
                System.out.println("    [NavCommand] Checkout step 3 -> confirmation");
                yield "checkout/confirmation";
            }
            default -> {
                System.out.println("    [NavCommand] Invalid step -> cart");
                yield "cart";
            }
        };
        
        return new Screen(nextScreen);
    }
}

class CommandBasedApplicationController {
    private final Map<String, NavigationCommand> commands;
    
    public CommandBasedApplicationController() {
        this.commands = new HashMap<>();
        registerCommands();
    }
    
    private void registerCommands() {
        commands.put("login", new LoginNavigationCommand());
        commands.put("checkout", new CheckoutNavigationCommand());
        System.out.println("  [AppController] Navigation commands registered");
    }
    
    public Screen navigate(String commandName, Map<String, Object> context) {
        System.out.println("  [AppController] Executing command: " + commandName);
        
        NavigationCommand command = commands.get(commandName);
        if (command != null) {
            return command.execute(context);
        }
        
        System.out.println("  [AppController] Command not found");
        return new Screen("error");
    }
}

/**
 * Example 3: State-Based Application Controller
 * 
 * Navigation based on application state
 */
enum UserState {
    ANONYMOUS, AUTHENTICATED, PREMIUM, SUSPENDED
}

class StateBasedApplicationController {
    private final Map<UserState, Set<String>> allowedScreens;
    private final Map<String, BiFunction<UserState, Map<String, Object>, Screen>> handlers;
    
    public StateBasedApplicationController() {
        this.allowedScreens = new HashMap<>();
        this.handlers = new HashMap<>();
        initializeStateRules();
    }
    
    private void initializeStateRules() {
        // Define allowed screens per state
        allowedScreens.put(UserState.ANONYMOUS, 
            Set.of("home", "login", "register"));
        allowedScreens.put(UserState.AUTHENTICATED, 
            Set.of("home", "dashboard", "profile", "settings"));
        allowedScreens.put(UserState.PREMIUM, 
            Set.of("home", "dashboard", "profile", "settings", "premium"));
        allowedScreens.put(UserState.SUSPENDED, 
            Set.of("home", "suspended"));
        
        // Define navigation handlers
        handlers.put("dashboard", (state, context) -> {
            if (state == UserState.ANONYMOUS) {
                System.out.println("    [StateNav] Anonymous user -> redirect to login");
                return new Screen("login");
            } else if (state == UserState.SUSPENDED) {
                System.out.println("    [StateNav] Suspended user -> suspended page");
                return new Screen("suspended");
            } else {
                System.out.println("    [StateNav] Authorized -> dashboard");
                return new Screen("dashboard");
            }
        });
        
        handlers.put("premium", (state, context) -> {
            if (state == UserState.PREMIUM) {
                System.out.println("    [StateNav] Premium user -> premium features");
                return new Screen("premium");
            } else {
                System.out.println("    [StateNav] Not premium -> upgrade page");
                return new Screen("upgrade");
            }
        });
        
        System.out.println("  [AppController] State rules initialized");
    }
    
    public Screen navigate(String targetScreen, UserState currentState, Map<String, Object> context) {
        System.out.println("  [AppController] Navigate to '" + targetScreen + 
                         "' with state: " + currentState);
        
        // Check if handler exists
        BiFunction<UserState, Map<String, Object>, Screen> handler = handlers.get(targetScreen);
        if (handler != null) {
            return handler.apply(currentState, context);
        }
        
        // Default behavior: check allowed screens
        Set<String> allowed = allowedScreens.get(currentState);
        if (allowed != null && allowed.contains(targetScreen)) {
            System.out.println("  [AppController] Access granted -> " + targetScreen);
            return new Screen(targetScreen);
        }
        
        System.out.println("  [AppController] Access denied -> home");
        return new Screen("home");
    }
}

/**
 * Example 4: Wizard Flow Controller
 * 
 * Multi-step wizard with validation and branching
 */
class WizardStep {
    private final String name;
    private final Map<String, Object> data;
    private boolean completed;
    
    public WizardStep(String name) {
        this.name = name;
        this.data = new HashMap<>();
        this.completed = false;
    }
    
    public String getName() { return name; }
    public void setData(String key, Object value) { data.put(key, value); }
    public Object getData(String key) { return data.get(key); }
    public void markCompleted() { completed = true; }
    public boolean isCompleted() { return completed; }
}

class WizardFlowController {
    private final List<String> steps;
    private int currentStepIndex;
    private final Map<String, WizardStep> stepData;
    
    public WizardFlowController(List<String> steps) {
        this.steps = steps;
        this.currentStepIndex = 0;
        this.stepData = new HashMap<>();
        
        for (String step : steps) {
            stepData.put(step, new WizardStep(step));
        }
        
        System.out.println("  [WizardController] Initialized with " + steps.size() + " steps");
    }
    
    public Screen next(Map<String, Object> data) {
        if (currentStepIndex >= steps.size()) {
            System.out.println("  [WizardController] Wizard complete");
            return new Screen("completion");
        }
        
        String currentStep = steps.get(currentStepIndex);
        WizardStep step = stepData.get(currentStep);
        
        // Save data from current step
        data.forEach(step::setData);
        step.markCompleted();
        
        // Move to next step
        currentStepIndex++;
        
        if (currentStepIndex < steps.size()) {
            String nextStep = steps.get(currentStepIndex);
            System.out.println("  [WizardController] Step " + (currentStepIndex + 1) + 
                             "/" + steps.size() + ": " + nextStep);
            return new Screen(nextStep);
        } else {
            System.out.println("  [WizardController] Final step complete");
            return new Screen("completion");
        }
    }
    
    public Screen previous() {
        if (currentStepIndex > 0) {
            currentStepIndex--;
            String prevStep = steps.get(currentStepIndex);
            System.out.println("  [WizardController] Back to step: " + prevStep);
            return new Screen(prevStep);
        }
        
        System.out.println("  [WizardController] Already at first step");
        return new Screen(steps.get(0));
    }
    
    public int getCurrentStepIndex() {
        return currentStepIndex;
    }
}

/**
 * Demonstration of the Application Controller Pattern
 */
public class ApplicationControllerPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Application Controller Pattern Demo ===\n");
        
        // Example 1: Simple Application Controller
        System.out.println("1. Simple Flow-Based Controller:");
        SimpleApplicationController simpleAC = new SimpleApplicationController();
        
        simpleAC.navigate("login", "success");
        simpleAC.navigate("login", "register");
        simpleAC.navigate("dashboard", "profile");
        simpleAC.navigate("dashboard", "logout");
        
        // Example 2: Command-Based Controller
        System.out.println("\n2. Command-Based Controller:");
        CommandBasedApplicationController commandAC = new CommandBasedApplicationController();
        
        Map<String, Object> loginContext = new HashMap<>();
        loginContext.put("authenticated", true);
        loginContext.put("username", "alice");
        commandAC.navigate("login", loginContext);
        
        Map<String, Object> checkoutContext = new HashMap<>();
        checkoutContext.put("step", 1);
        commandAC.navigate("checkout", checkoutContext);
        checkoutContext.put("step", 2);
        commandAC.navigate("checkout", checkoutContext);
        
        // Example 3: State-Based Controller
        System.out.println("\n3. State-Based Controller:");
        StateBasedApplicationController stateAC = new StateBasedApplicationController();
        
        stateAC.navigate("dashboard", UserState.ANONYMOUS, new HashMap<>());
        stateAC.navigate("dashboard", UserState.AUTHENTICATED, new HashMap<>());
        stateAC.navigate("premium", UserState.AUTHENTICATED, new HashMap<>());
        stateAC.navigate("premium", UserState.PREMIUM, new HashMap<>());
        
        // Example 4: Wizard Flow Controller
        System.out.println("\n4. Wizard Flow Controller:");
        WizardFlowController wizard = new WizardFlowController(
            Arrays.asList("account", "personal", "preferences", "review")
        );
        
        wizard.next(Map.of("email", "user@example.com"));
        wizard.next(Map.of("name", "John Doe"));
        wizard.previous();
        wizard.next(Map.of("name", "John Smith"));
        wizard.next(Map.of("theme", "dark"));
        wizard.next(Map.of("confirmed", true));
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Centralizes navigation logic");
        System.out.println("✓ Separates flow from presentation");
        System.out.println("✓ Reusable across different UIs");
        System.out.println("✓ Easier to modify workflows");
        System.out.println("✓ Handles complex multi-step processes");
        
        System.out.println("\n=== Use Cases ===");
        System.out.println("• Wizard-style interfaces");
        System.out.println("• Shopping cart checkout flows");
        System.out.println("• Multi-page forms");
        System.out.println("• Business process workflows");
    }
}
