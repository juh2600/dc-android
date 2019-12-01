package com.example.dc;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private EditText stdin;
    private TextView stdout, stderr;

    private Stack<Character> mainStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stdin = findViewById(R.id.etInput);
        stdout = findViewById(R.id.tvOutput);
        stderr = stdout;
        mainStack = new Stack<>();
        // https://stackoverflow.com/a/3256305/6627273
        stdout.setMovementMethod(new ScrollingMovementMethod());
        // https://stackoverflow.com/a/4889059/6627273
        TextView.OnEditorActionListener enterListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    onEnter();
                }
                return true;
            }
        };
        stdin.setOnEditorActionListener(enterListener);
    }

    // https://stackoverflow.com/a/8423728/6627273
    class EmptyStackException extends Exception {
        EmptyStackException() {}
        EmptyStackException(String msg) { super(msg); }
    }

    /**
     * This function runs whenever a line ends.
     * TODO add better description of what functionality this should implement
     */
    public void onEnter() {
        String input = stdin.getText().toString();
        stdin.setText("");
        stdout.append("\n" + input);
        // ignore empty input
        if (input.equals("")) return;

        // elementary parsing of tokens
        for (Character c : input.toCharArray()) {
            switch (c) {
                case ' ':
                case '\t':
                case '\n':
                    break;
                case '?':
                    // TODO add docs
                    break;
                case 'c':
                    mainStack.clear();
                    break;
                case 'f':
                    for(int i = mainStack.size(); i-->0;) {
                        stdout.append("\n" + mainStack.get(i));
                    }
                    break;
                default:
                    mainStack.push(c);
            }
        }
    }
}
