package com.skypan.school;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button Getbt = (Button) findViewById(R.id.searchbt);
        /**傳送GET*/
        Getbt.setOnClickListener(v -> sendGET());
    }


    private void sendGET() {

        TextView searchview = (TextView) findViewById(R.id.searchview);
        EditText search = (EditText) findViewById(R.id.searchedittext);
        String search_str = search.getText().toString();

        //呼叫textview捲軸的方法
        searchview.setMovementMethod(ScrollingMovementMethod.getInstance());


        /**建立連線*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();
        /**設置傳送需求*/
        Request request = new Request.Builder()
                .url("https://opendata.cwb.gov.tw/api/v1/rest/datastore/F-C0032-001?Authorization=CWB-FC2FB02B-1AFA-410F-8D99-E13877F15B3C")
//                .header("Cookie","")//有Cookie需求的話則可用此發送
//                .addHeader("","")//如果API有需要header的則可使用此發送
                .build();
        /**設置回傳*/
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                /**如果傳送過程有發生錯誤*/
                searchview.setText(e.getMessage());
            }



            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                /**取得回傳*/
                try{

                    //先把網頁資料(json)轉成字串丟進來
                    String chrom_json = response.body().string();

                    //把json資料丟進jsonObject
                    JSONObject jsonObject = new JSONObject(chrom_json);

                    //再從已經丟完資料的jsonObject裡，取出json的records資料
                    jsonObject = jsonObject.getJSONObject("records");

                    //取完從records挑到需要的資訊(location)，location裡有地區名&天氣各種資訊
                    JSONArray jsonArray = jsonObject.getJSONArray("location");

                    String str_weatherElement[] = {"天氣現象","降雨機率","最低溫度","舒適度","最高溫度"};

                    //天氣資訊變數
                    String str_area = "";


                    //取得每個縣市的天氣資訊
                    for(int i=0 ; i<jsonArray.length() ; i++) {

                        //判斷搜尋地區變數
                        String serch_area = "";

                        //抓該地區資訊
                        jsonObject = jsonArray.getJSONObject(i);

                        //抓地區名
                        serch_area = jsonArray.getJSONObject(i).getString("locationName")+"\n";

                        //找出地區搜尋對應的資訊
                        if(serch_area.contains(search_str)){

                            str_area = str_area + serch_area;

                            Log.d("",str_area);

                            //jsonArray_weatherElement 放該地區的天氣狀況
                            JSONArray jsonArray_weatherElement = jsonObject.getJSONArray("weatherElement");

                            //取得該地區的天氣狀況
                            for(int j=0 ; j<jsonArray_weatherElement.length() ; j++) {

                                //jsonObject_weather{0~4}裡放第j個的 jsonArray_weatherElement 天氣狀況
                                JSONObject jsonObject_weather = jsonArray_weatherElement.getJSONObject(j);

                                //jsonArray_time 放個時段的天氣資訊
                                JSONArray jsonArray_time = jsonObject_weather.getJSONArray("time");

                                str_area = str_area + "[" + str_weatherElement[j] + "]" + "\n";
                                Log.d("","["+str_weatherElement[j]+"]");

                                //判斷目前需列印何種天氣資訊
                                String weatherInfo;
                                switch (j){
                                    case 0:
                                        weatherInfo = "天氣現象";
                                        break;
                                    case 1:
                                        weatherInfo = "降雨機率";
                                        break;
                                    case 2:
                                        weatherInfo = "最低溫度";
                                        break;
                                    case 3:
                                        weatherInfo = "舒適度";
                                        break;
                                    case 4:
                                        weatherInfo = "最高溫度";
                                        break;
                                    default:
                                        weatherInfo = "";
                                        break;
                                }

                                //列印天氣資訊
                                for(int k=0 ; k<jsonArray_time.length() ; k++) {

                                    switch (k){
                                        case 1:
                                            //列印時段
                                            str_area = str_area + jsonArray_time.getJSONObject(k).getString("startTime")+"~18:00:00" + "\n";
                                            Log.d("",jsonArray_time.getJSONObject(k).getString("startTime")+"~18:00:00");
                                            break;
                                        default:
                                            str_area = str_area + jsonArray_time.getJSONObject(k).getString("startTime")+"~06:00:00" + "\n";
                                            Log.d("",jsonArray_time.getJSONObject(k).getString("startTime")+"~06:00:00");
                                            break;
                                    }
                                    //從 jsonArray_time 取得"parameter"屬性對應的值(String)
                                    String str_parameter = jsonArray_time.getJSONObject(k).getString("parameter");

                                    //將抓到的 string 放進 jsonObject_parameter 物件
                                    JSONObject jsonObject_parameter = new JSONObject(str_parameter);

                                    str_area = str_area + weatherInfo + ":" + jsonObject_parameter.getString("parameterName") + "\n";
                                    Log.d("",weatherInfo + ":" + jsonObject_parameter.getString("parameterName"));
                                }

                                str_area =str_area + "\n";
                                Log.d("","  ");
                            }
                            Log.d("","=============="+i);
                        }

                    }
                    //該縣市天氣資訊列印出來
                    searchview.setText(str_area);
                }
                //如果沒有抓到回傳顯示 error
                catch (JSONException e){
                    Log.d("","error");
                }

            }
        });
    }
}