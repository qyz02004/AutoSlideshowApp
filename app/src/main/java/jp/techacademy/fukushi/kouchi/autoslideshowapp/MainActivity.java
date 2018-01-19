package jp.techacademy.fukushi.kouchi.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    Button prevButton;      // 戻るボタン
    Button playButton;      // 再生ボタン
    Button nextButton;      // 進むボタン
    Cursor mCursor; // 画像のアクセスに使用するカーソル

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCursor = null; // カーソル初期化

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
                setClickListerner();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
            setClickListerner();
        }
    }
    
    @Override
    public void onClick(View v) {
        // Log.d("UI_PARTS", "ボタンをタップしました");
        if ( v == prevButton) {      // 戻るボタン
            if (mCursor.moveToNext()) { // 次があれば
                displayImage( mCursor );    // 表示
            } else if (mCursor.moveToFirst()) { // なければ最初に移動
                displayImage( mCursor );    // 表示
            }
        }
        else if( v == playButton ) { // 再生ボタン

        }
        else if( v == nextButton ) { // 進むボタン
            if (mCursor.moveToPrevious()) { // 前があれば
                displayImage( mCursor );    // 表示
            } else if (mCursor.moveToLast()) { // なければ最後に移動
                displayImage( mCursor ); // 表示
            }
        }
    }

    // ボタンにリスナーを登録
    private void setClickListerner()    {
        prevButton = (Button) findViewById(R.id.prev_button);
        prevButton.setOnClickListener(this);

        playButton = (Button) findViewById(R.id.play_button);
        prevButton.setOnClickListener(this);

        nextButton = (Button) findViewById(R.id.next_button);
        nextButton.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo() {

        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        mCursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        if (mCursor.moveToFirst()) {
            displayImage( mCursor );
        }
        // mCursor.close();
    }

    // カーソルにあるイメージを表示
    private void  displayImage( Cursor cursor ) {
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageURI(imageUri);
    }

}
