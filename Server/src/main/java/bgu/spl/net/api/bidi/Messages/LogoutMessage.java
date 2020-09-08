package bgu.spl.net.api.bidi.Messages;

public class LogoutMessage extends Message {

    private short opcode;

    public LogoutMessage() {
        opcode = 3;
    }


    @Override
    public short getOpcode() {
        return opcode;
    }



}
