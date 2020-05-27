package com.example.androidchatbot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private EditText editText;
    private Button button;
    private TextView responseText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {

        editText = findViewById(R.id.message_input);
        button = findViewById(R.id.message_send);
        responseText = findViewById(R.id.response_text);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserMessage();
            }
        });

    }

    private void sendUserMessage(){
        //Obtain an instance of Retrofit by calling the static method.
        Retrofit retrofit = NetworkClient.getRetrofitClient();

        WResponse.BotAPIs botapis = retrofit.create(WResponse.BotAPIs.class);

        Call call = botapis.postUserMessage(editText.getText().toString());



         /*
        This is the line which actually sends a network request. Calling enqueue() executes a call asynchronously. It has two callback listeners which will invoked on the main thread
        */

         call.enqueue(new Callback() {
             @Override
             public void onResponse(Call call, Response response) {
                 /*This is the success callback. Though the response type is JSON, with Retrofit we get the response in the form of WResponse POJO class
                  */

                 if (response.body() != null) {
                     WResponse wResponse = (WResponse) response.body();

                     responseText.setText("Temp: " + wResponse.getStatus() + "\n " +
                             "Humidity: " + wResponse.getResponse());
                 }
             }

             @Override
             public void onFailure(Call call, Throwable t) {
                /*
                Error callback
                */
             }
         });
    }
}
