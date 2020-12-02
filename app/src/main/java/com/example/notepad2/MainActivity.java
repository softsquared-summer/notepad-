package com.example.notepad2;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    private int note_count; // 이것도 저장해야할 정보임
    private ArrayList<Note> mNoteList = new ArrayList<>();
    private MainAdapter mMainAdapter;
    private ListView mLvNoteList;
    private SharedPreferences mSharedPref;
    private EditText mSearchNote;
    private FloatingActionButton mFabAddNote;
    private ImageButton mBtnSearch, mBtnDelete;
    private Gson gson;
    private AlertDialog.Builder alBuilder;
    private boolean isKeyboardChecked = false;
    private int REQUEST_TEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set sharedPreferences
        mSharedPref = getApplicationContext().getSharedPreferences("TAG", MODE_PRIVATE);
        gson = new Gson();
        String json = mSharedPref.getString("noteList", "");
        Type type = new TypeToken<ArrayList<Note>>() {
        }.getType();
        if (gson.fromJson(json, type) != null) {
            mNoteList = gson.fromJson(json, type);
        } else {
            mNoteList = new ArrayList<>();
        }
        note_count = mNoteList.size();

        //view binding
        mLvNoteList = findViewById(R.id.listView);
        mBtnDelete = findViewById(R.id.multiple_delete);
        mSearchNote = findViewById(R.id.search_note);
        mFabAddNote = findViewById(R.id.addButton);
        mBtnSearch = findViewById(R.id.search);


        mMainAdapter = new MainAdapter(mNoteList, this);//bind ArrayList in a ArrayList
        mLvNoteList.setAdapter(mMainAdapter);//Attaching the Adapter to a ListView
        registerForContextMenu(mLvNoteList);//Using contextMenu with listView

        //popup
        alBuilder = new AlertDialog.Builder(this);
        alBuilder.setMessage(R.string.isDeleteText);
        alBuilder.setCancelable(false);
        alBuilder.setPositiveButton(R.string.popup_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                SparseBooleanArray checkedItems = mLvNoteList.getCheckedItemPositions();
                int count = mMainAdapter.getCount();

                for (int i = count - 1; i >= 0; i--) {
                    if (checkedItems.get(i)) {
                        item_remove(i);
                    }
                }
//              hideAllDelete();//체크박스 숨기고 focus out, 체크 해제
//              mSearchNote.setText(""); //텍스트 지우고
//              checkSelectDelete=false;
                mSearchNote.setText(""); //텍스트 지우고
                mLvNoteList.clearChoices();//선택 목록 초기화

                mMainAdapter.notifyDataSetChanged();
            }
        });
        alBuilder.setNegativeButton(R.string.popup_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//              mSearchNote.setText("");
//              hideAllDelete();
//              checkSelectDelete=false
                mMainAdapter.notifyDataSetChanged();
            }
        });

        alBuilder.setTitle(getString(R.string.popup_delete));


        mSearchNote.requestFocus();
        mBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (!isKeyboardChecked) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

                    isKeyboardChecked = true;
                } else {
                    mSearchNote.clearFocus();
                    InputMethodManager immhide = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    immhide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    isKeyboardChecked = false;
                }

            }
        });//키보드 자판 위로 띄우고 닫기

        mFabAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                intent.putExtra("add_check", true);
                startActivityForResult(intent, REQUEST_TEST);//여러개의 액티비티를 두번째 인자로 구분(숫자)
//                if (checkSelectDelete) {
//                    hideAllDelete();
//                    mSearchNote.setText(""); //텍스트 지우고
//                    checkSelectDelete=false;
//
//                }
                mSearchNote.setText(""); //텍스트 지우고
                mLvNoteList.clearChoices();//선택 목록 초기화
            }
        });


        mSearchNote.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable edit) {

                String filterText = edit.toString();
                ((MainAdapter) mLvNoteList.getAdapter()).getFilter().filter(filterText);

                mLvNoteList.clearChoices();
                mMainAdapter.notifyDataSetChanged();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                mMainAdapter.notifyDataSetChanged();
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                mMainAdapter.notifyDataSetChanged();
            }
        });
        mBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (checkSelectDelete) {
//                    alBuilder.show();
//                } else {
//                }

                alBuilder.show();
            }
        });
//        mBtnDelete.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//
//                if (checkSelectDelete) {
//                    //longClick을 했을때
//                    hideAllDelete();
//                    checkSelectDelete=false;
//                } else {
//                    //longClick종료시
//                    showAllDelete();
//                    checkSelectDelete=true;
//                }
//                //리스트뷰 보여지고 가리기
//                return true;
//            }
//        });
    }


//    public void showAllDelete() {
//
//        mLvNoteList.clearChoices();//선택초기화
//        for (int i = 0; i < mMainAdapter.getFilteredItemList().size(); i++) {
//            CheckBox cb = (CheckBox) (mLvNoteList.getChildAt(i).findViewById(R.id.checkBox));
//            cb.setVisibility(View.VISIBLE);
//        }
//
//
//    }
//
//    public void hideAllDelete() {
//        mLvNoteList.clearChoices();//선택초기화
//
//        for (int i = 0; i < mMainAdapter.getFilteredItemList().size(); i++) {
//            CheckBox cb = (CheckBox) (mLvNoteList.getChildAt(i).findViewById(R.id.checkBox));
//            cb.setVisibility(View.INVISIBLE);
//        }
//
//        mSearchNote.clearFocus();
//    }


    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo;
        int index;
        switch (item.getItemId()) {
            //수정하기
            case R.id.item_update:
                menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                index = menuInfo.position;
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                intent.putExtra("update_check", true);
                intent.putExtra("item_position", mMainAdapter.getFilteredItemList().get(index).getItem_index());
                intent.putExtra("main_date", mMainAdapter.getFilteredItemList().get(index).getDate());
                intent.putExtra("main_contents", mMainAdapter.getFilteredItemList().get(index).getContents());
                startActivityForResult(intent, REQUEST_TEST + 1);//여러개의 액티비티를 두번째 인자로 구분(숫자)
                return true;
            //지우기
            case R.id.item_delete:

                menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                index = menuInfo.position;
                item_remove(index);
                mMainAdapter.notifyDataSetChanged();
                return true;
            case R.id.item_cancel:
                return true;
        }
        return super.onContextItemSelected(item);
    }
    public void item_remove(int index) {
        int temp = mMainAdapter.getFilteredItemList().get(index).getItem_index();
        mMainAdapter.getFilteredItemList().remove(index);//지워진다고 실제로 adapter내에 리스트가 지워지는지는 잘 모르겠음
        ((MainAdapter) mLvNoteList.getAdapter()).getFilter().filter(mSearchNote.getText());//필터된 것 지우고 바로 보여주기
        //2개씩 지워지는 이상현상은
        //Filter링 안되었을때 필터된것과
        if (mMainAdapter.getFilteredItemList() != mNoteList) {
            mNoteList.remove(temp);//실제로 지우는 것
        }//두 리스트가 다를 경우만 noteArrayList에도 지우는 것

        for (int i = temp; i < mNoteList.size(); i++) {
            int n = mNoteList.get(i).getItem_index();
            n--;
            String contents = mNoteList.get(i).getContents();
            String date = mNoteList.get(i).getDate();
            mNoteList.set(i, new Note(contents, date, n));
        }
        note_count--;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TEST) {
            if (resultCode == RESULT_OK) {

                mNoteList.add(new Note(data.getStringExtra(Intent.EXTRA_TEXT), data.getStringExtra("date"), note_count++));//리스트 뷰에 추가후 카운트 증가
                ((MainAdapter) mLvNoteList.getAdapter()).getFilter().filter(mSearchNote.getText());//필터링 갱신 바로 보여주기
                mMainAdapter.notifyDataSetChanged();
                //이런 식으로 제목과 date값을 받아온다. 그리고 list에 추가한다.

            } else {   // RESULT_CANCEL
            }
        } else if (requestCode == REQUEST_TEST + 1) {
            if (resultCode == RESULT_OK) {
                mNoteList.set(data.getIntExtra("item_position", 0), new Note(data.getStringExtra(Intent.EXTRA_TEXT), data.getStringExtra("date"), data.getIntExtra("item_position", 0)));//리스트 뷰에 업데이트
                ((MainAdapter) mLvNoteList.getAdapter()).getFilter().filter(mSearchNote.getText());//필터링 갱신 바로 보여주기
                mMainAdapter.notifyDataSetChanged();
            } else {   // RESULT_CANCEL
            }
        } else {
        }
    }//add 버튼을 누르고 새 액티비티에서 result 값을 받아오는 부분이다.

    @Override
    protected void onStart() {
        super.onStart();
        //실행중 인텐트를 받을 수 있기 때문에
        //add버튼으로 활성화된 액티비티가 아닌 액티비티에서 받을때 데이터를 저장하는 부분이다.
        if (getIntent().getBooleanExtra("from_external_check", false)) {
            mNoteList.add(new Note(getIntent().getStringExtra(Intent.EXTRA_TEXT), getIntent().getStringExtra("date"), note_count++));//리스트 뷰에 추가후 카운트 증가
            mMainAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences.Editor editor = mSharedPref.edit();
        String json = gson.toJson(mNoteList);
        editor.putString("noteList", json);
        editor.apply();
    }
}
