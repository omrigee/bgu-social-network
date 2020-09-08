package bgu.spl.net.api;

import bgu.spl.net.api.bidi.Messages.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MessageEncoderDecoderImpl implements MessageEncoderDecoder<Message> {

    private byte[] OpcodeArray;
    private int readOpcode;
    private byte[] bytes;
    private int len;
    private short Opcode;
    private int counter;
    private int followCounter;
    private byte[] followNumOfUsers;

    public MessageEncoderDecoderImpl(){
        OpcodeArray = new byte[2];
        readOpcode = 0;
        bytes = new byte[1 << 10];
        len = 0;
        Opcode = -1;
        counter = -1;
        followCounter = 0;
        followNumOfUsers = new byte[2];
    }

    @Override
    public Message decodeNextByte(byte nextByte) {

        //First 2 bytes initialize the Opcode
        if (readOpcode <2){ // Initializing the opcode array.
            OpcodeArray[readOpcode] = nextByte;

            if (OpcodeArray[1] == 3 || OpcodeArray[1] == 7){
                Opcode =  bytesToShort(OpcodeArray);
                OpcodeArray[0]=0;
                OpcodeArray[1]=0;
                readOpcode = 0;
                return messageGenerator(Opcode);
            }
            readOpcode = readOpcode + 1;
            return null;
        }


        if (readOpcode ==2){
            pushByte(nextByte);
            Opcode =  bytesToShort(OpcodeArray); // Opcode in 'short' after bytes convertion
            readOpcode = 3; // after 'Opcode' is initialized, no need to get into this 'if' every new byte.
            return null;
        }


       switch (Opcode) {
           case 1: {
               if (counter == -1) {
                   counter = 2;
               }
               if (nextByte == '\0') {
                   counter = counter - 1;
               }
               if (counter == 0) {
                   readOpcode = 0;
                   counter = -1;
                   return messageGenerator(Opcode);
               }
               pushByte(nextByte);
               return null;
           }
           case 2: {
               if (counter == -1) {
                   counter = 2;
               }
               if (nextByte == '\0') {
                   counter = counter - 1;
               }
               if (counter == 0) {
                   readOpcode = 0;
                   counter = -1;
                   return messageGenerator(Opcode);
               }
               pushByte(nextByte);
               return null;
           }



           case 4:
           {
               if (followCounter==0){ //means the follow/unfollow byte isnt stored yet - storing it now
                   followCounter++;
                   followNumOfUsers[0]=nextByte;
                   pushByte(nextByte);
                   return null;
               }

               if (followCounter==1) { //means passed the follow/unfollow byte, current byte is number of users.
                 followCounter++;
                   followNumOfUsers[1]=nextByte;
                 counter = bytesToShort(followNumOfUsers);
                   pushByte(nextByte);
                   return null;
               }


               if (nextByte == '\0') {
                   counter = counter - 1;
                   if (counter !=0){
                       byte space = ' ';
                       pushByte(space);
                       return null;
                   }
               }
               if (counter == 0) {
                   pushByte(nextByte);
                   readOpcode = 0;
                   followCounter = 0;
                   counter = -1;
                   followNumOfUsers[0]=0;
                   followNumOfUsers[1]=0;
                   return messageGenerator(Opcode);
               }
               pushByte(nextByte);
               return null;
           }
           case 5:
               if (counter == -1) {
                   counter = 1;
               }
               if (nextByte == '\0') {
                   counter--;
               }
               if (counter == 0) {
                   readOpcode = 0;
                   counter = -1;
                   return messageGenerator(Opcode);
               }
               pushByte(nextByte);
               return null;

           case 6:
               if (counter == -1) {
                   counter =2;
               }
               if (nextByte == '\0') {
                   counter = counter-1;
               }
               if (counter == 0) {
                   readOpcode = 0;
                   counter = -1;
                   return messageGenerator(Opcode);
               }
               pushByte(nextByte);
               return null;

           case 7:
               readOpcode = 0;
               return messageGenerator(Opcode);
           case 8:
               if (counter == -1) {
                   counter =1;
               }
               if (nextByte == '\0') {
                   counter = counter-1;

               }
                if (counter == 0) {
                   readOpcode = 0;
                   counter = -1;
                   return messageGenerator(Opcode);
               }
               pushByte(nextByte);
               return null;
       }
           return null; //not a line yet

 }

    @Override
    public byte[] encode(Message message) {

        byte[] messageOpcodeInBytes = shortToBytes(message.getOpcode());
        int currentMessageOpcode = message.getOpcode();

            //ACK Message
        if (currentMessageOpcode == 10 ){
            byte[] createdByOpcode = shortToBytes (((AckMessage) message).getCreatedByOpcode());
              byte[]opcodes = concatenateByteArrays(messageOpcodeInBytes,createdByOpcode);

              //if this ACK message is of 'Follow/Unfollow' needs to add the 'num of successful actions' in bytes
        if (((AckMessage) message).getCreatedByOpcode() == 4 || ((AckMessage) message).getCreatedByOpcode() == 7 ){

            byte[] numOfSuccesfullInArray = shortToBytes(((AckMessage) message).getNumOfSuccesfullFollowsUnfollows());
            byte[] opcodesAndFollowersArray = concatenateByteArrays(opcodes,numOfSuccesfullInArray);
            byte[] optionalInBytes = ((AckMessage) message).getOptional().getBytes();
            byte[] combined = concatenateByteArrays(opcodesAndFollowersArray,optionalInBytes);
            return combined;

        }

            if (((AckMessage) message).getCreatedByOpcode() == 8 ){
                byte[] NumOfPostsArray = shortToBytes(((AckMessage) message).getNumOfPosts());
                byte[] array1 = concatenateByteArrays(opcodes,NumOfPostsArray);
                byte[] NumOfFollowersArray = shortToBytes(((AckMessage) message).getNumFollowers());
                byte[] array2 = concatenateByteArrays(array1,NumOfFollowersArray);
                byte[] NumOfFollowingArray = shortToBytes(((AckMessage) message).getNumFollowing());
                byte[] toReturn = concatenateByteArrays(array2,NumOfFollowingArray);
                return toReturn;
            }

              if (((AckMessage)message).getOptional()!=null) {
                  byte[] optionalInBytes = ((AckMessage) message).getOptional().getBytes();
                  byte[] combined = concatenateByteArrays(opcodes, optionalInBytes);
                  return combined;
              }
              else return opcodes;
        }
            //ERROR message
        else if (currentMessageOpcode == 11 ){
            byte[] createdByOpcode = shortToBytes (((ErrorMessage)message).getCreatedByOpcode());
           byte[] combined = concatenateByteArrays(messageOpcodeInBytes,createdByOpcode);
           return combined;
        }

        //means it is notification message.
        else {
        byte[] notificationStringArray = ((NotificationMessage)message).notificationToBytes();
            byte[] combined = concatenateByteArrays(messageOpcodeInBytes,notificationStringArray);
            return combined;
        }

    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len] = nextByte;
        len++;
    }

    private Message messageGenerator(short opcode) {
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.

        String result;
        switch (opcode) {
            case 1:

                result = new String(bytes, 0, len, StandardCharsets.UTF_8);
                RegisterMessage message1 = new RegisterMessage(result);
                len = 0;
                return message1;
            case 2:
                result = new String(bytes, 0, len, StandardCharsets.UTF_8);
                LoginMessage message2 = new LoginMessage(result);
                len = 0;
                return message2;

            case 3:
                LogoutMessage message3 = new LogoutMessage();
                return message3;

            case 4:
                int followState = bytes[0];
                byte[] NumOfUsersArray = new byte[2];
                NumOfUsersArray[0] = bytes[1];
                NumOfUsersArray[1] = bytes[2];
                short NumOfUsers = bytesToShort(NumOfUsersArray);
                result = new String(bytes, 0, len, StandardCharsets.UTF_8);
                result =  result.substring(3);
                FollowMessage message4 = new FollowMessage(followState,NumOfUsers,result);
                len = 0;
                return message4;

            case 5:
                result = new String(bytes, 0, len, StandardCharsets.UTF_8);
                PostMessage message5 = new PostMessage(result);
                len = 0;
                return message5;

            case 6:
                result = new String(bytes, 0, len, StandardCharsets.UTF_8);
                PmMessage message6 = new PmMessage(result);
                len = 0;
                return message6;
            case 7:
                UserlistMessage message7 = new UserlistMessage();
                return message7;
            case 8:
                result = new String(bytes, 0, len, StandardCharsets.UTF_8);
                StatMessage message8 = new StatMessage(result);
                len = 0;
                return message8;

        }

            return null;

    }


     public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }


     byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

}


