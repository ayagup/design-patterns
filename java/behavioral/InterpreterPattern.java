package behavioral;

import java.util.*;

/**
 * Interpreter Pattern
 * Defines a grammar and interpreter for a language.
 */
public class InterpreterPattern {
    
    // Abstract Expression
    interface Expression {
        int interpret();
    }
    
    // Terminal Expression - Number
    static class NumberExpression implements Expression {
        private int number;
        
        public NumberExpression(int number) {
            this.number = number;
        }
        
        @Override
        public int interpret() {
            return number;
        }
    }
    
    // Non-terminal Expression - Addition
    static class AddExpression implements Expression {
        private Expression left;
        private Expression right;
        
        public AddExpression(Expression left, Expression right) {
            this.left = left;
            this.right = right;
        }
        
        @Override
        public int interpret() {
            return left.interpret() + right.interpret();
        }
    }
    
    // Non-terminal Expression - Subtraction
    static class SubtractExpression implements Expression {
        private Expression left;
        private Expression right;
        
        public SubtractExpression(Expression left, Expression right) {
            this.left = left;
            this.right = right;
        }
        
        @Override
        public int interpret() {
            return left.interpret() - right.interpret();
        }
    }
    
    // Non-terminal Expression - Multiplication
    static class MultiplyExpression implements Expression {
        private Expression left;
        private Expression right;
        
        public MultiplyExpression(Expression left, Expression right) {
            this.left = left;
            this.right = right;
        }
        
        @Override
        public int interpret() {
            return left.interpret() * right.interpret();
        }
    }
    
    // Boolean expression interpreter
    interface BooleanExpression {
        boolean interpret(Map<String, Boolean> context);
    }
    
    static class VariableExpression implements BooleanExpression {
        private String name;
        
        public VariableExpression(String name) {
            this.name = name;
        }
        
        @Override
        public boolean interpret(Map<String, Boolean> context) {
            return context.getOrDefault(name, false);
        }
    }
    
    static class AndExpression implements BooleanExpression {
        private BooleanExpression left;
        private BooleanExpression right;
        
        public AndExpression(BooleanExpression left, BooleanExpression right) {
            this.left = left;
            this.right = right;
        }
        
        @Override
        public boolean interpret(Map<String, Boolean> context) {
            return left.interpret(context) && right.interpret(context);
        }
    }
    
    static class OrExpression implements BooleanExpression {
        private BooleanExpression left;
        private BooleanExpression right;
        
        public OrExpression(BooleanExpression left, BooleanExpression right) {
            this.left = left;
            this.right = right;
        }
        
        @Override
        public boolean interpret(Map<String, Boolean> context) {
            return left.interpret(context) || right.interpret(context);
        }
    }
    
    static class NotExpression implements BooleanExpression {
        private BooleanExpression expression;
        
        public NotExpression(BooleanExpression expression) {
            this.expression = expression;
        }
        
        @Override
        public boolean interpret(Map<String, Boolean> context) {
            return !expression.interpret(context);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Interpreter Pattern Demo ===\n");
        
        // Arithmetic expression: (5 + 10) * 2 - 3
        System.out.println("1. Arithmetic Expression Interpreter:");
        Expression expr1 = new SubtractExpression(
            new MultiplyExpression(
                new AddExpression(
                    new NumberExpression(5),
                    new NumberExpression(10)
                ),
                new NumberExpression(2)
            ),
            new NumberExpression(3)
        );
        
        System.out.println("Expression: (5 + 10) * 2 - 3");
        System.out.println("Result: " + expr1.interpret());
        
        // Another expression: 7 + 3 * 4
        Expression expr2 = new AddExpression(
            new NumberExpression(7),
            new MultiplyExpression(
                new NumberExpression(3),
                new NumberExpression(4)
            )
        );
        
        System.out.println("\nExpression: 7 + (3 * 4)");
        System.out.println("Result: " + expr2.interpret());
        
        // Boolean expression interpreter
        System.out.println("\n\n2. Boolean Expression Interpreter:");
        
        // Expression: (A AND B) OR (C AND NOT D)
        BooleanExpression boolExpr = new OrExpression(
            new AndExpression(
                new VariableExpression("A"),
                new VariableExpression("B")
            ),
            new AndExpression(
                new VariableExpression("C"),
                new NotExpression(new VariableExpression("D"))
            )
        );
        
        Map<String, Boolean> context1 = new HashMap<>();
        context1.put("A", true);
        context1.put("B", true);
        context1.put("C", false);
        context1.put("D", true);
        
        System.out.println("Expression: (A AND B) OR (C AND NOT D)");
        System.out.println("Context: A=true, B=true, C=false, D=true");
        System.out.println("Result: " + boolExpr.interpret(context1));
        
        Map<String, Boolean> context2 = new HashMap<>();
        context2.put("A", false);
        context2.put("B", true);
        context2.put("C", true);
        context2.put("D", true);
        
        System.out.println("\nContext: A=false, B=true, C=true, D=true");
        System.out.println("Result: " + boolExpr.interpret(context2));
        
        Map<String, Boolean> context3 = new HashMap<>();
        context3.put("A", false);
        context3.put("B", false);
        context3.put("C", true);
        context3.put("D", false);
        
        System.out.println("\nContext: A=false, B=false, C=true, D=false");
        System.out.println("Result: " + boolExpr.interpret(context3));
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Easy to change and extend grammar");
        System.out.println("✓ Implements grammar naturally");
        System.out.println("✓ Adding new expressions is straightforward");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• SQL parsers");
        System.out.println("• Expression evaluators");
        System.out.println("• Configuration language interpreters");
        System.out.println("• Rule engines");
    }
}
