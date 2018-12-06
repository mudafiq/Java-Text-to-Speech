package Billing;

import javax.swing.*;

public class CentralServerGui extends JFrame  {
	final String LogFile= "Server.log";
	
	private CentralServer central;
	
	public CentralServerGui(){	
		super("Central Server");
		setServer();
		ShowClientSymbol();
		setVisible(true);
		//Thread thread = new Thread(this);
		//thread.start();
	}
	
	private void setServer(){
		ServerSettingDialog dial = new ServerSettingDialog(this);
		if(dial.getBanyakKlien()!=0 && dial.getPortNumber()!=0 &&  dial.getTarif()!=0 ){
			MessageDialog dialog = new MessageDialog("           Waiting All Client");
			dialog.setVisible(true);
			central = new 		CentralServer(dial.getBanyakKlien(),dial.getPortNumber(),dial.getTarif());
			dialog.setVisible(false);
			dialog=null;
		}
		dial=null;
	}
	
	private void ShowClientSymbol(){
		int banyakComputer=central.getBanyakComputer();
		int half=banyakComputer/2;
		getContentPane().setLayout(new java.awt.GridLayout(banyakComputer-half,half,20,20));
		for(int i=0; i<banyakComputer; i++)
			getContentPane().add(new BillingServerPanel(i, central));
		if(banyakComputer<=8)
			setSize(200,200);
		else
			setSize(half*30,half*30);
	}
	
	public static void main(String [] args){

		try{
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		}
		catch (ClassNotFoundException e) {
		}
		catch (InstantiationException e) {
		}
		catch (IllegalAccessException e) {
		}
		catch (UnsupportedLookAndFeelException e) {
		}
		new CentralServerGui();
	}
}


class BillingServerPanel extends JPanel{
	
	private JLabel label;
	private JPopupMenu popup;
	//private CentralServer centralServer;
	//private int ComputerNumber;
	
	public BillingServerPanel(final int number, final CentralServer c){
		label =new JLabel(c.getBillingServer(number).getHostName());
		this.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));
		this.add(label);
		this.setBackground(java.awt.SystemColor.desktop);
		
		 popup = new JPopupMenu();
    		 JMenuItem menuItem = new JMenuItem("Start Timer");
    		 menuItem.addActionListener(new java.awt.event.ActionListener(){
		 	public void actionPerformed(java.awt.event.ActionEvent e){
				c.getBillingServer(number).UnlockAndStartTimer();
			}
		 });
    		 popup.add(menuItem);
    		 menuItem = new JMenuItem("Stop Timer");
    		 menuItem.addActionListener(new java.awt.event.ActionListener(){
		 	public void actionPerformed(java.awt.event.ActionEvent e){
				c.getBillingServer(number).LockAndStopTimer();
			}
		 });
    		 popup.add(menuItem);
		 menuItem = new JMenuItem("Show Cost");
    		 menuItem.addActionListener(new java.awt.event.ActionListener(){
		 	public void actionPerformed(java.awt.event.ActionEvent e){
				if(c.getBillingServer(number).isLocked())
					JOptionPane.showMessageDialog(null,"Customer Must Pay "+c.getBillingServer(number).getCost());
			}
		 });
    		 popup.add(menuItem);

    		//Add listener to components that can bring up popup menus.
    		
    		this.addMouseListener(new java.awt.event.MouseAdapter(){
			public void mousePressed(java.awt.event.MouseEvent e) {
        			ShowPopup(e);
    			}

    			public void mouseReleased(java.awt.event.MouseEvent e) {
        			ShowPopup(e);
    			}

    			private void ShowPopup(java.awt.event.MouseEvent e) {
        			if (e.isPopupTrigger()) {
       					popup.show(e.getComponent(),e.getX(), e.getY());
        			}	
    			}
		});
		
		this.setSize(30,30);
	}	
}


