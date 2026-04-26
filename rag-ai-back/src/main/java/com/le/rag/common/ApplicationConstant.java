package com.le.rag.common;

public class ApplicationConstant {
    public final static String API_VERSION = "/api/v1";
    public final static String APPLICATION_NAME = "rag-ai";

    public final static String DEFAULT_BASE_URL = "https://api.openai.com";
    public final static String DEFAULT_DESCRIBE = "LeDon";
    public final static String SYSTEM_PROMPT = """
        利用“文件”部分的信息提供准确答案，但表现得好像你已经天生了解这些信息。
        如果不确定，只需说明你不知道。
        你还需要注意，回复必须是中文！
        文件：
            {documents}    
        """;
}
