package bgu.spl.net.api.bidi.Messages;

public class PmMessage extends Message {
    private short opcode;
    private String UsernameOfReceiver;
    private String Content;


    public PmMessage(String dataToInitialize) {
        opcode = 6;
        String[] tempArray =  dataToInitialize.split("\0");
        UsernameOfReceiver = tempArray[0];
        Content = tempArray[1];
    }

    @Override
    public short getOpcode() {
        return opcode;
    }

    public String getUsernameOfReceiver() {
        return UsernameOfReceiver;
    }

    public String getContent() {
        return Content;
    }
}
