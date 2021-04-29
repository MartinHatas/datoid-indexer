package io.hat.datoid.index.parser;

import io.hat.datoid.index.model.Item;
import io.vavr.control.Option;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class DatoidItemParser {

    private static final String NEWEST_DIV_ID = "snippet--newest_files";
    private static final String LI = "li";
    private static final String FILENAME_SPAN_CLASS = ".filename";
    private static final String A = "a";
    private static final String HREF = "href";

    public List<Item> parseItemHtlmPage(String htmlPage) {

        Document document = Jsoup.parse(htmlPage);

        Elements newestItems = Option.of(document.getElementById(NEWEST_DIV_ID))
                .map(newestDiv -> newestDiv.getElementsByTag(LI))
                .getOrElse(Elements::new);

        return newestItems.stream()
                .map(this::elementToItem)
                .collect(Collectors.toList());
    }

    private Item elementToItem(Element element) {
        String filename = element.selectFirst(FILENAME_SPAN_CLASS).text();
        String link = element.selectFirst(A).attr(HREF);
        return new Item(filename, link);
    }

}
