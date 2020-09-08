package bgu.spl.net.api.bidi.Messages;

public class RegisterMessage extends Message {

    private short opcode;
    private String username;
    private String password;

    public RegisterMessage(String dataToInitialize) {
      opcode = 1;
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

