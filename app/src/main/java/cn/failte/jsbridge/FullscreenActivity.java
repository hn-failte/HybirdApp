package cn.failte.jsbridge;

import android.annotation.SuppressLint;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class FullscreenActivity extends AppCompatActivity {
    private WebView webView;
    private Context context;

    private Map getParamsMap(String url, String pre) {
        ArrayMap qsMap = new ArrayMap<>();
        if (url.contains(pre)) {
            int index = url.indexOf(pre);
            int end = index + pre.length();
            String queryString = url.substring(end + 1);
            String[] queryStringSplit = queryString.split("&");
            String[] queryStringParam;
            for (String qs : queryStringSplit) {
                if (qs.toLowerCase().startsWith("data=")) {
                    //单独处理 data 项，避免 data 内部的 & 被拆分
                    int dataIndex = queryString.indexOf("data=");
                    String dataValue = queryString.substring(dataIndex + 5);
                    qsMap.put("data", dataValue);
                } else {
                    queryStringParam = qs.split("=");
                    String value = "";
                    if (queryStringParam.length > 1) {
                        //避免后台有时候不传值,如 key= 这种
                        value = queryStringParam[1];
                    }
                    qsMap.put(queryStringParam[0].toLowerCase(), value);
                }
            }
        }
        return qsMap;
    }

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        webView = findViewById(R.id.view_webview);

        WebSettings settings = webView.getSettings();

        // 允许在WebView中使用js
        settings.setJavaScriptEnabled(true);

        context = this;

        // 实例化方法类
        JavaScriptMethod method = new JavaScriptMethod(this, webView);

        webView.addJavascriptInterface(method, JavaScriptMethod.JAVASCRIPTINTERFACE);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                Log.v("url", url);
                Log.v("message", message);
                Log.v("defaultValue", defaultValue);
                String pre = "cordova://android";
                if (message.contains(pre)) {
                    Map map = getParamsMap(message, pre);
                    String code = (String) map.get("code");
                    String data = (String) map.get("data");
                    if(code.equals("plugin")) {
                        try {
                            JSONObject json = new JSONObject(data);
                            String toast = (String)json.optString("data");
                            Log.v("plugin", toast);
                            result.confirm("{\"code\": 0}\", \"data\": {}\"}");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        result.cancel();
                    }
                }
                return true;
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            // 返回 true，即根据代码逻辑执行相应操作，webview 不加载该url
            // 返回 false，除执行相应代码外，webview 加载该url
            // 返回 super.shouldOverrideUrlLoading()，在父类中，返回的其实还是 false
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 通过判断拦截到的url是否含有pre，来辨别是http请求还是调用android方法的请求
                String pre = "failte://android";
                if (url.contains(pre)) {
                    // 该url是调用 android 方法的请求
                    Map map = getParamsMap(url, pre);
                    // 解析 url 中的参数来执行相应方法
                    String code = (String) map.get("code");
                    String data = (String) map.get("data");
                    if(code.equals("toast")) {
                        try {
                            JSONObject json = new JSONObject(data);
                            String toast = (String)json.optString("data");
                            Log.v("toast", toast);
                            Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    return true;
                }
                // 该 url 是http请求，用webview加载url
                return false;
            }
        });

        webView.loadUrl("file:///android_asset/index.html");
    }
}