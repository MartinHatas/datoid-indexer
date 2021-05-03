package io.hat.datoid.index.api;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.vavr.control.Option;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller("/search")
public class SearchController {

    private record Results(long total, List<Map<String, Object>> items) {}

    private static final int DEFAULT_FROM = 0;
    private static final int DEFAULT_SIZE = 25;

    @Inject
    private RestHighLevelClient elastic;

    @Get()
    public Results searchItems(
            @Nullable String query,
            @Nullable Integer from,
            @Nullable Integer size) throws IOException {

        final var searchRequest = new SearchRequest();
        final var searchBuilder = new SearchSourceBuilder();

        var qb = Option.of(query)
                .map(q -> (QueryBuilder)QueryBuilders.simpleQueryStringQuery(q))
                .getOrElse(QueryBuilders::matchAllQuery);

        searchBuilder.query(qb);

        searchBuilder.from(getPositiveOrDefault(from, DEFAULT_FROM));
        searchBuilder.size(getPositiveOrDefault(size, DEFAULT_SIZE));

        searchRequest.source(searchBuilder);

        final var response = elastic.search(searchRequest, RequestOptions.DEFAULT);
        final var totalHits = response.getHits().getTotalHits().value;
        final var results = Arrays.stream(response.getHits().getHits()).map(SearchHit::getSourceAsMap).collect(Collectors.toList());

        return new Results(totalHits, results);
    }

    private int getPositiveOrDefault(Integer number, int defaultValue) {
        return number != null && number >= 0 ? number : defaultValue;
    }

}
