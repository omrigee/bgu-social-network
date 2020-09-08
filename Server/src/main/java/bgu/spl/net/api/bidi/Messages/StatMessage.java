package bgu.spl.net.api.bidi.Messages;

public class StatMessage extends Message {
    private short opcode;
    private String desiredUsername;


    public StatMessage(String desiredUsername) {
        opcode = 8;
       this.desiredUsername= desiredUsername;
    }

    @Override
    public short getOpcode() {
        return opcode;
    }

    public String getDesiredUsername() {
        return desiredUsername;
    }
}
