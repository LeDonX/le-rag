package com.le.rag.controller;

import com.alibaba.fastjson.JSON;
import com.le.rag.advisors.MetadataAwareQuestionAnswerAdvisor;
import com.le.rag.annotation.Loggable;
import com.le.rag.common.ApplicationConstant;
import com.le.rag.context.BaseContext;
import com.le.rag.entity.SensitiveWord;
import com.le.rag.service.SensitiveWordService;
import com.le.rag.service.SuperSqlIntegrationService;
import com.le.rag.tools.RagTool;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "AiRagController", description = "Rag接口")
@Slf4j
@RestController
@RequestMapping(ApplicationConstant.API_VERSION + "/ai")
public class AiRagController {

    /**
     * 对话代理
     */
    ChatClient chatClient;
    VectorStore vectorStore;
    @Autowired
    private SensitiveWordService sensitiveWordService;
    private ChatModel chatModel;

    /*@Autowired
    private OllamaRagModel ollamaRagModel; */

    private static final String DEFAULT_SYSTEM_PROMPT = """
            你是"le"知识库系统的对话助手，请以乐于助人的方式进行对话，
            {rag_message}
            今天的日期：{current_date}
            """;

//    @Autowired
//    private SuperSqlIntegrationService superSqlIntegrationService;

    public AiRagController(ChatModel chatModel, ChatMemory chatMemory,
                           VectorStore vectorStore,
                           RagTool ragTool) {
        this.chatModel = chatModel;
        this.chatClient = ChatClient.builder(chatModel)
                // 隐式
                .defaultSystem(DEFAULT_SYSTEM_PROMPT)
                .defaultSystem(p -> p.param("rag_message", ""))
                .defaultAdvisors(
                        PromptChatMemoryAdvisor.builder(chatMemory).build(),
                        SimpleLoggerAdvisor.builder().build(),
                        new MetadataAwareQuestionAnswerAdvisor()
                )
                .defaultTools(ragTool)
                .build();
        this.vectorStore = vectorStore;
    }

    @Operation(summary = "rag post", description = "Rag对话接口POST版本")
    @PostMapping(value = "/rag")
    @Loggable
    public Flux<String> generatePost(@RequestParam(value = "sources", required = false) List<String> sources,
                                     @RequestParam(value = "message", defaultValue = "你好") String message) throws IOException {
        // 敏感词过滤
        List<SensitiveWord> list = sensitiveWordService.list();

        for (SensitiveWord sensitiveWord : list) {
            if (message.contains(sensitiveWord.getWord())) {
                return Flux.just("包含敏感词:" + sensitiveWord.getWord());
            }
        }

        // 检查是否是聚合查询   router方式
       /*boolean isSql= chatClient.prompt()
                .system("用户的查询是否涉及统计数据、求和、计数、平均值等聚合操作？")
                .user(message)      // 徐庶这个热词出现了多少次
                .call()
                .entity(Boolean.class); */
        return processNormalRagQuery(sources, message);
    }

    /**
     * 处理正常的 RAG 查询
     *
     * @param sources 数据源列表
     * @param message 用户消息
     * @return 响应流
     */
    private Flux<String> processNormalRagQuery(List<String> sources, String message) {
        Long userId = BaseContext.getCurrentId();
        ChatClient.ChatClientRequestSpec clientRequestSpec = chatClient.prompt()
                .user(message)
                .system(a -> a.param("current_date", LocalDate.now().toString()))
                // 为什么要存userMessage  为了MetadataAwareQuestionAnswerAdvisor中获取
                .advisors(a -> a.param("userMessage", message))
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId));
        // 如果提供了sources参数，使用向量数据库查询
        if (sources != null && !sources.isEmpty()) {

            SearchRequest.Builder searchRequestBuilder = SearchRequest.builder()
                    .query(message)
                    .similarityThreshold(0.1d).topK(5)
                    // source in ['xxx.pdf','xxxx']
                    .filterExpression("source in " + JSON.toJSONString(sources));

            // 增强QuestionAnswerAdvisor  ：
            // 包含:
            // 1. 检索为空时，返回提示
            // 2. 查询重写
            /*Advisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                    // 查 = QuestionAnswerAdvisor
                    .documentRetriever(VectorStoreDocumentRetriever.builder()
                            .similarityThreshold(0.10)
                            .vectorStore(vectorStore)
                            .build())
                    // 检索为空时，返回提示
                    .queryAugmenter(ContextualQueryAugmenter.builder()
                            .allowEmptyContext(false)
                            .emptyContextPromptTemplate(PromptTemplate.builder().template("用户查询位于知识库之外。礼貌地告知用户您无法回答").build())
                            .build())
                    .queryTransformers(RewriteQueryTransformer.builder()
                            .chatClientBuilder(ChatClient.builder(chatModel))
                            .build())
                    .build();*/

            // 自行从vectorStore中查询，自行拼接
            /*List<Document> documents = vectorStore.similaritySearch(searchRequestBuilder.build());
            for (Document document : documents){
                String text = document.getText();
                String source = document.getMetadata().get("source").toString();
                message+=text+"文件来源"+source;
            }*/

            // 重排序 2次筛选--->只有前面步骤已经优化完毕
            /*RetrievalRerankAdvisor retrievalRerankAdvisor =
                    new RetrievalRerankAdvisor(vectorStore, dashScopeRerankModel
                            , SearchRequest.builder().topK(200).build());*/

            clientRequestSpec = clientRequestSpec
                    .system(a -> a.param("rag_message", """
                            如果涉及RAG，请提供文件来源，我会提供给你文件来源，
                            请严格基于知识库内容回答用户问题，
                            不要添加任何知识库之外的信息。如果知识库内容不完整，仅需基于已有信息作答，
                            不要自行补充。
                            """))
                    // filterExpression的param指定方式：
                    //.advisors(advisorSpec -> advisorSpec.param(QuestionAnswerAdvisor.FILTER_EXPRESSION,"source in "+JSON.toJSONString(sources)));
                    //.advisors(retrievalRerankAdvisor);
                    .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                            .searchRequest(searchRequestBuilder.build())
                            .build());
        }

        Flux<String> content = clientRequestSpec
                .stream()// 流式方式
                .content();

        return content;
    }

}

