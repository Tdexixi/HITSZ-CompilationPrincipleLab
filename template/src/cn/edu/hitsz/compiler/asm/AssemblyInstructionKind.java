package cn.edu.hitsz.compiler.asm;

public enum AssemblyInstructionKind {
	li, sub, mul, mv, addi ;

    /**
     * @return  是否是二元的 (有返回值, 有两个参数)
     */
    public boolean isBinary() {
        return this != mv && this != li;
    }

    /**
     * @return IR 是否是MV指令的 (有返回值, 有一个参数)
     */
    public boolean isMV() {
        return this == mv;
    }

    /**
     * @return IR 是否是LI指令的 (有返回值, 有一个参数)
     */
    public boolean isLI() {
        return this == li;
    }
}
