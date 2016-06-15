package info.gsgl.simplePlayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import util.SystemUtil;

public class MainActivity extends AppCompatActivity {
    private TextInputLayout tilRemoteVideo;
    private EditText etRemoteVideo;

    private final static int PERMISSIONS_REQUEST = 0;
    private final static int VIDEO_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tilRemoteVideo = (TextInputLayout) findViewById(R.id.tilRemoteVideo);
        etRemoteVideo = (EditText) findViewById(R.id.etRemoteVideo);

        //本地
        Button btnLocalVideo = (Button) findViewById(R.id.btnLocalVideo);
        btnLocalVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(MainActivity.this, "播放本地视频需要读取SD卡，请允许操作SD卡的权限。", Toast.LENGTH_SHORT).show();
                }

                //检查权限状态
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    startActivityForResult(intent, VIDEO_REQUEST);
                } else {
                    //如果没有权限, 动态请求权限
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST);
                }
            }
        });

        //远程
        Button btnRemoteVideo = (Button) findViewById(R.id.btnRemoteVideo);
        btnRemoteVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etRemoteVideo.getText().toString().equals("")) {
                    tilRemoteVideo.setError("远程视频地址不能为空！");
                    return;
                }

                Intent intent = new Intent();
                intent.setClass(MainActivity.this, VLCPlayerActivity.class);
                intent.putExtra("VideoType", "Remote");
                intent.putExtra("VideoUrl", etRemoteVideo.getText().toString());
                startActivity(intent);
            }
        });     
    }

    /**
     * 请求权限后, 系统会弹出请求权限的Dialog, 用户选择允许或需要后, 会回调onRequestPermissionsResult方法, 该方法类似于
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    startActivityForResult(intent, VIDEO_REQUEST);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == VIDEO_REQUEST && data != null) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, VLCPlayerActivity.class);
                intent.putExtra("VideoType", "Local");
                intent.putExtra("VideoUrl", SystemUtil.getPath(MainActivity.this, data.getData()));
                startActivity(intent);
            }
        } catch (Exception e) {
            Log.d("Local", e.toString());
        }
    }
}