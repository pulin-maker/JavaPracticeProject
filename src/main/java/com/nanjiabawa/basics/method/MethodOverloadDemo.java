package com.nanjiabawa.basics.method;

/**
 * 知识点：方法重载（Overload）
 *
 * 规则（三同一不同）：
 *   1. 同一个类中
 *   2. 方法名相同
 *   3. 返回值无关
 *   4. 参数列表必须不同（个数、类型或顺序）
 *
 * 误区：
 *   - 只有返回值不同不构成重载（编译报错）
 *   - 只有参数名不同不构成重载（参数列表相同）
 *
 * 好处：调用者用同一个方法名，编译器根据实参自动匹配版本，
 *       不需要记 setAgeInt / setAgeString 这样的一堆名字。
 */
public class MethodOverloadDemo {

    public static void main(String[] args) {
        // 1. 构造器重载：三种创建方式
        Person p1 = new Person("小明", 18, "male"); // 三参版本
        Person p2 = new Person("小红", 17);         // 两参版本，性别默认 unknown
        Person p3 = new Person("小刚");              // 一参版本，年龄性别默认

        // 2. 方法重载：编译器根据实参自动选择匹配的版本
        System.out.println(p1.introduce());               // 调用版本1（无参）
        System.out.println(p1.introduce(false));          // 调用版本2（boolean）
        System.out.println(p2.introduce("大家好！"));      // 调用版本3（String 前缀）

        // 3. setAge 重载：传 int 或传 String 都能用
        p1.setAge(25);          // 调用 setAge(int)
        p1.setAge("30");        // 调用 setAge(String)，内部转成数字
        System.out.println(p1.getName() + " 现在的年龄：" + p1.getAge());
    }
}
