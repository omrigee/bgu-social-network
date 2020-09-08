package bgu.spl.net.api.bidi.Messages;

import java.util.ArrayList;
import java.util.List;

public class FollowMessage extends Message {
    private short opcode;
    private int stateFollow;//follow is 0 and unfollow is 1
    private int NumOfUsers;
    private  String[] UserNameList;

    public FollowMessage(int stateFollow,int NumOfUsers, String dataToInitialize) {
        super();
        opcode = 4;
        this.stateFollow = stateFollow;
        this.NumOfUsers = NumOfUsers;
        UserNameList = dataToInitialize.split(" ");

        for (int i = 0; i< UserNameList.length; i++){
         UserNameList[i] =  UserNameList[i].replace("\0",""); //gets username to follow
        }

    }



    @Override
    public short getOpcode() {
        return opcode;
    }

    public int getStateFollow() {
        return stateFollow;
    }

    public int getNumOfUsers() {
        return NumOfUsers;
    }

    public String[] getUserNameList() {
        return UserNameList;
    }

}
