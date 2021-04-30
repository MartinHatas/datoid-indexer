package io.hat.datoid.index.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hat.datoid.index.http.DatoidHttpClient;
import io.hat.datoid.index.parser.DatoidItemParser;
import io.micronaut.scheduling.annotation.Scheduled;
import io.vavr.control.Try;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class IndexService {

    private static final Logger log = LoggerFactory.getLogger(IndexService.class);
    private static final String INDEX_NAME = "datoid-items";

    @Inject
    private DatoidHttpClient datoid;

    @Inject
    private DatoidItemParser parser;

    @Inject
    private RestHighLevelClient elastic;

    @Inject
    private ObjectMapper mapper;

    @Scheduled(fixedDelay = "${datoid.poll.interval}")
    public void executeIndexJob() {
        log.info("Starting index job");
        datoid.getLatestItemsHtml()
                .flattenAsFlowable(parser::getItemsPaths)
                .concatMapSingle(pathParams -> datoid.getItem(pathParams._1, pathParams._2))
                .map(parser::parseItemHtmlPage)
                .doOnNext(item -> log.info("{}", item))
                .map(item -> Try.of(() -> mapper.writeValueAsString(item)))
                .filter(Try::isSuccess)
                .map(Try::get)
                .map(item -> new IndexRequest(INDEX_NAME).id(String.valueOf(item.hashCode())).source(item, XContentType.JSON))
                .subscribe(indexRequest -> elastic.index(indexRequest, RequestOptions.DEFAULT));
    }

}
