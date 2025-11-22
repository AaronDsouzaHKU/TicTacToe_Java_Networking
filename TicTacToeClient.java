import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.io.*;
import java.net.*;
import java.lang.Thread; 


public class TicTacToeClient {
    /**
     * This class implements a client connected to the server 
     * of the game tic tac toe. 
     */
    private String PLAYER = "X";
    private String OPPONENT = "O";
    private int playerNum;

    private JFrame frame = new JFrame();
    private JButton[][] board_grid_button = new JButton[3][3];
    private JLabel message = new JLabel("Enter your player name... ");
    private JLabel currentTimeLabel = new JLabel();
    private JButton submit= new JButton("Submit");

    private JLabel player1ScoreLabel = new JLabel("0");
    private JLabel player2ScoreLabel = new JLabel("0");
    private JLabel drawScoreLabel = new JLabel("0");

    private String playerName = "";
    private int player1Wins = 0, player2Wins = 0, draws = 0;
    
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    private Thread listenerThread;


    /**
     * the main program that runs the game
     * it calls the function buildGUI, which implements the rest of the game
     */
    public static void main(String[] args) {
        TicTacToeClient ttt=new TicTacToeClient();
        ttt.buildGUI();
    }

    /**
     * this method sets the frame and 
     * calls the functions to build the various panels of the window
     */
    private void buildGUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Tic Tac Toe");
        frame.setSize(700, 600);
        frame.setLayout(new BorderLayout());

        buildNorthPanel();
        buildCenterPanel();
        buildSouthPanel();
        buildEastPanel();
        buildMenuBar();

        frame.setVisible(true);
        startClock();
    }

    //===================North=================
    private void buildNorthPanel() {
        message.setHorizontalAlignment(JTextField.CENTER);
        message.setFont(new Font("Arial", Font.PLAIN, 16));
        message.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        frame.add(message, BorderLayout.NORTH);
    }

    //================== Center==================
    private void buildCenterPanel() {
        JPanel boardPanel = new JPanel(new GridLayout(3, 3, 10, 10));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                board_grid_button[r][c] = new JButton();
                board_grid_button[r][c].setFont(new Font("Arial", Font.PLAIN, 90));
                board_grid_button[r][c].setEnabled(false);

                final int row = r, col = c;
                board_grid_button[r][c].addActionListener(e -> playerClicked(row, col));

                boardPanel.add(board_grid_button[r][c]);
            }
        }
        frame.add(boardPanel, BorderLayout.CENTER);
    }

    /**
     * This method is implemented when the player clicks on a particular
     * board_grid_button. It sets the text of the player, disables that button,
     * sends the coordinates of the button, and
     * it also checks if the player has won or if a draw occured.
     * In either of the cases the score is changed accordingly and a message box
     * pops up to ask if the player wants to continue
     * if neither of the cases take place, the opponent's move is called
     * after 2 seconds
     * @param row -the row of the button which the player clicked on
     * @param col - the column of the button which the player clicked on
     */

    private void playerClicked(int row, int col) {
        JButton btn = board_grid_button[row][col];

        btn.setText(PLAYER);
        //btn.setForeground(Color.GREEN);
        btn.setEnabled(false);
        disableBoard();
        writer.println(row+""+col);
        
        message.setText("Valid move, waiting for your opponent.");

        if (checkWinner(PLAYER)) {
            int choice = JOptionPane.showConfirmDialog(
                frame,
                "Congragulations. You Win!\n\nDo you want to Play again?",
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE
            );
            if(choice== JOptionPane.YES_OPTION){
                if(playerNum==1) {
                    player1Wins++;
                    player1ScoreLabel.setText(String.valueOf(player1Wins));
                }
                else {
                    player2Wins++;
                    player2ScoreLabel.setText(String.valueOf(player2Wins));
                }
                resetGame();
                writer.println("restart");
            }
            else if (choice== JOptionPane.NO_OPTION) System.exit(0);
            
        }
        else if (isBoardFull()) {
            int choice = JOptionPane.showConfirmDialog(
                frame,
                "It's a Draw!\n\nPlay again?",
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE
            );
            if(choice== JOptionPane.YES_OPTION){
                draws++;
                drawScoreLabel.setText(String.valueOf(draws));
                resetGame();
                writer.println("restart");
            }
            else if (choice== JOptionPane.NO_OPTION) System.exit(0);
        }
    }

    //================== SOUTH ================== 
    private void buildSouthPanel() {
        JPanel south = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel Enter_Name_Label=new JLabel("Enter your name:");
        south.add(Enter_Name_Label, gbc);

        JTextField nameField = new JTextField(15);
        gbc.gridx = 1;
        south.add(nameField, gbc);

        submit.setPreferredSize(new Dimension(80, 26));
        gbc.gridx = 2;
        south.add(submit, gbc);

        submit.addActionListener(e -> submitName(nameField));

        gbc.gridx = 1; gbc.gridy = 1;
        currentTimeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        currentTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        south.add(currentTimeLabel, gbc);

        south.setBorder(BorderFactory.createEmptyBorder(20, 0, 5, 0));
        frame.add(south, BorderLayout.SOUTH);
    }

    private void submitName(JTextField nameField) {
        playerName = nameField.getText().trim();
        if (playerName.isEmpty()) playerName = "Player";

        nameField.setEnabled(false);
        submit.setEnabled(false);

        frame.setTitle("Tic Tac Toe - Player: " + playerName);
        message.setText("WELCOME " + playerName);
        connectToServer();
        //enableEmptyCells();
    }

    private void startClock() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        new Timer(1000, e -> currentTimeLabel.setText(
                "Current Time: " + LocalTime.now().format(fmt))).start();
    }

    //================ EAST====================
    private void buildEastPanel() {
        JPanel east = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JLabel ScoreTitleLabel= new JLabel("Score");
        east.add(ScoreTitleLabel, gbc);

        gbc.gridx=0; gbc.gridy=1;
        JLabel PlayerScoreTitleLabel= new JLabel("Player 1 Wins: ");
        east.add(PlayerScoreTitleLabel, gbc);

        gbc.gridx=1; gbc.gridy=1;
        east.add(player1ScoreLabel, gbc);

        gbc.gridx=0; gbc.gridy=2;
        JLabel ComputerScoreTitleLabel= new JLabel("Player 2 Wins: ");
        east.add(ComputerScoreTitleLabel, gbc);

        gbc.gridx=1; gbc.gridy=2;
        east.add(player2ScoreLabel, gbc);

        JLabel Draw_Score_Value_Label=new JLabel("Draws:");
        gbc.gridx=0; gbc.gridy=3;
        east.add(Draw_Score_Value_Label, gbc);

        gbc.gridx=1; gbc.gridy=3;
        east.add(drawScoreLabel, gbc);

        east.setPreferredSize(new Dimension(220, 0));
        frame.add(east, BorderLayout.EAST);
    }

    //==========Menu=============
    private void buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu control = new JMenu("Control");
        JMenu help = new JMenu("Help");

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> System.exit(0));
        control.add(exit);

        JMenuItem instr = new JMenuItem("Instruction");
        instr.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                "Criteria for a valid move:\n" +
                "- The move is not occupied by any mark.\n" +
                "- The move is made in the player's turn.\n" +
                "- The move is made within the 3 x 3 board.",
                "Instructions", 
                JOptionPane.INFORMATION_MESSAGE));
        help.add(instr);

        bar.add(control);
        bar.add(help);
        frame.setJMenuBar(bar);
    }

    private void enableEmptyCells() {
        for (int i = 0; i < 3; i++) {
            for(int j=0; j<3; j++){
                if(board_grid_button[i][j].getText().isEmpty())
                    board_grid_button[i][j].setEnabled(true);
            }
        }
    }

    private void disableBoard(){
        for (int i = 0; i < 3; i++) {
            for(int j=0; j<3; j++){
                board_grid_button[i][j].setEnabled(false);
            }
        }
    }

    private boolean checkWinner(String mark) {
        for (int i = 0; i < 3; i++) {
            if (board_grid_button[i][0].getText().equals(mark) && board_grid_button[i][1].getText().equals(mark) && board_grid_button[i][2].getText().equals(mark))
                return true;
            if (board_grid_button[0][i].getText().equals(mark) && board_grid_button[1][i].getText().equals(mark) && board_grid_button[2][i].getText().equals(mark))
                return true;
        }
        if (board_grid_button[0][0].getText().equals(mark) && board_grid_button[1][1].getText().equals(mark) && board_grid_button[2][2].getText().equals(mark))
            return true;
        return board_grid_button[0][2].getText().equals(mark) && board_grid_button[1][1].getText().equals(mark) && board_grid_button[2][0].getText().equals(mark);
    }

    private boolean isBoardFull() {
        for (int i = 0; i < 3; i++) {
            for(int j=0; j<3; j++){
                if(board_grid_button[i][j].getText().isEmpty())
                    return false;
            }
        }
        return true;
    }

    private void resetGame(){
        for (int i = 0; i < 3; i++) {
            for(int j=0; j<3; j++){
                board_grid_button[i][j].setText("");
                board_grid_button[i][j].setEnabled(false);
            }
        }
    }

    //=======Networking==========

    private void connectToServer(){
        try{
            socket= new Socket("127.0.0.1", 5000);
            reader= new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            listenerThread = new Thread(()->listenForOpponentMoves());
            listenerThread.setDaemon(true);
            listenerThread.start();

        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * This method is a listener that is continuously listening to
     * messages from the server. There are 4 strings possible:
     * 1. with length 2 is a move which tells the row and column 
     *    of the button clicked. That button is also shown on the 
     *    current player's screen
     * 2. with length 9 is "Player: x" which tells the client the
     *    player number
     * 3. "LEFT" tells the client one of the players left and handles exit
     * 4. "start" tells that both the clients are ready and can start the game
     */

    private void listenForOpponentMoves(){
        try{
            String move;
            while((move=reader.readLine())!= null){
                System.out.println(move);
                if(move.length()==2){
                    
                    final int row=move.charAt(0)-'0';
                    final int col=move.charAt(1)-'0';

                    SwingUtilities.invokeLater(() -> {
                        JButton btn = board_grid_button[row][col];
                        if (btn.getText().isEmpty()) {
                            if (!btn.getText().equals(PLAYER)) {  
                                btn.setEnabled(false);

                                btn.setText(OPPONENT);
                                enableEmptyCells();
                            }
                            message.setText("Your opponent has moved, now it is your turn.");
                            if (checkWinner(OPPONENT)) {
                                disableBoard();
                                int choice = JOptionPane.showConfirmDialog(
                                    frame,
                                    "You Lose.\n\nDo you want to Play again?",
                                    "Game Over",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.INFORMATION_MESSAGE
                                );
                                if(choice== JOptionPane.YES_OPTION){
                                    if(playerNum==2) {
                                        player1Wins++;
                                        player1ScoreLabel.setText(String.valueOf(player1Wins));
                                    }
                                    else {
                                        player2Wins++;
                                        player2ScoreLabel.setText(String.valueOf(player2Wins));
                                    }
                                    resetGame();
                                    writer.println("restart");
                                }
                                else if (choice== JOptionPane.NO_OPTION) System.exit(0);
                                
                            } 
                            else if (isBoardFull()) {
                                disableBoard();
                                int choice = JOptionPane.showConfirmDialog(
                                    frame,
                                    "It's a Draw!\n\nDo you want to Play again?",
                                    "Game Over",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.INFORMATION_MESSAGE
                                );
                                if(choice== JOptionPane.YES_OPTION){
                                    draws++;
                                    drawScoreLabel.setText(String.valueOf(draws));
                                    writer.println("restart");
                                    resetGame();
                                } 
                                else if (choice== JOptionPane.NO_OPTION) System.exit(0);
                            }
                        }
                    });
                }
                else if(move.length()==9){
                    playerNum=move.charAt(8)-'0';
                    if(playerNum==1){
                        PLAYER="X";
                        OPPONENT="O";
                    }
                    else if(playerNum==2){
                        PLAYER="O";
                        OPPONENT="X";
                    }
                }
                else if(move.equals("LEFT")){
                    JOptionPane.showMessageDialog(
                        frame,
                        "Game Ends. One of the players left",
                        "Game Over",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    System.exit(0);
                }
                else if(move.equals("start") && playerNum==1) enableEmptyCells();
            }
        } catch (IOException ex) {}
    }

}