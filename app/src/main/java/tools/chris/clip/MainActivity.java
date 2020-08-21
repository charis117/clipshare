package tools.chris.clip;

import android.app.*;
import android.content.SharedPreferences;
import android.os.*;
import android.content.Intent;
import android.widget.TextView;
import android.widget.EditText;

import java.net.ServerSocket;
import java.net.Socket;
import android.view.View;
import java.io.IOException;
import java.io.OutputStream;
import android.content.ClipboardManager;
import android.widget.Toast;

public class MainActivity extends Activity 
{


	public static final String prefName="user";
	public static final String ipkeyName ="IP";
	public static final String CNULL="NOIN";
	Socket s=null;
	String text;
	boolean searching=true;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		try{
			text=getIntent().getExtras().getString(Intent.EXTRA_TEXT,CNULL);
		}catch(Exception e){
			e.printStackTrace();
		}
		if(text==CNULL){
			ClipboardManager clip=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
			text=clip.getText().toString();
		}
		
		TextView tv=(TextView)findViewById(R.id.mainTextView);

		tv.setText("Text to send:"+text);
		
		SharedPreferences sh=getSharedPreferences(prefName,0);
		if(sh.contains(ipkeyName)){
			EditText ed=(EditText)findViewById(R.id.mainEditText);
			ed.setText(sh.getString(ipkeyName,"192.168.1."));
		}
		//Currently not implemented fully
		Thread server=new Thread(new Runnable(){

			@Override
			public void run() {
				try {
					ServerSocket ss=new ServerSocket(1458);
					Socket s=ss.accept();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});


    }


	public void send(View v){
		SharedPreferences.Editor editor=getSharedPreferences(prefName,0).edit();

		EditText ed=(EditText)findViewById(R.id.mainEditText);
		final String IP=ed.getText().toString();
		editor.putString(ipkeyName,IP);
		editor.commit();
		Thread th=new Thread(){
			public void run(){
				try{
					Socket s=new Socket(IP,1456);
					OutputStream os=s.getOutputStream();
					os.write(text.length());
					os.write(text.getBytes());
					runOnUiThread(new Runnable(){

							@Override
							public void run(){
								sent();
							}


						});
				}catch(IOException e){
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
