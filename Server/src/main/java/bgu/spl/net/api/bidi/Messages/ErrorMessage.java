package bgu.spl.net.api.bidi.Messages;

public class ErrorMessage extends Message{

    private short opcode;
    private short createdByOpcode;

    public ErrorMessage(short createdByOpcode) {
        opcode = 11;
        this.createdByOpcode = createdByOpcode;
    }


    public short getCreatedByOpcode() {
        return createdByOpcode;
    }

    @Override
    public short getOpcode() {
        return opcode;
    }
}
