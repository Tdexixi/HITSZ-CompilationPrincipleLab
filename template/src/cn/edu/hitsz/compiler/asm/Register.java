package cn.edu.hitsz.compiler.asm;

public class Register {
    public static Register named(String name) {
        return new Register(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Register reg && name.equals(reg.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    private Register(String name) {
        this.name = name;
    }

    public void setUsedValid() {
        this.used = true;
    }

    public void setUsedInvalid() {
        this.used = false;
    }

    private final String name;

    public  boolean getUsed() {
        return used;
    }


    private boolean used = false;


}
