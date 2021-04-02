package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.byted.camp.todolist.beans.Note;
import com.byted.camp.todolist.beans.Priority;
import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.db.TodoDbHelper;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static com.byted.camp.todolist.beans.Priority.*;


public class NoteChangeActivity extends AppCompatActivity {
    private EditText editText;
    private Button addBtn;
    private RadioGroup radioGroup;
    private AppCompatRadioButton lowRadio;

    private TodoDbHelper dbHelper;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        setTitle(R.string.change_your_note);

        dbHelper = new TodoDbHelper(this);
        database = dbHelper.getWritableDatabase();
        //      TODO  长按修改TODOLIST内容
        // 获取需要修改的note_id,并按照预设好原有内容和优先级。
        final Long note_id = getIntent().getExtras().getLong("key");
        final Note note = loadNoteFromDatabase(note_id);

        editText = findViewById(R.id.edit_text);
        editText.setText(note.getContent());
        editText.setFocusable(true);
        editText.requestFocus();


        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.showSoftInput(editText, 0);
        }
        radioGroup = findViewById(R.id.radio_group);
        setSelectedPriority(note);
        addBtn = findViewById(R.id.btn_add);



        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence content = editText.getText();

                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(NoteChangeActivity.this,
                            "No content to add", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean succeed = updateNode2Database(content.toString().trim(),getSelectedPriority(),note.getState(),note_id);
                if (succeed) {
                    Toast.makeText(NoteChangeActivity.this,
                            "Note changed", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                } else {
                    Toast.makeText(NoteChangeActivity.this,
                            "Error", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
        database = null;
        dbHelper.close();
        dbHelper = null;
    }

// 更新note信息到数据库，时间更新
    private boolean updateNode2Database(String content, Priority priority, State state, Long note_id) {
        if (database == null|| TextUtils.isEmpty(content)) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(TodoContract.TodoNote.COLUMN_CONTENT, content);
        values.put(TodoContract.TodoNote.COLUMN_STATE, state.intValue);
        values.put(TodoContract.TodoNote.COLUMN_DATE, System.currentTimeMillis());
        values.put(TodoContract.TodoNote.COLUMN_PRIORITY, priority.intValue);
        int rows = database.update(TodoContract.TodoNote.TABLE_NAME, values,
                TodoContract.TodoNote._ID + "=?", new String[]{String.valueOf(note_id)});
        return true;
    }
    private Priority getSelectedPriority() {
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.btn_high:
                return High;
            case R.id.btn_medium:
                return Medium;
            default:
                return Low;
        }
    }

    private void setSelectedPriority(Note note) {
        switch (note.getPriority()) {
            case High:
                radioGroup.check(R.id.btn_high);
                break;
            case Medium:
                radioGroup.check(R.id.btn_medium);
                break;
            default:
                radioGroup.check(R.id.btn_low);
        }
    }


    private Note loadNoteFromDatabase(Long note_id) {
        Note note = new Note(note_id);
        if (database == null) {
            return note;
        }
        List<Note> result = new LinkedList<>();
        Cursor cursor = null;
        try {
            cursor = database.query(TodoContract.TodoNote.TABLE_NAME, null,
                    "_id=" + String.valueOf(note_id), null,
                    null, null,
                    TodoContract.TodoNote.COLUMN_PRIORITY + " DESC");

            cursor.moveToNext();
            long id = cursor.getLong(cursor.getColumnIndex(TodoContract.TodoNote._ID));
            String content = cursor.getString(cursor.getColumnIndex(TodoContract.TodoNote.COLUMN_CONTENT));
            long dateMs = cursor.getLong(cursor.getColumnIndex(TodoContract.TodoNote.COLUMN_DATE));
            int intState = cursor.getInt(cursor.getColumnIndex(TodoContract.TodoNote.COLUMN_STATE));
            int intPriority = cursor.getInt(cursor.getColumnIndex(TodoContract.TodoNote.COLUMN_PRIORITY));

            note.setContent(content);
            note.setDate(new Date(dateMs));
            note.setState(State.from(intState));
            note.setPriority(from(intPriority));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return note;
    }
}
