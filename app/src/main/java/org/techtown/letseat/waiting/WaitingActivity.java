package org.techtown.letseat.waiting;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.techtown.letseat.MainActivity;
import org.techtown.letseat.R;
import org.techtown.letseat.util.AppHelper;

public class WaitingActivity extends AppCompatActivity {

    DatabaseReference mRoootRef = FirebaseDatabase.getInstance().getReference();
    int num;
    private TextView waiting_queue, person_number;
    private Button waiting_btn1, waiting_btn2;
    private ImageButton minus_btn, plus_btn;
    private int person = 1;
    EditText editTextPhone;
    String phoneNum;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.waiting_activity);


        editTextPhone = findViewById(R.id.editTextPhone);
        person_number = findViewById(R.id.person_number);
        minus_btn = findViewById(R.id.minus_btn);
        plus_btn = findViewById(R.id.plus_btn);
        waiting_btn1 = findViewById(R.id.waiting_btn1);
        waiting_btn2 = findViewById(R.id.waiting_btn2);

        person_number.setText(""+person);

        MaterialToolbar toolbar = findViewById(R.id.topMain);

        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        minus_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                person--;
                person_number.setText(""+person);
            }
        });

        plus_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                person++;
                person_number.setText(""+person);
            }
        });

        //등록버튼
        waiting_btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phoneNum = editTextPhone.getText().toString();
                sendWaiting();
                finish();
            }
        });

        //닫기버튼
        waiting_btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "취소되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    // 웨이팅 POST 요청
    public void sendWaiting() {
        String url = "http://125.132.62.150:8000/letseat/waiting/register";
        JSONObject resData = new JSONObject();
        JSONObject userData = new JSONObject();
        JSONObject postData = new JSONObject();
        try {
            resData.put("resId", 1);
            userData.put("userId", 1);
            postData.put("restaurant",resData);
            postData.put("user",userData);
            postData.put("peopleNum",person);
            postData.put("phoneNumber",phoneNum);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                postData,
                new Response.Listener<JSONObject>() {
                    @Override // 응답 잘 받았을 때
                    public void onResponse(JSONObject response) {
                        int waitingId;
                        try {
                            waitingId = response.getInt("waitingId");
                            DatabaseReference myRef = mRoootRef.child("waiting_ownerId_1");
                            myRef.setValue(waitingId);
                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                            startActivity(intent);
                            Toast.makeText(getApplicationContext(), "등록되었습니다.", Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override // 에러 발생 시
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "오류발생", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        request.setShouldCache(false); // 이전 결과 있어도 새로 요청해 응답을 보내줌
        AppHelper.requestQueue = Volley.newRequestQueue(this); // requsetQueue 초기화
        AppHelper.requestQueue.add(request);
    }
}
