package jp.techacademy.fukushi.kouchi.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final int TIMER_INTERVAL = 2000; // タイマー周期 (ms)
    Timer mTimer;

    Button prevButton;      // 戻るボタン
    Button playButton;      // 再生ボタン
    Button nextButton;      // 進むボタン
    Cursor mCursor;          // 画像のアクセスに使用するカーソル
    boolean permission = false; // 画像にアクセスするパーミッション判定結果

    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 外部ストレージへのアクセス許可を判定する
        if (checkPermission()) {
            permission = true;
            // コンテンツへのアクセス情報を取得
            getContentsInfo();
        }
        setClickListener();
    }

    // 外部ストレージへのアクセス許可を判定する
    private boolean checkPermission() {

        boolean result = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0以降の場合
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                result = true;
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
           }
        } else {
            // Android 6.0未満の場合はインストール時に権限が付与される
            result = true;
        }

        return result;
    }

    @Override
    public void onClick(View v) {

        Log.d("UI_PARTS", "ボタンをタップしました");

        // ストレージへのアクセス権限がなければ
        if ( !permission ) {
            // アクセス権限をチェックして
            if (checkPermission()) {
                // コンテンツ情報を取得
                permission = true;
                getContentsInfo();
            }
        }

        // ストレージへのアクセス権限があれば
        if( permission ) {
            if (v == prevButton) {      // 戻るボタン
                if (mCursor.moveToPrevious()) { // 前があれば
                    displayImage(mCursor);    // 表示
                } else if (mCursor.moveToLast()) { // なければ最後に移動
                    displayImage(mCursor);    // 表示
                }
            } else if (v == playButton) { // 再生ボタン
                onClickPlayButton();
            } else if (v == nextButton) { // 進むボタン
                if (mCursor.moveToNext()) { // 次があれば
                    displayImage(mCursor);    // 表示
                } else if (mCursor.moveToFirst()) { // なければ最初に移動
                    displayImage(mCursor); // 表示
                }
            }
        }
    }


    // 再生ボタンを押した時の処理
    public void onClickPlayButton() {
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mCursor.moveToPrevious()) { // 前があれば
                                displayImage( mCursor );    // 表示
                            } else if (mCursor.moveToLast()) { // なければ最後に移動
                                displayImage( mCursor ); // 表示
                            }
                        }
                    });
                }
            }, TIMER_INTERVAL, TIMER_INTERVAL);

            playButton.setText("停止");       // 停止ボタンに変更
            prevButton.setEnabled(false);       // 戻るボタン無効化
            nextButton.setEnabled(false);       // 進むボタン無効化
        } else {
            // 再生中
            mTimer.cancel();          // タイマー停止
            mTimer = null;
            playButton.setText("再生");   // 再生ボタンに変更
            prevButton.setEnabled(true);       // 戻るボタン有効化
            nextButton.setEnabled(true);       // 進むボタン有効化
        }
    }

    // ボタンにリスナーを登録
    private void setClickListener()    {
        prevButton = (Button) findViewById(R.id.prev_button);
        prevButton.setOnClickListener(this);

        playButton = (Button) findViewById(R.id.play_button);
        playButton.setOnClickListener(this);

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
