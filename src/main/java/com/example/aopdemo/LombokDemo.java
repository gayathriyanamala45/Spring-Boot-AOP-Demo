package com.example.aopdemo;


public class LombokDemo {

    public static void main(String[] args) {

        Student s = new Student();

        s.setId(101);
        s.setName("Gayathri");
        s.setCourse("Java Full Stack");

        System.out.println(s.getId());
        System.out.println(s.getName());
        System.out.println(s.getCourse());
    }
}