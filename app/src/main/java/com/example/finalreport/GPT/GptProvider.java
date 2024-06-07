package com.example.finalreport.GPT;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GptProvider {
    private static GptProvider instance;
    private final OkHttpClient client;
    private final String apiKey;
    private final String url;
    private String prompt;
    private int maxTokens;
    private float temperature;


    private GptProvider(Context context) {
 
        url = "https://api.openai.com/v1/chat/completions";
        client = new OkHttpClient();
        prompt = "";
        maxTokens = 4000;
        temperature = 0;
    }

    public static GptProvider getInstance(Context context) {
        if (instance == null) {
            instance = new GptProvider(context);
        }
        return instance;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public void getResponse(String prompt, Callback callback) {
        if (!prompt.isEmpty()) {
            try {
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("model", "text-davinci-003");
                jsonBody.put("prompt", prompt);
                jsonBody.put("max_tokens", maxTokens);
                jsonBody.put("temperature", temperature);

                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(jsonBody.toString(), mediaType);

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", "Bearer " + apiKey)
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "Request failed", e);
                        callback.onFailure(call, e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        callback.onResponse(call, response);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                // Handle the JSON exception, log it, or notify the user.
            }
        } else {
            // Handle the case where the prompt is empty.
            // Toast.makeText(context, "Prompt cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }


    public static void handleResponse(String responseBody, TextView txtResponse) {
        try {
            JSONObject jsonObject = new JSONObject(responseBody);
            Log.d("GptProvider", "JSON Response: " + jsonObject.toString());

            if (jsonObject.has("choices") && !jsonObject.isNull("choices")) {
                JSONArray jsonArray = jsonObject.getJSONArray("choices");
                if (jsonArray.length() > 0) {
                    String textResult = jsonArray.getJSONObject(0).optString("text", "");
                    txtResponse.post(() -> txtResponse.setText(textResult));
                } else {
                    txtResponse.post(() -> txtResponse.setText("No choices in the response"));
                }
            } else {
                txtResponse.post(() -> txtResponse.setText("No 'choices' field in the response"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            txtResponse.post(() -> txtResponse.setText("Error parsing response"));
        }
    }

    public String getPrompt() {
        return prompt;
    }
}
