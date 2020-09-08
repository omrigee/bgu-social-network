package bgu.spl.net.api.bidi.Messages;

public class NotificationMessage extends Message {
    private short opcode;
    private String Content;
    private byte NotificationType; // PM = 0, Public Post = 1
    private String PostingUser;

    public NotificationMessage(byte NotificationType, String Content,String PostingUser ) {
        opcode = 9;
        this.NotificationType = NotificationType;
        this.Content = Content;
        this.PostingUser = PostingUser;
    }




    public byte[] notificationToBytes(){
        byte nType = (byte) NotificationType;
        byte[] postinguserBytes = (PostingUser+"\0").getBytes();
        byte[] contentBytes = (Content+"\0").getBytes();

        byte[] result = new byte[1 + postinguserBytes.length + contentBytes.length];
        result[0] = nType;
        for (int i = 0; i<postinguserBytes.length ; i++){
            result[i+1] = postinguserBytes[i];
        }

        for (int j = 0; j<contentBytes.length; j++){
            result[j+1+postinguserBytes.length] = contentBytes[j];
        }

        return result;

    }

    @Override
    public short getOpcode() {
        return opcode;
    }

}
