package moonlit.solutions.sabdakathmandu;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashScreen extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        exitSplash();
    }

    private void exitSplash(){
        final Thread thread=  new Thread(){
            @Override
            public void run(){
                try {
                    synchronized(this){
                        wait(SPLASH_DURATION);
                    }
                }
                catch(InterruptedException ex){
                    ex.printStackTrace();
                }
                startActivity(new Intent(SplashScreen.this, EncodeActivity.class));
                finish();
            }
        };
        thread.start();
    }
}
