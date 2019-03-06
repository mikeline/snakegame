import org.w3c.dom.css.Rect;

import javax.management.timer.TimerNotification;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.PixelGrabber;
import java.util.*;
import java.util.Timer;
import java.util.concurrent.BlockingQueue;

import static java.lang.Thread.sleep;

public class SnakeGame extends JFrame
{
    Grid grid;
    Snake snake;
    Stack<int[]> food;
    ArrayList<Stack<int[]>> toDraw;
    FoodItem item;
    static Timer timer = new Timer();

    public SnakeGame()
    {
        initUI();
    }

    public final void initUI()
    {
        panel = new JPanelSnake();
        panel.setBackground(new java.awt.Color(255, 255, 255));
        panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        setContentPane(panel);

        grid = new Grid();
        snake = new Snake(grid);
        food = new Stack<>();
        item = new FoodItem();
        food.push(new int[]{item.getX(), item.getY()});

        toDraw = new ArrayList<>();
        toDraw.add(snake.body);
        toDraw.add(food);

        setTitle("Snake Game");
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        pack();

        MyTimer();

    }


    public static void main(String[] args)
    {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SnakeGame();
            }
        });

    }

    private static JPanelSnake panel;

    public static void MyTimer() {

        TimerTask task;

        task = new TimerTask() {
            @Override
            public void run() {
                panel.repaint();
            }
        };
        timer.schedule(task, 0, 400);

    }



    class JPanelSnake extends JPanel implements KeyListener
    {
        private long currentTimeMillis;
        JPanelSnake()
        {
            timer = new Timer();
            currentTimeMillis = System.currentTimeMillis();
            setPreferredSize(new Dimension(400, 400));
            addKeyListener(this);
            setFocusable(true);
            requestFocusInWindow();
        }

        private void update()
        {
            snake.move();
        }

        @Override
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            try
            {
                doDrawing(g);
            }
            catch (Exception e)
            {

            }
        }

        private void doDrawing(Graphics g) throws Exception
        {
            var g2d = (Graphics2D)g;
            g2d.setColor(new java.awt.Color(255, 146, 0));
            ListIterator bodyIter = toDraw.get(0).listIterator(snake.body.size());
            ListIterator foodIter = toDraw.get(1).listIterator(food.size());
            while(bodyIter.hasPrevious())
            {
                g2d.setColor(new java.awt.Color(255, 146, 0));
                int[] body = (int[]) bodyIter.previous();
                g2d.fillRect(grid.getX(body), grid.getY(body), grid.getCellWidth(), grid.getCellHeight());
                g2d.setColor(new java.awt.Color(255, 209, 141));
                g2d.drawRect(grid.getX(body), grid.getY(body), grid.getCellWidth(), grid.getCellHeight());
            }
            g2d.setColor(new java.awt.Color(9, 183, 0, 233));
            while(foodIter.hasPrevious())
            {
                int[] item = (int[]) foodIter.previous();
                if(grid.getState(item[0], item[1]) == 2)
                {
                    g2d.fillRect(grid.getX(item), grid.getY(item), 10, 10);
                }
            }
            update();
        }

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (System.currentTimeMillis() - currentTimeMillis > 280) {
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_UP) {
                    snake.turnUp();
                } else if (keyCode == KeyEvent.VK_DOWN) {
                    snake.turnDown();
                } else if (keyCode == KeyEvent.VK_RIGHT) {
                    snake.turnRight();
                } else if (keyCode == KeyEvent.VK_LEFT) {
                    snake.turnLeft();
                }
            }
            currentTimeMillis = System.currentTimeMillis();
        }

        @Override
        public void keyReleased(KeyEvent e) {

        }
    }

    class Grid {
        private int[][][] coordinates = new int[40][40][2];
        private static final int cellWidth = 10;
        private static final int cellHeight = 10;
        private byte[][] states = new byte[40][40]; // 0 - empty, 1 - snake, 2 - food

        public Grid() {
            initCoordinates();
        }

        private void initCoordinates() {
            int x, y = 0;
            for (int i = 0; i < 40; i++) {
                x = 0;
                for (int j = 0; j < 40; j++) {
                    coordinates[i][j][0] = x;
                    coordinates[i][j][1] = y;
                    x += 10;
                }
                y += 10;
            }
        }

        public int[] getBoxCoordinates(int x, int y) {
            return coordinates[x][y];
        }

        public int getX(int[] xy) {
            return coordinates[xy[0]][xy[1]][0];
        }

        public int getY(int[] xy) {
            return coordinates[xy[0]][xy[1]][1];
        }

        public byte getState(int x, int y) {
            return states[x][y];
        }

        public void setState(int x, int y, byte val) {
            states[x][y] = val;
        }

        public int getCellWidth() {
            return cellWidth;
        }

        public int getCellHeight()
        {
            return cellHeight;
        }
    }

    class FoodItem
    {
        private int[] pos = new int[]{(int)(Math.random() * 10 + Math.random() * 30), (int)(Math.random() * 10 + Math.random() * 30)};
        public FoodItem()
        {
            grid.setState(pos[0], pos[1], (byte)2);
        }
        public int getX()
        {
            return pos[0];
        }
        public int getY()
        {
            return pos[1];
        }
        public void generate()
        {
            pos = new int[]{(int)(Math.random() * 10 + Math.random() * 30), (int)(Math.random() * 10 + Math.random() * 30)};
            grid.setState(pos[0], pos[1], (byte)2);
        }
    }

    class Snake
    {
        // declare list of snake body parts
        Stack<int[]> body;
        Stack<int[]> directions;

        Grid grid;

        // create random values for direction
        private Random rnd = new Random();
        private int randX = rnd.nextInt(2);
        private int randY = randX == 0 ? 1 : 0;

        // define initial position and direction
        private int[] pos = {(int)(Math.random() * 10 + Math.random() * 30), (int)(Math.random() * 10 + Math.random() * 30)};
        private int[] d = {randX, randY};

        private int[] head;
        private int[] tail;

        public Snake(Grid grid)
        {
            this.grid = grid;
            body = new Stack<>();
            directions = new Stack<>();

            for (int i = 0; i < 3; i++)
            {
                body.push(pos.clone());
                grid.setState(pos[0], pos[1], (byte)1);
                directions.push(d.clone());
                pos[0] += d[0];
                pos[1] += d[1];
            }

            head = directions.peek();
            tail = directions.lastElement();
        }

        public void move()
        {
            int i = body.size() - 1;
            int[] prev = new int[2];
            int[] prev_dir = new int[2];
            boolean canAdd = false;
            while(i >= 0)
            {
                int[] part = body.get(i);
                int[] direction = directions.get(i);

                int incX = direction[0];
                int incY = direction[1];

                grid.setState(part[0], part[1], (byte)0);

                part[0] += incX;
                part[1] += incY;
                body.set(i, part.clone());

                // check if out of bounds
                if (part[0] > 39 || part[1] > 39 || part[0] < 0 || part[1] < 0)
                {
                    System.exit(0);
                }

                // check if hits itself
                if (i == body.size() - 1)
                {
                    if (grid.getState(part[0], part[1]) == 1)
                    {
                        System.exit(0);
                    }
                }

                // change direction of parts if needed
                if (i < body.size() - 1)
                {
                    if (timeToChangeDirection(part[0] + incX, part[1] + incY, prev[0], prev[1]))
                    {
                        direction[0] = prev[0] - part[0];
                        direction[1] = prev[1] - part[1];
                        directions.set(i, direction.clone());
                    }
                }

                // check if hits a food item
                if(i == body.size() - 1)
                {
                    if(grid.getState(part[0], part[1]) == (byte)2)
                    {
                        canAdd = true;
                    }
                }

                grid.setState(part[0], part[1], (byte)1);


                prev[0] = part[0];
                prev[1] = part[1];

                prev_dir[0] = direction[0];
                prev_dir[1] = direction[1];

                i--;
            }
            if(canAdd)
            {
                body.push(new int[]{body.peek()[0] + head[0], body.peek()[1] + head[1]});
                directions.push(head.clone());
                head = directions.peek();
                item.generate();
                food.set(0, new int[]{item.getX(), item.getY()});

            }
        }

        public boolean timeToChangeDirection(int x, int y, int prevX, int prevY)
        {
            return x != prevX || y != prevY;
        }


        public void turnDown()
        {
            if (head[0] == 0)
            {
                head[0] = 1;
                head[1] = 0;
            }
        }

        public void turnUp()
        {
            if (head[0] == 0)
            {
                head[0] = -1;
                head[1] = 0;
            }
        }

        public void turnLeft()
        {
            if (head[1] == 0)
            {
                head[1] = -1;
                head[0] = 0;
            }
        }

        public void turnRight()
        {
            if (head[1] == 0)
            {
                head[1] = 1;
                head[0] = 0;
            }
        }
    }
}
