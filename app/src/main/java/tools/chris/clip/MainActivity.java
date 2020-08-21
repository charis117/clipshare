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

	Socket s=null;
	String text;
	boolean searching=true;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		try{
		text=getIntent().getExtras().getString(Intent.EXTRA_TEXT,"NOIN");
		}catch(Exception e){
			e.printStackTrace();
		}
		if(text=="NOIN"){
			ClipboardManager clip=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
			text=clip.getText().toString();
		}
		
		TextView tv=(TextView)findViewById(R.id.mainTextView);

		tv.setText("Text to send:"+text);
		
		Thread th=new Thread(){
			public void run(){
			
				while(s==null|searching){
					for(int i=10;i<30;i++){
						try{
							s=new Socket("192.168.1."+i,1456);
							searching=false;
						}catch(IOException e){
							e.printStackTrace();
						}
					}
				}
				if(s!=null){
					runOnUiThread(new Runnable(){

							@Override
							public void run(){
								EditText ed=(EditText)findViewById(R.id.mainEditText);
								String ip=s.getInetAddress().getHostAddress();
								
								ed.setText(ip);
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
