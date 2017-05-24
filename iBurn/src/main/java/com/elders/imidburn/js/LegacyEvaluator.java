package com.elders.imidburn.js;

import android.annotation.SuppressLint;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import rx.subjects.PublishSubject;

/**
 * Evaluates Javascript on a WebView, returning the result to a {@link PublishSubject}.
 * This implementation is used on Android versions below {@link android.os.Build.VERSION_CODES#KITKAT}
 *
 * Important : This class should *not* be used on Android versions below {@link android.os.Build.VERSION_CODES#JELLY_BEAN_MR1}
 * with untrusted javascript.
 *
 * Created by dbro on 8/7/15.
 */
public class LegacyEvaluator implements JSEvaluator.Evaluator {

    private JsBridge jsBridge;

    public LegacyEvaluator(WebView webView) {
        // This is unsafe pre API-17 but we're running only trusted, bundled Javascript
        jsBridge = new JsBridge();
        webView.addJavascriptInterface(jsBridge, "subject");
    }

    @SuppressLint("AddJavascriptInterface")
    @Override
    public void evaluate(WebView webView, String script, EvaluatorCallback callback) {
        //Timber.d("Initial script: %s", script);

        jsBridge.setCallback(callback);

        // Modify script to publish result to JsBridge#publishResult instead of
        // returning
        int returnIdx = script.indexOf("return");
        int endIdx = script.indexOf(';', returnIdx);

        String returnExpression = script.substring(returnIdx + 7, endIdx);
        //Timber.d("Got return expression %s from script", returnExpression);

        script = script.substring(0, returnIdx) + "subject.publishResult(" + returnExpression + ")" + script.substring(endIdx);
        //Timber.d("Reformed script: %s", script);

        webView.loadUrl("javascript:" + script);
        //webView.removeJavascriptInterface("subject");
    }

    class JsBridge {
        private EvaluatorCallback callback;

        public void setCallback(EvaluatorCallback callback) {
            this.callback = callback;
        }

        @JavascriptInterface
        public void publishResult(String result) {
            if (callback != null) callback.onResult(result); }
    }
}
