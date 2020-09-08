package bgu.spl.net.api.bidi;

import bgu.spl.net.api.bidi.Messages.*;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;


public class BidiMessagingProtocolImpl<T> implements BidiMessagingProtocol<T> {

    private boolean shouldTerminate;
    private DatabaseBGS database;
    private Connections connections;
    private int myConnectionID;
    private BGSUser myUser;

    public BidiMessagingProtocolImpl(DatabaseBGS database) {
        this.shouldTerminate = false;
        this.database = database;
        this.connections = null;
        this.myConnectionID = -1;
        this.myUser = null;
    }

    @Override
    public void start(int connectionId, Connections connections) {
        this.connections = connections;
        myConnectionID = connectionId;
    }




    @Override
    public void process(T message) {

        //Checks for the opcode of the message, process according to the right opcode.
        switch (((Message) message).getOpcode()) {

            case 1:{ //Register process
                processRegister((RegisterMessage)message);
                break;
            }
            case 2: { //Login process
                processLogIn((LoginMessage) message);
                break;
            }
            case 3: {//Logout process
                processLogout((LogoutMessage) message);
                break;
            }
            case 4: {//Follow/Unfollow process
                processFollowUnfollow((FollowMessage)message);
                break;
            }

            case 5:{ //Post process
                processPost((PostMessage)message);
                break;
            }
            case 6:{ //PM process
                processPmMessage((PmMessage)message);
                break;
            }
            case 7:{ //Userlist process
                processUserlist((UserlistMessage)message);
                break;
            }
            case 8:{ //Stats process
                processStat((StatMessage)message);
                break;
            }
        } // end of process
    }



    private void processRegister (RegisterMessage msg){

        //checks if user is already logged in:
        if (myUser != null){
            ErrorMessage error = new ErrorMessage(msg.getOpcode());
            connections.send(myConnectionID,error);
            return;
        }

        // checks if there is a user with same username - if there is no user with this username:


        //only 1 connection will be allowed to register to avoid registering with the same username.
        synchronized (database.getRegisterLock()) {
            if (!database.getRegisteredUsersMap().containsKey(msg.getUsername())) {
                database.register(msg.getUsername(), msg.getPassword());
                database.getRegisteredCounter().getAndIncrement();
                // adds the username to the registered database string for the userlist:
                database.setRegisteredUsersString(database.getRegisteredUsersString() + msg.getUsername() + "\0");
                AckMessage doneRegister = new AckMessage((short) 1);
                connections.send(myConnectionID, doneRegister);
            } else {
                ErrorMessage errorRegistering = new ErrorMessage((short) 1);
                connections.send(myConnectionID, errorRegistering);
            }

        }
    }



    private void processLogIn(LoginMessage msg){




        //Collect the desired user for logging in
        BGSUser userToLog  = database.getRegisteredUsersMap().get(msg.getUsername());

        if (myUser != null){
            ErrorMessage error = new ErrorMessage(msg.getOpcode());
            connections.send(myConnectionID,error);
            return;
        }

        if (userToLog!=null) {
            synchronized (userToLog) {
                if (!userToLog.isLoggedIn() && (msg.getPassword().equals(userToLog.getPassword())) ){
                    userToLog.LogIn(myConnectionID); //changes the user status
                    myUser = userToLog; //sets the protocol user to the one from database
                }
            }
        }
        // means user logged in, needs to send ACK and receive the message from his queue.
        if (myUser != null){
            AckMessage loginSuccess = new AckMessage((short)2); // sends ACK message of login
            connections.send(myConnectionID,loginSuccess); // sends to the client the ACK of login message
            //Checks the FutureMessageList and sends the user the message he got while he was logged off:
            ConcurrentLinkedQueue<NotificationMessage> MessagesToSend =  myUser.getFutureMessageList();
            while (!MessagesToSend.isEmpty()){
                NotificationMessage message = MessagesToSend.poll();
                connections.send(myConnectionID,message);
            }
            return;
        }

        ErrorMessage error = new ErrorMessage((short)2);
        connections.send(myConnectionID,error);
    }





    private void processLogout(LogoutMessage msg){
        //means there is no user connected to this protocol, cannot log off
        if (myUser == null){
            ErrorMessage NotLoggedIn = new ErrorMessage((short)3);
            connections.send(myConnectionID,NotLoggedIn);
            return;
        }


        //means this protocol is connected to a user, may logout.
        else {
            AckMessage succesfulLogout = new AckMessage((short)3); // in order to send the logout ACK to the client
            connections.send(myConnectionID,succesfulLogout); //sends the ACK to the client
            synchronized (myUser) {
                myUser.LogOut(); // will change the user boolean login value to false;
                myUser = null; // will delete this protocol reference to the logged off user
            }
            this.shouldTerminate = true; //changes the terminate value to TRUE, will end this ConnectionHandler run.
        }
    }



    private void processFollowUnfollow (FollowMessage msg){

        // means this protocol is not connected to any user
        if (myUser == null){
            ErrorMessage notLoggedin = new ErrorMessage((short)4);
            connections.send(myConnectionID,notLoggedin);
            return;
        }

        // user wants to FOLLOW:
        if ( msg.getStateFollow() == 0){
            short NumOfSuccesfulFollows = 0;
            LinkedList<String> usersAddedUsername = new LinkedList<>();
            for (int i=0; i<msg.getNumOfUsers(); i++){
                String username = msg.getUserNameList()[i];
                BGSUser userToAdd = database.getRegisteredUsersMap().get(username); // gets this user from the database
                // if the user is not null - means there is a user with this username in database &&  'myUser' doesnt have him on the followingList
                if (userToAdd != null && !myUser.getFollowingList().contains(userToAdd)) {
                    NumOfSuccesfulFollows++; // succesful follows counter++
                    usersAddedUsername.add(username); // creates the string list for the ACK message
                    userToAdd.getFollowersList().put(myUser.getUsername(),myUser); // // adds myUser to the userToAdd followersList
                    myUser.getFollowingList().put(userToAdd.getUsername(),userToAdd); // adds userToAdd to myUser followingList
                }
            }

            //means didnt add any of the users to myUser following list because they do not exist\ myUser already followed them, needs to send error
            if (NumOfSuccesfulFollows == 0){
                ErrorMessage NoNewFollowers = new ErrorMessage(msg.getOpcode());
                connections.send(myConnectionID,NoNewFollowers);
                return;
            }

            //means at least 1 user has been added, needs to send ACK with usernames added.
            if (NumOfSuccesfulFollows > 0){

                followUnfollowAck(msg,NumOfSuccesfulFollows,usersAddedUsername);

                return;
            }
        }

        // user wants to UNFOLLOW
        if (msg.getStateFollow() == 1){
            short NumOfSuccesfulUnfollows = 0;
            LinkedList<String> usersRemovedUsernames = new LinkedList<>();

            for (int i=0; i<msg.getNumOfUsers(); i++){
                String username = msg.getUserNameList()[i]; //gets username to unfollow
                BGSUser userToRemove = database.getRegisteredUsersMap().get(username); // gets this user from the database
                //if there is a user with this username and myUser has him on his following list, needs to be removed
                if (userToRemove != null && myUser.getFollowingList().contains(userToRemove)){
                    NumOfSuccesfulUnfollows++;
                    usersRemovedUsernames.add(username);
                    userToRemove.getFollowersList().remove(myUser.getUsername()); //remove myUser from his followers list.
                    myUser.getFollowingList().remove(username); //removes user from myUser following list.
                }
            }

            //means didnt remove any of the users from myUser following list because he didnt follow any of those users, needs to send an error
            if (NumOfSuccesfulUnfollows == 0){
                ErrorMessage DidNotFollowUsers = new ErrorMessage((short)4);
                connections.send(myConnectionID,DidNotFollowUsers);
            }


            //means at least 1 user has been added, needs to send ACK with usernames added.
            if (NumOfSuccesfulUnfollows > 0){
                followUnfollowAck(msg,NumOfSuccesfulUnfollows,usersRemovedUsernames);
                return;
            }

        }

    } //end of processFollowUnfollow



    private String usersListGenerator(LinkedList<String> list){
        String toReturn = "";
        for (int i = 0; i < list.size() ; i++){
            toReturn = toReturn+list.get(i)+"\0";
        }
        return toReturn;
    }



    private void followUnfollowAck(FollowMessage msg, short NumOfSuccesfullActions , LinkedList<String> usernamelist){
        String usernames = usersListGenerator(usernamelist);

        AckMessage followersAdded = new AckMessage((short)4,NumOfSuccesfullActions,usernames);
        connections.send(myConnectionID,followersAdded);
    }


    private void processPost(PostMessage msg){

        //checks if user is logged in, if he is not sends error and returns:
        if (myUser == null){
            ErrorMessage NotLoggedIn = new ErrorMessage((short)5);
            connections.send(myConnectionID,NotLoggedIn);
            return;
        }

        String msgContent = msg.getContent();

        //increment the user's PostsCounter by 1 for the Stat message.
        myUser.getPostsCounter().getAndIncrement();

        //collecting the @username users from the message and store them in 'usernamesInMessage' list.
        LinkedList<String> usernamesInMessage = new LinkedList<>();
        for (int i=0; i<msgContent.length(); i++){
            if (msgContent.charAt(i) == '@') {
                String cutted = msgContent.substring(i);
                String username;
                int indexOfSpace = cutted.indexOf(' ');
                if (indexOfSpace!=-1) {
                    username = cutted.substring(1, indexOfSpace);
                }
                else username = cutted.substring(1);
                if (!usernamesInMessage.contains(username) && !myUser.getFollowersList().containsKey(username) ) {
                    usernamesInMessage.add(username);
                }
            }
        }


        //sends the post to the @users in the message:
        for (int i=0; i<usernamesInMessage.size(); i++){
            String username = usernamesInMessage.get(i);

            //checks if @username is registered
            if (database.getRegisteredUsersMap().containsKey(username)){

                //collects @username from the RegisteredMap
                BGSUser user = database.getRegisteredUsersMap().get(username);

                //checks if @username is logged in, if he is sends the post directly
                synchronized (user) {
                    if (user.isLoggedIn()) {
                        NotificationMessage post = new NotificationMessage((byte) 1, msg.getContent(), myUser.getUsername());
                        connections.send(user.getConnectionID(), post);
                    }

                    // if @username is not logged in, message will be stored in his FutureMessageList;

                    else {
                        NotificationMessage post = new NotificationMessage((byte)1,msg.getContent(),myUser.getUsername());
                        connections.send(user.getConnectionID(), post);
                        user.getFutureMessageList().add(post);
                    }
                }

            }
        }


        //Sending the post to myUser followers list:
        for (BGSUser userToSend : myUser.getFollowersList().values()){

            synchronized (userToSend) {
                //if user is LOGGED IN:
                if (userToSend.isLoggedIn()) {
                    NotificationMessage post = new NotificationMessage((byte) 1, msgContent, myUser.getUsername());
                    connections.send(userToSend.getConnectionID(), post);

                }

                // user is LOGGED OFF:
                else {
                    NotificationMessage post = new NotificationMessage((byte) 1, msg.getContent(), myUser.getUsername());
                    connections.send(userToSend.getConnectionID(), post);
                    userToSend.getFutureMessageList().add(post);
                }
            }

        }

        //Sending the ACK Message of Post
        AckMessage postSuccesful = new AckMessage(msg.getOpcode());
        connections.send(myConnectionID,postSuccesful);

    }


    private void processPmMessage(PmMessage msg){

        //getting the message receiver in database
        BGSUser MsgReceiver = database.getRegisteredUsersMap().get(msg.getUsernameOfReceiver());


        //checks if sending client is logged in && message receiver exists in database, if not sends error message.
        if (myUser == null || MsgReceiver == null){
            ErrorMessage NotLoggedIn = new ErrorMessage(msg.getOpcode());
            connections.send(myConnectionID,NotLoggedIn);
            return;
        }

        //means sending user is logged in, message receiver is valid:
        else {
            NotificationMessage pm = new NotificationMessage((byte)0,msg.getContent(),myUser.getUsername());

            // if receiving client is logged in:
            synchronized (MsgReceiver) {
                if (MsgReceiver.isLoggedIn())
                    connections.send(MsgReceiver.getConnectionID(), pm);

                // if receiving client is logged off, message will be added to his FutureMessageList.
                else
                    MsgReceiver.getFutureMessageList().add(pm);
            }

            //Sending the ACK Message of Post
            AckMessage pmSuccesful = new AckMessage(msg.getOpcode());
            connections.send(myConnectionID,pmSuccesful);
        }
    }


    private void processUserlist(UserlistMessage msg){

        //checks if client is logged in
        if (myUser == null){
            ErrorMessage NotLoggedIn = new ErrorMessage((short)7);
            connections.send(myConnectionID,NotLoggedIn);
            return;
        }

        else {
            String userlistString = database.getRegisteredUsersString();
            short registeredCounter = (short)database.getRegisteredCounter().get();
            AckMessage userlistMessage = new AckMessage((short)7,registeredCounter,userlistString);
            connections.send(myConnectionID,userlistMessage);
        }
    }


    private void processStat(StatMessage msg){

        //gets the user needed to get stats of from database
        BGSUser desiredUser = database.getRegisteredUsersMap().get(msg.getDesiredUsername());

        //checks if myUser is not logged in || desired user username is not in database - needs to send error.
        if (myUser == null || desiredUser == null){
            ErrorMessage NotLoggedIn = new ErrorMessage(msg.getOpcode());
            connections.send(myConnectionID,NotLoggedIn);
            return;
        }

        else{
            short NumOfPosts = (short)desiredUser.getPostsCounter().get();
            short NumOfFollowers = (short)desiredUser.getFollowersList().size();
            short NumOfFollowing = (short)desiredUser.getFollowingList().size();
            AckMessage statmsg = new AckMessage((short)8,NumOfPosts,NumOfFollowers,NumOfFollowing);
            connections.send(myConnectionID,statmsg);
        }

    }



    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }



} // end of 'BidiMessageProtocolImp' class

