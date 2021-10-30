package org.techtown.letseat.mytab;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.appbar.MaterialToolbar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.techtown.letseat.R;
import org.techtown.letseat.util.AppHelper;
import org.techtown.letseat.RestSearch2;
import org.techtown.letseat.restaurant.list.RestListAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class waiting_Layout extends AppCompatActivity {
    TextView peopleNumtv,waitingIdtv,nametv,waitingNumbertv,datetv;
    int userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mytab_waiting);
        Intent intent = getIntent();
        userId = intent.getIntExtra("userId",0);
        waitingIdtv = findViewById(R.id.waitingIdtv);
        nametv = findViewById(R.id.nametv);
        waitingNumbertv = findViewById(R.id.waitingNumbertv);
        peopleNumtv = findViewById(R.id.peopleNum);
        datetv = findViewById(R.id.datetv);
        getWatingOrderList();
    }
    void getWatingOrderList() {
        String url = "http://125.132.62.150:8000/letseat/waiting/user/load?userId="+userId;
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject userObject = response.getJSONObject("user");
                            String waitingId = response.getString("waitingId");   //대기번호
                            String name = userObject.getString("name");     //유저이름
                            String waitingNumber = response.getString("waitingNumber");   //대기순서
                            int peopleNum = response.getInt("peopleNum");     //접수인원
                            String date = response.getString("date");     //접수시간
                            waitingIdtv.setText(waitingId);
                            nametv.setText(name+"님의 대기현황");
                            waitingNumbertv.setText(waitingNumber);
                            peopleNumtv.setText("접수인원: "+peopleNum+"명");
                            datetv.setText("접수시간: " + date);
                            Log.d("응답", response.toString());
                        } catch (JSONException e) {
                            Log.d("예외", e.toString());
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("에러", error.toString());
                    }
                }
        );
        request.setShouldCache(false); // 이전 결과 있어도 새로 요청해 응답을 보냄
        AppHelper.requestQueue.add(request);

        MaterialToolbar toolbar = findViewById(R.id.topMain);

        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
}