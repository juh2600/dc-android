package com.example.dc;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Stack;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private EditText stdin;
    private TextView stdout, stderr;

    private Stack<BigDecimal> mainStack;
    private ArrayList<Stack<BigDecimal>> namedStacks;
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
        namedStacks = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            namedStacks.add(new Stack<BigDecimal>());
        }
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
                        event == null
                                || event.getAction() == KeyEvent.ACTION_UP
                );
                boolean keyCodeMatches = (
                        event == null
                                || event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                                || event.getKeyCode() == KeyEvent.KEYCODE_FORWARD
                );
                if (actionIdMatches && actionMatches && keyCodeMatches) {
                    onEnter();
                }
                return true;
            }
        };
        stdin.setOnEditorActionListener(enterListener);

        stdout.setMovementMethod(new ScrollingMovementMethod());

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

    private BigDecimal warnIfNotInteger(BigDecimal input, String desc) {
        if (input.scale() != 0) {
            cerr("dc: runtime warning: non-zero scale" + (desc.isEmpty() ? "" : " in " + desc));
        }
        return input;
    }

    private int roundOffToInt(BigDecimal input, String desc) {
        return warnIfNotInteger(input, desc).intValue();
    }

    private int roundOffToInt(BigDecimal input) {
        return roundOffToInt(input, "");
    }

    private BigDecimal roundOff(BigDecimal input, String desc) {
        return warnIfNotInteger(input, desc).setScale(0, RoundingMode.DOWN);
    }

    private BigDecimal roundOff(BigDecimal input) {
        return roundOff(input, "");
    }

    private Stack<BigDecimal> getNamedStack(Character name) {
        return namedStacks.get((int)name);
    }

    /**
     * This function runs whenever a line ends.
     * TODO add better description of what functionality this should implement
     */
    public void onEnter() {
        try {
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
                            cout("+,-,*,/,% : Add, Subtract, Multiply, Divide, Mod");
                            cout("c: Clear the stack");
                            cout("f: Print all contents of the stack");
                            cout("^: a^b ");
                            cout("v: Square root");
                            cout("|: Current input base");
                            cout("k: Set the scale factor");
                            cout("K: Push the scale factor");
                            cout("z: Push the number of items");
                            cout("Z: Push the length of the top item");
                            cout("X: Replace top number with its scale factor");
                            cout("d: Duplicate the top of the stack");
                            cout("r: Swap the top 2 values");
                            cout("i: Change input base");
                            cout("I: Push current input base");
                            cout("o: Change output base");
                            cout("O: Push current output base");
                            cout("s"); //TODO: Add desc of s
                            cout("S"); // TODO: Add desc of S
                            cout("l"); // TODO: Add desc of l
                            cout("L"); //TODO: Add desc of L
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
                            mainStack.push(calc.pow(roundOffToInt(mainStack.pop(), "exponent"), mainStack.pop()));
                            break;
                        case '|':
                            validateStackDepth(3);
                            mainStack.push(calc.modexp(warnIfNotInteger(mainStack.pop(), "modulus"), roundOffToInt(mainStack.pop(), "exponent"), mainStack.pop()));
                            break;
                        case 'v':
                            validateStackDepth(1);
                            cerr("dc: v (square root): unimplemented"); // TODO
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
                            cerr("dc: runtime warning: output radix is unused; all output is decimal");
                            // TODO make use of output radix
                            break;
                        case 'O':
                            mainStack.push(Radices.output);
                            break;
                        case 's':
                            validateStackDepth(1);
                            Character sName;
                            if (input.isEmpty()) sName = '\n';
                            else {
                                sName = input.get(0);
                                input.remove(0);
                            }
                            if (namedStacks.size() < (int) sName)
                                throw new Exception("dc: register name out of bounds: " + sName);
                            if (namedStacks.get((int) sName).size() == 0)
                                namedStacks.get((int) sName).push(mainStack.pop());
                            else
                                namedStacks.get((int) sName).set(0, mainStack.pop());
                            break;
                        case 'S':
                            validateStackDepth(1);
                            Character SName;
                            if (input.isEmpty()) SName = '\n';
                            else {
                                SName = input.get(0);
                                input.remove(0);
                            }
                            if (namedStacks.size() < (int) SName)
                                throw new Exception("dc: register name out of bounds: " + SName);
                            namedStacks.get((int) SName).push(mainStack.pop());
                            break;
                        case 'l':
                            Character lName;
                            if (input.isEmpty()) lName = '\n';
                            else {
                                lName = input.get(0);
                                input.remove(0);
                            }
                            if (namedStacks.size() < (int) lName)
                            throw new Exception("dc: register name out of bounds: " + lName);
                            if(getNamedStack(lName).isEmpty())
                                mainStack.push(BigDecimal.ZERO);
                            else mainStack.push(getNamedStack(lName).peek());
                            break;
                        case 'L':
                            Character LName;
                            if (input.isEmpty()) LName = '\n';
                            else {
                                LName = input.get(0);
                                input.remove(0);
                            }
                            if (namedStacks.size() < (int) LName)
                                throw new Exception("dc: register name out of bounds: " + LName);
                            try {
                                validateStackDepth(1, getNamedStack(LName));
                                mainStack.push(getNamedStack(LName).pop());
                            } catch(EmptyStackException e) {
                                cerr("dc: stack register '"+LName+"' (0"+Integer.toOctalString((int)LName)+") is empty");
                            }
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
                            cerr("dc: '" + cmd + "' (0" + Integer.toOctalString((int) cmd) + ") unimplemented");
                    }

                } catch (EmptyStackException e) {
                    cerr("dc: stack empty");
                }

            }
        } catch (Exception e) {
            cerr(e.toString()); // we are invincible
        }
    }
}
