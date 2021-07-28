package cn.failte.jsbridge;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

public class JavaScriptMethod {
    private Context mContext;

    private WebView mWebView;

    // 需要挂在在 webview 的接口，在 h5 中表现为某个全局对象
    public static final String JAVASCRIPTINTERFACE = "JsBridge";

    // andorid 4.2（包括android4.2）以上，如果不写该注解，js无法调用android方法
    @JavascriptInterface
    public void showToast(String json){
        Toast.makeText(mContext, json, Toast.LENGTH_SHORT).show();
    }
    public JavaScriptMethod(Context context, WebView webView) {
        mContext = context;
        mWebView = webView;
    }
}
