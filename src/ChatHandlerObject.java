import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ChatHandlerObject extends Thread{
    private ObjectInputStream reader;
    private ObjectOutputStream writer;
    private Socket socket;

    private List<ChatHandlerObject> list;

    public ChatHandlerObject(Socket socket, List<ChatHandlerObject> list) throws IOException {
        this.socket = socket;
        this.list = list;
        writer = new ObjectOutputStream(socket.getOutputStream());
        reader = new ObjectInputStream(socket.getInputStream());
    }

    public void run(){
        InfoDTO dto = null;
        String nickname;
        try{
            while(true){
                dto = (InfoDTO) reader.readObject();
                nickname = dto.getNickname();

                if(dto.getCommand() == Info.EXIT){
                    InfoDTO sendDto = new InfoDTO();
                    sendDto.setCommand(Info.EXIT);
                    writer.writeObject(sendDto);
                    writer.flush();

                    reader.close();
                    writer.close();
                    socket.close();

                    list.remove(this);

                    sendDto.setCommand(Info.SEND);
                    sendDto.setMessage(nickname + "님 퇴장하였습니다.");
                    broadcast(sendDto);
                    break;
                }else if(dto.getCommand() == Info.JOIN){
                    InfoDTO sendDto = new InfoDTO();
                    sendDto.setCommand(Info.SEND);
                    sendDto.setMessage(nickname + "님 입장하였습니다.");
                    broadcast(sendDto);
                }else if(dto.getCommand() == Info.SEND){
                    InfoDTO sendDto = new InfoDTO();
                    sendDto.setCommand(Info.SEND);
                    sendDto.setMessage("[" + nickname + "]" + dto.getMessage());
                    broadcast(sendDto);
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public void broadcast(InfoDTO sendDto)throws IOException{
        for(ChatHandlerObject handler: list){
            handler.writer.writeObject(sendDto);
            handler.writer.flush();
        }
    }
}
