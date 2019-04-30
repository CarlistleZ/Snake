import java.awt.event.*;
import javax.swing.*;

public class Main {
    private static SolverMode solverMode = SolverMode.AStar;
    private static boolean selected = false;
    public static void main(String[] args) {

        JFrame f=new JFrame("Snake Game");
        JLabel l1=new JLabel("Please choose a game mode:");
        l1.setBounds(10,10, 300,30);
        f.add(l1);

        JRadioButton b1=new JRadioButton("AStar");
        b1.setBounds(35,50,95,30);
        b1.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                solverMode = SolverMode.AStar;
            }
        });

        JRadioButton b2=new JRadioButton("ID AStar");
        b2.setBounds(35,90,95,30);
        b2.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                solverMode = SolverMode.idAstar;
            }
        });

        JRadioButton b3=new JRadioButton("MCTS (Beta...)");
        b3.setBounds(35,130,155,30);
        b3.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                solverMode = SolverMode.MCTS;
            }
        });

        ButtonGroup bg=new ButtonGroup();
        bg.add(b1);
        bg.add(b2);
        bg.add(b3);
        f.add(b1);
        f.add(b2);
        f.add(b3);

        JButton b4=new JButton("Start");
        b4.setBounds(40,160,95,30);
        b4.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                Main.selected = true;
            }
        });
        f.add(b4);

        f.setSize(250,250);
        f.setLayout(null);
        f.setVisible(true);

        while (true){
            try {
                if (Main.selected == true) {
                    SnakeGame.startGame(Main.solverMode);
                } else {
                    Thread.sleep(500);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}