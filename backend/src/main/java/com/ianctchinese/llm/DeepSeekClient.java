package com.ianctchinese.llm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeepSeekClient {

  private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";

  /**
   * 将 DEEPSEEK_API_KEY 替换成你的真实 key，或改为从配置文件 / 环境变量读取。
   */
  private static final String API_KEY_PLACEHOLDER = "PASTE_YOUR_DEEPSEEK_API_KEY_HERE";

  private final RestTemplate restTemplate = new RestTemplate();

  public String classifyText(String textContent) {
    String systemPrompt = "你是一名古汉语文本分类助手，需要根据输入文本判断其属于战争纪实、游记地理、人物传记或其他类型。";
    String userPrompt = """
        请输出 JSON：{"category":"warfare|travelogue|biography|other","confidence":0-1,"reasons":["..."]}
        文本：
        %s
        """.formatted(textContent);
    return callDeepSeek(systemPrompt, userPrompt);
  }

  public String extractEntities(String textContent) {
    String systemPrompt = "你是一名古籍实体与关系抽取助手，需要返回实体和关系结构。";
    String userPrompt = """
        请输出 JSON：
        {
          "entities":[{"label":"","category":"","startOffset":0,"endOffset":0}],
          "relations":[{"sourceLabel":"","targetLabel":"","relationType":"","confidence":0-1}],
          "notes":"可选补充说明"
        }
        文本：
        %s
        """.formatted(textContent);
    return callDeepSeek(systemPrompt, userPrompt);
  }

  public String punctuateText(String textContent) {
    String systemPrompt = "你是一名古文句读助手，需要输出句读结果（保持原文，添加标点）以及结构摘要。";
    String userPrompt = """
        请输出 JSON：
        {
          "sentences":[
            {"original":"","punctuated":"","summary":""}
          ]
        }
        文本：
        %s
        """.formatted(textContent);
    return callDeepSeek(systemPrompt, userPrompt);
  }

  private String callDeepSeek(String systemPrompt, String userPrompt) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(API_KEY_PLACEHOLDER);

    DeepSeekRequest request = DeepSeekRequest.builder()
        .model("deepseek-chat")
        .messages(List.of(
            Message.builder().role("system").content(systemPrompt).build(),
            Message.builder().role("user").content(userPrompt).build()
        ))
        .temperature(0.2)
        .maxTokens(1024)
        .build();

    HttpEntity<DeepSeekRequest> entity = new HttpEntity<>(request, headers);
    ResponseEntity<DeepSeekResponse> response = restTemplate.postForEntity(API_URL, entity,
        DeepSeekResponse.class);
    if (response.getBody() == null
        || response.getBody().getChoices() == null
        || response.getBody().getChoices().isEmpty()) {
      log.warn("DeepSeek 无返回内容，status={}", response.getStatusCode());
      return "";
    }
    return response.getBody().getChoices().get(0).getMessage().getContent();
  }

  @Data
  @Builder
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private static class DeepSeekRequest {

    private String model;
    private List<Message> messages;
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    private Double temperature;
  }

  @Data
  @Builder
  private static class Message {

    private String role;
    private String content;
  }

  @Data
  private static class DeepSeekResponse {

    private List<Choice> choices;

    @Data
    private static class Choice {

      private Message message;
    }
  }
}
