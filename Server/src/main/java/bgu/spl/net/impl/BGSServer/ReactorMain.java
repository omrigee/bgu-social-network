package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.MessageEncoderDecoderImpl;
import bgu.spl.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl.net.api.bidi.DatabaseBGS;
import bgu.spl.net.srv.Server;

public class ReactorMain {

    public static void main(String[] args) {

        DatabaseBGS database = new DatabaseBGS();


        Server reactor = Server.reactor(
                Integer.parseInt(args[1]),// num of threads
                Integer.parseInt(args[0]), //port
                () -> new BidiMessagingProtocolImpl<>(database),
                ()-> new MessageEncoderDecoderImpl() );

        reactor.serve();
    }


    }
