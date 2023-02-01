package calculator;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.Border;
import java.util.Deque;

public class Calculator extends JFrame {
    private final String[] buttonNames = {
            "Parentheses", "CE", "Clear", "Delete",
            "PowerTwo", "PowerY", "SquareRoot", "Divide",
            "Seven", "Eight", "Nine", "Multiply",
            "Four", "Five", "Six", "Subtract",
            "One", "Two", "Three","Add",
            "PlusMinus", "Zero", "Dot", "Equals"};
    private final String[] buttonSigns = {
            "()", "CE", "C", "Del",
            "X²", "Xʸ", "√", "÷",
            "7", "8", "9", "×",
            "4", "5", "6", "-",
            "1", "2", "3", "+",
            "±", "0", ".", "="};
    Font font1 = new Font("Inter", Font.PLAIN, 15);
    public Calculator() {
        //super("Calculator");
        JFrame f = new JFrame("Calculator");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(300, 430);
        f.setLayout(null);
        f.setLocationRelativeTo(null);
        f.getContentPane().setBackground(new Color(40, 40, 40));
        initialize(f);
        f.setVisible(true);
    }

    private void initialize(JFrame f) {
        JLabel equation = new JLabel();
        equation.setName("EquationLabel");
        equation.setBounds(18, 50, 248, 30);
        equation.setHorizontalAlignment(SwingConstants.RIGHT);
        equation.setFont(font1);
        equation.setForeground(Color.lightGray);
        f.add(equation);

        JLabel result = new JLabel();
        result.setName("ResultLabel");
        result.setBounds(18, 15, 248, 30);
        result.setHorizontalAlignment(SwingConstants.RIGHT);
        result.setFont(new Font("Inter", Font.BOLD, 30));
        result.setForeground(Color.white);
        f.add(result);

        int k = 0;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                addButton(buttonNames[k], buttonSigns[k],
                        18+63*j, 91+i*47, equation, result, f);
                k++;
            }
        }
    }

    private void addButton(String name, String sign, int x, int y,
                           JLabel equation, JLabel result, JFrame f) {
        JButton button = new JButton();
        Border emptyBorder = BorderFactory.createEmptyBorder();
        button.setBorder(emptyBorder);
        button.setFocusable(false);
        button.setText(sign);
        button.setName(name);
        button.setBounds(x, y, 60, 45);
        button.setBackground(Color.BLACK);
        button.setFont(font1);
        String buttonText = button.getText();
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(20,20,20));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.black);
            }
        });
        switch (buttonText) {
            case "-", "+", "÷", "×", ".", "±", "()" -> button.setForeground(new Color(183, 251, 255));
            case "X²", "Xʸ", "√" -> button.setForeground(new Color(255, 186, 83));
            case "=" -> {
                button.setFont(new Font("Inter", Font.BOLD, 18));
                button.setForeground(new Color(177, 255, 157));
            }
            case "C", "CE", "Del" -> button.setForeground(new Color(255, 143, 143));
            default -> button.setForeground(Color.white);
        }

        f.add(button);

        Pattern p = Pattern.compile("[0-9]|[-+÷×.]");

        if (p.matcher(buttonText).find()) {
            button.addActionListener(e -> {
                boolean errorFound = handleErrors(equation, button);
                if (!errorFound) equation.setText(equation.getText() + buttonText);
                else System.out.println("Error found");
            });
        } else {
            button.addActionListener(e -> {
                boolean errorFound = handleErrors(equation, button);
                switch (buttonText) {
                    case "()" -> {
                        if (!errorFound) {
                            addParentheses(equation);
                        }
                    }
                    case "C" -> {
                        equation.setText("");
                        equation.setForeground(Color.white);
                        result.setText("");
                    }
                    case "Del", "CE" -> {
                        equation.setText(equation.getText().substring(0, equation.getText().length() - 1));
                        equation.setForeground(Color.white);
                    }
                    case "X²" -> // X squared
                            equation.setText(equation.getText() + "^(2)");
                    case "Xʸ" -> // X power Y
                            equation.setText(equation.getText() + "^(");
                    case "√" -> // square root
                            equation.setText(equation.getText() + "√(");
                    case "±" -> { // plus minus
                        String str = equation.getText();
                        if (str.endsWith("(-"))
                            equation.setText(str.substring(0, str.length() - 2));
                        else equation.setText(str + "(-");
                        handleNegation(equation);
                    }
                    case "=" -> {
                        if (!errorFound) {
                            String answer = solve(equation);
                            result.setText(answer);
                            equation.setForeground(Color.white);
                        }
                    }
                }
            });
        }
    }

    private void handleNegation(JLabel equation) {
        String text = equation.getText();
        Matcher m1 = Pattern.compile("\\d+\\(-").matcher(text);

        if (m1.find()) { // "3(-" -> "(-3"
            equation.setText(text.substring(0, m1.start()) + "(-" +
                    text.substring(m1.start(), text.length() - 2));
        }
        text = equation.getText();
        Matcher m2 = Pattern.compile("\\(-\\(-\\d+").matcher(text);
        if (m2.find()) { // "(-(-" -> ""
            equation.setText(text.substring(0, m2.start()) + text.substring(m2.start() + 4));
        }
    }
    private boolean handleErrors(JLabel field, JButton button) {
        String equation = field.getText() + button.getText();
        boolean isEqual = button.getName().equals("Equals");
        boolean isDot = button.getName().equals("Dot");

        Pattern p = Pattern.compile("[-+÷×]");
        Pattern p2 = Pattern.compile("÷0[-+÷×=]"); // division by 0
        Matcher m = p.matcher(equation); // first operator is operation sign
        if (equation.length() == 1 && m.matches()) {
            if (isDot) field.setText("0.");
            else field.setText("");
            return true;
        }
        if (equation.length() >= 2) {
            String previous = equation.substring(equation.length() - 2, equation.length() - 1);
            String current = equation.substring(equation.length() - 1);
            Matcher m2 = p.matcher(previous); //previous char is operator
            Matcher m3 = p.matcher(current); //current char is operator
            int leftParentheses = countParentheses(equation, "left");
            int rightParentheses = countParentheses(equation, "right");

            if (m3.matches() && equation.charAt(0) == '.') { // ".6+" -> "0.6+"
                field.setText("0" + equation);
                return true;
            } else if (m3.matches() && previous.equals(".")) { // "6.+" -> "6.0+"
                field.setText(equation.substring(0, m3.end()) + ".0" + button.getText());
                return true;
            } else if (isDot && m2.matches()) { // "3+." -> "3+0."
                field.setText(equation.substring(0, equation.length() - 1) + "0.");
                return true;
            } else if (isDot && previous.equals(".")) { // ".." -> "."
                return true;
            } else if (m3.matches() && m2.matches()) { // "3+-" -> "3-"
                field.setText(equation.substring(0, m2.end()) + button.getText());
                return true;
            } else if (isEqual && m2.matches()) { // "3+2+=" -> error
                field.setForeground(Color.RED.darker());
                System.out.println(1);
                return true;
            } else if (isEqual && p2.matcher(equation).find()) { // division by 0
                field.setForeground(Color.RED.darker());
                return true;
            } else if (isEqual && leftParentheses != rightParentheses) {
                do {
                    field.setText(equation.substring(0, equation.length() - 1) + ")");
                    rightParentheses = countParentheses(field.getText(), "right");
                } while (leftParentheses != rightParentheses);
            }
        }
        return false;
    }

    private void addParentheses(JLabel field) {
        String equation = field.getText();
        if (equation.length() > 0)  {
            String last = equation.substring(equation.length()-1);
            int leftCount = countParentheses(equation, "left");
            int rightCount = countParentheses(equation, "right");
            Pattern p = Pattern.compile("[-+÷×(]");
            if (leftCount == rightCount || p.matcher(last).matches()) {
                field.setText(equation + "(");
            } else field.setText(equation + ")");
        } else field.setText("(");
    }

    private int countParentheses(String equation, String parentheses) {
        int left = (int) equation.chars().filter(ch -> ch == '(').count();
        int right = (int) equation.chars().filter(ch -> ch == ')').count();
        return parentheses.equalsIgnoreCase("left") ? left : right;
    }

    private String solve(JLabel textField) {
        StringBuilder equation = new StringBuilder(textField.getText());
        ArrayList<String> arr;
        Pattern parentheses = Pattern.compile("\\(([^()]*)\\)");
        Matcher m1 = parentheses.matcher(equation.toString());
        try {
            while (m1.find(0)) {
                arr = toPostfix(m1.group().substring(1, m1.group().length()-1));
                StringBuilder b = new StringBuilder();
                b.append(calculate(arr));
                if (b.charAt(0) == '-') {
                    b.delete(0,1);
                    b.append("-");
                }
                equation.replace(m1.start(), m1.end(), b.toString());
                if (equation.indexOf("--") > -1) {
                    equation.replace(equation.indexOf("--"), equation.indexOf("--") + 2, "+");
                }
                m1 = parentheses.matcher(equation.toString());
            }
            if (equation.indexOf("√") == equation.indexOf("-")- 1 && equation.indexOf("√") != -1) {
                throw new Exception("Negative square root");
            } else arr = toPostfix(String.valueOf(equation));
        } catch (Exception e) {
            textField.setForeground(Color.RED.darker());
            return "Invalid input";
        }
        return String.valueOf(calculate(arr));
    }

    private String calculate(ArrayList<String> equation) {
        Deque<Double> stack = new ArrayDeque<>();
        Deque<String> operations = new ArrayDeque<>();
        for(String a: equation) {
            try {
                stack.push(Double.parseDouble(a));
            } catch (NumberFormatException e) {
                double op1 = stack.pop();
                if (!operations.isEmpty() && a.equals("-")) {
                    String operation = operations.pop();
                    double op2 = stack.pop();
                    switch (operation) {
                        case "×" -> stack.push(changeSign(op1 * op2));
                        case "÷" -> stack.push(changeSign(op2 / op1));
                        case "^" -> stack.push(Math.pow(op2, -op1));
                    }
                } else if (stack.size() == 0 || a.equals("√")) {
                    switch (a) {
                        case "+"-> stack.push(op1);
                        case "-" -> stack.push(changeSign(op1));
                        case "×", "÷", "^"-> {
                            operations.push(a);
                            stack.push(op1);
                        }
                        case "√" -> stack.push(Math.sqrt(op1));
                    }
                } else {
                    double op2 = stack.pop();
                    switch (a) {
                        case "+" -> stack.push(op1 + op2);
                        case "-" -> stack.push(op2 - op1);
                        case "×" -> stack.push(op1 * op2);
                        case "÷" -> stack.push(op2 / op1);
                        case "^" -> stack.push(Math.pow(op2, op1));
                    }
                }
            }
        }
        double x = stack.pop();
        int y = (int) x;
        String a = String.valueOf(x);
        if (x - y == 0) a = a.substring(0,a.indexOf("."));
        return a;
    }
    private double changeSign(double num) {
        return num < 0 ? Math.abs(num) : -num;
    }

    private ArrayList<String> toPostfix(String equation) {
        Deque<Character> operations = new ArrayDeque<>();
        ArrayList<String> postfix = new ArrayList<>();
        StringBuilder num = new StringBuilder();
        for (char a: equation.toCharArray()) {
            if (Character.isDigit(a) || a == '.') {
                num.append(a);
            } else {
                if (!num.isEmpty()) {
                    postfix.add(num.toString());
                    num = new StringBuilder();
                }
                while (!operations.isEmpty() && estimateWeight(a) <= estimateWeight(operations.peekLast())) {
                    postfix.add(operations.pollLast().toString());
                }
                operations.add(a);
            }
        }
        if (!num.isEmpty()) {
            postfix.add(num.toString());
        }
        while (!operations.isEmpty()) {
            postfix.add(operations.pollLast().toString());
        }
        return postfix;
    }

    private int estimateWeight(char operator) {
        return switch (operator) {
            case '+', '-' -> 1;
            case '×', '÷' -> 2;
            case '√', '^' -> 3;
            default -> 0;
        };
    }
}
