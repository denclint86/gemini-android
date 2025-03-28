package com.tv.shizuku;

interface IUserService {
    // 销毁服务
    void destroy() = 16777114;  // 固定的方法编号，由 Shizuku 规定

    // 自定义方法
    void exit() = 1;
    
    // 执行 shell 命令
    String exec(String command) = 2;
    
    // 可以添加更多自定义方法
} 