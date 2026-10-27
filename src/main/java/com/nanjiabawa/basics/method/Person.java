package com.nanjiabawa.basics.method;

public class Person {

    private String name;
    private int age;
    private String gender;

    // 构造器重载：根据参数个数不同，提供多种创建对象的方式
    public Person(String name, int age, String gender) {
        this.name = name;
        this.age = age;
        this.gender = gender;
    }

    public Person(String name, int age) {
        // this(...) 调用另一个构造器，避免重复代码
        this(name, age, "unknown");
    }

    public Person(String name) {
        this(name, 0, "unknown");
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    // 方法重载：同名 introduce，参数列表各不相同
    // 版本1：无参数
    public String introduce() {
        return "我是" + name + "，今年" + age + "岁，性别" + gender;
    }

    // 版本2：参数类型不同（boolean 控制是否显示性别）
    public String introduce(boolean showGender) {
        if (showGender) {
            return introduce();
        }
        return "我是" + name + "，今年" + age + "岁";
    }

    // 版本3：参数个数不同（指定说的话作为前缀）
    public String introduce(String prefix) {
        return prefix + introduce();
    }

    // 版本4：参数类型不同（用 String 接收年龄）
    public void setAge(String ageStr) {
        this.age = Integer.parseInt(ageStr);
    }

    public void setAge(int age) {
        this.age = age;
    }
}
