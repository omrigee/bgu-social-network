package bgu.spl.net.api.bidi.Messages;

public class PostMessage extends Message {

    private short opcode;
    private String content;

    public PostMessage(String dataToInitialize) {
        opcode = 5;
        content = dataToInitialize;

    }

    @Override
    public short getOpcode() {
        return opcode;
    }

    public String getContent() {
        return content;
    }
}
