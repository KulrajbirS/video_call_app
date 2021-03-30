package com.example.video_call_app

import android.webkit.JavascriptInterface

class Javascriptinterface(val callActivity: CallActivity) {

    @JavascriptInterface
    public fun onPeerConnected() {
        callActivity.onPeerConnected()
    }
}