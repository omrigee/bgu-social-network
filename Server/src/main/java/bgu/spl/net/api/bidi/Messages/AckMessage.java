package bgu.spl.net.api.bidi.Messages;

public class AckMessage extends Message{
    private short opcode;
    private short createdByOpcode;
    private short numOfSuccesfullFollowsUnfollows;
    private short NumOfPosts;
    private short NumFollowers;
    private short NumFollowing;
    private String optional;




    public AckMessage(short createdBy) {
        opcode = 10;
        createdByOpcode = createdBy;
    }




    public AckMessage(short createdByOpcode, short numOfSuccesfullFollowsUnfollows, String optional) {
        opcode = 10;
        this.createdByOpcode = createdByOpcode;
        this.numOfSuccesfullFollowsUnfollows = numOfSuccesfullFollowsUnfollows;
        this.optional = optional;
    }

    //Ack for STATS
    public AckMessage(short createdByOpcode,short NumOfPosts, short NumFollowers, short NumFollowing){
        opcode = 10;
        this.createdByOpcode = createdByOpcode;
        this.NumOfPosts = NumOfPosts;
        this.NumFollowers=NumFollowers;
        this.NumFollowing = NumFollowing;
    }

    public short getCreatedByOpcode() {
        return createdByOpcode;
    }

    public String getOptional() {
        return optional;
    }

    public short getNumOfSuccesfullFollowsUnfollows() {
        return numOfSuccesfullFollowsUnfollows;
    }

    public short getNumOfPosts() {
        return NumOfPosts;
    }

    public short getNumFollowers() {
        return NumFollowers;
    }

    public short getNumFollowing() {
        return NumFollowing;
    }

    @Override
    public short getOpcode() {
        return opcode;
    }
}


