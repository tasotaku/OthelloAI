import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

public class MoveRef2 {
    static MoveRef2 ref = new MoveRef2();
    long move = 0b00000000;
    long ownBoard = 0b00000000;
    long enemyBoard = 0b00000000;
    static String my_color;
    static String enemy_color;
    static int hw = 8;
    static int n_line = 6561;

    public static long getMoves(final long own, final long enemy) {
        long topBottomMask = 0b1111111111111111111111111111111111111111111111111111111111111111L;
        long leftRightMask = 0b0111111001111110011111100111111001111110011111100111111001111110L;
        long emptyCells = getEmptyCells(own, enemy);
        // top
        long m = getMovesL(own, enemy, emptyCells, topBottomMask, 8);
        // top right
        m |= getMovesL(own, enemy, emptyCells, leftRightMask, 7);
        // right
        m |= getMovesR(own, enemy, emptyCells, leftRightMask, 1);
        // bottom right
        m |= getMovesR(own, enemy, emptyCells, leftRightMask, 9);
        // bottom
        m |= getMovesR(own, enemy, emptyCells, topBottomMask, 8);
        // bottom left
        m |= getMovesR(own, enemy, emptyCells, leftRightMask, 7);
        // left
        m |= getMovesL(own, enemy, emptyCells, leftRightMask, 1);
        // top left
        return m | getMovesL(own, enemy, emptyCells, leftRightMask, 9);
    }

    public static long getEmptyCells(final long own, final long enemy) {
        return ~(own | enemy);
    }

    private static long getMovesR(final long own, final long enemy, final long emptyCells, final long mask,
            final int offset) {
        long e = enemy & mask;
        long m = (own << offset) & e;
        m |= (m << offset) & e;
        m |= (m << offset) & e;
        m |= (m << offset) & e;
        m |= (m << offset) & e;
        m |= (m << offset) & e;
        return (m << offset) & emptyCells;
    }

    private static long getMovesL(final long own, final long enemy, final long emptyCells, final long mask,
            final int offset) {
        long e = enemy & mask;
        long m = (own >>> offset) & e;
        m |= (m >>> offset) & e;
        m |= (m >>> offset) & e;
        m |= (m >>> offset) & e;
        m |= (m >>> offset) & e;
        m |= (m >>> offset) & e;
        return (m >>> offset) & emptyCells;
    }

    public static long selectMove(final long moveCandidates) {
        return Long.lowestOneBit(moveCandidates);
    }

    public static long getReverses(final long own, final long enemy, final long move) {
        long topBottomMask = 0b1111111111111111111111111111111111111111111111111111111111111111L;
        long leftRightMask = 0b0111111001111110011111100111111001111110011111100111111001111110L;
        // top
        long m = getReversesL(own, enemy, move, topBottomMask, 8);
        // top right
        m |= getReversesL(own, enemy, move, leftRightMask, 7);
        // right
        m |= getReversesR(own, enemy, move, leftRightMask, 1);
        // bottom right
        m |= getReversesR(own, enemy, move, leftRightMask, 9);
        // bottom
        m |= getReversesR(own, enemy, move, topBottomMask, 8);
        // bottom left
        m |= getReversesR(own, enemy, move, leftRightMask, 7);
        // left
        m |= getReversesL(own, enemy, move, leftRightMask, 1);
        // top left
        return m | getReversesL(own, enemy, move, leftRightMask, 9);
    }

    private static long getReversesR(final long own, final long enemy, final long move, final long mask,
            final int offset) {
        long e = enemy & mask;
        long m = (move << offset) & e;
        m |= (m << offset) & e;
        m |= (m << offset) & e;
        m |= (m << offset) & e;
        m |= (m << offset) & e;
        m |= (m << offset) & e;
        long o = (own >>> offset) & e;
        o |= (o >>> offset) & e;
        o |= (o >>> offset) & e;
        o |= (o >>> offset) & e;
        o |= (o >>> offset) & e;
        o |= (o >>> offset) & e;
        return m & o;
    }

    private static long getReversesL(final long own, final long enemy, final long move, final long mask,
            final int offset) {
        long e = enemy & mask;
        long m = (move >>> offset) & e;
        m |= (m >>> offset) & e;
        m |= (m >>> offset) & e;
        m |= (m >>> offset) & e;
        m |= (m >>> offset) & e;
        m |= (m >>> offset) & e;
        long o = (own << offset) & e;
        o |= (o << offset) & e;
        o |= (o << offset) & e;
        o |= (o << offset) & e;
        o |= (o << offset) & e;
        o |= (o << offset) & e;
        return m & o;
    }

    static int[] cell_weight = {
            30, -12, 0, -1, -1, 0, -12, 30,
            -12, -15, -3, -3, -3, -3, -15, -12,
            0, -3, 0, -1, -1, 0, -3, 0,
            -1, -3, -1, -1, -1, -1, -3, -1,
            -1, -3, -1, -1, -1, -1, -3, -1,
            0, -3, 0, -1, -1, 0, -3, 0,
            -12, -15, -3, -3, -3, -3, -15, -12,
            30, -12, 0, -1, -1, 0, -12, 30
    };

    // 盤位置の評価
    static int evalBP(final long ownBoard, final long enemyBoard) {
        int res = 0;
        boolean zero_stone = true;
        for (int i = 0; i < 64; ++i) {
            if (((ownBoard >> i) & 1) == 1) {
                res += cell_weight[i];
                zero_stone = false;
            }
        }
        if (zero_stone) {
            res -= 1000;
        }
        for (int i = 0; i < 64; ++i) {
            if (((enemyBoard >> i) & 1) == 1) {
                res -= cell_weight[i];
            }
        }

        return res;
    }

    // 確定石(辺のみ)(近似的に)の評価
    static int evalFS(final long ownBoard, final long enemyBoard) {
        int res = 0;
        int tres = 0;
        int ures = 0;
        int lres = 0;
        int rres = 0;

        // 辺ごとに考える
        boolean tl, tr, ul, ur;

        if (((ownBoard >> 63) & 1) == 1) {
            tl = true;
            res++;
        } else {
            tl = false;
        }
        if (((ownBoard >> 56) & 1) == 1) {
            tr = true;
            res++;
        } else {
            tr = false;
        }
        if (((ownBoard >> 7) & 1) == 1) {
            ul = true;
            res++;
        } else {
            ul = false;
        }
        if (((ownBoard >> 0) & 1) == 1) {
            ur = true;
            res++;
        } else {
            ur = false;
        }

        // 上の辺
        if (tl) {
            for (int i = 0; i < 6; i++) {
                if (((ownBoard >> 62 - i) & 1) == 1) {
                    tres++;
                } else {
                    break;
                }
            }
        }
        if (tr) {
            for (int i = 0; i < 6; i++) {
                if (((ownBoard >> 57 + i) & 1) == 1) {
                    tres++;
                } else {
                    break;
                }
            }
        }
        if (tres == 12) {
            tres = 6;
        }

        // 右の辺
        if (tr) {
            for (int i = 0; i < 6; i++) {
                if (((ownBoard >> 48 - (i * 8)) & 1) == 1) {
                    rres++;
                } else {
                    break;
                }
            }
        }
        if (ur) {
            for (int i = 0; i < 6; i++) {
                if (((ownBoard >> 8 + (i * 8)) & 1) == 1) {
                    rres++;
                } else {
                    break;
                }
            }
        }
        if (rres == 12) {
            rres = 6;
        }

        // 下の辺
        if (ul) {
            for (int i = 0; i < 6; i++) {
                if (((ownBoard >> 6 - i) & 1) == 1) {
                    ures++;
                } else {
                    break;
                }
            }
        }
        if (ur) {
            for (int i = 0; i < 6; i++) {
                if (((ownBoard >> 1 + i) & 1) == 1) {
                    ures++;
                } else {
                    break;
                }
            }
        }
        if (ures == 12) {
            ures = 6;
        }

        // 左の辺
        if (tl) {
            for (int i = 0; i < 6; i++) {
                if (((ownBoard >> 55 - (i * 8)) & 1) == 1) {
                    lres++;
                } else {
                    break;
                }
            }
        }
        if (ul) {
            for (int i = 0; i < 6; i++) {
                if (((ownBoard >> 15 + (i * 8)) & 1) == 1) {
                    lres++;
                } else {
                    break;
                }
            }
        }
        if (lres == 12) {
            lres = 6;
        }

        res += tres + ures + lres + rres;

        int enemy_res = 0;
        int enemy_tres = 0;
        int enemy_ures = 0;
        int enemy_lres = 0;
        int enemy_rres = 0;

        boolean enemy_tl, enemy_tr, enemy_ul, enemy_ur;
        if (((enemyBoard >> 63) & 1) == 1) {
            enemy_tl = true;
            enemy_res++;
        } else {
            enemy_tl = false;
        }
        if (((enemyBoard >> 56) & 1) == 1) {
            enemy_tr = true;
            enemy_res++;
        } else {
            enemy_tr = false;
        }
        if (((enemyBoard >> 7) & 1) == 1) {
            enemy_ul = true;
            enemy_res++;
        } else {
            enemy_ul = false;
        }
        if (((enemyBoard >> 0) & 1) == 1) {
            enemy_ur = true;
            enemy_res++;
        } else {
            enemy_ur = false;
        }

        // 上の辺
        if (enemy_tl) {
            for (int i = 0; i < 6; i++) {
                if (((enemyBoard >> 62 - i) & 1) == 1) {
                    enemy_tres++;
                } else {
                    break;
                }
            }
        }
        if (enemy_tr) {
            for (int i = 0; i < 6; i++) {
                if (((enemyBoard >> 57 + i) & 1) == 1) {
                    enemy_tres++;
                } else {
                    break;
                }
            }
        }
        if (enemy_tres == 12) {
            enemy_tres = 6;
        }

        // 右の辺
        if (enemy_tr) {
            for (int i = 0; i < 6; i++) {
                if (((enemyBoard >> 48 - (i * 8)) & 1) == 1) {
                    enemy_rres++;
                } else {
                    break;
                }
            }
        }
        if (enemy_ur) {
            for (int i = 0; i < 6; i++) {
                if (((enemyBoard >> 8 + (i * 8)) & 1) == 1) {
                    enemy_rres++;
                } else {
                    break;
                }
            }
        }
        if (enemy_rres == 12) {
            enemy_rres = 6;
        }

        // 下の辺
        if (enemy_ul) {
            for (int i = 0; i < 6; i++) {
                if (((enemyBoard >> 6 - i) & 1) == 1) {
                    enemy_ures++;
                } else {
                    break;
                }
            }
        }
        if (enemy_ur) {
            for (int i = 0; i < 6; i++) {
                if (((enemyBoard >> 1 + i) & 1) == 1) {
                    enemy_ures++;
                } else {
                    break;
                }
            }
        }
        if (enemy_ures == 12) {
            enemy_ures = 6;
        }

        // 左の辺
        if (enemy_tl) {
            for (int i = 0; i < 6; i++) {
                if (((enemyBoard >> 55 - (i * 8)) & 1) == 1) {
                    enemy_lres++;
                } else {
                    break;
                }
            }
        }
        if (enemy_ul) {
            for (int i = 0; i < 6; i++) {
                if (((enemyBoard >> 15 + (i * 8)) & 1) == 1) {
                    enemy_lres++;
                } else {
                    break;
                }
            }
        }
        if (enemy_lres == 12) {
            enemy_lres = 6;
        }

        enemy_res += enemy_tres + enemy_ures + enemy_lres + enemy_rres;

        return res - enemy_res;
    }

    public static int evalCN(final long ownBoard, final long enemyBoard) {
        long mymove = getMoves(ownBoard, enemyBoard);
        int res = 0;

        for (int i = 0; i < 64; i++) {
            if (((mymove >> i) & 1) == 1) {
                res++;
            }
        }

        return res;
    }

    public static int evaluate(final long ownBoard, final long enemyBoard) {
        return evalBP(ownBoard, enemyBoard) + 35 * evalFS(ownBoard, enemyBoard) + 5 * (evalCN(ownBoard, enemyBoard));
    }

    public static long deleteMove(final long moves, final long move) {
        return moves ^ move;
    }

    public static int alphabetaPerfect(final long ownBoard, final long enemyBoard, final boolean alreadyPassed,
            final int limit, final int alpha, final int beta, final MoveRef2 ref) {
        if (limit == 0) {
            return evaluate(ownBoard, enemyBoard);
        }
        int bestScore = alpha;
        boolean pass = true;
        long moves = getMoves(ownBoard, enemyBoard);
        long move;
        while ((move = selectMove(moves)) != 0) {
            long reverses = getReverses(ownBoard, enemyBoard, move);
            pass = false;
            int score = -alphabetaPerfect(enemyBoard ^ reverses, ownBoard ^ (move | reverses), pass, limit - 1, -beta,
                    -bestScore, null);
            if (score > bestScore) {
                if (score >= beta) {
                    // cut
                    return score;
                }
                bestScore = score;
                if (ref != null) {
                    ref.move = move;
                }
            }
            moves = deleteMove(moves, move);
        }

        return pass
                ? alreadyPassed
                        ? evaluate(ownBoard, enemyBoard)
                        : -alphabetaPerfect(enemyBoard, ownBoard, pass, limit, -beta, -bestScore, null)
                : bestScore;
    }

    public static long str_to_mybit(String[] str) {
        long bit = 0;
        for (int i = 1; i < 65; i++) {
            if (str[i].equals(MoveRef2.my_color)) {
                bit |= 1L << i - 1;
            }
        }
        return bit;
    }

    public static long str_to_enemybit(String[] str) {
        long bit = 0;
        for (int i = 1; i < 65; i++) {
            if (str[i].equals(MoveRef2.enemy_color)) {
                bit |= 1L << i - 1;
            }
        }
        return bit;
    }

    // 1が一つだけあるビットボードから、その位置を返す
    public static String[] move_to_put(long move) {
        for (int i = 0; i < 64; i++) {
            if ((move & (1L << i)) != 0) {
                return new String[] { String.valueOf(i / 8), String.valueOf(i % 8) };
            }
        }
        return null;
    }

    public static void print_board(String[] s) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                System.out.print(s[i + 1 + (8 * j)] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void main(String args[]) {
        
        Socket s;

        while (true) {

            InputStream sIn;
            OutputStream sOut;
            BufferedReader br;
            PrintWriter pw;
            String str1;
            String senString = "";
            String[] strs1;
            String[] boardstr = new String[65];

            try {
                s = new Socket(args[0], 50001);
                boolean flag = true;

                sIn = s.getInputStream();
                sOut = s.getOutputStream();
                br = new BufferedReader(new InputStreamReader(sIn));
                pw = new PrintWriter(new OutputStreamWriter(sOut), true);
                long maxTime = 0;
                int x;
                int y;

                while (flag) {
                    str1 = br.readLine();
                    strs1 = str1.split(" ");

                    switch (strs1[0]) {
                        case "START":
                            System.out.println(str1);
                            MoveRef2.my_color = strs1[1];
                            if (MoveRef2.my_color.equals("1")) {
                                MoveRef2.enemy_color = "-1";
                            } else {
                                MoveRef2.enemy_color = "1";
                            }
                            senString = "NICK CPU";
                            pw.println(senString);
                            break;

                        case "BOARD":
                            // System.out.println(strs1[0]);
                            boardstr = strs1;
                            print_board(boardstr);
                            break;

                        case "TURN":
                            // System.out.println(str1);
                            // System.out.println(strs1[1]);
                            // System.out.println(my_color);
                            if (strs1[1].equals(MoveRef2.my_color)) {
                                // System.out.println("a");
                                long ownBoard = str_to_mybit(boardstr);
                                long enemyBoard = str_to_enemybit(boardstr);
                                // System.out.println("FSnum = " + evalFS(ownBoard, enemyBoard));
                                // System.out.println("BPnum = " + evalBP(ownBoard, enemyBoard));
                                // System.out.println("CNnum = " + evalCN(ownBoard, enemyBoard));
                                // System.out.println("eval = " + evaluate(ownBoard, enemyBoard));
                                long startTime = System.currentTimeMillis();
                                alphabetaPerfect(ownBoard, enemyBoard, false, 9, -1000, 1000, ref);
                                long endTime = System.currentTimeMillis();
                                System.out.println("処理時間：" + (endTime - startTime) + " ms");
                                if (maxTime < endTime - startTime) {
                                    maxTime = endTime - startTime;
                                }
                                strs1 = move_to_put(ref.move);
                                senString = "PUT " + strs1[0] + " " + strs1[1];
                                System.out.println(senString);
                                pw.println(senString);
                            }
                            break;

                        case "ERROR":
                            System.out.println(str1);
                            Random rand1 = new Random();
                            Random rand2 = new Random();
                            x = rand1.nextInt(8);
                            y = rand2.nextInt(8);
                            pw.println("PUT " + x + " " + y);
                            break;

                        case "END":
                            System.out.println(str1);
                            flag = false;
                            System.out.println("maxtime = " + maxTime);
                            break;

                        case "CLOSE":
                            // System.out.println(str1);
                            flag = false;
                            break;

                    }

                }

            } catch (IOException e) {
                System.err.println("Caught IOException");
                System.exit(1);
            }
        }
    }
}