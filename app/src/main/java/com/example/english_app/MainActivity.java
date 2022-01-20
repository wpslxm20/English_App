package com.example.english_app;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;

import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "phptest_MainActivity";

    private static final String TAG_JSON = "dictionary";
    private static final int MAX_ID_SIZE = 4;
    private static final int[] ID_LIST = {R.id.textView_list_id, R.id.textView_list_item1, R.id.textView_list_item2, R.id.textView_list_item3};

    private TextView mTextViewResult;
    ArrayList<HashMap<String, String>> mArrayList;
    ListView mlistView;
    String mJsonString;

    private List<String> list;          // 데이터를 넣은 리스트변수
    private ListView listView;          // 검색을 보여줄 리스트변수
    private EditText editSearch;        // 검색어를 입력할 Input 창
    private SearchAdapter adapter;      // 리스트뷰에 연결할 아답터
    private ArrayList<String> arraylist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextViewResult = (TextView)findViewById(R.id.textView_main_result);
        mlistView = (ListView)findViewById(R.id.listView_main_list);
        mArrayList = new ArrayList<>();

        GetData task = new GetData(); //AsyncTask 객체 생성
        task.execute("https://circlezero.loca.lt/DictionaryApp/dbtest.php"); //스레드 실행 ("A")
        editSearch = (EditText) findViewById(R.id.editSearch);
        listView = (ListView) findViewById(R.id.listView_main_list);

        // 리스트를 생성한다.
        list = new ArrayList<String>();


        // 리스트의 모든 데이터를 arraylist에 복사한다.// list 복사본을 만든다.
        arraylist = new ArrayList<String>();
        arraylist.addAll(list);

        // 리스트에 연동될 아답터를 생성한다.
        adapter = new SearchAdapter(list, this);

        // 리스트뷰에 아답터를 연결한다.
        listView.setAdapter(adapter);

        // input창에 검색어를 입력시 "addTextChangedListener" 이벤트 리스너를 정의한다.
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                // input창에 문자를 입력할때마다 호출된다.
                // search 메소드를 호출한다.
                String text = editSearch.getText().toString();
                search(text);
            }
        });
    }
    public void search(String charText) {

        // 문자 입력시마다 리스트를 지우고 새로 뿌려준다.
        list.clear();

        // 문자 입력이 없을때는 모든 데이터를 보여준다.
        if (charText.length() == 0) {
            list.addAll(arraylist);
        }
        // 문자 입력을 할때..
        else
        {
            // 리스트의 모든 데이터를 검색한다.
            for(int i = 0;i < arraylist.size(); i++)
            {
                // arraylist의 모든 데이터에 입력받은 단어(charText)가 포함되어 있으면 true를 반환한다.
                if (arraylist.get(i).toLowerCase().contains(charText))
                {
                    // 검색된 데이터를 리스트에 추가한다.
                    list.add(arraylist.get(i));
                }
            }
        }
        // 리스트 데이터가 변경되었으므로 아답터를 갱신하여 검색된 데이터를 화면에 보여준다.
        adapter.notifyDataSetChanged();
    }
    //스레드(thread): 프로세스(process) 내에서 실제로 작업을 수행하는 주체.
    //AsyncTask는 자바에서 필수로 사용하는 기능이 모두 구축된 가장 간편한 스레드 클래스라고 볼 수 있음.
    //작업 순서: onPreExecute() -> doInBackground() -> onPostExecute()
    //A: 메인스레드에서 생성된 스레드에게 보내는 인자
    //B: 생성된 스레드에서 갱신 작업을 할 때 메인 스레드에게 보내는 인자
    //C: 생성된 스레드에서 작업 종료 후의 처리를 할 때 메인 스레드에게 보내는 인자
    private class GetData extends AsyncTask<String, Void, String>{ //AsyncTask<A, B, C>
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() { //스레드가 시작하기 전에 수행할 작업(메인 스레드)
            super.onPreExecute();

            //앱에서 시간이 걸리는 작업을 수행할 때, ProgressDialog 클래스를 이용하면 사용자에게 실시간 진행 상태를 알릴 수 있음
            //onPreExecute()에서 객체 생성
            progressDialog = ProgressDialog.show(MainActivity.this,
                    "Please Wait", null, true, true);
        }

        @Override
        protected String doInBackground(String... params) { //스레드가 수행할 작업(생성된 스레드)
            String serverURL = params[0]; //params: A, 내용: http://circlezero.loca.lt/dbtest.php

            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection(); //REST API를 호출하기 위한...
                //HttpURLConnection은 기본적으로 GET 메서드 사용. setRequestMethod()를 사용해서 POST, HEAD 등으로 변경 가능
                //GET은 웹 서버로부터 리소스를 가져옴

                httpURLConnection.setReadTimeout(5000); //InputStream 읽어오는 Timeout 시간 설정
                httpURLConnection.setConnectTimeout(5000); //서버에 연결되는 Timeout 시간 설정
                httpURLConnection.connect(); //서버 접속

                //서버의 응답 코드(예: 200, 404)
                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) { //서버가 제대로 응답할 경우
                    inputStream = httpURLConnection.getInputStream(); //InputStream 값을 뽑아냄
                }
                else{
                    inputStream = httpURLConnection.getErrorStream(); //에러 발생 시에 리턴되는 데이터
                }

                //InputStream은 바이트 스트림이기 때문에 InputStreamReader를 이용해 char 형태로 처리
                //char 형태를 String 형태로, 즉 문자 배열을 문자열로 변환하기 위해 BufferedReader 사용
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                //StringBuilder를 이용하여 문자열 여러개를 문자열 하나로 합침
                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();

                //trim()은 마지막 문자 하나를 제거함
                return sb.toString().trim(); //sb.toString().trim() : C 
            } catch (Exception e) {
                Log.d(TAG, "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) { //스레드 작업이 모두 끝난 후에 수행할 작업(메인 스레드)
            super.onPostExecute(result); //result = C

            progressDialog.dismiss(); //ProgressDialog 종료
            mTextViewResult.setText(result);
            Log.d(TAG, "response  - " + result);

            if (result == null){
                mTextViewResult.setText(errorString);
            }
            else {
                mJsonString = result;
                showResult();
            }
        }
    }
    private void showResult(){
        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            ArrayList<String> TAG_LIST = new ArrayList<String>();

            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject item = jsonArray.getJSONObject(i);

                if (i == 0) {
                    Iterator iterator = item.keys();
                    while (iterator.hasNext()) {
                        TAG_LIST.add(iterator.next().toString());
                    }
                }

                String[] values = new String[TAG_LIST.size()];
                for (int j = 0; j < TAG_LIST.size(); j++) {
                    values[j] = item.getString(TAG_LIST.get(j));
                }
                HashMap<String, String> hashMap = new HashMap<>();

                for (int j = 0; j < MAX_ID_SIZE; j++) {
                    if (j < TAG_LIST.size()) {
                        hashMap.put(TAG_LIST.get(j), values[j]);
                    } else {
                        hashMap.put("" + j, "");
                    }
                }

                mArrayList.add(hashMap);
            }
            for (int i = TAG_LIST.size(); i < MAX_ID_SIZE; i++) {
                TAG_LIST.add("" + i);
            }

            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, mArrayList, R.layout.word_list,
                    TAG_LIST.toArray(new String[TAG_LIST.size()]), ID_LIST
            );

            mlistView.setAdapter(adapter);
        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }
    }

}