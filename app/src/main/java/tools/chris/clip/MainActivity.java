package tools.chris.clip;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.*;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;
import android.widget.EditText;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import android.view.View;
import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;

import android.content.ClipboardManager;
import android.widget.Toast;

public class MainActivity extends Activity 
{


	public static final String prefName="user";
	public static final String ipkeyName ="IP";
	public static final String CNULL="NOIN";
	Socket s=null;
	String text=CNULL;
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
			Log.v("text","from clipboard");
		}
		
		TextView tv=(TextView)findViewById(R.id.mainTextView);

		tv.setText("Text to send:"+text);
		
		SharedPreferences sh=getSharedPreferences(prefName,0);
		if(sh.contains(ipkeyName)){
			EditText ed=(EditText)findViewById(R.id.mainEditText);
			ed.setText(sh.getString(ipkeyName,"192.168.1."));
		}

		Thread server=new Thread(new Runnable(){

			@Override
			public void run() {
				try{
					ServerSocket ss=new ServerSocket(1458);
					InetAddress inet= null;
					try {
						inet = InetAddress.getLocalHost();


					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
					final String MYIP=inet.getHostAddress();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							TextView serverTV=(TextView)findViewById(R.id.mainServer);
							serverTV.setText("IP Address:"+getIpAddress(MainActivity.this));
						}
					});
					Socket s=ss.accept();
					System.out.println("CONNECTED");
					InputStream in=s.getInputStream();
					int len=0;
					for(int i=0;i<4;i++){
						int recvbyte=in.read();
						len=len|recvbyte<<(8*i);
					}
					StringBuilder sb=new StringBuilder();
					for(int i=0;i<len;i++){
						int hibyte=in.read();
						int lowbyte=in.read();
						int c=hibyte<<8|lowbyte;
						char ch=(char)c;
						sb.append(ch);
					}
					final String text=sb.toString();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							recieved(text);
						}
					});

				}catch(IOException e){
					e.printStackTrace();
				}
			}
		});
		server.start();
		//getActionBar().setSubtitle("Listenning on port 1458...");



    }


	public static String getIpAddress(Context context) {
		WifiManager wifiManager = (WifiManager) context.getApplicationContext()
				.getSystemService(WIFI_SERVICE);

		String ipAddress = intToInetAddress(wifiManager.getDhcpInfo().ipAddress).toString();

		ipAddress = ipAddress.substring(1);

		return ipAddress;
	}

	public static InetAddress intToInetAddress(int hostAddress) {
		byte[] addressBytes = { (byte)(0xff & hostAddress),
				(byte)(0xff & (hostAddress >> 8)),
				(byte)(0xff & (hostAddress >> 16)),
				(byte)(0xff & (hostAddress >> 24)) };

		try {
			return InetAddress.getByAddress(addressBytes);
		} catch (UnknownHostException e) {
			throw new AssertionError();
		}
	}

	private void recieved(final String text) {
    	AlertDialog.Builder al=new AlertDialog.Builder(this);
    	al.setTitle("Text Recieved:");
    	al.setMessage(text);
    	al.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.dismiss();
			}
		});
    	al.setNeutralButton("Copy", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				ClipboardManager clip=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
				clip.setText(text);
				//dialogInterface.dismiss();
			}
		});
    	AlertDialog ad=al.create();
    	ad.show();
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
					int len=text.length();
					for(int i=0;i<4;i+=1){
						os.write(len>>(8*i)&0xff);
					}

					for(char c:text.toCharArray()){
						os.write(c>>8);
						os.write(c&0xff);
					}
					os.flush();
					os.close();
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
