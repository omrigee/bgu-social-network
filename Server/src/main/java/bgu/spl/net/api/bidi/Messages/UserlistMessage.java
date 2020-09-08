package bgu.spl.net.api.bidi.Messages;

public class UserlistMessage extends Message {
    private short opcode;

    public UserlistMessage() {
        opcode = 7;
    }


    @Override
    public short getOpcode() {
        return opcode;
    }


}
