package exam2;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.*;

public class Server {

    private LinkedBlockingQueue<Message> messages;//очередь сообщений
    private CopyOnWriteArraySet<Connection> connections;//множество соединений


    void start(){
        messages = new LinkedBlockingQueue<>();
        connections = new CopyOnWriteArraySet<>();
        ServerSocket serverS;
        try{
            serverS = new ServerSocket(8080);
            System.out.println("Cервер на связи!");

            //запускаем один поток записи сервера
            ServerWriter newWriter = new ServerWriter();
            newWriter.start();

            while(true){
               //ожидание клиента
                Connection connection = new Connection(serverS.accept());
                connections.add(connection);

                //поток чтения на каждого клиента
                ServerReader newReader = new ServerReader(connection);
                newReader.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //читает сообщения и помещает их очередь собщений
    private class ServerReader extends Thread {

        private final Connection connection;
        public ServerReader(Connection newConnection) {
            this.connection = newConnection;
        }
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()){

                try{
                    Message message = connection.readMessage();
                    connection.setSender(message.getSender());

                    //клиент запрашивает отключение от сервера
                    if (("/stop").equals(message.getText())) {
                        connection.close();
                        connections.remove(connection);
                        break;
                    }
                    System.out.println(message);
                    messages.put(message);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    //рассылка сообщений из очереди всем клиентам, кроме отправителя
    private class ServerWriter extends Thread{
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()){

                try {
                    Message sendMessage = messages.take();
                    for (Connection connection : connections) {
                        if (!sendMessage.getSender().equals(connection.getSender())) {
                            connection.sendMessage(sendMessage);
                        }
                    }
                } catch (InterruptedException | IOException e) {

                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {

        Server server = new Server();

        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


