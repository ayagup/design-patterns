package behavioral;

import java.util.*;

/**
 * Command Pattern
 * Encapsulates a request as an object.
 */
public class CommandPattern {
    
    // Command interface
    interface Command {
        void execute();
        void undo();
    }
    
    // Receiver
    static class Light {
        private boolean isOn = false;
        
        public void turnOn() {
            isOn = true;
            System.out.println("💡 Light is ON");
        }
        
        public void turnOff() {
            isOn = false;
            System.out.println("💡 Light is OFF");
        }
    }
    
    // Concrete Commands
    static class LightOnCommand implements Command {
        private Light light;
        
        public LightOnCommand(Light light) {
            this.light = light;
        }
        
        @Override
        public void execute() {
            light.turnOn();
        }
        
        @Override
        public void undo() {
            light.turnOff();
        }
    }
    
    static class LightOffCommand implements Command {
        private Light light;
        
        public LightOffCommand(Light light) {
            this.light = light;
        }
        
        @Override
        public void execute() {
            light.turnOff();
        }
        
        @Override
        public void undo() {
            light.turnOn();
        }
    }
    
    // Text editor example
    static class TextEditor {
        private StringBuilder text = new StringBuilder();
        
        public void write(String text) {
            this.text.append(text);
            System.out.println("Text: " + this.text);
        }
        
        public void delete(int length) {
            int start = text.length() - length;
            if (start >= 0) {
                text.delete(start, text.length());
                System.out.println("Text: " + text);
            }
        }
        
        public String getText() {
            return text.toString();
        }
    }
    
    static class WriteCommand implements Command {
        private TextEditor editor;
        private String text;
        
        public WriteCommand(TextEditor editor, String text) {
            this.editor = editor;
            this.text = text;
        }
        
        @Override
        public void execute() {
            editor.write(text);
        }
        
        @Override
        public void undo() {
            editor.delete(text.length());
        }
    }
    
    // Invoker with undo support
    static class RemoteControl {
        private Command command;
        private Stack<Command> commandHistory = new Stack<>();
        
        public void setCommand(Command command) {
            this.command = command;
        }
        
        public void pressButton() {
            command.execute();
            commandHistory.push(command);
        }
        
        public void pressUndo() {
            if (!commandHistory.isEmpty()) {
                Command lastCommand = commandHistory.pop();
                lastCommand.undo();
                System.out.println("⏪ Undo executed");
            } else {
                System.out.println("Nothing to undo");
            }
        }
    }
    
    // Macro Command
    static class MacroCommand implements Command {
        private List<Command> commands;
        
        public MacroCommand(List<Command> commands) {
            this.commands = commands;
        }
        
        @Override
        public void execute() {
            System.out.println("Executing macro command...");
            for (Command command : commands) {
                command.execute();
            }
        }
        
        @Override
        public void undo() {
            System.out.println("Undoing macro command...");
            for (int i = commands.size() - 1; i >= 0; i--) {
                commands.get(i).undo();
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Command Pattern Demo ===\n");
        
        // Light control example
        System.out.println("1. Light Control with Undo:");
        Light light = new Light();
        Command lightOn = new LightOnCommand(light);
        Command lightOff = new LightOffCommand(light);
        
        RemoteControl remote = new RemoteControl();
        
        remote.setCommand(lightOn);
        remote.pressButton();
        
        remote.setCommand(lightOff);
        remote.pressButton();
        
        System.out.println("\nUndo operations:");
        remote.pressUndo();
        remote.pressUndo();
        remote.pressUndo();
        
        // Text editor example
        System.out.println("\n\n2. Text Editor with Undo:");
        TextEditor editor = new TextEditor();
        RemoteControl editorControl = new RemoteControl();
        
        editorControl.setCommand(new WriteCommand(editor, "Hello "));
        editorControl.pressButton();
        
        editorControl.setCommand(new WriteCommand(editor, "World"));
        editorControl.pressButton();
        
        editorControl.setCommand(new WriteCommand(editor, "!"));
        editorControl.pressButton();
        
        System.out.println("\nUndo last change:");
        editorControl.pressUndo();
        
        System.out.println("\nUndo again:");
        editorControl.pressUndo();
        
        // Macro command
        System.out.println("\n\n3. Macro Command:");
        Light light1 = new Light();
        Light light2 = new Light();
        Light light3 = new Light();
        
        List<Command> commands = Arrays.asList(
            new LightOnCommand(light1),
            new LightOnCommand(light2),
            new LightOnCommand(light3)
        );
        
        MacroCommand partyMode = new MacroCommand(commands);
        RemoteControl macroRemote = new RemoteControl();
        
        macroRemote.setCommand(partyMode);
        System.out.println("Activating party mode:");
        macroRemote.pressButton();
        
        System.out.println("\nDeactivating party mode:");
        macroRemote.pressUndo();
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Decouples invoker from receiver");
        System.out.println("✓ Supports undo/redo operations");
        System.out.println("✓ Commands can be queued and scheduled");
        System.out.println("✓ Supports macro commands");
        System.out.println("✓ Follows Single Responsibility Principle");
    }
}
