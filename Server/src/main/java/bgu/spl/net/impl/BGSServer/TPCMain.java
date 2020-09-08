package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.*;
import bgu.spl.net.api.bidi.*;
import bgu.spl.net.srv.Server;


//Thread Per Client main:
public class TPCMain {

    public static void main(String[] args){

        DatabaseBGS database = new DatabaseBGS();

        Server TPC = Server.threadPerClient(
                Integer.parseInt(args[0]), //port
                () -> new BidiMessagingProtocolImpl<>(database),
                ()-> new MessageEncoderDecoderImpl() );

        TPC.serve();
    }

}


