package Billing;

import java.net.ServerSocket;
import java.io.IOException;	
import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class CentralServer extends Thread {
	private ServerSocket server;
	private BillingServer[] billingServerList;
	
	//banyak komputer yang akan terhubung
	private int BanyakComputer;
	private int TarifPerJam;
	
	public CentralServer(int Computer,int port,int tarif){
		try{
			server= new ServerSocket(port);	
		}
		catch(IOException e){
			e.printStackTrace();
			System.exit(1);
		}
		
		billingServerList = new BillingServer[Computer];
		this.BanyakComputer=Computer;
		this.TarifPerJam=tarif;
		
		System.out.println("Waiting for client");
		
		this.execute();
		this.start();
	}
	
	public int getBanyakComputer(){
		return BanyakComputer;
	}
	
	
	//menuggu sampai semua client terhubung lalu memulai thread yang menagani client  tersebut
	private void execute(){
		for(int i=0; i<billingServerList.length; i++){
			try{
				billingServerList[i] =new BillingServer(server.accept(),this.TarifPerJam,new Day(0,0,0));
				System.out.println("client found");
				billingServerList[i].start();
			}
			catch (IOException e){
				e.printStackTrace();
				System.exit(1);
			}	
		}
	}
	
	public BillingServer getBillingServer(int ComputerNumber){
		return this.billingServerList[ComputerNumber];
	}
	//jika suatu server terputus maka akan menunggu sampai client tersebut hidup kembali lalu memulai kembali servis untuk klien tersebut
	public void run(){
		while(true){
			for(int i=0; i<billingServerList.length; i++){
				if(billingServerList[i].isClosed()){
					try{
						Day dayStarted = billingServerList[i].getStoppedTime();
						System.out.println("Computer"+i+"is closed at time"+dayStarted.toString());
						//billingServerList[i]=null;
						billingServerList[i] =new BillingServer(server.accept(),this.TarifPerJam, dayStarted);
						System.out.println("client found");
						billingServerList[i].start();
					}
					catch (IOException e){
						e.printStackTrace();
						System.exit(1);
					}
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