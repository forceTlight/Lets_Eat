package org.techtown.letseat.restaurant.qr;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.techtown.letseat.MainActivity;
import org.techtown.letseat.pay_test.PayActivity;
import org.techtown.letseat.util.AppHelper;
import org.techtown.letseat.R;
import org.techtown.letseat.util.PhotoSave;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class qr_restActivity extends AppCompatActivity {


    DatabaseReference mRoootRef = FirebaseDatabase.getInstance().getReference();
    private int num = 0;
    private ArrayList<QR_Menu> list = new ArrayList<>();
    private ArrayList<Integer> menus_id = new ArrayList<>();
    private ImageView resImage;
    private int resId, tableNumber, orderId;
    private QR_MenuAdapter adapter;
    private RecyclerView recyclerView;
    private View view;
    private AppCompatButton orderButton;
    private String title;
    TextView res_title, res_table, requestTextView;
    public static TextView sumTextView;
    public static ArrayList<Integer> selectMenus = new ArrayList<>();
    int data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QR_MenuAdapter.menuNames.clear();
        setContentView(R.layout.activity_qr_rest);
        sumTextView = findViewById(R.id.sumTextView);
        res_title = findViewById(R.id.waitingNumbertv);
        res_table = findViewById(R.id.res_tableNumber);
        resImage = findViewById(R.id.qr_res_image);
        recyclerView = findViewById(R.id.qr_recyclerView);
        orderButton = findViewById(R.id.qr_order_button);
        requestTextView = findViewById(R.id.requestTextView);
        sumTextView.setText("0원");
        // 번들 가져오기
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        resId = bundle.getInt("resId");
        tableNumber = bundle.getInt("tableNumber");
        // 어댑터 설정
        adapter = new QR_MenuAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        // 주문버튼
        orderButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if(QR_MenuAdapter.sum == 0){
                    Toast.makeText(qr_restActivity.this, "메뉴를 고른뒤에 주문하실 수 있습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                requestOrderList();
                String sum = String.valueOf(QR_MenuAdapter.sum);
                String menuName = getMenuNames();
                PayActivity payActivity = new PayActivity(sum, menuName);
                Intent intent = new Intent(getApplicationContext(), payActivity.getClass());
                startActivity(intent);
                QR_MenuAdapter.sum = 0;
                finish();
            }
        });
        get_Restaurant();
        get_MenuData();
    }
    // 주문 리스트 보내기
    @RequiresApi(api = Build.VERSION_CODES.O)
    void requestOrderList(){
        LocalDateTime now = LocalDateTime.now();
        String orderTime = now.format(DateTimeFormatter.ofPattern("HH시 mm분 ss초"));
        String url = "http://125.132.62.150:8000/letseat/order/list/register";
        String request_string = requestTextView.getText().toString();
        JSONObject postData = new JSONObject();
        JSONObject restData = new JSONObject();
        JSONObject userData = new JSONObject();
        int userId = MainActivity.userId;
        try {
            restData.put("resId", resId);
            restData.put("resName",resId);
            userData.put("userId",userId);
            postData.put("orderTime",orderTime);
            postData.put("tableNumber",tableNumber);
            postData.put("sum",QR_MenuAdapter.sum);
            postData.put("user",userData);
            postData.put("servingYN","N");
            postData.put("checkYN","N");
            postData.put("reviewYN","N");
            postData.put("orderYN","Y");
            postData.put("request", request_string);
            postData.put("restaurant",restData);
        }catch (JSONException e){
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                postData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            orderId = response.getInt("orderId");
                            requestMenuList();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        Toast.makeText(getApplicationContext(), "연결 불량으로 인해 주문 실패.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
        );
        request.setShouldCache(false);
        AppHelper.requestQueue = Volley.newRequestQueue(this); // requsetQueue 초기화
        AppHelper.requestQueue.add(request);
    }
    // 메뉴 리스트 보내기
    void requestMenuList(){
        String url = "http://125.132.62.150:8000/letseat/order/menu/register";
        JSONArray menusData = new JSONArray();
        JSONObject menuPostData = new JSONObject();
        ArrayList<String> selectMenus = adapter.getSelectMenu();
        HashMap<Integer, Integer> amount_map = adapter.getAmount_map();
        for(int i = 0; i < selectMenus.size(); i++){
            int idx = Integer.parseInt(selectMenus.get(i));
            try {
                JSONObject postData = new JSONObject();
                JSONObject orderData = new JSONObject();
                JSONObject menuData = new JSONObject();
                menuData.put("resMenuId",menus_id.get(idx));
                orderData.put("orderId", orderId);
                postData.put("orderList",orderData);
                postData.put("amount",amount_map.get(idx));
                postData.put("resMenu",menuData);
                menusData.put(postData);
                menuPostData.put("orderMenus", menusData);
            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                menuPostData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        DatabaseReference myRef = mRoootRef.child("ownerId_1");
                        myRef.setValue(orderId);
                        Log.d("OrderMenu","성공");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("OrderMenu","Error: " + error);
                    }
                }
        );
        request.setShouldCache(false);
        AppHelper.requestQueue = Volley.newRequestQueue(this); // requsetQueue 초기화
        AppHelper.requestQueue.add(request);
    }
    // 레스토랑 정보 가져오기
    void get_Restaurant() {
        String url = "http://125.132.62.150:8000/letseat/store/findOne?resId="+resId;
        JsonObject jsonObject = new JsonObject();
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Bitmap bitmap = null;
                        String image = null;
                        try {
                            title = response.getString("resName");
                            image = response.getString("image");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        bitmap = PhotoSave.StringToBitmap(image);
                        resImage.setImageBitmap(bitmap);
                        res_title.setText(title);
                        res_table.setText(tableNumber+"번 테이블");
                        Log.d("응답", response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        Toast.makeText(getApplicationContext(), "연결 불량으로 인해 주문 실패.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
        );
        request.setShouldCache(false);
        AppHelper.requestQueue = Volley.newRequestQueue(this); // requsetQueue 초기화
        AppHelper.requestQueue.add(request);
    }
    // 메뉴 리스트 가져오기
    void get_MenuData() {
        String url = "http://125.132.62.150:8000/letseat/store/menu/load?resId="+resId;
        JSONArray getData = new JSONArray();
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                getData,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            int menuId;
                            String menuPrice,menuName,image,excription;
                            Bitmap bitmap;

                            for(int i = 0; i < response.length(); i++){
                                JSONObject jsonObject = (JSONObject) response.get(i);
                                menuId = jsonObject.getInt("resMenuId");
                                menuPrice = jsonObject.getString("price");
                                menuName = jsonObject.getString("name");
                                image = jsonObject.getString("photo");
                                excription = jsonObject.getString("excription");
                                bitmap = PhotoSave.StringToBitmap(image);
                                QR_Menu menu = new QR_Menu(bitmap, menuName, menuPrice, excription);
                                list.add(menu);
                                menus_id.add(menuId);
                            }
                            adapter.setItems(list);
                            adapter.notifyDataSetChanged();
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
        request.setShouldCache(false); // 이전 결과 있어도 새로 요청해 응답을 보내줌
        AppHelper.requestQueue = Volley.newRequestQueue(this); // requsetQueue 초기화
        AppHelper.requestQueue.add(request);
    }
    public String getMenuNames(){
        HashMap<Integer,String> menuNames = QR_MenuAdapter.menuNames;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < menuNames.size(); i++){
            sb.append(menuNames.get(i));
        }
        return sb.toString();
    }
}