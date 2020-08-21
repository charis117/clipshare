package tools.chris.clip;

import android.app.*;
import android.os.*;
import android.content.Intent;
import android.widget.TextView;
import android.widget.EditText;
import java.net.Socket;
import android.view.View;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.OutputStream;
import android.content.ClipboardManager;
import android.widget.Toast;

public class MainActivity extends Activity 
{
	
	String text;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		text=getIntent().getExtras().getString(Intent.EXTRA_TEXT,"NOIN");
		if(text=="NOIN"){
			ClipboardManager clip=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
			text=clip.getText().toString();
		}
		TextView tv=(TextView)findViewById(R.id.mainTextView);
		
		tv.setText("Text to send:"+text);
		
		Thread th=new Thread(){
			public void run(){
				try {
					Socket s=new Socket("192.168.1.45", 1456);
					//OutputStream os=s.getOutputStream();
					//os.write(text.length());
					//os.write(text.getBytes());
					
				} catch (final IOException e) {
					e.printStackTrace();
					runOnUiThread(new Runnable(){

							@Override
							public void run() {
								Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
							}


						});
				}


			}
		};
		th.start();
		
		
    }
	
	
	public void send(View v){
		EditText ed=(EditText)findViewById(R.id.mainEditText);
		final String IP=ed.getText().toString();
		Thread th=new Thread(){
			public void run(){
				try {
					Socket s=new Socket(IP, 1456);
					OutputStream os=s.getOutputStream();
					os.write(text.length());
					os.write(text.getBytes());
					runOnUiThread(new Runnable(){

							@Override
							public void run() {
								sent();
							}

						
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				
			}
		};
		th.start();
	}
	
	public void sent(){
		Toast.makeText(this,"Text sent",Toast.LENGTH_SHORT).show();
	}
}
