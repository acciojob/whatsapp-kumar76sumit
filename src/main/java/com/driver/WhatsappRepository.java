package com.driver;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.ToIntFunction;

import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    @PostMapping("/add-user")
    public String createUser(String name, String mobile) throws Exception {
        for(String currMobile:userMobile)
        {
            if(currMobile.equals(mobile))
            {
                throw new Exception("User already exists");
            }
        }
        User user=new User(name,mobile);
        userMobile.add(mobile);
        return "SUCCESS";
    }

    @PostMapping("/add-group")
    public Group createGroup(List<User> users){
        Group group=null;
        if(users.size()==2)
        {
            group=new Group(users.get(1).getName(),2);
        }
        else if(users.size()>2)
        {
            customGroupCount++;
            String newGroupName="Group "+customGroupCount;
            group=new Group(newGroupName,users.size());
        }
        groupUserMap.put(group,users);
        groupMessageMap.put(group,new ArrayList<>());
        adminMap.put(group,users.get(0));
        return group;
    }

    @PostMapping("/add-message")
    public int createMessage(String content){
        messageId++;
        Message message=new Message(messageId,content);
        message.setTimestamp(new Date());
        return messageId;
    }

    @PutMapping("/send-message")
    public int sendMessage(Message message, User sender, Group group) throws Exception{
        if(!groupUserMap.containsKey(group))
        {
            throw new Exception("Group does not exist");
        }
        boolean isSenderPresent=false;
        for(User user:groupUserMap.get(group))
        {
            if(user==sender)
            {
                isSenderPresent=true;
            }
        }
        if(!isSenderPresent)
        {
            throw new Exception("You are not allowed to send message");
        }
        else {
            List<Message> listOfGroupMessage=groupMessageMap.get(group);
            listOfGroupMessage.add(message);
            messageId++;
            groupMessageMap.put(group,listOfGroupMessage);
            senderMap.put(message,sender);
            return groupMessageMap.get(group).size();
        }
    }
    @PutMapping("/change-admin")
    public String changeAdmin(User approver, User user, Group group) throws Exception{
        if(!groupUserMap.containsKey(group))
        {
            throw new Exception("Group does not exist");
        }
        else if(adminMap.get(group)!=approver)
        {
            throw new Exception("Approver does not have rights");
        }
        boolean isUserPresent=false;
        for(User user1:groupUserMap.get(group))
        {
            if(user1==user)
            {
                isUserPresent=true;
            }
        }
        if(!isUserPresent)
        {
            throw new Exception("User is not a participant");
        }
        else {
            adminMap.put(group,user);
            return "SUCCESS";
        }
    }

    @DeleteMapping("/remove-user")
    public int removeUser(User user) throws Exception{
        Group group1=null;
        for(Group group:groupUserMap.keySet())
        {
            for(int i=0;i<groupUserMap.get(group).size();i++)
            {
                User user1=groupUserMap.get(group).get(i);
                if(user1==user)
                {
                    group1=group;
                }
            }
        }
        if(group1==null)
        {
            throw new Exception("User not found");
        }
        if(adminMap.get(group1)==user)
        {
            throw new Exception("Cannot remove admin");
        }
        groupUserMap.get(group1).remove(user);
        group1.setNumberOfParticipants(group1.getNumberOfParticipants()-1);
        for(Message message:senderMap.keySet())
        {
            if(senderMap.get(message)==user)
            {
                senderMap.remove(user);
                groupMessageMap.get(group1).remove(message);
                messageId--;
            }
        }
        return groupUserMap.get(group1).size()+groupMessageMap.get(group1).size()+messageId;
    }

    @GetMapping("/find-messages")
    public String findMessage(Date start, Date end, int K) throws Exception{
        //This is a bonus problem and does not contains any marks
        // Find the Kth latest message between start and end (excluding start and end)
        // If the number of messages between given time is less than K, throw "K is greater than the number of messages" exception
        List<Message> kthLatestMessage=new ArrayList<>();
        for(Message message:senderMap.keySet())
        {
            Date date=message.getTimestamp();
            if(date.after(start) && date.before(end))
            {
                kthLatestMessage.add(message);
            }
        }
        Collections.sort(kthLatestMessage,(a,b)->
        {
            return b.getTimestamp().compareTo(a.getTimestamp());
        });
        if(kthLatestMessage.size()<K)
        {
            throw new Exception("K is greater than the number of messages");
        }
        return kthLatestMessage.get(K).getContent();
    }
}
