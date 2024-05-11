package com.linkcircle.ss;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2021/11/15 17:27
 */

public class C extends ClassLoader{
    private A a = new A();
    public C() {
        super(Thread.currentThread().getContextClassLoader());
    }
    @Override
    protected Class<?> findClass(String name) {
        A a = this.a == null ? new A() : this.a;
        byte[] data = a.b(name);
        int length = data == null ? 0 : data.length;
        return defineClass(name, data, 0, length);
    }


}
