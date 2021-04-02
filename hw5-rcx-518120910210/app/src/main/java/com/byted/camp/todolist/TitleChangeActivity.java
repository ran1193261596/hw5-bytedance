package com.byted.camp.todolist;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;


public class TitleChangeActivity extends AppCompatActivity {

    private EditText editTextTitle;
    private Button confirmBtn;
    private static final int RESULT_CODE_CHANGETITLE = 2000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_change);
        setTitle(R.string.change_the_title);


        editTextTitle = findViewById(R.id.edit_text_title);
        editTextTitle.setFocusable(true);
        editTextTitle.requestFocus();
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.showSoftInput(editTextTitle, 0);
        }

        confirmBtn = findViewById(R.id.btn_confirm_title);

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence content = editTextTitle.getText();
                Intent intent = new Intent();

                intent.putExtra("title_after", content.toString().trim());

                setResult(RESULT_CODE_CHANGETITLE, intent);
                finish();
            }
        });
    }
}