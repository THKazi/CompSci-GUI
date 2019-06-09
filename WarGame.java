/**
 * The game modules for the game of WAR.
 *
 * @author (Timurul H. Kazi)
 * @version (May 31st, 2019)
 */
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
//MOUSE LISTENER RESOURCES
//https://docs.oracle.com/javase/tutorial/uiswing/events/mouselistener.html
//https://stackoverflow.com/questions/13203694/setting-and-getting-an-object-in-a-jlabel-with-a-mouselistener
//https://www.math.uni-hamburg.de/doc/java/tutorial/uiswing/misc/timer.html
import javax.swing.Timer; 
//https://www.javatpoint.com/java-get-current-date
import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;   
//https://www.javacodex.com/Java-IO/AudioInputStream,  https://docs.oracle.com/javase/7/docs/api/javax/sound/sampled/Clip.html
import java.io.*;
import java.net.URL;
import java.util.*;
import javax.sound.sampled.*; //.AudioInputStream, .AudioSystem, .Clip
//https://docs.oracle.com/javase/8/docs/api/javax/swing/JDialog.html#JDialog-java.awt.Dialog-
import java.awt.Dialog;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
//https://docs.oracle.com/javase/7/docs/api/javax/swing/BorderFactory.html#createBevelBorder(int)  <------ Borders
//https://www.geeksforgeeks.org/shuffle-a-given-array-using-fisher-yates-shuffle-algorithm/  <------ Shuffling
import java.util.*; 
public class WarGame extends JPanel implements ActionListener, KeyListener, MouseListener
{
    War war = new War(); //Instantiate war (enables calling upon game close & calling music from war)
    static JFrame frame; //Makes frame referenceable throughout (for pop-ups & disposing)
    
    //Mute button
    JButton mute;
    int muteCount = 0;
    
    //Date & time
    JLabel time;
    
    //Minor frames (i.e. "Ready", "Shuffling", and "Escape" pop-ups)
    JFrame init,init2,pause;
    int initCount = 0;
    int escCount = 0;
    JLabel p1txt,p2txt,p1stat,p2stat,enter;
    //Dot timer for Shuffling pop-up
    Timer dots1,dots2,dots3,in;
    int dotNum = 0;

    //OPPONENT CARDS & CALCULATIONS
    int[] oStack = new int[54];//Opponent's stack
    JLabel oCard[] = new JLabel[7];//Opponent's hand
    int oNum[] = new int[7]; //Value of opponent's hand
    int oField; //Opponent's fielded card value
    int oCount = 18; //Index of last card in opponent's stack
    JLabel ODcount; //Count under opponent's stack

    //PLAYER CARDS & CALCULATIONS
    int[] pStack = new int[54];//Player's stack
    JLabel pCard[] = new JLabel[7];//Player's hand
    int pNum[] = new int[7]; //Value of player's hand
    int pField; //Player's fielded card value
    int pCount = 18; //Index of last card in player's stack
    JLabel PDcount; //Count under player's stack

    int var = 6; //Removes cards when the number falls below the cards in the hand
    int battleCount = 0; //Tracks status of battle (on/off)
    int oBattle, pBattle; //Value of player's & opponent's battle ante card

    JLabel playCard[] = new JLabel[7]; //Battlefield

    public static void main (String args[])
    {
        frame = new JFrame(":: War ::");
        WarGame content = new WarGame();
        frame.setContentPane(content);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(true); //removes standard window frame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.enable(false); //disables frame interaction - effectively makes pop-ups modal
    }

    public WarGame()
    {
        war.bgMusic.close(); //close music opened by instantiating "War.java"
        game(); //Conjures the game frame
        escapeDialog(); //Creates the Escape dialog (invisible)
    }

    public void game()
    {
        //BACKGROUND
        JLabel background = new JLabel(img("MenuBG.gif"));
        background.setLayout(new BorderLayout(1,1));
        setBackground(Color.black);

        JPanel cover = new JPanel();
        cover.setBackground(new Color(0,0,0,33));
        background.add(cover);

        //NORTH
        JPanel north = new JPanel(new BorderLayout(1,1));
        north.setBackground(new Color(0,0,0,33));

        //CENTRE
        JPanel center = new JPanel(new GridLayout(3,1,5,5));
        center.setBackground(new Color(0,0,0,0));

        //=============================================THE GAME================================================
        int[] deck = shuffle();//Get shuffled deck
        for (int i = 0; i<27;i++) //Distribute half of cards to player
        {
            pStack[i] = deck[i];
        }

        for (int i = 0; i<27;i++) //Distributes remaining cards to opponent
        {
            oStack[i] = deck[i+27];
        }
        
        //  OPPONENT'S SIDE
        JPanel top = new JPanel(new GridLayout(1,8,5,5));//7 cards + stack
        top.setBackground(new Color(0,0,0,0));
        center.add(top);

        JPanel oDecker = new JPanel(new BorderLayout()); //Stack
        oDecker.add(new JLabel(img("Deck.gif")),BorderLayout.CENTER);

        top.add(oDecker);

        for (int i = 0; i<7;i++) //Create, mutate, and add opponent's hand
        {
            oNum[i] = oStack[0];
            oCard[i] = new JLabel(img("13.jpg"));
            oCard[i].setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED),BorderFactory.createBevelBorder(BevelBorder.LOWERED)));
            top.add(oCard[i]);
            for(int j = 0; j<oStack.length-1; j++) //Shifts opponent's stack(array) down
                oStack[j] = oStack[j+1];
        }

        ODcount = new JLabel(""+arrCount(oStack)); //Stack count (number of cards in stack)
        ODcount.setHorizontalAlignment(JLabel.CENTER);
        oDecker.add(ODcount,BorderLayout.SOUTH);

        //  BATTLEFIELD
        JPanel mid = new JPanel(new GridLayout(1,7,5,5));
        mid.setBackground(new Color(0,0,0,0));
        center.add(mid);
        for (int i = 0; i<7;i++) //Create new labels
        {
            playCard[i] = new JLabel("");
            playCard[i].setBackground(new Color(0,0,0,0));
            mid.add(playCard[i]);
        }
        //Mutate middle of battlefield for text
        playCard[3].setLayout(new FlowLayout());
        playCard[3].setFont(new Font("Lucida Console",Font.ITALIC,40));

        //  PLAYER SIDE
        JPanel low = new JPanel(new GridLayout(1,8,5,5));
        low.setBackground(new Color(0,0,0,0));
        center.add(low);

        for (int i = 0; i<7;i++) //Create, mutate, and add player's hand
        {
            pNum[i] = pStack[0];
            pCard[i] = new JLabel(img(pStack[0]+".jpg"));
            pCard[i].setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED),BorderFactory.createBevelBorder(BevelBorder.LOWERED)));
            pCard[i].addMouseListener(this);
            low.add(pCard[i]);
            for(int j = 0; j<pStack.length-1; j++) //Shift player's stack(array) down
            {
                pStack[j] = pStack[j+1];
            }
        }
        //Player's stack
        JPanel pDecker = new JPanel(new BorderLayout());
        pDecker.add(new JLabel(img("Deck.gif")),BorderLayout.CENTER);
        PDcount = new JLabel(""+arrCount(pStack));
        PDcount.setHorizontalAlignment(JLabel.CENTER);
        pDecker.add(PDcount,BorderLayout.SOUTH);
        low.add(pDecker);

        //EAST
        JPanel east = new JPanel();
        east.setBackground(new Color(0,0,0,33));
        east.setPreferredSize(new Dimension(20,200));
        //WEST
        JPanel west = new JPanel();
        west.setBackground(new Color(0,0,0,33));
        west.setPreferredSize(new Dimension(20,200));
        //SOUTH
        JPanel south = new JPanel(new GridLayout(1,1));
        south.setBackground(new Color(0,0,0,33));
        south.setPreferredSize(new Dimension(200,20));

        //Key Listeners to enable calling up the escape menu
        //Can't add to frame because "this" cannot be called in a static context, and "frame" must be static
        background.addKeyListener(this);
        center.addKeyListener(this);
        top.addKeyListener(this);
        low.addKeyListener(this);

        background.add(north,BorderLayout.NORTH);
        background.add(south,BorderLayout.SOUTH);
        background.add(center,BorderLayout.CENTER);
        background.add(east,BorderLayout.EAST);
        background.add(west,BorderLayout.WEST);
        background.setFocusable(true);

        add(background);
    }

    public int arrCount(int arr[]) //Finds last card in the given stack
    {
        int j,i;

        for (i = 0; i<arr.length; i++)
        {
            j = arr[i];
            if (j==0)
            {
                break;
            }
        }

        return i;
    }

    public void ops(JLabel label, int i) //The series of operations undertaken in the game
    {
        //PLAYERSIDE OPERATIONS
        pField = pNum[i]; // set value for actual game comparisons (value of player's fielded card)

        Icon play = label.getIcon(); //Get clicked card picture
        playCard[4].setIcon(play); //Place card in battlefield

        if (pStack[0]==0) //If the player's stack is out of cards, don't replace the hole in the hand with anything
        {
            label.setEnabled(false);
            label.setVisible(false);
        }
        else //If the player's stack has cards, replace the hole in the hand with the next card
        {
            label.setIcon(img(pStack[0]+".jpg"));
            pNum[i] = pStack[0];
        }

        for(int j = 0; j<pStack.length-1; j++) //Shift the player's stack(array) down
        {
            pStack[j] = pStack[j+1];
        }

        //OPPONENTSIDE OPERATIONS
        int oIndex = (int)(Math.random()*var); //Randomly select a card from the hand
        oField = oNum[oIndex]; // set value for actual game comparison (value of opponent's fielded card)
        playCard[2].setIcon(img(oField+".jpg")); //set icon in battlefield
        if (oStack[0]==0) //If the opponent's stack is out of cards, don't replace the hole in the hand with anything
        {
            var--;
            for (int j = oIndex; j < oNum.length-1; j++)
            {
                oNum[j] = oNum[j+1];
            }
            oCard[var+1].setIcon(null);
        }
        else //If the opponent's stack has cards, replace the hole in the hand with the next card
        {
            oNum[oIndex] = oStack[0];
            for(int j = 0; j<oStack.length-1; j++)
            {
                oStack[j] = oStack[j+1];
            }
        }

        //CALCULATIONS
        
        int enable = -1; //Sets up variables to keep disabled cards disabled
        for (int j = 0; j<pCard.length; j++)
        {
            if (!pCard[j].isEnabled())
                enable = j;
        }
        for (int j = 0; j<pCard.length; j++) //disables cards so that player can't click them prematurely
        {
            pCard[j].setEnabled(false);
        }
        
        //COMPARISONS
        if ((pField/10)>(oField/10)) //If player wins
        {
            pStack[pCount] = pField;
            pStack[pCount+1] = oField;
            pCount++;//not +=2 because everything gets shifted down by 1
            oCount--;
            if (battleCount!=0) //Additional considerations if battle is won
            {
                pCount++;
                pStack[pCount] = pBattle;
                pStack[pCount+1] = oBattle;
                battleCount = 0;
                pCount++;
                if (oCount!=0)
                    oCount--;
                playCard[3].setText("");
            }
            Timer clear = new Timer(1500,new ActionListener() //clears field 
                    {
                        public void actionPerformed(ActionEvent e)
                        {
                            for (int j = 0; j<playCard.length-1; j++)
                                playCard[j].setIcon(null);
                        }
                    });
            clear.setRepeats(false);
            clear.start();
        }
        else if ((pField/10)<(oField/10)) //If opponent wins
        {
            oStack[oCount] = oField;
            oStack[oCount+1] = pField;
            oCount++;
            if (pCount!=0)
                pCount--;
            if (battleCount!=0) //Additional considerations if battle is won
            {
                oCount++;
                oStack[oCount] = oBattle;
                oStack[oCount+1] = pBattle;
                oCount++;
                if (pCount!=0)
                    pCount--;
                battleCount = 0;
                playCard[3].setText("");
            }
            Timer clear = new Timer(1500,new ActionListener() //clears field
                    {
                        public void actionPerformed(ActionEvent e)
                        {
                            for (int j = 0; j<playCard.length-1; j++)
                                playCard[j].setIcon(null);
                        }
                    });
            clear.setRepeats(false);
            clear.start();
        }
        else
        {
            if (battleCount==0)
            {
                Timer hold = new Timer(1500,new ActionListener() // sets aside cards as ante for war
                        {
                            public void actionPerformed(ActionEvent e)
                            {
                                //Sets centre text to inform player that the battle has begun
                                playCard[3].setText("WAR!");
                                playCard[3].setHorizontalAlignment(JLabel.CENTER);
                                playCard[3].setForeground(Color.white);
                                playCard[3].setFont(new Font("Lucida Console",Font.BOLD,30));
                                
                                //Shift fielded cards aside as ante
                                playCard[1].setIcon(img(oField+".jpg"));
                                oBattle = oField;
                                playCard[5].setIcon(play);
                                pBattle = pField;
                                playCard[2].setIcon(null);
                                playCard[4].setIcon(null);
                                
                                battleCount++;
                            }
                        });
                hold.setRepeats(false);
                hold.start();
                if (pCount!=0)
                    pCount--;
                if (oCount!=0)
                    oCount--;
            }
            else //If battle cards are equal, all is returned to players.
            {
                //Send cards back to their owners
                playCard[3].setText("Draw");
                oStack[oCount] = oField;
                oStack[oCount+1] = oBattle;
                oCount++;
                pStack[pCount] = pField;
                pStack[pCount+1] = pBattle;
                pCount++;
                Timer hold = new Timer(1500,new ActionListener() // clears all cards
                        {
                            public void actionPerformed(ActionEvent e)
                            {
                                for (int j = 0; j<playCard.length-1; j++)
                                    playCard[j].setIcon(null);
                                playCard[3].setText("");
                                battleCount = 0;
                            }
                        });
                hold.setRepeats(false);
                hold.start();
            }
        }
        Timer hold = new Timer(1500,new ActionListener() //re-enables  cards
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        for (int j = 0; j<pCard.length; j++)
                        {
                            pCard[j].setEnabled(true);
                        }
                    }
                });
        hold.setRepeats(false);
        hold.start();

        if (enable!=-1) //Re-disables cards disabled for other purposes (not enough cards left in deck)
            pCard[enable].setEnabled(false);
            
        //Update stack count
        ODcount.setText(""+arrCount(oStack));
        PDcount.setText(""+arrCount(pStack));
        
        //End game conditions
        if (arrCount(pStack)==0) //CPU wins
        {
            war.music("Quit.wav");
            JOptionPane.showMessageDialog(frame, "The firewall has defeated you. It's over, hacker.");
            exit();
        }
        else if (arrCount(oStack)==0) //Player wins
        {
            war.music("Quit.wav");
            JOptionPane.showMessageDialog(frame, "You have defeated the firewall. Well done, hacker.");
            exit();
        }
    }

    public void exit() //Disposes everything that needs to be disposed, and opens main menu.
    {
        war.bgMusic.close();
        war.music("Quit.wav");
        pause.dispose();
        war.frame.setVisible(true);
        frame.dispose();
    }

    static int[] shuffle() //Initial shuffling of cards
    { 
        int deck[] = new int[54];//deck array
        String valS;//String used to create image paths
        int i = 0;//index
        for (int val = 2; val<=14; val++)//For loop: value of Card (2 to 14(Ace))
        {
            for (int suite = 1; suite<=4; suite++)//For loop: suite (1=Diamonds,2=Spades,3=Hearts,4=Clubs)
            {valS = val+""+suite;
                deck[i] = Integer.parseInt(valS);
                i++;
            }
        }

        //Add jokers to deck
        deck[52] = 14;
        deck[53] = 15;

        // = = = = = SHUFFLING = = = = = \\
        // Creating a object for Random class 
        Random r = new Random();
        // Start from the last element and swap one by one. 
        for (i = (deck.length)-1; i>0; i--) { 
            // Pick a random index from 0 to i 
            int j = r.nextInt(i+1);
            // Swap arr[i] with the element at random index 
            int temp = deck[i]; 
            deck[i] = deck[j]; 
            deck[j] = temp; 
        } 
        // Returns the random array 
        return deck; 
    }

    public void initMsg() //Ready up pop-up
    {
        init = new JFrame();
        init.setLayout(new GridLayout(3,1));

        JPanel ready = new JPanel(new GridLayout(2,1,5,5));
        ready.setBackground(Color.black);

        JLabel space = new JLabel("Press space to ready up");
        space.setHorizontalAlignment(JLabel.CENTER);
        space.setForeground(Color.white);
        space.setFont(new Font("Lucida Console",Font.PLAIN,40));

        p1txt = new JLabel("Hacker"); //Text that turns green when space is pressed
        p1txt.setFont(new Font("Lucida Console",Font.PLAIN,40));
        p1txt.setForeground(Color.white);
        p1txt.setHorizontalAlignment(JLabel.CENTER);
        ready.add(p1txt);

        p1stat = new JLabel(img("no.png")); //image that turns from "x" to a checkmark when space is pressed
        p1stat.setHorizontalAlignment(JLabel.CENTER);
        ready.add(p1stat);

        enter = new JLabel("..."); //Text that changes to ominous phrase
        enter.setFont(new Font("Lucida Console",Font.PLAIN,40));
        enter.setForeground(Color.white);
        enter.setHorizontalAlignment(JLabel.CENTER);

        //Adding everything to pop-up frame content
        Container cont = init.getContentPane();
        cont.add(space);
        cont.add(ready);
        cont.add(enter);
        cont.setBackground(Color.black);

        init.setUndecorated(true);
        init.setSize(1000,500);
        init.setVisible(true);
        init.addKeyListener(this); //adds listener to frame to listen for space pressed 
        init.setLocationRelativeTo(null);
    }

    public void shuffleDialog() //Second flavour pop-up
    {
        init2 = new JFrame();
        init2.setLayout(new GridLayout(1,1));

        Container cont = init2.getContentPane();
        cont.setBackground(Color.black);

        JPanel shuffPane = new JPanel(new BorderLayout(5,5));
        shuffPane.setBackground(Color.black);
        shuffPane.setBorder(BorderFactory.createEtchedBorder());

        JLabel shuff = new JLabel(img("DribbleUpDown.gif")); //Fun gif
        shuff.setHorizontalAlignment(JLabel.CENTER);
        shuffPane.add(shuff,BorderLayout.CENTER);

        JLabel shuffText = new JLabel("Shuffling"); //Text that changes
        shuffText.setHorizontalAlignment(JLabel.CENTER);
        shuffText.setFont(new Font("Lucida Console",Font.ITALIC,30));
        shuffText.setBackground(Color.black);
        shuffText.setForeground(Color.white);
        shuffPane.add(shuffText,BorderLayout.SOUTH);

        //Dots - Every 400 milliseconds, a dot is added to the text to give an animated "..." - until 3 dots, then it resets
        dotNum = 0;
        Timer dots = new Timer(400,new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        if (dotNum!=3) //adding dots
                        {
                            shuffText.setText(shuffText.getText()+".");
                            dotNum++;
                        }
                        else //reset at 3
                        {
                            shuffText.setText("Shuffling");
                            dotNum = 0;
                        }
                    }
                });
        dots.start();
        Timer dotStop = new Timer(3000,new ActionListener() //after 3 seconds, it informs the player shuffling is complete (flavour)
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        dots.stop();
                        shuffText.setText("Shuffle complete.");
                        Timer dotStopPlus = new Timer(2000,new ActionListener() //After 2 seconds, instruct player to press space.
                                {
                                    public void actionPerformed(ActionEvent e)
                                    {
                                        shuffText.setText("Press space to begin hacking.");
                                    }
                                });
                        dotStopPlus.setRepeats(false);
                        dotStopPlus.start();
                    }
                });
        dotStop.setRepeats(false);
        dotStop.start();

        cont.add(shuffPane);

        init2.setUndecorated(true);
        init2.setSize(800,450);
        init2.setVisible(true);
        init2.addKeyListener(this); //Listens for space
        init2.setLocationRelativeTo(null);
    }

    public void escapeDialog() //Pause menu pop-up
    {
        pause = new JFrame();
        pause.setLayout(new GridLayout(1,1));

        Container cont = pause.getContentPane();
        cont.setBackground(Color.black);

        JPanel menu = new JPanel(new GridLayout(3,1,10,10));
        menu.setBackground(Color.black);
        menu.setBorder(BorderFactory.createEtchedBorder());

        JButton main = new JButton("Main Menu"); //Allows player to go to the home screen
        main.setForeground(Color.white);
        main.setFont(new Font("Lucida Console",Font.PLAIN,25));
        main.setBackground(new Color(0,0,0));
        main.setFocusable(false);
        main.addActionListener(this);
        main.setActionCommand("main");
        menu.add(main);

        mute = new JButton(img("mute0.png")); //Mute music (non-functional)
        mute.setForeground(Color.white);
        mute.setFont(new Font("Lucida Console",Font.PLAIN,25));
        mute.setBackground(Color.black);
        mute.addActionListener(this);
        mute.setActionCommand("mute");
        mute.enable(false); //Disabled because music does not close when requested
        menu.add(mute);

        //Date and time updater
        Timer timeTimer = new Timer(1000,new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
                        LocalDateTime now = LocalDateTime.now();  
                        time.setText(dtf.format(now));
                    }
                });
        timeTimer.start();

        time = new JLabel("");
        time.setFont(new Font("Candara",Font.PLAIN,20));
        time.setForeground(Color.white);
        time.setBackground(new Color(0,0,0,0));
        time.setHorizontalAlignment(JLabel.LEFT);
        time.setVerticalAlignment(JLabel.BOTTOM);
        menu.add(time);

        cont.add(menu);

        pause.setUndecorated(true);
        pause.setSize(600,600);
        pause.setVisible(false);
        pause.addKeyListener(this);
        pause.setLocationRelativeTo(null);
    }

    @Override
    public void keyPressed(KeyEvent e) 
    {
        if (e.getKeyCode()==KeyEvent.VK_SPACE)
        {
            if (initCount==0) //On "Ready up" pop-up
            {
                war.music("Quit.wav");
                war.music("MainMenu.wav"); //Begin music
                p1stat.setIcon(img("yes.png")); //Change icon to checkmark
                p1txt.setForeground(new Color(0,153,0));
                enter.setText("Very well, hacker. Prepare yourself."); //Aforementioned ominous text

                Timer inits = new Timer(3500,new ActionListener() //After a pause (so player can read), go to "shuffling" pop-up.
                        {
                            public void actionPerformed(ActionEvent e)
                            {
                                init.setVisible(false);
                                shuffleDialog();

                                initCount++;
                            }
                        });
                inits.setRepeats(false);
                inits.start();
            }
            else if (initCount ==1) //On "shuffling: pop-up
            {
                //close pop-ups
                init.dispose();
                init2.dispose();
                //remove main menu frame
                war.frame.setVisible(false);
                //Enable game & make game frame visible
                frame.enable(true);
                frame.setVisible(true);

                initCount++;//Makes space useless hereforth
            }
        }
        if (e.getKeyCode()==KeyEvent.VK_ESCAPE)
        {
            if (escCount == 0) //If escape is off
            {
                frame.enable(false); //Disable game frame (effectively makes escape modal)
                pause.setVisible(true); //Pulls up escape menu
                escCount++;
            }
            else //If escape is on
            {
                pause.setVisible(false); //Removes escape menu
                frame.setVisible(true); //Focuses on game frame
                frame.enable(true); //Enables game
                escCount--;
            }
        }
    }

    //Necessary methods for KeyListener
    @Override
    public void keyReleased(KeyEvent e)
    {
    }

    @Override
    public void keyTyped(KeyEvent e) 
    {
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand().equals("mute")) //Mute button commands (do not work presently)
        {
            if (muteCount==1)
            {
                mute.setIcon(img("mute0.png"));

                war.music("Button1.wav");
                muteCount--;
                war.music("MainMenu.wav");
            }
            else
            {
                mute.setIcon(img("mute1.png"));

                war.bgMusic.close();
                war.music("Button1.wav");
                muteCount++;
            }
        }
        else if (e.getActionCommand().equals("main")) //Sends player back to main menu from escape menu
        {
            exit();
        }
    }

    public void mousePressed(MouseEvent e) 
    {
        JLabel label = (JLabel) e.getSource();//Get which label was clicked
        war.music("card.wav");
        if (label.isEnabled())
        {
            for (int i = 0; i < pCard.length; i++) //FOR LOOP: find index of which card was clicked
            {
                if (pCard[i].getIcon() == label.getIcon()) 
                {
                    ops(label,i);
                    break;
                }
            }
        }
    }

    public void mouseReleased(MouseEvent e) 
    {

    }

    //Dynamic border
    public void mouseEntered(MouseEvent e) 
    {
        JLabel label = (JLabel) e.getSource();
        label.setBorder(BorderFactory.createEtchedBorder(new Color(66,224,209),new Color(20,225,225)));
    }

    public void mouseExited(MouseEvent e) 
    {
        JLabel label = (JLabel) e.getSource();
        label.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED),BorderFactory.createBevelBorder(BevelBorder.LOWERED)));
    }

    public void mouseClicked(MouseEvent e) 
    {
    }

    protected static ImageIcon img(String path)
    {
        java.net.URL imgURL = WarGame.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon (imgURL);
        }
        else
        {
            System.err.println("Couldn't find file: "+path);
            return null;
        }
    }
}
