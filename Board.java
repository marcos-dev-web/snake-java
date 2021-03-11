import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.Timer;
import javax.swing.JPanel;


public class Board extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;

    private final int B_WIDTH = 500;
    private final int B_HEIGHT = 500;
    private final int DOT_SIZE = 10;
    private final int ALL_DOTS = 900;
    private final int RAND_POS = 29;
    private final int DELAY = 140;

    private final int x[] = new int[ALL_DOTS];
    private final int y[] = new int[ALL_DOTS];

    private int dots;
    private int apple_x;
    private int apple_y;

    private boolean leftDirection = false;
    private boolean rightDirection = false;
    private boolean upDirection = false;
    private boolean downDirection = false;
    private boolean inGame = false;

    private Timer timer;
    private Image ball;
    private Image apple;
    private Image head;

    public Board() { //constructor
        initBoard();
    }

    private void playSound(String path) {
        URL url = this.getClass().getClassLoader().getResource(path);
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.stop();
            clip.setFramePosition(0);
            clip.start();
        } catch(UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
         }
    }

    private void initBoard() {
        
        playSound("./src/res/sounds/init.wav");

        addKeyListener(new TAdapter());
        setBackground(Color.black);
        setFocusable(true); // has focus

        setPreferredSize(new Dimension(B_WIDTH, B_HEIGHT)); //width panel is the width window
        loadImages();
        initGame();
    }

    private void loadImages() {
        //loading images
        ImageIcon iid = new ImageIcon("src/res/body.png");
        ball = iid.getImage();

        ImageIcon iia = new ImageIcon("src/res/apple.png");
        apple = iia.getImage();

        ImageIcon iih = new ImageIcon("src/res/head.png");
        head = iih.getImage();

    }

    private void initGame() {
        inGame = true;
        dots = 3; //initial length snake

        for (int z = 0; z < dots; z++) {
            x[z] = 50 - z * 10;
            y[z] = 50;
        }

        locateApple();

        timer = new Timer(DELAY, this);
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        doDrawing(g);
    }

    private void doDrawing(Graphics g) {
        if (inGame) { //if game has running
            g.drawImage(apple, apple_x, apple_y, this);

            for (int z = 0; z < dots; z++) {
                if (z == 0) {
                    g.drawImage(head, x[z], y[z], this);
                } else {
                    g.drawImage(ball, x[z], y[z], this);
                }
            }

            Toolkit.getDefaultToolkit().sync();
        } else {
            gameOver(g);
        }
    }

    private void gameOver(Graphics g) {
        ArrayList<String> frases = new ArrayList<String>();
        frases.add("Game Over");
        frases.add("MAX: "+(dots-3));
        frases.add("Press Enter to Restart game");

        for (int i = 0; i < frases.size(); i++) {
            Font font = new Font("Helvetica", Font.BOLD, 22);
            FontMetrics metr = getFontMetrics(font);

            g.setColor(Color.white);
            g.setFont(font);
            g.drawString(frases.get(i), (B_WIDTH - metr.stringWidth(frases.get(i))) / 2, (B_HEIGHT / 2) + (-30 * (frases.size() - (i+1))));
        }
       
        

        playSound("./src/res/sounds/loser.wav");
    }

    private void checkApple() {
        if ((x[0] == apple_x) && (y[0] == apple_y)) {
            dots++;
            locateApple();
            if ((float) dots / 10 % 2 == 1.0f || (float) dots / 10 % 2 == 0.0f) {
                playSound("./src/res/sounds/new.wav");
            } else {
                playSound("./src/res/sounds/plus.wav");
            }
        }
    }

    private void move() {
        if (leftDirection || rightDirection || upDirection || downDirection) {
            for (int z = dots; z > 0; z--) {
                x[z] = x[(z-1)];
                y[z] = y[(z-1)];
            }
        }

        if (leftDirection) {
            x[0] -= DOT_SIZE;
        }
        if (rightDirection) {
            x[0] += DOT_SIZE;
        }
        if (upDirection) {
            y[0] -= DOT_SIZE;
        }
        if (downDirection) {
            y[0] += DOT_SIZE;
        }
    }

    private void checkCollision() {
        for (int i = 0; i < dots; i++) {
            if (i > 3 && x[0] == x[i] && y[0] == y[i]) {
                inGame = false;
            }
        }

        if (x[0] >= B_WIDTH) {
            inGame = false;
        }
        if (x[0] < 0) {
            inGame = false;
        }
        if (y[0] >= B_HEIGHT) {
            inGame = false;
        }
        if (y[0] < 0) {
            inGame = false;
        }
        if (!inGame) {
            timer.stop();
        }
    }

    private void locateApple() {
        int r = (int) (Math.random() * RAND_POS);
        apple_x = ((r * DOT_SIZE));

        r = (int) (Math.random() * RAND_POS);
        apple_y = ((r * DOT_SIZE));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (inGame) {
            checkApple();
            checkCollision();
            move();
        }

        repaint();
    }

    private void restart() {
        removeAll();
        revalidate();
        repaint();
        leftDirection = false;
        rightDirection = false;
        downDirection = false;
        upDirection = false;
        initBoard();
    }

    private class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (!inGame && key == KeyEvent.VK_ENTER) {
                restart();
            }

            if ((key == KeyEvent.VK_LEFT) && (!rightDirection)) {
                leftDirection = true;
                upDirection = false;
                downDirection = false;
            }
            if ((key == KeyEvent.VK_RIGHT) && (!leftDirection)) {
                rightDirection = true;
                upDirection = false;
                downDirection = false;
            }
            if ((key == KeyEvent.VK_UP) && (!downDirection)) {
                upDirection = true;
                leftDirection = false;
                rightDirection = false;
            }
            if ((key == KeyEvent.VK_DOWN) && (!upDirection)) {
                downDirection = true;
                leftDirection = false;
                rightDirection = false;
            }
        }
    }
}


