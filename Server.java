package chatapp;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends JFrame implements ActionListener{
    JButton openbutton;
    JTextField textField;
    JButton sendButton;
    static JTextArea textArea;
    String path = new String("Nothing");
    static Socket s;
    static DataInputStream din;
    static DataOutputStream dout;
    static ObjectInputStream ois;
    static ObjectOutputStream oos;
    private static final int CHAT_BOX_WIDTH = 150;
    private static final int CHAT_BOX_HEIGHT = 150;
    private static JPanel boxPanel;

    static ServerSocket ss;
    static String msgin = "";
    static JScrollPane scrollPane;

    // Constructor
    Server() throws IOException{
        super("Server");
        this.setLayout(new BorderLayout());
        textField = new JTextField();
        textField.setPreferredSize(new Dimension(250,40));
        //textField.setBounds(100,330,400,30);
        textField.setFont(new Font("Arial",Font.PLAIN,30));
        openbutton = new JButton("Select an image");
        //openbutton.setBounds(0, 330, 100, 30);
        openbutton.setPreferredSize(new Dimension(100,50));
        openbutton.addActionListener(this);
        openbutton.setFocusable(false);
        sendButton = new JButton("Send");
        //sendButton.setBounds(500,330,100,30);
        sendButton.setPreferredSize(new Dimension(100,50));
        sendButton.addActionListener(this);
        sendButton.setFocusable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(openbutton);
        panel.add(textField);
        panel.add(sendButton);

        boxPanel = new JPanel();
        boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));

        scrollPane = new JScrollPane(boxPanel);
        scrollPane.setPreferredSize(new Dimension(700,200));
        this.add(scrollPane, BorderLayout.CENTER);

        this.setMinimumSize(new Dimension(CHAT_BOX_WIDTH + 150, 400));
        this.setLocationByPlatform(true);
        this.pack();

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(700, 400);
        this.add(panel,BorderLayout.SOUTH);

        this.setVisible(true);
    }


    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Server server = new Server();
        ss = new ServerSocket(1201); //server port number
        s = ss.accept(); // accepts connection
        System.out.println("CC");
        try{
            oos = new ObjectOutputStream(s.getOutputStream());
            oos.flush();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        try{
            ois = new ObjectInputStream(s.getInputStream());
        }
        catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("Connection successful");
        while (true) {
            Message<?> msg = (Message<?>) ois.readObject();
            System.out.println("Got");
            if(msg.getPayload() instanceof String){
                if(msg.getPayload().equals("exit")){
                    break;
                }
                else {
                    addLeftChat(((String) msg.getPayload()).toString());
                    JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
                    SwingUtilities.invokeLater(() -> verticalScrollBar.setValue(verticalScrollBar.getMaximum()));
                }

            }
            else if(msg.getPayload() instanceof ImageIcon) {
                addLeftImage((ImageIcon) msg.getPayload());
            }
            JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
            SwingUtilities.invokeLater(() -> verticalScrollBar.setValue(verticalScrollBar.getMaximum()));
        }

    }

    // To add image
    private static void addImage(ImageIcon icon,int flowLayoutAlign, Color borderColor){
        JPanel panel = new JPanel(new FlowLayout(flowLayoutAlign));
        JLabel label = new JLabel();
        label.setBorder(BorderFactory.createLineBorder(borderColor));
        label.setIcon(icon);
        panel.add(label);
        boxPanel.add(panel);
        boxPanel.revalidate();
    }
    private void addRightImage(ImageIcon icon) {
        addImage(icon, FlowLayout.TRAILING, Color.red);
    }

    private static void addLeftImage(ImageIcon icon) {
        addImage(icon,FlowLayout.LEADING, Color.green);
    }

    private static void addChat(String s, int flowLayoutAlign, Color borderColor) {
        JPanel panel = new JPanel(new FlowLayout(flowLayoutAlign));
        JLabel label = new JLabel();
        label.setBorder(BorderFactory.createLineBorder(borderColor));
        if(borderColor==Color.red){
            label.setText("<html><body style='width: " + CHAT_BOX_WIDTH + "px;'>"+"You: "+s );
        }
        else if(borderColor==Color.green){
            label.setText("<html><body style='width: " + CHAT_BOX_WIDTH + "px;'>"+"Server: "+s);
        }
        panel.add(label);
        boxPanel.add(panel);
        boxPanel.revalidate();
    }

    private void addRightChat(String s) {
        addChat(s, FlowLayout.TRAILING, Color.red);
    }

    private static void addLeftChat(String s) {
        addChat(s,FlowLayout.LEADING, Color.green);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==openbutton){
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "Images", "jpg", "gif","jpeg","png");
            chooser.setFileFilter(filter);
            int returnVal = chooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                System.out.println("You chose to open this file: " +
                        chooser.getSelectedFile());
            }
            path = chooser.getSelectedFile().toString();
        }
        else if(e.getSource()==sendButton){
            if(path.compareTo("Nothing")==0){
                try{
                    String msgout = "";
                    msgout = textField.getText().trim();
                    textField.setText("");
                    oos.writeObject(new Message<String>(msgout));
                    addRightChat((String)msgout);
                    JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
                    SwingUtilities.invokeLater(() -> verticalScrollBar.setValue(verticalScrollBar.getMaximum()));
                }catch(Exception ex) {
                    System.out.println(ex);
                }
            }
            else {
                ImageIcon icon = new ImageIcon(path);
                try {
                    oos.writeObject(new Message<ImageIcon>(icon));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                addRightImage(icon);
                path = "Nothing";
                JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
                SwingUtilities.invokeLater(() -> verticalScrollBar.setValue(verticalScrollBar.getMaximum()));
            }
        }

    }
}
class Message<T extends Serializable> implements Serializable {
    private T payload;

    public Message() {
        super();
    }

    public Message(T data) {
        super();
        setPayload(data);
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T aPayload) {
        payload = aPayload;
    }
}

