package com.driver;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.Date;
import java.util.List;

public class WhatsappService {
    WhatsappRepository whatsappRepository=new WhatsappRepository();

    @PostMapping("/add-user")
    public String createUser(String name, String mobile) throws Exception {
        return whatsappRepository.createUser(name,mobile);
    }

    @PostMapping("/add-group")
    public Group createGroup(List<User> users){
        return whatsappRepository.createGroup(users);
    }

    @PostMapping("/add-message")
    public int createMessage(String content){
        return whatsappRepository.createMessage(content);
    }

    @PutMapping("/send-message")
    public int sendMessage(Message message, User sender, Group group) throws Exception{
        return whatsappRepository.sendMessage(message,sender,group);
    }
    @PutMapping("/change-admin")
    public String changeAdmin(User approver, User user, Group group) throws Exception{
        return whatsappRepository.changeAdmin(approver,user,group);
    }

    @DeleteMapping("/remove-user")
    public int removeUser(User user) throws Exception{
        return whatsappRepository.removeUser(user);
    }

    @GetMapping("/find-messages")
    public String findMessage(Date start, Date end, int K) throws Exception{
        return whatsappRepository.findMessage(start,end,K);
    }
}
