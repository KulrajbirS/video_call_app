package com.example.video_call_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class CallActivity : AppCompatActivity() {

    var username = ""
    var frienduser = ""
    var isPeerConnected = false
    var firebaseRef = Firebase.database.getReference("users")
    var isAudio = true
    var isVideo = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        val call = findViewById<Button>(R.id.callBtn)
        val togAudio = findViewById<ImageView>(R.id.toogleAudio)
        val togVideo = findViewById<ImageView>(R.id.toggleVideo)


        username = intent.getStringExtra("username")!!

        call.setOnClickListener {

        }

        togAudio.setOnClickListener {

        }

        togVideo.setOnClickListener {

        }

        setupWebView()
    }

    private fun setupWebView() {
        val webV = findViewById<WebView>(R.id.webView)
        webV.webChromeClient = object: WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                super.onPermissionRequest(request)
                request?.grant(request.resources)
            }
        }

        webV.settings.javaScriptEnabled = true
        webV.settings.mediaPlaybackRequiresUserGesture = false
        webV.addJavascriptInterface(Javascriptinterface(this), "Android")

        loadVideoCall()
    }

    private fun loadVideoCall() {
        val filePath = "file:android_asset/call.html"
        val webV = findViewById<WebView>(R.id.webView)
        webV.loadUrl(filePath)

        webV.webViewClient = object: WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                intializePeer()
            }
        }
    }

    var u_id = ""

    private fun intializePeer() {

        u_id = getUniqueId()

        callJavascriptFunction("javascript:init(\"${getUniqueId()}\")")

        firebaseRef.child(username).child("incoming").addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                onCallRequest(snapshot.value as? String)
            }

        })
    }

    private fun onCallRequest(caller: String?) {
        if (caller == null) return

        val call = findViewById<View>(R.id.calllayout)
        call.visibility = View.VISIBLE
        val itext = findViewById<TextView>(R.id.incomingCallTxt)
        itext.text = "$caller is calling..."

        val acc_btn = findViewById<ImageView>(R.id.acceptCall)
        acc_btn.setOnClickListener {
            firebaseRef.child(username).child("connId").setValue(u_id)
        }

        val call_l = findViewById<View>(R.id.calllayout)


    }

    private fun getUniqueId(): String {
        return UUID.randomUUID().toString()
    }

    private fun callJavascriptFunction(functionString: String) {
        val webV = findViewById<WebView>(R.id.webView)
        webV.post { webV.evaluateJavascript(functionString, null) }
    }

    fun onPeerConnected() {
        isPeerConnected = true
    }
}