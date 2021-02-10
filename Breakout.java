import acm.graphics.*;
import acm.util.RandomGenerator;
import com.shpp.cs.a.graphics.WindowProgram;
import java.awt.*;
import java.awt.event.MouseEvent;


@SuppressWarnings({"unused", "SpellCheckingInspection", "IntegerDivisionInFloatingPointContext"})

public class Breakout extends WindowProgram {

    /**
    *   ATTENTION!
    *
    *  DEAR REVIEWER! MAKE THIS FLAG "true" AND:
    *
    *  1. GAME WILL BE WON AUTOMATICALLY
    *
    *  2. TO SAVE YOUR REVIEW TIME PAUSE WILL BE NULLED
    *
    *
    * Game test flag on/off - add/remove bottom line */
    public static final boolean TEST = false;

    /**  Pause in mSec */
    private static final int PAUSE = 10;

    /** Width and height of application window in pixels */
    public static final int APPLICATION_WIDTH = 400;
    public static final int APPLICATION_HEIGHT = 600;

    /** Dimensions of the paddle */
    private static final int PADDLE_WIDTH = 60;
    private static final int PADDLE_HEIGHT = 10;

    /** Color of the paddle */
    private static final Color PADDLE_COLOR = Color.BLACK;

    /** Offset of the paddle up from the bottom */
    private static final int PADDLE_Y_OFFSET = 30;

    /** Number of bricks per row */
    private static final int NBRICKS_PER_ROW = 10;

    /** Number of rows of bricks */
    private static final int NBRICK_ROWS = 10;

    /** Separation between bricks */
    private static final int BRICK_SEP = 4;

    /** Width of a brick */
    private static final int BRICK_WIDTH =
            (APPLICATION_WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

    /** Height of a brick */
    private static final int BRICK_HEIGHT = 8;

    /** Radius of the ball in pixels */
    private static final int BALL_RADIUS = 10;

    /** Diameter of the ball in pixels */
    private static final int BALL_DIAM = BALL_RADIUS * 2;

    /** Color of the ball */
    private static final Color BALL_COLOR = Color.BLACK;

    /** Offset of the top brick row from the top */
    private static final int BRICK_Y_OFFSET = 70;

    /** Number of turns */
    private static final int NTURNS = 3;


    private GRect paddle; // unique paddle object
    private GOval ball; // unique ball object
    double vx;
    double vy;
    int count = 0;
    int games = 0;
    int paused = PAUSE;

// main engine
    public void run() {

// if test flag is ON - speeds up animation
        if (TEST) paused = 0;

        addBricks();
        addPaddle();
        addBall();
        while (games < NTURNS && count > 0) {
            actionRun();
            ball.setLocation(getWidth() / 2.0, getHeight() / 2.0);
            games++;
            }
// final tracing
        println("final tracing:   Attempts left: " + (NTURNS - games) + "   Bricks left: " + count);
        if (count > 0) lostGame(); else winGame();
    }

// add bricks
    private void addBricks(){
        for (int i = 0; i < NBRICK_ROWS; i++){
            int y = BRICK_Y_OFFSET + (i * (BRICK_HEIGHT + BRICK_SEP));
            for (int j = 0; j < NBRICKS_PER_ROW; j++){
                int x = BRICK_SEP + (j * (BRICK_WIDTH + BRICK_SEP));
                GRect brick = new GRect(x, y, BRICK_WIDTH, BRICK_HEIGHT);
                brick.setFilled (true);
                brickColor(brick, i);
                add (brick);
                count++;
            }
        }
    }

// add colors to bricks
    private void brickColor(GRect brick, int currentRow){
        if (currentRow <= 1) brick.setColor(Color.red);
        if (currentRow == 2 || currentRow == 3) brick.setColor(Color.orange);
        if (currentRow == 4 || currentRow == 5) brick.setColor(Color.yellow);
        if (currentRow == 6 || currentRow == 7) brick.setColor(Color.green);
        if (currentRow >= 8) brick.setColor(Color.cyan);
    }

// add paddle
    private void addPaddle() {
        double x = getWidth()/2 - PADDLE_WIDTH/2;
        double y = getHeight() - PADDLE_Y_OFFSET - PADDLE_HEIGHT;
        paddle = new GRect (x, y, PADDLE_WIDTH, PADDLE_HEIGHT);
        paddle.setFilled(true);
        add (paddle);
        addMouseListeners();
    }

// paddle is mouse driven now
    public void mouseMoved(MouseEvent e) {
        if ((e.getX() < getWidth() - PADDLE_WIDTH/2) && (e.getX() > PADDLE_WIDTH/2)) {
            paddle.setLocation(e.getX() - PADDLE_WIDTH/2, getHeight() - PADDLE_Y_OFFSET - PADDLE_HEIGHT);
        }
    }

// add ball
    private void addBall() {
        ball = new GOval(getWidth() / 2.0, getHeight() / 2.0, BALL_DIAM, BALL_DIAM);
        ball.setFilled(true);
        ball.setColor(BALL_COLOR);
        add(ball);
    }

// runs the ball
    private void actionRun() {
// tracing
        println("on-game trace:   Attempts left: " + (NTURNS - games));
        waitForClick();
        vy = 3.0;
        RandomGenerator rgen = RandomGenerator.getInstance();
        vx = rgen.nextDouble(1.0, 3.0);
        if (rgen.nextBoolean(0.5)) {
            vx = -vx;
        }
        while (ball.getY() < getHeight() && count > 0) ballFly();
    }

// wall kick processing
    private void kickWalls(){
        if (ball.getX() + ball.getWidth() >= getWidth() || ball.getX() <= 0) vx = -vx;
        if (ball.getY() <= 0) vy = -vy;
        if (ball.getY() + ball.getHeight() >= getHeight() && TEST) vy = -vy;
    }

// brick remove
    private void checkAllCollision(){
        GObject collider = getCollidingObject();
        if (collider != null) {
            if (collider != paddle) {
                remove(collider);
                count--;
            }
// tracing
            println("on-game trace:   Attempts left: " + (NTURNS - games) + "   Bricks left: " + count);
            vy = -vy;
        }
    }

// game won
    private void winGame() {
        removeAll();
        GLabel won = new GLabel ("you won", getWidth()/2, getHeight());
        won.move(-won.getWidth()/2, -won.getHeight());
        won.setColor(PADDLE_COLOR);
        add (won);
    }

// game lost
    private void lostGame() {
        remove(ball);
        remove(paddle);
        GLabel lost = new GLabel ("you lose", getWidth()/2, getHeight());
        lost.move(-lost.getWidth()/2, -lost.getHeight());
        lost.setColor(PADDLE_COLOR);
        add (lost);
    }

// get collider
    private GObject getCollidingObject(){
        GObject  collider1 = getElementAt (ball.getX(), ball.getY());
        GObject  collider2 = getElementAt (ball.getX(), ball.getY() + BALL_DIAM);
        GObject  collider3 = getElementAt (ball.getX() + BALL_DIAM, ball.getY());
        GObject  collider4 = getElementAt (ball.getX() + BALL_DIAM, ball.getY() + BALL_DIAM);
        if (collider1 != null) return (collider1);
        else if (collider2 != null) return (collider2);
        else if (collider3 != null) return (collider3);
        else return (collider4);
    }

// ball fly
    private void ballFly() {
        ball.move(vx, vy);
        kickWalls();
        checkAllCollision();
        pause (paused);
    }
}