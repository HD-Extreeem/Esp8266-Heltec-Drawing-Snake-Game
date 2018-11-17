import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class DrawingUI extends JPanel {

    private int x1=320,x2=320,y1=320,y2=320;
    private int w = 720, h = 600;
    private int line_length = 0;
    private String dir = "non";
    private  JLabel dirLbl;
    private int oldData[] = new int[30000];
    private int g_mode;
    private Random rand = new Random();
    //private int foodX=rand.nextInt(630)+40;
    //private int foodY=rand.nextInt(530)+40;
    private int foodX = (int) (Math.random() * 20)*10;
    private int foodY=(int) (Math.random() * 20)*10;
    private Image body,food,head;
    private int y[] =new int [1000];
    private int x[] = new int [1000];

    public DrawingUI() {
        setImages();
        initGUI();
    }

    private void initGUI() {
        String[] opt = {"QUIT",
                "Drawing","Snake"};

        JPanel pnl = new JPanel();
        pnl.add(new JLabel("Choose a game mode to play"));

        g_mode = JOptionPane.showOptionDialog(null, pnl, "Choose game mode",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, opt, null);

        JFrame frame = new JFrame("ESP8266 "+opt[g_mode]+" Game");
        dirLbl = new JLabel("No Movement");

        if (g_mode==0){
            System.exit(1);
        } else if (g_mode == 1){
            setBackground(Color.white);
            dirLbl.setForeground (Color.black);
        } else {
            setBackground(Color.black);
            line_length = 1;
            x[0] = 320;
            x[1] = 340;
            y[0] = 320;
            y[1] = 320;
            dirLbl.setForeground (Color.white);
        }
        dirLbl.setFont(new Font("Serif", Font.BOLD, 18));
        add(dirLbl);

        frame.add(this);
        frame.pack();
        frame.setSize(w, h);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void setImages(){

        body = new ImageIcon("body.png").getImage();
        food = new ImageIcon("food.png").getImage();
        head = new ImageIcon("head.png").getImage();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if(dir.equals("LEFT") || dir.equals("RIGHT") || dir.equals("UP") || dir.equals("DOWN")){
            System.out.println(dir);

            if (g_mode == 2){
                snake_operation(g);
                draw_snake(g);

            } else{
                draw_drawing(g);
                draw_operation(g);

            }
            System.out.println("X1= "+x1+"X2= "+x2+"Y1= "+y1+"Y2= "+y2);
            dirLbl.setText(dir);
        } else{
            if (g_mode==2){
                draw_snake(g);
            } else{
                draw_drawing(g);
            }
            dirLbl.setText("unknown move");
        }

    }



    private void draw_snake(Graphics g) {
        g.drawImage(food,foodX,foodY,this);
        for(int m=0; m<line_length;m++){
            if (m==0){
                g.drawImage(head,x[m],y[m],this);
            }
            else{
                g.drawImage(body,x[m],y[m],this);
            }
        }
    }

    private void draw_drawing(Graphics g) {
        g.setColor(Color.BLACK);
        for (int i=0; i<line_length; i+=4){
            g.drawLine(oldData[i], oldData[i+1], oldData[i+2], oldData[i+3]);
        }
    }


    private void snake_operation(Graphics g) {

        for(int i = line_length; i>0;i--){
            x[i] = x[(i-1)];
            y[i] = y[(i-1)];
        }
        switch (dir) {

            case "UP":
                y2 = y1 - 10;
                y[0] = y2;
                break;

            case "DOWN":
                y2 = y1 + 10;
                y[0] = y2;
                break;

            case "LEFT":
                x2 = x1 - 10;
                x[0] = x2;
                break;

            case "RIGHT":
                x2 = x1 + 10;
                x[0] = x2;
                break;
        }
        x1=x2;
        y1=y2;

        checkCollision();
        checkFood();

    }

    private void draw_operation(Graphics g) {

        switch (dir) {

            case "UP":
                if(y1-10>=0){
                    y2 = y1 - 10;
                    g.drawLine(x1, y1, x2, y2);
                }
                break;
            case "DOWN":
                if(y1+10<=h-10) {
                    y2 = y1 + 10;
                    g.drawLine(x1, y1, x2, y2);
                }

                break;
            case "LEFT":
                if(x1-10>=0) {
                    x2 = x1 - 10;
                    g.drawLine(x1, y1, x2, y2);
                }
                break;
            case "RIGHT":
                if(x1+10<=w) {
                    x2 = x1 + 10;
                    g.drawLine(x1, y1, x2, y2);
                }
                break;
        }

        saveState();
    }

    private void saveState(){
        /*Save the data like this if you want to draw whole lines */
        oldData[line_length] = x1;
        oldData[line_length+1] = y1;
        oldData[line_length+2] = x2;
        oldData[line_length+3] = y2;
        x1=x2;
        y1=y2;

        /*Or... Save like this if you want to draw dotted lines */
        /*x1=x2;
        y1=y2;
        oldData[a] = x1;
        oldData[a+1] = y1;
        oldData[a+2] = x2;
        oldData[a+3] = y2;*/
        line_length +=4;
    }

    void reDraw(String dir) {
        this.dir = dir;
        this.repaint();
    }

    private void checkCollision(){
        if (x2 <= 0 || x2 >= w || y2 <= 0 || y2 >= h-10){
            System.out.println("Game over!");
        }
    }

    private void checkFood(){
        if (foodX == x[0] && foodY == y[0]){


            foodX = (int) (Math.random() * 20)*10;
            foodY = (int) (Math.random() * 20)*10;

            System.out.println("New position x: "+foodX);
            System.out.println("New position x: "+foodY);

            x[line_length] = x1;
            y[line_length] = y1;
            line_length++;
        }
    }
}

