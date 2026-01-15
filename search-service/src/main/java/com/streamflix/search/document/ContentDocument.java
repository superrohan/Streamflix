package com.streamflix.search.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Elasticsearch document for content search.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "streamflix-content")
@Setting(settingPath = "elasticsearch/settings.json")
public class ContentDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "autocomplete", searchAnalyzer = "standard")
    private String titleAutocomplete;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String originalTitle;

    @Field(type = FieldType.Keyword)
    private String slug;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private String contentType;

    @Field(type = FieldType.Keyword)
    private List<String> genres;

    @Field(type = FieldType.Integer)
    private Integer releaseYear;

    @Field(type = FieldType.Keyword)
    private String maturityRating;

    @Field(type = FieldType.Keyword)
    private List<String> cast;

    @Field(type = FieldType.Keyword)
    private List<String> directors;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Text)
    private String posterUrl;

    @Field(type = FieldType.Double)
    private BigDecimal averageRating;

    @Field(type = FieldType.Double)
    private BigDecimal popularityScore;

    @Field(type = FieldType.Boolean)
    private Boolean isOriginal;

    @Field(type = FieldType.Date)
    private Instant publishedAt;

    @Field(type = FieldType.Date)
    private Instant updatedAt;
}
