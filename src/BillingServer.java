//kelas yang menangani setiap servis dari sebuah client, setiap billing server akan memegang satu client
package Billing;

import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BillingServer extends Thread{
	private float TarifPerDetik;
	private Socket connection;
	private String host;
	protected GraphicalStopWatch timer;
	protected DataOutputStream output;
	protected DataInputStream input;
	private boolean locked=true;
	//status koneksi klien apakah sudah terputus ataupun tidak
	private boolean closed=false;
	//waktu terakhir client saat server di tutup
	private Day timeStarted;
		
	public BillingServer(Socket s, int harga, Day start){
		connection=s;
		host=s.getLocalAddress().getHostName();
		timeStarted=start;
		try{
			input= new DataInputStream (connection.getInputStream ());
			output=new DataOutputStream (connection.getOutputStream ());
		}
		catch(IOException e){
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("start"+timeStarted);
		//mengambil data terakhir client jika server suatu saat mati 
		this.getClientLog();
		
		System.out.println("start"+timeStarted);
		timer= new GraphicalStopWatch(timeStarted);
		this.TarifPerDetik=(float)harga/3600;
		
		try{
			//megirimkan waktu terakhir client
			output.flush();
			output.writeInt(Day.parseInt(timeStarted));
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	
	//jika server mati sedang client masih hidup maka mengambil log dari client
	private void getClientLog(){
		try{
			int hours=input.readInt();
			System.out.println("get "+hours+"from client");
			int minutes=input.readInt();
			System.out.println("get "+minutes+"from client");
			int seconds=input.readInt();
			System.out.println("get "+seconds+"from client");
			if(hours !=0  ||  minutes !=0 || seconds !=0)
				this.timeStarted=new Day(hours,minutes,seconds);
				
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public String getHostName(){
		return this.host;
	}
	
	//mengrimkan status ke client 0 mati sedang 1 hidup
	public void sendStatus(){
		if(locked)
			this.sendMessage(0);
		else
			this.sendMessage(1);
	}
	
	public void setStatus(boolean b){
		locked=b;
	}
	
	public boolean isLocked(){
		return locked;
	}
	
	//send message to client connect with this server, if client's status "lock" send 0
	//otherwise send 1
	private void sendMessage(int m){
		System.out.println("send message"+m+" to client");
		try{
			output.flush();
			output.writeInt(m);
		}catch(IOException e){
			e.printStackTrace();
		}
	} 
	
	public void LockAndStopTimer(){
		this.sendMessage(0);
		timer.stopTimer();
		locked=true;
	}
	
	
	public void UnlockAndStartTimer(){
		if(locked){
			this.sendMessage(1);
			timer.startTimer();
			locked=false;
		}
	}
	
	
	//mendapatkan tarif yang harus dibayar customer berdasarkan timer
	public double getCost(){
		Day d = this.getStoppedTime();
		int total=3600*d.Hours+60*d.Minutes+d.Seconds;
		return Math.ceil(total*this.TarifPerDetik);
	} 
	
	
	//mendapatkan waktu saat timer dihentikan
	public Day getStoppedTime(){
		return timer.getStopWatch().getStoppedTime();
	}
	
	public boolean isClosed(){
		return closed;
	}
	
	public void run(){
		while(true){
			//menunngu dikirimnya karakter r dari klien jika mendapat response maka
			//akan mengrimkan status clien tersebut
			try{
				char c= input.readChar();
				if(c=='r'){
					System.out.println(" read r from client");
					this.sendStatus();
				}
			}catch(IOException e){
				e.printStackTrace();
				this.closed=true;
				timer.stopTimer();
					
			}
			finally{	
				try{
					this.connection.close();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
			
			try{
				Thread.sleep(1000);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
	}
	
}