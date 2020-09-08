package bgu.spl.net.api.bidi.Messages;

public class LoginMessage extends Message{
    private String username;
    private String password;
    private short opcode;


    public LoginMessage(String dataToInitialize) {
        opcode = 2;
        String[] tempArray =  dataToInitialize.split("\0");
        username = tempArray[0];
        password = tempArray[1];

    }

    @Override
    public short getOpcode() {
        return opcode;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
