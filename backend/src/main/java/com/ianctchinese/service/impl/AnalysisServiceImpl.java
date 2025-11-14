package com.ianctchinese.service.impl;

import com.ianctchinese.dto.AutoAnnotationResponse;
import com.ianctchinese.dto.ClassificationResponse;
import com.ianctchinese.dto.TextInsightsResponse;
import com.ianctchinese.dto.TextInsightsResponse.MapPathPoint;
import com.ianctchinese.dto.TextInsightsResponse.Stats;
import com.ianctchinese.dto.TextInsightsResponse.TimelineEvent;
import com.ianctchinese.dto.TextInsightsResponse.WordCloudItem;
import com.ianctchinese.model.EntityAnnotation;
import com.ianctchinese.model.EntityAnnotation.EntityCategory;
import com.ianctchinese.model.RelationAnnotation;
import com.ianctchinese.model.RelationAnnotation.RelationType;
import com.ianctchinese.model.TextDocument;
import com.ianctchinese.model.TextSection;
import com.ianctchinese.repository.EntityAnnotationRepository;
import com.ianctchinese.repository.RelationAnnotationRepository;
import com.ianctchinese.repository.TextDocumentRepository;
import com.ianctchinese.repository.TextSectionRepository;
import com.ianctchinese.service.AnalysisService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {

  private static final Map<String, String> CATEGORY_LABELS = Map.of(
      "warfare", "战争纪实",
      "travelogue", "游记地理",
      "biography", "人物传记"
  );

  private final TextDocumentRepository textDocumentRepository;
  private final EntityAnnotationRepository entityAnnotationRepository;
  private final RelationAnnotationRepository relationAnnotationRepository;
  private final TextSectionRepository textSectionRepository;

  @Override
  @Transactional
  public ClassificationResponse classifyText(Long textId) {
    TextDocument text = loadText(textId);
    Map<String, Integer> scoreMap = calculateCategoryScores(text.getContent());
    String category = scoreMap.entrySet().stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse(text.getCategory());
    double confidence = calculateConfidence(scoreMap, category);
    text.setCategory(category);
    textDocumentRepository.save(text);
    List<String> reasons = buildReasons(category);
    return ClassificationResponse.builder()
        .textId(textId)
        .suggestedCategory(category)
        .confidence(confidence)
        .reasons(reasons)
        .build();
  }

  @Override
  public TextInsightsResponse buildInsights(Long textId) {
    TextDocument text = loadText(textId);
    List<EntityAnnotation> entities = entityAnnotationRepository.findByTextDocumentId(textId);
    List<RelationAnnotation> relations = relationAnnotationRepository.findByTextDocumentId(textId);
    List<TextSection> sections = textSectionRepository.findByTextDocumentId(textId);

    Stats stats = Stats.builder()
        .entityCount(entities.size())
        .relationCount(relations.size())
        .punctuationProgress(calculatePunctuationProgress(sections))
        .build();

    List<WordCloudItem> wordCloud = buildWordCloud(text.getContent());
    List<TimelineEvent> timeline = buildTimelineForCategory(text.getCategory());
    List<MapPathPoint> mapPoints = buildMapPointsForCategory(text.getCategory());
    List<String> recommendedViews = buildRecommendedViews(text.getCategory());
    List<TextInsightsResponse.BattleEvent> battleTimeline = buildBattleTimeline(text.getCategory());
    List<TextInsightsResponse.FamilyNode> familyTree = buildFamilyTree(text.getCategory());

    return TextInsightsResponse.builder()
        .textId(textId)
        .category(text.getCategory())
        .stats(stats)
        .wordCloud(wordCloud)
        .timeline(timeline)
        .mapPoints(mapPoints)
        .battleTimeline(battleTimeline)
        .familyTree(familyTree)
        .recommendedViews(recommendedViews)
        .analysisSummary(buildAnalysisSummary(text, stats))
        .build();
  }

  @Override
  @Transactional
  public AutoAnnotationResponse autoAnnotate(Long textId) {
    TextDocument text = loadText(textId);
    List<EntityAnnotation> existingEntities = entityAnnotationRepository.findByTextDocumentId(textId);
    if (!existingEntities.isEmpty()) {
      return AutoAnnotationResponse.builder()
          .textId(textId)
          .createdEntities(0)
          .createdRelations(0)
          .message("已有实体标注，已跳过自动生成。")
          .build();
    }

    List<EntityAnnotation> generatedEntities = generateEntitiesForCategory(text);
    entityAnnotationRepository.saveAll(generatedEntities);

    List<RelationAnnotation> generatedRelations = generateRelationsForCategory(text, generatedEntities);
    relationAnnotationRepository.saveAll(generatedRelations);

    return AutoAnnotationResponse.builder()
        .textId(textId)
        .createdEntities(generatedEntities.size())
        .createdRelations(generatedRelations.size())
        .message("已根据文本类型生成示例标注，可在前端继续校对。")
        .build();
  }

  private TextDocument loadText(Long textId) {
    return textDocumentRepository.findById(textId)
        .orElseThrow(() -> new IllegalArgumentException("Text not found: " + textId));
  }

  private Map<String, Integer> calculateCategoryScores(String content) {
    Map<String, List<String>> keywordMap = new HashMap<>();
    keywordMap.put("warfare", List.of("战", "军", "攻", "守", "兵", "城", "矢"));
    keywordMap.put("travelogue", List.of("山", "水", "江", "湖", "行", "至", "游", "路", "舟"));
    keywordMap.put("biography", List.of("生", "卒", "仕", "官", "字", "年", "家", "传"));

    Map<String, Integer> scores = new HashMap<>();
    keywordMap.forEach((category, keywords) -> {
      int score = 0;
      for (String keyword : keywords) {
        score += countOccurrences(content, keyword);
      }
      scores.put(category, score);
    });
    return scores;
  }

  private int countOccurrences(String content, String keyword) {
    return content.length() - content.replace(keyword, "").length();
  }

  private double calculateConfidence(Map<String, Integer> scores, String category) {
    int bestScore = scores.getOrDefault(category, 1);
    int total = scores.values().stream().mapToInt(Integer::intValue).sum();
    if (total == 0) {
      return 0.5;
    }
    return Math.min(0.95, (double) bestScore / total);
  }

  private List<String> buildReasons(String category) {
    String label = CATEGORY_LABELS.getOrDefault(category, "综合");
    return List.of("文本特征与 " + label + " 语料高度相似", "关键实体与关系模式匹配该类型");
  }

  private Double calculatePunctuationProgress(List<TextSection> sections) {
    if (sections.isEmpty()) {
      return 0.1;
    }
    long completed = sections.stream()
        .filter(section -> section.getPunctuatedText() != null && !section.getPunctuatedText().isBlank())
        .count();
    return (double) completed / sections.size();
  }

  private List<WordCloudItem> buildWordCloud(String content) {
    String[] tokens = content.split("[，。、；：？！\\s]");
    Map<String, Integer> frequency = new HashMap<>();
    for (String token : tokens) {
      if (token.length() < 2) {
        continue;
      }
      frequency.merge(token, 1, Integer::sum);
    }
    return frequency.entrySet().stream()
        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
        .limit(12)
        .map(entry -> WordCloudItem.builder()
            .label(entry.getKey())
            .weight(entry.getValue() / 10.0 + 0.3)
            .build())
        .collect(Collectors.toList());
  }

  private List<TimelineEvent> buildTimelineForCategory(String category) {
    List<TimelineEvent> events = new ArrayList<>();
    switch (category) {
      case "travelogue" -> events.addAll(List.of(
          TimelineEvent.builder().title("启程").description("作者离开京师，溯江而上").dateLabel("初春").significance(6).build(),
          TimelineEvent.builder().title("抵达名山").description("记述山川胜景与碑刻").dateLabel("暮春").significance(8).build(),
          TimelineEvent.builder().title("归程总结").description("整理沿途见闻与思考").dateLabel("初夏").significance(7).build()
      ));
      case "biography" -> events.addAll(List.of(
          TimelineEvent.builder().title("少有奇志").description("幼年立志求学").dateLabel("少年").significance(5).build(),
          TimelineEvent.builder().title("入仕任官").description("受知于名臣，踏入仕途").dateLabel("壬午").significance(8).build(),
          TimelineEvent.builder().title("升迁与谪居").description("仕途沉浮，辗转多地").dateLabel("癸未").significance(7).build()
      ));
      default -> events.addAll(List.of(
          TimelineEvent.builder().title("部队集结").description("调兵遣将，整训兵甲").dateLabel("初旬").significance(6).build(),
          TimelineEvent.builder().title("决战节点").description("火攻突袭，扭转战局").dateLabel("望日").significance(10).build(),
          TimelineEvent.builder().title("善后与盟约").description("划定疆界，订立盟约").dateLabel("下旬").significance(7).build()
      ));
    }
    return events;
  }

  private List<MapPathPoint> buildMapPointsForCategory(String category) {
    if (!"travelogue".equals(category)) {
      return List.of();
    }
    return List.of(
        MapPathPoint.builder().label("长安").latitude(34.3416).longitude(108.9398).sequence(1).build(),
        MapPathPoint.builder().label("襄阳").latitude(32.065).longitude(112.153).sequence(2).build(),
        MapPathPoint.builder().label("武夷山").latitude(27.728).longitude(118.035).sequence(3).build(),
        MapPathPoint.builder().label("建康").latitude(32.058).longitude(118.796).sequence(4).build()
    );
  }

  private List<TextInsightsResponse.BattleEvent> buildBattleTimeline(String category) {
    if (!"warfare".equals(category)) {
      return List.of();
    }
    return List.of(
        TextInsightsResponse.BattleEvent.builder()
            .phase("先声夺人")
            .description("奇兵夜袭，对手措手不及")
            .intensity(6)
            .opponent("北军")
            .build(),
        TextInsightsResponse.BattleEvent.builder()
            .phase("火攻突袭")
            .description("顺风纵火，焚毁敌营")
            .intensity(9)
            .opponent("曹营")
            .build(),
        TextInsightsResponse.BattleEvent.builder()
            .phase("追击与议和")
            .description("乘胜北伐，并商议城下之盟")
            .intensity(7)
            .opponent("东路军")
            .build()
    );
  }

  private List<TextInsightsResponse.FamilyNode> buildFamilyTree(String category) {
    if (!"biography".equals(category)) {
      return List.of();
    }
    return List.of(
        TextInsightsResponse.FamilyNode.builder()
            .name("祖父")
            .relation("祖")
            .children(List.of(
                TextInsightsResponse.FamilyNode.builder()
                    .name("父亲")
                    .relation("父")
                    .children(List.of(
                        TextInsightsResponse.FamilyNode.builder()
                            .name("主人公")
                            .relation("本人")
                            .children(List.of(
                                TextInsightsResponse.FamilyNode.builder()
                                    .name("长子")
                                    .relation("子")
                                    .children(List.of())
                                    .build(),
                                TextInsightsResponse.FamilyNode.builder()
                                    .name("次女")
                                    .relation("女")
                                    .children(List.of())
                                    .build()
                            ))
                            .build(),
                        TextInsightsResponse.FamilyNode.builder()
                            .name("妹")
                            .relation("妹")
                            .children(List.of())
                            .build()
                    ))
                    .build()
            ))
            .build()
    );
  }

  private List<String> buildRecommendedViews(String category) {
    return switch (category) {
      case "travelogue" -> List.of("地图", "时间轴", "词云");
      case "biography" -> List.of("时间轴", "亲情树", "知识图谱");
      default -> List.of("知识图谱", "对抗视图", "统计图表");
    };
  }

  private String buildAnalysisSummary(TextDocument text, Stats stats) {
    String categoryLabel = CATEGORY_LABELS.getOrDefault(text.getCategory(), "综合");
    return String.format(Locale.CHINA,
        "模型建议该文本归类为「%s」，已完成 %d 个实体、%d 条关系。建议优先查看 %s 视图以洞察关键信息。",
        categoryLabel,
        stats.getEntityCount(),
        stats.getRelationCount(),
        String.join(" / ", buildRecommendedViews(text.getCategory())));
  }

  private List<EntityAnnotation> generateEntitiesForCategory(TextDocument text) {
    List<EntityAnnotation> entities = new ArrayList<>();
    switch (text.getCategory()) {
      case "travelogue" -> entities.addAll(List.of(
          buildEntity(text, "作者", EntityCategory.PERSON, 0, 2, 0.95),
          buildEntity(text, "名山", EntityCategory.LOCATION, 15, 17, 0.8),
          buildEntity(text, "江河", EntityCategory.LOCATION, 30, 32, 0.78)
      ));
      case "biography" -> entities.addAll(List.of(
          buildEntity(text, "主人公", EntityCategory.PERSON, 0, 3, 0.96),
          buildEntity(text, "父亲", EntityCategory.PERSON, 10, 12, 0.82),
          buildEntity(text, "官职", EntityCategory.ORGANIZATION, 40, 42, 0.75)
      ));
      default -> entities.addAll(List.of(
          buildEntity(text, "主帅", EntityCategory.PERSON, 0, 2, 0.94),
          buildEntity(text, "敌军", EntityCategory.ORGANIZATION, 20, 22, 0.78),
          buildEntity(text, "战场", EntityCategory.LOCATION, 35, 37, 0.81)
      ));
    }
    return entities;
  }

  private EntityAnnotation buildEntity(TextDocument text, String label, EntityCategory category,
      int start, int end, double confidence) {
    return EntityAnnotation.builder()
        .textDocument(text)
        .startOffset(start)
        .endOffset(end)
        .label(label)
        .category(category)
        .confidence(confidence)
        .color("#d16a5d")
        .build();
  }

  private List<RelationAnnotation> generateRelationsForCategory(TextDocument text,
      List<EntityAnnotation> entities) {
    if (entities.size() < 2) {
      return List.of();
    }
    List<RelationAnnotation> relations = new ArrayList<>();
    EntityAnnotation first = entities.get(0);
    EntityAnnotation second = entities.get(1);
    RelationAnnotation.RelationType relationType = switch (text.getCategory()) {
      case "travelogue" -> RelationType.TRAVEL;
      case "biography" -> RelationType.FAMILY;
      default -> RelationType.CONFLICT;
    };
    relations.add(RelationAnnotation.builder()
        .textDocument(text)
        .source(first)
        .target(second)
        .relationType(relationType)
        .confidence(0.72)
        .evidence("示例推理")
        .build());
    return relations;
  }
}
