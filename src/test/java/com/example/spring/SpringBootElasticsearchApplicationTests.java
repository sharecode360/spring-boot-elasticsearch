package com.example.spring;

import com.example.spring.entity.Student;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;  
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class SpringBootElasticsearchApplicationTests {

    private RestHighLevelClient client;

    private ObjectMapper objectMapper;

    @BeforeEach
    void before() {
        HttpHost httpHost = new HttpHost("192.168.56.10",9200,"http");
        RestClientBuilder restClientBuilder = RestClient.builder(httpHost);
        client = new RestHighLevelClient(restClientBuilder);

        // Json <-> Object転換
        objectMapper = new ObjectMapper();
    }

    @Test
    void testIndexRequest() throws IOException {
        IndexRequest index = new IndexRequest("students").id("1");

        Student student = new Student(1001, "Tom", "male", 12, "tom@gmail.com");
        String jsonStu = objectMapper.writeValueAsString(student);
        index.source(jsonStu, XContentType.JSON);
        IndexResponse indexResponse = client.index(index, RequestOptions.DEFAULT);
    }

    @Test
    void testGetRequest() throws IOException {
        GetRequest getRequest = new GetRequest("students").id("1");
        GetResponse response = client.get(getRequest, RequestOptions.DEFAULT);
        if(response.isExists()) {
            String msg = response.getSourceAsString();
            System.out.println("json:" + msg);
            Student student = objectMapper.readValue(msg, Student.class);
            System.out.println(student);
        }
    }

    @Test
    void testUpdatRequest() throws IOException {
        UpdateRequest request = new UpdateRequest("students", "1");
        Student student = new Student(1001, "Tom_update", "male", 22, "tom_update@gmail.com");
        String jsonStu = objectMapper.writeValueAsString(student);
        request.doc(jsonStu, XContentType.JSON);
        client.update(request, RequestOptions.DEFAULT);
    }

    @Test
    void testDeleteRequest() throws IOException {
        DeleteRequest request = new DeleteRequest("students", "1");
        client.delete(request, RequestOptions.DEFAULT);
    }

    @Test
    void testBulkRequest() throws IOException {
        BulkRequest bulkRequest = new BulkRequest("students");

        IndexRequest indexRequest1 = new IndexRequest("students").id("1");
        Student student1 = new Student(1001, "Tom", "male", 22, "tom@gmail.com");
        indexRequest1.source(objectMapper.writeValueAsString(student1), XContentType.JSON);

        Student student2 = new Student(1002, "Jerry", "male", 25, "jerry@gmail.com");
        IndexRequest indexRequest2= new IndexRequest("students").id("2");
        indexRequest2.source(objectMapper.writeValueAsString(student2), XContentType.JSON);

        DeleteRequest deleteRequest = new DeleteRequest("students", "1");

        List<DocWriteRequest<?>> lst = new ArrayList<>();
        lst.add(indexRequest1);
        lst.add(indexRequest2);
        lst.add(deleteRequest);

        bulkRequest.add(lst);

        client.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    @Test
    void testSearchRequest() throws IOException {
        SearchRequest searchRequest = new SearchRequest("students");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        SearchSourceBuilder searchSourceBuilder = sourceBuilder.query(QueryBuilders.matchQuery("sex", "male"));

        // 平均年齢
        AvgAggregationBuilder aggregationBuilder = new AvgAggregationBuilder("avg_age");
        aggregationBuilder.field("age");
        searchSourceBuilder.aggregation(aggregationBuilder);
        searchRequest.source(sourceBuilder);

        // 検索結果
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        Aggregations aggregations = searchResponse.getAggregations();
        Avg avg_age = aggregations.get("avg_age");
        System.out.println(avg_age.getValue());

        SearchHits searchHits = searchResponse.getHits();
        for (SearchHit hit : searchHits.getHits()) {
            String tmp = hit.getSourceAsString();
            Student stu = objectMapper.readValue(tmp, Student.class);
            System.out.println(stu);
        }
    }

    @AfterEach
    void after() throws IOException {
        if(client != null) {
            client.close();
        }
    }
}