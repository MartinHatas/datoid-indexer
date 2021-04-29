package io.hat.datoid.index.http;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;
import io.reactivex.Single;

@Client("${datoid.url}")
public interface DatoidHttpClient {

    @Get(value = "/nejstahovanejsi", processes = MediaType.TEXT_HTML)
    Single<String> getLatestItemsHtml();

}
