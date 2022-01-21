import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClientObject extends JFrame implements ActionListener, Runnable {
    private JTextArea output;
    private JTextField input;
    private JButton sendBtn;
    private Socket socket;
    private ObjectInputStream reader = null;
    private ObjectOutputStream writer = null;

    private String nickname;

    public ChatClientObject(){
        output = new JTextArea();
        output.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        output.setEditable(false);
        JScrollPane scroll = new JScrollPane(output);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel bottom = new JPanel();
        bottom.setLayout(new BorderLayout());
        input = new JTextField();

        sendBtn = new JButton("보내기");

        bottom.add("Center", input);
        bottom.add("East", sendBtn);

        Container c = this.getContentPane();
        c.add("Center", scroll);
        c.add("South", bottom);

        setBounds(300, 300, 300, 300);
        setVisible(true);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try{
                    InfoDTO dto = new InfoDTO();
                    dto.setNickname(nickname);
                    dto.setCommand(Info.EXIT);
                    writer.writeObject(dto);
                    writer.flush();
                }catch(IOException io){
                    io.printStackTrace();
                }
            }
        });
    }
    public static void main(String[] args){
        new ChatClientObject().service();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try{
            String msg = input.getText();
            InfoDTO dto = new InfoDTO();

            if(msg.equals("exit")){
                dto.setCommand(Info.EXIT);
            }else{
                dto.setCommand(Info.SEND);
                dto.setMessage(msg);
                dto.setNickname(nickname);
            }
            writer.writeObject(dto);
            writer.flush();
            input.setText("");
        }catch(IOException io){
            io.printStackTrace();
        }
    }

    public void service(){
        String serverIp = JOptionPane.showInputDialog(this, "서버IP를 입력하세요", "192.168.0.8");
        if(serverIp == null || serverIp.length() == 0){
            System.out.println("서버 IP가 입력되지 않았습니다. ");
            System.exit(0);
        }
        nickname = JOptionPane.showInputDialog(this, "닉네임을 입력하세요", "닉네임", JOptionPane.INFORMATION_MESSAGE);
        if(nickname == null || nickname.length() == 0){
            nickname = "guest";
        }try{
            socket = new Socket(serverIp, 9500);
            reader = new ObjectInputStream(socket.getInputStream());
            writer = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("전송 준비 완료!");
        }catch(UnknownHostException e){
            System.out.println("서버를 찾을 수 없습니다. ");
            e.printStackTrace();
            System.exit(0);
        }catch(IOException e){
            System.out.println("서버와 연결이 되지 않았습니다. ");
            e.printStackTrace();
            System.exit(0);
        }

        try{
            InfoDTO dto = new InfoDTO();
            dto.setCommand(Info.JOIN);
            dto.setNickname(nickname);
            writer.writeObject(dto);
            writer.flush();
        }catch(IOException e){
            e.printStackTrace();
        }

        Thread t = new Thread(this);
        t.start();
        input.addActionListener(this);
        sendBtn.addActionListener(this);
    }


    @Override
    public void run() {
        InfoDTO dto = null;
        while(true){
            try{
                dto = (InfoDTO) reader.readObject();
                if(dto.getCommand() == Info.EXIT){
                    reader.close();
                    writer.close();
                    socket.close();
                    System.exit(0);
                }else if(dto.getCommand() == Info.SEND){
                    output.append(dto.getMessage() + "\n");

                    int pos = output.getText().length();
                    output.setCaretPosition(pos);
                }
            }catch(IOException e){
                e.printStackTrace();
            }catch(ClassNotFoundException e){
                e.printStackTrace();
            }
        }
    }
}
