import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

import javax.swing.*;

class OthelloClient extends JFrame {
    final static int BLACK = 1;
    final static int WHITE = -1;

    private JTextField tf;
    private JTextArea ta;
    private JLabel label;
    private OthelloCanvas canvas;

    Socket s;
    boolean put_flag = true;
    String senString = "";

    public OthelloClient(String localhost) {
        this.setSize(640, 320);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                /* ウインドウが閉じられた時の処理 */
                System.exit(0);
            }
        });
        tf = new JTextField(40);
        tf.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                /* テキストフィールドに文字が入力された時の処理 */
                if (tf.getText().equals("quit")) {
                    System.exit(0);
                }

                // ここに送信部分追加

                System.out.println(tf.getText());
                tf.setText(""); // テキストフィールドの文字を初期化
            }
        });
        ta = new JTextArea(18, 40);
        ta.setLineWrap(true);
        ta.setEditable(false);
        label = new JLabel();

        JPanel mainp = (JPanel) getContentPane();
        JPanel ep = new JPanel();
        JPanel wp = new JPanel();
        canvas = new OthelloCanvas(localhost);
        GridLayout gl = new GridLayout(1, 2);
        gl.setHgap(5);
        mainp.setLayout(gl);
        ep.setLayout(new BorderLayout());
        ep.add(new JScrollPane(ta), BorderLayout.CENTER);
        ep.add(tf, BorderLayout.SOUTH);
        wp.setLayout(new BorderLayout());
        wp.add(label, BorderLayout.SOUTH);
        wp.add(canvas, BorderLayout.CENTER);
        mainp.add(wp);
        mainp.add(ep);
        this.setVisible(true);

        // 受信部分追加
        while (true) {
            boolean flag = true;

            InputStream sIn;
            OutputStream sOut;
            BufferedReader br;
            PrintWriter pw;
            String str1;
            String my_color_str = "";

            String[] strs1;
            String[] boardstr = new String[65];

            try {
                s = new Socket(localhost, 50001);
                sIn = s.getInputStream();
                sOut = s.getOutputStream();
                br = new BufferedReader(new InputStreamReader(sIn));
                pw = new PrintWriter(new OutputStreamWriter(sOut), true);
                int x;
                int y;
                String mycolor = "";

                while (flag) {
                    str1 = br.readLine();
                    System.out.println(str1);
                    strs1 = str1.split(" ");

                    switch (strs1[0]) {
                        case "START":
                            System.out.println(str1);
                            mycolor = strs1[1];
                            Font font = new Font("Serif", Font.PLAIN, 60);
                            ta.setFont(font);
                            if(mycolor.equals("1")) {
                                my_color_str = "あなたの色は黒です\n";
                            }
                            else {
                                my_color_str = "あなたの色は白です\n";
                            }
                            senString = "NICK YOU";
                            pw.println(senString);
                            break;

                        case "BOARD":
                            // System.out.println(strs1[0]);
                            boardstr = strs1;
                            byte[][] board = new byte[8][8];
                            for (int i = 0; i < 8; i++) {
                                for (int j = 0; j < 8; j++) {
                                    board[i][j] = Byte.parseByte(boardstr[i * 8 + j + 1]);
                                }
                            }
                            canvas.board = board;
                            canvas.repaint();
                            break;

                        case "TURN":
                            System.out.println(str1);
                            if (strs1[1].equals(mycolor)) {
                                ta.setText(my_color_str + "あなたのターンです\n");
                                while (!canvas.put_flag) {
                                    // System.out.println("待機中");
                                    // System.out.println(canvas.put_flag);
                                }
                                senString = canvas.senString;
                                System.out.println(senString);
                                pw.println(senString);
                                canvas.put_flag = false;
                            }
                            else {
                                ta.setText(my_color_str + "相手のターンです\n");
                            }
                            break;

                        case "ERROR":
                            System.out.println(str1);
                            if (strs1[1].equals("2") || strs1[1].equals("1")) {
                                while (!canvas.put_flag) {
                                    // System.out.println("待機中");
                                    // System.out.println(canvas.put_flag);
                                }
                                senString = canvas.senString;
                                System.out.println(senString);
                                pw.println(senString);
                                canvas.put_flag = false;
                            }
                            break;

                        case "END":
                            System.out.println(str1);
                            ta.append(str1 + "\n");
                            ta.append("もう一度遊ぶ場合はオセロ盤をクリックしてください\n");
                            canvas.end_flag = false;
                            while (!canvas.end_flag) {
                                // System.out.println("待機中");
                            }
                            ta.setText("");
                            flag = false;
                            canvas.board = new byte[][] {
                                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                                    { 0, 0, 0, 1, -1, 0, 0, 0 },
                                    { 0, 0, 0, -1, 1, 0, 0, 0 },
                                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                                    { 0, 0, 0, 0, 0, 0, 0, 0 },
                                    { 0, 0, 0, 0, 0, 0, 0, 0 }
                            };
                            break;

                        case "CLOSE":
                            // System.out.println(str1);
                            flag = false;
                            break;

                        default:
                            System.out.println("エラー");

                    }
                }
            } catch (IOException e) {
                System.err.println("Caught IOException");
                System.exit(1);

            }
        }
    }

    public static void main(String args[]) {
        new OthelloClient(args[0]);
    }
}

class OthelloCanvas extends JPanel {
    private final static int startx = 20;
    private final static int starty = 10;
    private final static int gap = 160;
    byte[][] board = {
            { 0, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 1, -1, 0, 0, 0 },
            { 0, 0, 0, -1, 1, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 0, 0, 0 }
    }; // サンプルデータ
    volatile String senString = "";
    volatile boolean put_flag = false;
    volatile boolean end_flag = false;

    public OthelloCanvas(String localhost) {
        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                /* 盤目上でマウスがクリックされた時の処理 */
                Point p = e.getPoint();
                System.out.println("" + p); // デバッグ用表示

                // ここに送信部分追加

                int x;
                int y;
                x = (p.x - startx) / 160;
                y = (p.y - starty) / 160;

                senString = "PUT " + x + " " + y;
                System.out.println(senString);
                put_flag = true;
                end_flag = true;

            }
        });
    }

    public void paintComponent(Graphics g) {
        g.setColor(new Color(0, 180, 0));
        g.fillRect(startx, starty, gap * 8, gap * 8);

        g.setColor(Color.BLACK);
        for (int i = 0; i < 9; i++) {
            g.drawLine(startx, starty + i * gap, startx + 8 * gap, starty + i * gap);
            g.drawLine(startx + i * gap, starty, startx + i * gap, starty + 8 * gap);
        }
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == OthelloClient.BLACK) {
                    g.setColor(Color.BLACK);
                    g.fillOval(startx + gap * i, starty + gap * j, gap, gap);
                } else if (board[i][j] == OthelloClient.WHITE) {
                    g.setColor(Color.WHITE);
                    g.fillOval(startx + gap * i, starty + gap * j, gap, gap);
                }
            }
        }
    }
}