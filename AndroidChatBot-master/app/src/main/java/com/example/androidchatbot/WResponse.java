package com.example.androidchatbot;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

//  This is what our response looks like:
//        {
//        "status": true,
//        "response": "Hello, how are you feeling today?"
//        }
//

public class WResponse {
    @SerializedName("status")
    @Expose
    private String status;

    @SerializedName("response")
    @Expose
    private String response;

    public String getStatus(){
        return status;
    }

    public void setStatus(String Status) {
        this.status = status;
    }

    public String getResponse(){
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public interface BotAPIs{

        /* Over here we are sending a POST request with two fields as POST request body params */
        @FormUrlEncoded
        @POST("/")
        Call < WResponse > postUserMessage(@Field("user-input") String userInput);
    }
}
