//kelas untuk client yang akan mencari server
package Billing;

import java.net.Socket;
import java.net.UnknownHostException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.EOFException;

public class BillingClient extends Thread {

    private Socket connection;
    private DataInputStream input;
    private DataOutputStream output;
    private String serverAddress;
    private int port;
    private WindowTimer timer;
    private Day TimeStarted = null;
    //nama file tempat pencatatan Data terakhir client jika koneksi dengan server tiba tiba terputus
    protected String CloseLog = ".CloseLog.log";
    private boolean connect = false;

    public BillingClient(String s, int p) {

        //saat client sedang mencari koneksi ke server maka komputer akan di lock dahulu
		/*Runtime r = Runtime.getRuntime();
        try{
        r.exec("xlock");
        }
        catch(IOException e){
        e.printStackTrace();
        System.exit(1);
        }*/

        System.out.println("lockScreen");

        while (connection == null) {
            try {
                this.connection = new Socket(s, p);
            } catch (UnknownHostException e) {
                //e.printStackTrace();
                //System.exit(1);
            } catch (IOException e) {
                //e.printStackTrace();
                //System.exit(1);
            }
        }

        //setelah server ditemukan maka komputer akan diunlock dan timer dihentikan
		/*Runtime r = Runtime.getRuntime();
        //jika di os linux
        try{
        r.exec("killall xlock");
        }
        catch(IOException e){
        e.printStackTrace();
        }*/
        System.out.println("unlockScreen");

        this.serverAddress = s;
        this.port = p;
        try {
            input = new DataInputStream(connection.getInputStream());
            output = new DataOutputStream(connection.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        checkLogFile();
        //send message to server in order to get client status
        //just for try in localhost
        //for test in real network uncooment this
		/*
        try{
        System.out.println("send r to server");
        output.flush();
        output.writeChar('r');
        }catch(IOException e){
        e.printStackTrace();
        System.exit(1);
        }*/
        this.start();
        //wait until get time started
        while (this.TimeStarted == null);
        timer = new WindowTimer(this.TimeStarted);
    }

    //check jika file CloseLog.log ada maka mengirim data terakhir client ke server
    //hal ini dilakukan untuk mengantisipasi server yang tiba-tiba mati
    private void checkLogFile() {
        int hours = 0;
        int minutes = 0;
        int seconds = 0;

        try {
            //membaca dahulu log
            java.io.FileInputStream fileinput = new java.io.FileInputStream(this.CloseLog);
            java.io.DataInputStream datainput = new java.io.DataInputStream(fileinput);
            hours = datainput.readInt();
            minutes = datainput.readInt();
            seconds = datainput.readInt();
            fileinput.close();
            datainput = null;
            fileinput = null;
            System.out.println("read" + hours + " " + minutes + " " + seconds);

            //clear file CloseLog
            this.clearLog();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //mengirim data dari log ke socket
        try {
            output.flush();
            output.writeInt(hours);
            output.flush();
            output.writeInt(minutes);
            output.flush();
            output.writeInt(seconds);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void lockScreen() {
        //jika di os linux
        Runtime r = Runtime.getRuntime();
        try {
            r.exec("xlock");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        //start timer too
        timer.stopTimer();
        clearLog();
    }

    private void unlockScreen() {
        Runtime r = Runtime.getRuntime();
        //jika di os linux
        try {
            r.exec("killall xlock");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        //stop timer too
        timer.startTimer();
    }

    public void run() {

        int i = 0;
        //menunngu sampai mendapat kiriman data tentang waktu awal client
        try {
            int timeStarted = input.readInt();
            this.TimeStarted = Day.IntToDay(timeStarted);
            System.out.println("startted time at client at" + this.TimeStarted.toString());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        while (true) {
            try {
                int msg = input.readInt();
                System.out.println("read");
                if (msg == 1) {
                    System.out.println("read input 1");
                    this.unlockScreen();
                    connect = true;
                } else if (msg == 0) {
                    System.out.println("read input 0");
                    this.lockScreen();
                    connect = false;
                }
            } catch (EOFException oef) {
                System.out.println("eof");
                oef.printStackTrace();
                if (connection.isConnected()) {
                    /*try{
                    Runtime.getRuntime().exec("poweroff");
                    }catch(IOException ioe){
                    ioe.printStackTrace();
                    }*/
                    //saat koneksi server putus maka komputer akan otomatis mati
                    //dan terlebih dahulu menulis waktu terakhirnya pada sebuah file
                    writeLog();
                    System.out.println("poweroff");
                    System.exit(1);
                }


            } catch (IOException e) {
                System.out.println("eof");
                e.printStackTrace();
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //secara periodik mencatat log file untuk mengantisipasi server dan klien mati secara bersamaan
            if (i == 60000) {
                writeLog();
                i = 0;
            }
            if (connect) {
                i++;
            }
        }
    }

    //menulis log ke komputer klien untuk mencatat data data klien
    private void writeLog() {
        try {
            java.io.FileOutputStream file = new java.io.FileOutputStream(this.CloseLog);
            java.io.DataOutputStream dataoutput = new java.io.DataOutputStream(file);
            Day d = this.timer.getGraphicalStopWatch().getStopWatch().getTime();
            System.out.println("write " + d.Hours + " " + d.Minutes + " " + d.Seconds);
            dataoutput.writeInt(d.Hours);
            dataoutput.writeInt(d.Minutes);
            dataoutput.writeInt(d.Seconds);
            dataoutput.flush();
            file.close();
            dataoutput = null;
            file = null;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("gagal dalam writeLog");
        }
    }

    //menghapu log dengan menulis 0 0 0 ke dalam file
    private void clearLog() {
        try {
            java.io.FileOutputStream fileoutput = new java.io.FileOutputStream(this.CloseLog);
            java.io.DataOutputStream dataoutput = new java.io.DataOutputStream(fileoutput);
            dataoutput.writeInt(0);
            dataoutput.writeInt(0);
            dataoutput.writeInt(0);
            dataoutput.flush();
            fileoutput.close();
            dataoutput = null;
            fileoutput = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            BillingClient b = new BillingClient(args[0], Integer.parseInt(args[1]));
        } catch (Exception e) {
            System.out.println("Penggunaan java Billing.BillingClient <alamat server> <port>");
        }
    }
}

class MessageDialog extends javax.swing.JDialog {

    public MessageDialog(String msg) {
        this.getContentPane().setLayout(new java.awt.BorderLayout());
        this.getContentPane().add(new javax.swing.JLabel(msg), java.awt.BorderLayout.CENTER);
        this.setResizable(false);
        this.setLocation(200, 200);
        this.setSize(200, 100);
    }
}
