/**
 * The menu modules for the game of WAR.
 *
 * @author (Timurul H. Kazi)
 * @version (May 31st, 2019)
 */
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
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
public class War extends JPanel implements ActionListener
{
    //
    CardLayout cdLay = new CardLayout();//Layout to switch screens
    public Clip bgMusic; //Makes music accessible anywhere (i.e. closeable)
    static JFrame frame; //Making frame global makes it disposable on command
    
    //Mute button
    JButton mute,mute1,mute2,mute3;
    int muteCount = 0;
    
    //Date & Time Labels
    static JLabel time,time1,time3,time4;//time2
    
    //Instructions labels/text
    JLabel inst[] = new JLabel[8];
    String instTxt[] = {"//MECHANICS\\\\","The escape key brings up the menu",
            "Your goal as a hacker is to delete all of the opponent's data - the number under the top right stack - while increasing your own (bottom left)",
            "Click on the card you want to place to select it","Every time you put down a card, it will be compared to the opponent's",
            "> If it's greater, you get both cards", "> If it's lower, the opponent gets both cards","",
            "//BATTLE\\\\",
            "If both cards are equal, the cards are set aside as an ante - the next card you and your opponent draw will decide who gets all 4",
            "> If they're equal twice in a row, the cards are returned to their initial owners",
            "//STRATEGY\\\\",
            "When either side wins cards, they go to the bottom of that side's stack - ",
            "Whenever someone places a card, that card is replenished from the top of their stack",
            "The ideal scenario for losing is placing a card much lower than your opponent's;",
            "The ideal scenario for winning is placing a card only slightly greater than your opponent's"};

    public static void main (String args[])
    {
        frame = new JFrame(":: War ::");
        War content = new War();
        frame.setContentPane(content);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(true); //Removes standard window frame
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        timer();//Calls the timer to begin the date/time updating on each page
    }

    public War() 
    {
        music('y',"MainMenu.wav");//Initiates music
        setLayout(cdLay);
        menu();
        instruct();
        //score();
        credits();
    }

    public void menu()
    {
        //BACKGROUND
        JLabel background = new JLabel(img("MenuBG.gif"));
        background.setLayout(new BorderLayout(5,5));
        setBackground(Color.black);

        //NORTH
        JPanel north = new JPanel(new BorderLayout(1,1));
        north.setBackground(new Color(0,0,0,0));

        JLabel title = new JLabel(img("WARtitle.gif"));//Title
        title.setHorizontalAlignment(JLabel.CENTER);
        north.add(title,BorderLayout.CENTER);

        if (muteCount==0)
            mute = new JButton(img("mute0.png"));
        else
            mute = new JButton(img("mute1.png"));
        mute.addActionListener(this);
        mute.setActionCommand("mute");
        mute.setBackground(new Color(0,0,0,0));
        mute.setFocusable(false);
        mute.setBorderPainted(false);
        mute.setFocusPainted(false);
        north.add(mute,BorderLayout.EAST);

        //WEST
        JPanel west = new JPanel(new GridLayout(5,1,20,20));
        west.setBackground(new Color(0,0,0,0));//Fourth 0 makes background transparent
        west.setBorder(new EmptyBorder(10,10,10,10));
        west.setSize(new Dimension(300,400));

        //Menu Buttons
        String optText[] = {"PLAY","INSTRUCTIONS","CREDITS","QUIT"};
        String optCmd[] = {"play","inst","credits","quit"};
        JButton opt[] = new JButton[4];
        for (int i = 0; i<4; i++) //Added in a loopusing string arrays because of the excessive number of mutators
        {
            opt[i] = new JButton();
            opt[i].setText(optText[i]);
            opt[i].setForeground(Color.white);
            opt[i].setBackground(new Color(0,0,0,90));
            opt[i].addActionListener(this);
            opt[i].setActionCommand(optCmd[i]);
            opt[i].setFont(new Font("Candara",Font.BOLD,27));
            opt[i].setPreferredSize(new Dimension(460, 300));
            west.add(opt[i]);
        }

        //EAST
        JPanel east = new JPanel();
        east.setBackground(new Color(0,0,0,0));
        east.setPreferredSize(new Dimension(460,200));

        //Image that is replaced every 4 seconds
        JLabel rImg = new JLabel(img("11.jpg"));
        rImg.setHorizontalAlignment(JLabel.LEFT);
        rImg.setVerticalAlignment(JLabel.CENTER);
        east.add(rImg);

        Timer rotate = new Timer(4000,new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        int n1 = (int)(Math.random()*14+1);
                        int n2 = (int)(Math.random()*3+1);
                        String path = n1+""+n2;
                        rImg.setIcon(img(path+".jpg"));
                    }
                });
        rotate.start();

        //SOUTH
        JPanel south = new JPanel();
        south.setBackground(new Color(0,0,0,80));
        south.setPreferredSize(new Dimension(200,120));
        south.setLayout(new BorderLayout(5,5));

        //Date and time
        time = new JLabel();
        time.setFont(new Font("Candara",Font.PLAIN,20));
        time.setForeground(Color.white);
        time.setBackground(new Color(0,0,0));
        time.setHorizontalAlignment(JLabel.LEFT);
        south.add(time,BorderLayout.SOUTH);

        //A few facts (replaced at random every 7 seconds)
        JPanel tipPan = new JPanel();
        tipPan.setBackground(new Color(0,0,0,75));
        String tipContent[] = {"War is called 'Battle' in the UK and 'Tod und Leben' in Germany",
                "'War' is studied by mathematicians because it is notoriously random",
                "Is an infinite game of war possible?"};
        JLabel tips = new JLabel("~ Tips ~");
        tips.setFont(new Font("Candara New",Font.ITALIC,28));
        tips.setForeground(Color.white);
        tips.setHorizontalAlignment(JLabel.CENTER);
        tips.setVerticalAlignment(JLabel.CENTER);
        tipPan.add(tips);
        south.add(tipPan,BorderLayout.CENTER);

        Timer tipTimer = new Timer(7000,new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        int i = (int)(Math.random()*3);
                        tips.setText(tipContent[i]);
                    }
                });
        tipTimer.start();

        //ADDITION
        background.add(north,BorderLayout.NORTH);
        background.add(west,BorderLayout.WEST);
        background.add(east,BorderLayout.EAST);
        background.add(south,BorderLayout.SOUTH);

        add("0",background);
    }

    public void instruct()
    {
        //BACKGROUND
        JLabel background = new JLabel(img("MenuBG.gif"));
        background.setLayout(new BorderLayout(5,5));
        setBackground(Color.black);

        //NORTH
        JPanel north = new JPanel(new BorderLayout(1,1));
        north.setBackground(new Color(0,0,0,80));
        north.setPreferredSize(new Dimension(1200,120));

        JLabel title = new JLabel("<html><b><u>INSTRUCTIONS</u></b></html>");//Title
        title.setFont(new Font("Lucida Console",Font.ITALIC,45));
        title.setForeground(Color.white);
        title.setHorizontalAlignment(JLabel.CENTER);
        north.add(title,BorderLayout.CENTER);

        if (muteCount==0)
            mute1 = new JButton(img("mute0.png"));
        else
            mute1 = new JButton(img("mute1.png"));
        mute1.addActionListener(this);
        mute1.setActionCommand("mute");
        mute1.setBackground(new Color(0,0,0,80));
        mute1.setFocusable(false);
        mute1.setBorderPainted(false);
        mute1.setFocusPainted(false);
        mute1.enable(false);
        north.add(mute1,BorderLayout.EAST);

        JButton back = new JButton("Back");//Back to main menu 
        back.setActionCommand("back");
        back.addActionListener(this);
        back.setBackground(new Color(108,28,167,90));
        back.setForeground(Color.white);
        back.setFont(new Font("Lucida Console",Font.BOLD,20));
        north.add(back,BorderLayout.WEST);

        //CENTER
        JPanel center = new JPanel(new GridLayout(2,1,5,5));
        center.setBackground(new Color(0,0,0,80));

        //Image of the playing field
        JLabel instImg = new JLabel(img("inst.png"));
        center.add(instImg);

        //2 pages of instructions that can be flipped between using the buttons
        JPanel instruct = new JPanel(new GridLayout(8,1));
        instruct.setPreferredSize(new Dimension(700,450));
        instruct.setBackground(new Color(0,0,0,80));

        for (int i = 0; i<8; i++) //Creating and adding label with instructions - using a loop due to the quantity of labels
        {
            inst[i] = new JLabel("> "+instTxt[i]);
            inst[i].setHorizontalAlignment(JLabel.LEFT);
            inst[i].setForeground(Color.white);
            instruct.add(inst[i]);
        }
        center.add(instruct);

        //EAST & WEST
        JPanel east = new JPanel(new GridLayout(1,1));
        east.setBackground(new Color(0,0,0,95));
        //Button: Next set of instructions
        JButton next = new JButton(">");
        next.setFont(new Font("Lucida Console",Font.BOLD,70));
        next.setForeground(Color.white);
        next.setVerticalAlignment(JLabel.CENTER);
        next.setHorizontalAlignment(JLabel.CENTER);
        next.addActionListener(this);
        next.setActionCommand("next");
        next.setBackground(new Color(0,0,0,80));
        next.setBorderPainted(false);
        next.setFocusPainted(false);
        east.add(next);

        JPanel west = new JPanel(new GridLayout(1,1));
        west.setBackground(new Color(0,0,0,80));
        //Button: Previous set of instructions
        JButton prev = new JButton("<");
        prev.setFont(new Font("Lucida Console",Font.BOLD,70));
        prev.setForeground(Color.white);
        prev.setVerticalAlignment(JLabel.CENTER);
        prev.setHorizontalAlignment(JLabel.CENTER);
        next.addActionListener(this);
        prev.setActionCommand("prev");
        prev.setBackground(new Color(0,0,0,80));
        prev.setBorderPainted(false);
        prev.setFocusPainted(false);
        west.add(prev);

        //SOUTH
        JPanel south = new JPanel(new GridLayout(1,1));
        south.setBackground(new Color(0,0,0,80));

        //Date & Time
        time1 = new JLabel();
        time1.setFont(new Font("Candara",Font.PLAIN,20));
        time1.setForeground(Color.white);
        time1.setBackground(new Color(0,0,0));
        time1.setHorizontalAlignment(JLabel.LEFT);
        south.add(time1);

        //ADDING
        background.add(north,BorderLayout.NORTH);
        background.add(south,BorderLayout.SOUTH);
        background.add(east,BorderLayout.EAST);
        background.add(west,BorderLayout.WEST);
        background.add(center,BorderLayout.CENTER);
        add("1",background);
    }

    //SEE README FILE;
    /*public void score()
    {
    //BACKGROUND
    JLabel background = new JLabel(img("MenuBG.gif"));
    background.setLayout(new BorderLayout(5,5));
    setBackground(Color.black);

    //NORTH
    JPanel north = new JPanel(new BorderLayout(1,1));
    north.setBackground(new Color(0,0,0,80));
    north.setPreferredSize(new Dimension(1200,120));

    JLabel title = new JLabel("<html><b><u>SCORE TRACKER</u></b></html>");
    title.setFont(new Font("Lucida Console",Font.ITALIC,45));
    title.setForeground(Color.white);
    title.setHorizontalAlignment(JLabel.CENTER);
    north.add(title,BorderLayout.CENTER);

    if (muteCount==0)
    mute2 = new JButton(img("mute0.png"));
    else
    mute2 = new JButton(img("mute1.png"));
    mute2.addActionListener(this);
    mute2.setActionCommand("mute");
    mute2.setBackground(new Color(0,0,0,80));
    mute2.setFocusable(false);
    mute2.setBorderPainted(false);
    mute2.setFocusPainted(false);
    north.add(mute2,BorderLayout.EAST);

    JButton back = new JButton("Back");
    back.setActionCommand("back");
    back.addActionListener(this);
    back.setBackground(new Color(108,28,167,90));
    back.setForeground(Color.white);
    back.setFont(new Font("Lucida Console",Font.BOLD,20));
    north.add(back,BorderLayout.WEST);

    //CENTER
    JPanel center = new JPanel(new BorderLayout(2,2));
    center.setBackground(new Color(0,0,0,99));

    //headers
    JPanel headers = new JPanel(new GridLayout(1,2));
    headers.setPreferredSize(new Dimension(100,40));
    headers.setBackground(new Color(0,0,0,0));

    JLabel cpuHeader = new JLabel("PvE Scores");
    cpuHeader.setFont(new Font("Lucida Console", Font.BOLD, 35));
    cpuHeader.setForeground(Color.white);
    cpuHeader.setHorizontalAlignment(JLabel.CENTER);
    cpuHeader.setPreferredSize(new Dimension(20,20));

    JLabel pvpHeader = new JLabel("PvP Scores");
    pvpHeader.setFont(new Font("Lucida Console", Font.BOLD, 35));
    pvpHeader.setForeground(Color.white);
    pvpHeader.setHorizontalAlignment(JLabel.CENTER);
    pvpHeader.setPreferredSize(new Dimension(20,20));

    headers.add(cpuHeader);
    headers.add(pvpHeader);

    //figures
    String names[] = new String[16];
    String times[] = new String[16];
    //placeholders
    for (int i = 0; i<16; i++)
    {
    names[i] = "Sample "+(i+1);
    times[i] = "TimeSample "+(i+1);
    }
    JLabel name[] = new JLabel[16];
    JLabel time[] = new JLabel[16];
    JLabel place[] = new JLabel[16];

    //cpu scores
    JPanel cpu = new JPanel(new GridLayout(8,3,1,1));
    cpu.setBackground(new Color(0,0,0,99));
    for(int i = 0; i<8; i++)
    {
    place[i] = new JLabel((i+1)+".");

    name[i] = new JLabel(names[i]);
    name[i].setHorizontalAlignment(JLabel.LEFT);

    time[i] = new JLabel(times[i]);

    place[i].setForeground(Color.white);
    name[i].setForeground(Color.white);
    time[i].setForeground(Color.white);

    place[i].setFont(new Font("Lucida Console", Font.PLAIN, 18));
    name[i].setFont(new Font("Lucida Console", Font.PLAIN, 18));
    time[i].setFont(new Font("Lucida Console", Font.PLAIN, 18));

    cpu.add(place[i]);
    cpu.add(name[i]);
    cpu.add(time[i]);
    }

    //pvp scores
    JPanel pvp = new JPanel(new GridLayout(8,3,1,1));
    pvp.setBackground(new Color(0,0,0,99));
    for(int i = 8; i<16; i++)
    {
    place[i] = new JLabel((i-7)+".");

    name[i] = new JLabel(names[i]);
    name[i].setHorizontalAlignment(JLabel.LEFT);

    time[i] = new JLabel(times[i]);

    place[i].setForeground(Color.white);
    name[i].setForeground(Color.white);
    time[i].setForeground(Color.white);

    place[i].setFont(new Font("Lucida Console", Font.PLAIN, 18));
    name[i].setFont(new Font("Lucida Console", Font.PLAIN, 18));
    time[i].setFont(new Font("Lucida Console", Font.PLAIN, 18));

    pvp.add(place[i]);
    pvp.add(name[i]);
    pvp.add(time[i]);
    }

    JPanel figures = new JPanel(new GridLayout(1,2));
    figures.add(cpu);
    figures.add(pvp);
    figures.setBackground(new Color(0,0,0,0));

    center.add(headers,BorderLayout.NORTH);
    center.add(figures, BorderLayout.CENTER);

    //EAST
    JPanel east = new JPanel();
    east.setBackground(new Color(0,0,0,0));
    east.setPreferredSize(new Dimension(40,200));
    //WEST
    JPanel west = new JPanel();
    west.setBackground(new Color(0,0,0,0));
    west.setPreferredSize(new Dimension(40,200));
    //SOUTH
    JPanel south = new JPanel(new GridLayout(1,1));
    south.setBackground(new Color(0,0,0,80));

    time2 = new JLabel();
    time2.setFont(new Font("Candara",Font.PLAIN,20));
    time2.setForeground(Color.white);
    time2.setBackground(new Color(0,0,0));
    time2.setHorizontalAlignment(JLabel.LEFT);
    south.add(time2);

    //ADDING
    background.add(north,BorderLayout.NORTH);
    background.add(south,BorderLayout.SOUTH);
    background.add(east,BorderLayout.EAST);
    background.add(west,BorderLayout.WEST);
    background.add(center,BorderLayout.CENTER);

    add("2",background);
    }*/

    public void credits()
    {
        //BACKGROUND
        JLabel background = new JLabel(img("MenuBG.gif"));
        background.setLayout(new BorderLayout(5,5));
        setBackground(Color.black);

        //NORTH
        JPanel north = new JPanel(new BorderLayout(1,1));
        north.setBackground(new Color(0,0,0,80));
        north.setPreferredSize(new Dimension(1200,120));

        JLabel title = new JLabel("<html><b><u>CREDITS</u></b></html>"); //Title
        title.setFont(new Font("Lucida Console",Font.ITALIC,45));
        title.setForeground(Color.white);
        title.setHorizontalAlignment(JLabel.CENTER);
        north.add(title,BorderLayout.CENTER);

        if (muteCount==0)
            mute3 = new JButton(img("mute0.png"));
        else
            mute3 = new JButton(img("mute1.png"));
        mute3.addActionListener(this);
        mute3.setActionCommand("mute");
        mute3.setBackground(new Color(0,0,0,80));
        mute3.setFocusable(false);
        mute3.setBorderPainted(false);
        mute3.setFocusPainted(false);
        mute3.enable(false);
        north.add(mute3,BorderLayout.EAST);

        JButton back = new JButton("Back"); //Button: Back to main menu
        back.setActionCommand("back");
        back.addActionListener(this);
        back.setBackground(new Color(108,28,167,90));
        back.setForeground(Color.white);
        back.setFont(new Font("Lucida Console",Font.BOLD,20));
        north.add(back,BorderLayout.WEST);

        //CENTER
        JPanel center = new JPanel();
        center.setBackground(new Color(0,0,0,0));

        JPanel top = new JPanel(new FlowLayout()); //Top half
        top.setPreferredSize(new Dimension(1400,450));
        top.setBackground(new Color(0,0,0,0));

        //List of people who have contributed in some way
        JPanel people = new JPanel(new GridLayout(8,1)); ///Panel for layout purposes
        people.setPreferredSize(new Dimension(650,450));
        people.setBackground(new Color(0,0,0,99));

        JLabel peopleHead = new JLabel("Contributors"); //Section header
        peopleHead.setFont(new Font("Lucida Console", Font.BOLD, 35));
        peopleHead.setForeground(Color.white);
        peopleHead.setHorizontalAlignment(JLabel.CENTER);
        people.add(peopleHead);

        //Content - List of people & their contributions
        String pers[] = {"Mr. Couckuyt - The course this project was for has reinvigorated by interest in programming.",
                "Annie - For the derisive comments >_>",
                "Rishabh - For the advice from an elder",
                "Douglas, Farzan, & Punit - For keeping me sane",
                "Umang - For the stressed but productive interaction over the past few days",
                "Noel, Om, & Vetrivel - The best table in P1 ICS 2O8",
                "Precious - For the late nights and... everything"};
        JLabel person[] = new JLabel[7];
        for(int i = 0; i<7;i++) //Loop to create/mutate/add labels - enables easier changing of text by having it collected in the array above
        {
            person[i] = new JLabel("              "+pers[i]);
            person[i].setForeground(Color.white);
            person[i].setHorizontalAlignment(JLabel.LEFT);
            people.add(person[i]);
        }

        top.add(people);

        //Sources I used & for what
        JPanel sources = new JPanel(new GridLayout(19,1)); //Panel for layout/sizing
        sources.setPreferredSize(new Dimension(700,450));
        sources.setBackground(new Color(0,0,0,99));

        JLabel sourceHead = new JLabel("Sources (links)"); //Section Header
        sourceHead.setFont(new Font("Lucida Console", Font.BOLD, 35));
        sourceHead.setForeground(Color.white);
        sourceHead.setHorizontalAlignment(JLabel.CENTER);
        sources.add(sourceHead);

        JLabel source[] = new JLabel[18];
        String link[] = {"https://docs.oracle.com/javase/tutorial/uiswing/events/mouselistener.html",
                "https://stackoverflow.com/questions/13203694/setting-and-getting-an-object-in-a-jlabel-with-a-mouselistener",
                "https://www.math.uni-hamburg.de/doc/java/tutorial/uiswing/misc/timer.html",
                "https://www.javatpoint.com/java-get-current-date",
                "https://www.javacodex.com/Java-IO/AudioInputStream",  
                "https://docs.oracle.com/javase/7/docs/api/javax/sound/sampled/Clip.html",
                "https://docs.oracle.com/javase/8/docs/api/javax/swing/JDialog.html#JDialog-java.awt.Dialog-",
                "https://docs.oracle.com/javase/7/docs/api/javax/swing/BorderFactory.html#createBevelBorder(int)",
                "https://www.geeksforgeeks.org/shuffle-a-given-array-using-fisher-yates-shuffle-algorithm/"};
        String linkDef[] = {"Mouse Listener","Mouse Listener Object","Swing Timers","Date & Time","AudioInput","Audio Clip","JDialog","BorderFactory","Shuffling"};
        //placeholders
        int j = 0;
        for(int i = 0; i<source.length;i++) //For loop used to create/mutate/add labels - loop used for formatting (i.e. Definition <link>)
        {
            if (i%2 == 1)
            {
                source[i] = new JLabel("       <"+link[j]+">");
                j++;
            }
            else
                source[i] = new JLabel("   "+linkDef[j]);

            source[i].setForeground(Color.white);
            source[i].setHorizontalAlignment(JLabel.LEFT);
            sources.add(source[i]);
        }

        top.add(sources);

        center.add(top);

        //A little bit about myself
        JPanel self = new JPanel(); //Panel for layout/sizing
        self.setPreferredSize(new Dimension(1300,150));
        self.setLayout(new BoxLayout(self,BoxLayout.Y_AXIS));
        self.setBackground(new Color(0,0,0,99));

        JPanel selfHCenter = new JPanel(); //Section header panel - for Layout/formatting
        selfHCenter.setPreferredSize(new Dimension(1300,40));
        selfHCenter.setBackground(new Color(0,0,0,99));
        JLabel selfHead = new JLabel("Director HK"); //Section header
        selfHead.setFont(new Font("Lucida Console", Font.BOLD, 35));
        selfHead.setForeground(Color.white);
        selfHead.setHorizontalAlignment(JLabel.CENTER);
        selfHCenter.add(selfHead);
        self.add(selfHCenter);

        JPanel selfInfo = new JPanel(new GridLayout(3,2)); //Information panel
        selfInfo.setBackground(new Color(0,0,0,99));
        selfInfo.setPreferredSize(new Dimension(1300,100));

        String factTxt[] = {"I'm interested in Tech - primarily AR/VR, but I'm exploring",
                "I'm also interested in a whole lot of stuff - everything from Urban Design to History to Space Exploration",
                "I play the Guitar and Saxophone, and I want to learn Piano, Violin, and ~the Accordion~",
                "At the time of writing this, I'm craving those chocolate fingers","https://github.com/THKazi",""};
        JLabel fact[] = new JLabel[5];
        for(int i = 0; i < 5; i++) //For loop used to create/mutate/add labels - loop for ease of formatting
        {
            fact[i] = new JLabel("<"+(i+1)+"> "+factTxt[i]);
            fact[i].setForeground(Color.white);
            fact[i].setHorizontalAlignment(JLabel.CENTER);
            selfInfo.add(fact[i]);
        }
        self.add(selfInfo);
        center.add(self);

        //EAST
        JPanel east = new JPanel();
        east.setBackground(new Color(0,0,0,0));
        east.setPreferredSize(new Dimension(40,200));
        //WEST
        JPanel west = new JPanel();
        west.setBackground(new Color(0,0,0,0));
        west.setPreferredSize(new Dimension(40,200));
        //SOUTH
        JPanel south = new JPanel(new GridLayout(1,1));
        south.setBackground(new Color(0,0,0,80));

        //Date & time
        time3 = new JLabel();
        time3.setFont(new Font("Candara",Font.PLAIN,20));
        time3.setForeground(Color.white);
        time3.setBackground(new Color(0,0,0));
        time3.setHorizontalAlignment(JLabel.LEFT);
        south.add(time3);

        background.add(north,BorderLayout.NORTH);
        background.add(south,BorderLayout.SOUTH);
        background.add(center,BorderLayout.CENTER);
        background.add(east,BorderLayout.EAST);
        background.add(west,BorderLayout.WEST);

        add("3",background);
    }

    public static void timer() //Method used to update date & time across screens
    {
        Timer timeTimer = new Timer(1000,new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); //Format of time 
                        LocalDateTime now = LocalDateTime.now();  //Get time
                        //Combine time & format, and update for each screen
                        time.setText(dtf.format(now));  
                        time1.setText(dtf.format(now));  
                        //time2.setText(dtf.format(now));  
                        time3.setText(dtf.format(now));
                    }
                });
        timeTimer.start(); //Start timer
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand().equals("play")) //Play button
        {
            bgMusic.close();
            music("Button1.wav");
            WarGame play = new WarGame(); //instantiate game
            play.main(null); //Create game frame
            play.initMsg(); //Begin entry sequence
        }
        else if (e.getActionCommand().equals("inst"))
        {
            music("Button1.wav");
            cdLay.show(this,"1");
        }
        else if (e.getActionCommand().equals("next"))
        {
            music("Button1.wav");
            for (int i = 0; i<8; i++) //Update instructions
            {
                inst[i].setText("> "+instTxt[i+8]);
            }
        }
        else if (e.getActionCommand().equals("prev")) //Previous instructions
        {
            music("Button1.wav");
            for (int i = 0; i<8; i++)
            {
                inst[i].setText("> "+instTxt[i]);
                System.out.println(instTxt[i]);
            }
        }
        /*else if (e.getActionCommand().equals("score"))
        {
        music("Button1.wav");
        cdLay.show(this,"2");
        }*/
        else if (e.getActionCommand().equals("credits"))
        {
            music("Button1.wav");
            cdLay.show(this,"3");
        }
        else if (e.getActionCommand().equals("quit"))
        {
            music("Quit.wav");
            System.exit(0); //safe exit (closes System, so music & timers are also stopped)
        }
        else if (e.getActionCommand().equals("back"))
        {
            music("Button1.wav");
            cdLay.show(this,"0");
        }
        else if (e.getActionCommand().equals("mute"))
        {
            if (muteCount==1)
            {
                mute.setIcon(img("mute0.png"));
                mute1.setIcon(img("mute0.png"));
                //mute2.setIcon(img("mute0.png"));
                mute3.setIcon(img("mute0.png"));

                music("Button1.wav");
                muteCount--;
                music("MainMenu.wav");
            }
            else
            {
                mute.setIcon(img("mute1.png"));
                mute1.setIcon(img("mute1.png"));
                //mute2.setIcon(img("mute1.png"));
                mute3.setIcon(img("mute1.png"));

                bgMusic.close(); //Stop music
                music("Button1.wav");
                muteCount++;
            }
        }
    }

    public void music (String path) //Method Overloading - creates a default parameter for 'char loop' in the other music method
    //https://stackoverflow.com/questions/965690/java-optional-parameters
    {
        music('n',path);
    }

    public void music (char loop,String path)
    {
        try{
            URL url = this.getClass().getResource(path);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            bgMusic = AudioSystem.getClip();
            bgMusic.open(audioIn);
            if (loop=='y')
                bgMusic.loop(Clip.LOOP_CONTINUOUSLY);
            else
                bgMusic.loop(0);
        }
        catch(Exception g){
            JOptionPane.showMessageDialog(null,"There was an error loading the sound.");
        }
    }

    protected static ImageIcon img(String path)
    {
        java.net.URL imgURL = War.class.getResource(path);
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
