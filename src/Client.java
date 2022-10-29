
package exam2;

import java.io.IOException;
import java.util.Scanner;
import java.net.Socket;

/*
* Запускается сначала сервер, потом клиенты
* */

public class Client {

    private String nameClient;
    private final String host;
    private final int port;
    private Connection connection;

    public Client(String nameClient, String host, int port) {

        this.nameClient = nameClient;
        this.host = host;
        this.port = port;

    }

    //начало работы клиента
    void start() throws Exception {

        Socket socket = new Socket(this.host, this.port);

        try{
            this.connection = new Connection(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ClientReader newReader = new ClientReader();
        newReader.start();//запуск потока чтения клиента

    }

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите имя:");
        String nameSender = scanner.nextLine();//имя клиента может быть любым, в том числе неуникальным или пустым

        Client client = new Client(nameSender, "localhost", 8080);
        try {
            client.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //клиент читает сообщение из консоли, формирует объект message и отправляет на сервер
        //используем для этого поток main
        System.out.println("Чтобы прервать соединение введите \"/stop\"");
        while (true){
            System.out.println("Введите сообщение:");
            String newText = scanner.nextLine();

            try {
                client.connection.sendMessage(Message.getMessage(client.nameClient, newText));

            } catch (IOException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                client.connection.close();
            }
        }
    }


    //клиент получает сообщение от сервера и выводит его в консоль
    private class ClientReader extends Thread{
        @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted()){
                try {
                    Message message = connection.readMessage();
                    System.out.println(message);
                } catch (IOException | ClassNotFoundException e) {

                    System.out.println("Соединение прервано!");
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}

