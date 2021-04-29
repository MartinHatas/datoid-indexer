package io.hat.datoid.index.service;

import io.hat.datoid.index.http.DatoidHttpClient;
import io.hat.datoid.index.parser.DatoidItemParser;
import io.micronaut.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class IndexService {

    private static final Logger log = LoggerFactory.getLogger(IndexService.class);

    @Inject
    private DatoidHttpClient datoid;

    @Inject
    private DatoidItemParser parser;

    @Scheduled(fixedDelay = "${datoid.poll.interval}")
    public void executeIndexJob() {
        log.info("Starting index job");
        datoid.getLatestItemsHtml()
        .flattenAsFlowable(parser::parseItemHtlmPage)
        .subscribe(i -> log.info("{}", i), e -> log.error("Error", e));
    }

}
