package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.api.bidi.ConnectionsImpl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockingConnectionHandler<T> implements Runnable, bgu.spl.net.srv.ConnectionHandler<T> {


    private final BidiMessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;

    private final int connectionID;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;
    private Connections connections;

    public BlockingConnectionHandler(int connectionID, Connections connections, Socket sock, MessageEncoderDecoder<T> reader, BidiMessagingProtocol<T> protocol) {
        this.connectionID = connectionID;
        this.connections = connections;
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
    }

    @Override

    // To prevent a situtation of 2 threads writing to the buffer simultaneously and messing the message.


    public synchronized void send(T msg) {
        if (msg != null) {
            byte[] response = encdec.encode(msg);
            try {
                out.write(response);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void run() {


        protocol.start(connectionID,connections);

        try (Socket sock = this.sock) { //just for automatic closing
            int read;

            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());

            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                T nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    protocol.process(nextMessage);
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }



    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }
}