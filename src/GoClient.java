package Billing;

import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ImageIcon;
import javax.swing.JToolBar;
import java.awt.event.MouseListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.Image;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.io.IOException;
import java.net.InetAddress;
import java.awt.Event;
import java.awt.Color;

public class GoClient extends JFrame implements Runnable, MouseListener {

    private Square square[][];
    private JPanel boardPanel;
    private JPanel panel1;
    private JPanel panel2;
    private JPanel panel3;
    private JButton Button_Pass;
    private JTextArea display;
    private JMenuBar menuBar;
    private JScrollPane scrollbar;
    private JMenu fileMenu;
    private JMenu helpMenu;
    private JMenuItem MenuBoardSize;
    private JToolBar toolBar;
    private JButton iconBack;
    private JButton iconForward;
    private Image piecePlayer1;
    private Image piecePlayer2;
    private String myStone;
    private Thread thread;
    private Socket connection;
    private DataInputStream input;
    private DataOutputStream output;
    private int boardSize;
    private boolean myTurn = false;

    public GoClient() {
        super("GO");

        boardPanel = new JPanel();
        panel1 = new JPanel();
        panel2 = new JPanel();
        panel3 = new JPanel();

        menuBar = new JMenuBar();
        fileMenu = new JMenu("Game");

        MenuBoardSize = new JMenuItem("BoardSize");

        Button_Pass = new JButton("Pass");

        iconBack = new JButton(new ImageIcon("Images/back.gif"));
        iconForward = new JButton(new ImageIcon("Images/forward.png"));
        toolBar = new JToolBar();

        start();
    }

    private void InitializeBoard() {

        square = new Square[boardSize][boardSize];
        boardPanel.setLayout(new GridLayout(boardSize, boardSize, 0, 0));


        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                square[row][col] = new Square();
                boardPanel.add(square[row][col]);

            }
        }

        panel2.add(boardPanel, BorderLayout.CENTER);
        getContentPane().add(panel2, BorderLayout.CENTER);

    }

    private void InitializeAction() {

        Button_Pass.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    output.writeInt(-99);
                    output.writeInt(-99);
                    Button_Pass.setEnabled(false);
                } catch (IOException a) {
                    a.printStackTrace();
                }
            }
        });

        MenuBoardSize.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String value = null;
                value = JOptionPane.showInputDialog("Please input a value");
                if (value != null && Integer.parseInt(value) > 2) {
                    try {
                        //output.writeUTF("play");
                        output.writeInt(Integer.parseInt(value));
                    } catch (IOException w) {
                        System.err.println(w + "");
                    }


                }
            }
        });

        iconBack.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    output.writeInt(-100);
                    output.writeInt(-100);
                    //iconBack.setEnabled(false);
                } catch (IOException a) {
                    a.printStackTrace();
                }
            }
        });

        iconForward.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    output.writeInt(-101);
                    output.writeInt(-101);
                    //iconForward.setEnabled(false);
                } catch (IOException a) {
                    a.printStackTrace();
                }
            }
        });

    }

    private void setBoardSize(int value) {
        boardSize = value;
    }

    private void InitializeProperties() {
        display = new JTextArea(2, 20);
        display.setBackground(Color.lightGray);
        scrollbar = new JScrollPane(display, scrollbar.VERTICAL_SCROLLBAR_ALWAYS,
                scrollbar.HORIZONTAL_SCROLLBAR_ALWAYS);
        display.setEditable(false);
        piecePlayer1 = Toolkit.getDefaultToolkit().getImage("Images/Red.gif");
        piecePlayer2 = Toolkit.getDefaultToolkit().getImage("Images/Black.gif");

        fileMenu.setMnemonic('F');
        fileMenu.add(MenuBoardSize);
        menuBar.add(fileMenu);
        this.setJMenuBar(menuBar);

        toolBar.setFloatable(false);
        toolBar.add(iconBack);
        toolBar.add(iconForward);

        panel3.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel3.add(toolBar);
        panel1.setLayout(new BorderLayout());
        panel1.add(scrollbar, BorderLayout.CENTER);
        panel1.add(Button_Pass, BorderLayout.NORTH);
        getContentPane().add(panel3, BorderLayout.NORTH);
        getContentPane().add(panel1, BorderLayout.WEST);

    }

    private void Move_RepaintBoard() {
        boolean boardClicked = false;

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                if (square[row][col].getMouseClicked() == true) {
                    System.out.println("click" + row);
                    try {
                        output.writeInt(row);
                        output.writeInt(col);
                        System.out.println(row + " " + col);
                        boardClicked = true;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    square[row][col].setSquareClicked(false);
                }

            }

        }

    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {


        Move_RepaintBoard();
        System.out.println("halo");

    }

    public void start() {

        try {
            System.out.println(InetAddress.getLocalHost());
            connection = new Socket("192.168.0.1", 999);
            input = new DataInputStream(connection.getInputStream());
            output = new DataOutputStream(connection.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }



        thread = new Thread(this);
        thread.start();
    }

    private void setListener() {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                square[row][col].addMouseListener(this);
            }
        }
    }

    private void setBoard() {
        pack();
        setSize(700, 500);
        setVisible(true);
    }

    private void RemoveComponent() {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                boardPanel.remove(square[row][col]);

            }
        }
        //panel2.remove(boardPanel);
    }

    private void EnabledStone() {

        if (myStone.equals("Red")) {
            setEnabled(true);
            myTurn = true;
        } else {
            setEnabled(false);
            myTurn = false;
        }
    }

    public void run() {

        try {
            //get board size first
            this.setBoardSize(input.readInt());
            System.out.println(boardSize);
            InitializeProperties();
            InitializeBoard();
            InitializeAction();
            setBoard();
            setListener();

            myStone = input.readUTF();
            setTitle(" Your Stone is" + myStone);
            System.out.println(myTurn);
            EnabledStone();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String a = "";
        boolean play = true;
        while (play) {
            while (true) {

                try {

                    System.out.println("minta input");
                    String s = input.readUTF();
                    System.out.println(s);
                    if (s.equals("Game")) {
                        a = input.readUTF();
                        break;
                    } else {
                        processMessage(s);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("" + e);
                    //System.exit(1);
                }
            }
            setEnabled(false);
            JOptionPane.showMessageDialog(null, "You are " + a, "information", JOptionPane.INFORMATION_MESSAGE);
            if (a.equals("win") || (a.equals("draw") && myStone.equals("Red"))) {
                JOptionPane.showMessageDialog(null, "if you want play again, you can input at the menu", "information", JOptionPane.INFORMATION_MESSAGE);
                setEnabled(true);
            } else if (a.equals("lose")) {
                JOptionPane.showMessageDialog(null, "if you want play again, you must wait the winning player", "information", JOptionPane.INFORMATION_MESSAGE);
            }

            try {
                System.out.println("tunggu");
                int temp = input.readInt();
                //menghapus komponen square
                RemoveComponent();
                System.out.println("halo00" + temp);
                this.setBoardSize(temp);
            } catch (IOException io) {
                System.err.println(io + "");
            }

            InitializeBoard();
            setListener();
            setVisible(false);
            setVisible(true);
            setEnabled(true);
            Button_Pass.setEnabled(true);
            EnabledStone();

            //play=true;
        }
    }

    private void DrawBoard() {
        try {
            for (int row = 0; row < boardSize; row++) {
                for (int col = 0; col < boardSize; col++) {
                    switch (input.readInt()) {
                        case 0:
                            square[row][col].setMark(null);
                            break;
                        case 1:
                            square[row][col].setMark(piecePlayer1);
                            break;
                        case 2:
                            square[row][col].setMark(piecePlayer2);
                            break;
                    }

                    square[row][col].repaint();
                }
            }
        } catch (IOException e) {
            System.err.println("" + e);
        }


    }

    private void processMessage(String s) {
        System.out.println(s);
        if (s.equals("Valid Move")) {

            display.setText("Valid \n waitng opponent move");
            DrawBoard();
            setEnabled(false);
            display.setEnabled(true);


        } else if (s.equals("Invalid Move  try again \n")) {
            display.setText(s + "\n");
        } else if (s.equals("Opponent moved")) {
            DrawBoard();
            display.setText("Opponent moved your turn\n");
            setEnabled(true);
            Button_Pass.setEnabled(true);
            //iconForward.setEnabled(true);
            //iconBack.setEnabled(true);
        } else if (s.equals("Your Opponent Undo") || s.equals("Your Opponent Redo")) {
            display.setText(s + "\n");
            DrawBoard();
        } else {
            display.setText(s + "\n");
        }
    }

    public static void main(String[] args) {

        GoClient client = new GoClient();
    }
}
