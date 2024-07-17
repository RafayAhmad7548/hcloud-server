package com.rutils;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "\"User\"")
public class User{

    @Id   
    private String username;
    private String password;

    public User(String username, String password){
        this.username = username;
        this.password = password;
    }
    public User(){
        this.username = null;
        this.password = null;
    }

    public String getUsername(){
        return username;
    }
    public void setUsername(String username){
        this.username = username;
    }
    public String getPassword(){
        return password;
    }
    public void setPassword(String password){
        this.password = password;
    }
    



}