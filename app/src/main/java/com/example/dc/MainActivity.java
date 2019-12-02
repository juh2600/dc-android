package com.example.dc;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Stack;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private EditText stdin;
    private TextView stdout, stderr;

    private Stack<BigDecimal> mainStack;
    private Calculator calc;

    private static class Radices {
        public static BigDecimal input = BigDecimal.TEN;
        public static BigDecimal output = BigDecimal.TEN;
    }

    private Pattern digitPattern = Pattern.compile("^([0-9A-F])");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stdin = findViewById(R.id.etInput);
        stdout = findViewById(R.id.tvOutput);
        stderr = stdout;
        mainStack = new Stack<>();
        calc = new Calculator();
        //https://stackoverflow.com/a/4889059/6627273
        TextView.OnEditorActionListener enterListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean actionIdMatches = (
                           actionId == EditorInfo.IME_NULL
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_GO
                        || actionId == EditorInfo.IME_ACTION_NEXT
                        || actionId == EditorInfo.IME_ACTION_SEND
                        || actionId == EditorInfo.IME_ACTION_SEARCH
                        );
                boolean actionMatches = (
                        event.getAction() == KeyEvent.ACTION_UP
                );
                boolean keyCodeMatches = (
                           event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        || event.getKeyCode() == KeyEvent.KEYCODE_FORWARD
                        );
                if (actionIdMatches && actionMatches && keyCodeMatches) {
                    onEnter();
                }
                return true;
            }
        };
        stdin.setOnEditorActionListener(enterListener);

        cout("Ready to comply.");
    }

    private TextView cout(String s) {
        stdout.append('\n' + s);
        return stdout;
    }

    private TextView cerr(String s) {
        stderr.append('\n' + s);
        return stderr;
    }

    private boolean nextCharIsDigit(final ArrayList<Character> input) {
        return digitPattern.matcher("" + input.get(0)).matches(); // "" +, the Ugly String Cast
    }

    private BigDecimal charToBigDecimal(final Character c) {
        try {
            return new BigDecimal(Integer.parseInt("" + c));
        } catch (NumberFormatException e) {
            BigDecimal next;
            switch (c) {
                case 'A':
                    next = new BigDecimal(10);
                    break;
                case 'B':
                    next = new BigDecimal(11);
                    break;
                case 'C':
                    next = new BigDecimal(12);
                    break;
                case 'D':
                    next = new BigDecimal(13);
                    break;
                case 'E':
                    next = new BigDecimal(14);
                    break;
                case 'F':
                    next = new BigDecimal(15);
                    break;
                default:
                    throw e;
            }
            return next;
        }
    }

    private void validateStackDepth(int depth, Stack<BigDecimal> stack) {
        if (stack.size() < depth) throw new EmptyStackException();
    }

    private void validateStackDepth(int depth) {
        validateStackDepth(depth, mainStack);
    }

    /**
     * This function runs whenever a line ends.
     * TODO add better description of what functionality this should implement
     */
    public void onEnter() {
        String inputText = stdin.getText().toString();
        stdin.setText("");
        cout(inputText);
        // ignore empty input
        if (inputText.isEmpty()) return;

        ArrayList<Character> input = new ArrayList<>();
        for (Character c : inputText.toCharArray())
            input.add(c);


        while (!input.isEmpty()) {
            boolean readingNumber = false;
            boolean negative = false;
            BigDecimal number = BigDecimal.ZERO;
            if (input.get(0).equals('_')) {
                readingNumber = true;
                negative = true;
                input.remove(0);
            }
            while (!input.isEmpty() && nextCharIsDigit(input)) {
                readingNumber = true;
                try {
                    BigDecimal nextDigit = charToBigDecimal(input.get(0));
                    number = number.multiply(Radices.input);
                    number = number.add(nextDigit);
                    input.remove(0);
                } catch (NumberFormatException e) {
                    cerr("dc: failed to parse digit: " + input.get(0));
                    input.remove(0);
                    break;
                } catch (Exception e) {
                    cerr("dc: failed to parse digit: " + input.get(0));
                    cerr(e.toString());
                    input.remove(0);
                    break;
                }
            }
            if (!input.isEmpty() && input.get(0).equals('.')) {
                readingNumber = true;
                input.remove(0);
                BigDecimal placeValue = BigDecimal.ONE;
                while (!input.isEmpty() && nextCharIsDigit(input)) {
                    try {
                        BigDecimal nextDigit = charToBigDecimal(input.get(0));
                        placeValue = placeValue.divide(Radices.input);
                        number = number.add(nextDigit.multiply(placeValue));
                        input.remove(0);
                    } catch (NumberFormatException e) {
                        cerr("dc: failed to parse digit: " + input.get(0));
                        input.remove(0);
                        break;
                    } catch (Exception e) {
                        cerr("dc: failed to parse digit: " + input.get(0));
                        cerr(e.toString());
                        input.remove(0);
                        break;
                    }
                }
            }

            if (negative) {
                number = number.negate();
            }

            if (readingNumber) {
                mainStack.push(number);
                continue;
            }
            // if we get this far, we're not reading a number right now. process a command
            Character cmd = input.get(0);
            input.remove(0);
            try {
                switch (cmd) {
                    case ' ':
                    case '\t':
                    case '\n':
                        break;
                    case '#':
                        return; // rest of line is comment; don't bother parsing it
                    case '?':
                        // TODO add docs
                        break;
                    case '+':
                        validateStackDepth(2);
                        mainStack.push(calc.add(mainStack.pop(), mainStack.pop()));
                        break;
                    case '-':
                        validateStackDepth(2);
                        mainStack.push(calc.subtract(mainStack.pop(), mainStack.pop()));
                        break;
                    case '*':
                        validateStackDepth(2);
                        mainStack.push(calc.multiply(mainStack.pop(), mainStack.pop()));
                        break;
                    case '/':
                        validateStackDepth(2);
                        try {
                            mainStack.push(calc.divide(mainStack.pop(), mainStack.pop()));
                        } catch (ArithmeticException e) {
                            cerr("dc: BigDecimal: non-terminating decimal expansion in division :( giving up on life, goodbye numbers");
                            break;
                        }
                        break;
                    case '%':
                        validateStackDepth(2);
                        mainStack.push(calc.mod(mainStack.pop(), mainStack.pop()));
                        break;
                    case '^':
                        validateStackDepth(2);
                        if (mainStack.peek().scale() != 0) {
                            cerr("dc: runtime warning: non-zero scale in exponent");
                        }
                        mainStack.push(calc.pow(mainStack.pop(), mainStack.pop()));
                        break;
                    case 'k':
                        validateStackDepth(1);
                        calc.setScale((mainStack.pop().intValue()));
                        break;
                    case 'K':
                        mainStack.push(new BigDecimal(calc.getScale()));
                        break;
                    case 'z':
                        mainStack.push(new BigDecimal(mainStack.size()));
                        break;
                    case 'Z':
                        validateStackDepth(1);
                        mainStack.push(new BigDecimal(mainStack.pop().precision()));
                        break;
                    case 'X':
                        validateStackDepth(1);
                        mainStack.push(new BigDecimal(mainStack.pop().scale()));
                        break;
                    case 'd':
                        validateStackDepth(1);
                        mainStack.push(mainStack.peek());
                        break;
                    case 'r':
                        validateStackDepth(2);
                        BigDecimal a = mainStack.pop(), b = mainStack.pop();
                        mainStack.push(a);
                        mainStack.push(b);
                        break;
                    case 'i':
                        validateStackDepth(1);
                        Radices.input = new BigDecimal(mainStack.pop().toBigInteger());
                        break;
                    case 'I':
                        mainStack.push(Radices.input);
                        break;
                    case 'o':
                        validateStackDepth(1);
                        Radices.output = new BigDecimal(mainStack.pop().toBigInteger());
                        // FIXME make use of output radix
                        break;
                    case 'O':
                        mainStack.push(Radices.output);
                        break;
                    case 'c':
                        mainStack.clear();
                        break;
                    case 'f':
                        for (int i = mainStack.size(); i-- > 0; ) {
                            cout(mainStack.get(i).toString());
                        }
                        break;
                    default:
                        cerr("dc: unknown command: " + cmd);
                }

            } catch (EmptyStackException e) {
                cerr("dc: stack empty");
            }

        }
    }
}
