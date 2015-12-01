
import java.io.FileOutputStream;
import java.net.*;
import java.util.Random;

class DelayData implements Runnable {
    
    public static final int DATA_SIZE = 5000;
    private Thread t;
    private byte[] data = new byte[DATA_SIZE];
    private DatagramSocket serverSock;
    private InetAddress ipDest;
    private int portDest;

    DelayData(DatagramSocket sock, byte[] fileData, InetAddress addr, int port) {
        data = fileData;
        serverSock = sock;
        ipDest = addr;
        portDest = port;
    }

    public void run() {
        try {
            System.out.println("Thread Delaying Packet!");
            Thread.sleep(Main.randInt(1,20));
            Main.dataSender(serverSock, data, ipDest, portDest);
        } catch (InterruptedException e) {
            System.out.println("Thread Error: " + e);
        }
    }

    public void start() {
        if(t == null) {
            t = new Thread(this);
            t.start();
        }
    }
}

public class Main {
    public  static final int DATA_SIZE = 5000;
    public static int randInt(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    public static void checkArgs(String[] args) {
        if(args.length != 2) {
            System.out.println("server.java %DropRate %DelayChance");
            System.exit(1);
        } else {
            try {
                Integer.parseInt(args[0]);
                Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.println(args[0] + " %DropRate," + args[1] + " %DelayChance");
                System.exit(1);
            }
        }
    }

    public static void dataSender(DatagramSocket sock,byte[] data, InetAddress address, int port) {
        DatagramPacket sendPacket =
                new DatagramPacket(data,data.length,address,port);

        if(data.length > DATA_SIZE) {
            System.out.println("WARNING: Data length is over "+DATA_SIZE+" Bytes!");
        }

        try {
            sock.send(sendPacket);
        } catch (java.io.IOException e) {
            System.out.println("Failed to send Data: " + e);
        }
    }

    public static void main(String[] args) throws Exception{
        checkArgs(args);
        int dropChance = Integer.parseInt(args[0]);
        int delayChance = Integer.parseInt(args[1]);
	    DatagramSocket serverSock = new DatagramSocket(7005);

        byte[] receiveData = new byte[DATA_SIZE];

        InetAddress senderAddr = InetAddress.getByName("192.168.1.12");
        int senderPort = 7005;
        InetAddress receiverAddr = InetAddress.getByName("192.168.1.4");
        int receiverPort = 7005;

        while(true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);
            serverSock.receive(receivePacket);
            byte[] receivedData = receivePacket.getData();

            if(randInt(1,100) > dropChance) {
                InetAddress ipAddress = receivePacket.getAddress();
                if (ipAddress.equals(senderAddr)) {
                    if(randInt(1,100) > delayChance)
                        dataSender(serverSock,receivedData,receiverAddr,receiverPort);
                    else {
                        DelayData d1 = new DelayData(serverSock,receivedData,receiverAddr,receiverPort);
                        d1.start();
                    }
                } else if(ipAddress.equals(receiverAddr)) {
                    if(randInt(1,100) > delayChance)
                        dataSender(serverSock,receivedData,senderAddr,senderPort);
                    else {
                        DelayData d1 = new DelayData(serverSock,receivedData,senderAddr,senderPort);
                        d1.start();
                    }
                } else {
                    System.out.println("Address does not belong to sender or receiver client");
                }
            }
        }
    }
}
