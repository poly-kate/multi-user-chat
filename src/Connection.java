package exam2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Connection implements AutoCloseable {

    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private String sender;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        output = new ObjectOutputStream(this.socket.getOutputStream());
        input = new ObjectInputStream(this.socket.getInputStream());
    }

    public void sendMessage(Message message) throws IOException {
        message.setDateTime();//устанавливаем дату отправки
        output.writeObject(message);
        output.flush();
    }

    public  Message readMessage() throws IOException, ClassNotFoundException{

        return (Message) input.readObject();
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSender() {
        return sender;
    }

    @Override
    public void close() throws Exception {
        input.close();
        output.close();
        socket.close();
    }

}
