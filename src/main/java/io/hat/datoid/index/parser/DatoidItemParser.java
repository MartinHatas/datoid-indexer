package io.hat.datoid.index.parser;

import io.hat.datoid.index.model.Item;
import io.vavr.API;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;

@Singleton
public class DatoidItemParser {

    private static final Logger log = LoggerFactory.getLogger(DatoidItemParser.class);

    private static final String SUFFIX_SPAN_CLASS = ".suffix";
    private static final String FILENAME_SPAN_CLASS = ".filename";
    private static final String NEWEST_DIV_ID = "snippet--newest_files";
    private static final String TIME_ICON_CLASS = ".icon-time-white";
    private static final String SIZE_ICON_CLASS = ".icon-size-white";
    private static final String DATA_COUNT_ATTR = "data-count";

    private static final String SRC = "src";
    private static final String LI = "li";
    private static final String A = "a";
    private static final String HREF = "href";
    private static final String THUMB_DIV_CLASS = ".thumb";
    private static final String IMG = "img";
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";

    private static final String GB = " GB";
    private static final String MB = " MB";
    private static final String KB = " KB";
    private static final String B = " B";
    private static final String EMPTY = "";

    public List<Item> parseItemHtmlPage(String htmlPage) {

        Document document = Jsoup.parse(htmlPage);

        Elements newestItems = Option.of(document.getElementById(NEWEST_DIV_ID))
                .map(newestDiv -> newestDiv.getElementsByTag(LI))
                .getOrElse(() -> {
                    log.warn("Can't find any item in source: \n {}", htmlPage);
                    return new Elements();
                });


        return newestItems.stream()
                .map(this::elementToItem)
                .collect(Collectors.toList());
    }

    private Item elementToItem(Element element) {
        Element linkElement = element.selectFirst(A);

        String filename = element.selectFirst(FILENAME_SPAN_CLASS).text();
        String link = linkElement.attr(HREF);
        Item.Thumbnail thumbnail = getThumbnail(linkElement);
        String suffix = Option.of(element.selectFirst(SUFFIX_SPAN_CLASS)).map(Element::text).getOrNull();
        Integer lengthSeconds = getLengthInSeconds(element);
        Long sizeKilobytes = getSizeInKilobytes(element);

        return new Item(filename, link, thumbnail, suffix, lengthSeconds, sizeKilobytes);
    }

    private Long getSizeInKilobytes(Element element) {
        return Option.of(element.selectFirst(SIZE_ICON_CLASS))
                .map(Element::parent)
                .map(Element::text)
                .flatMap(sizeText -> API.Match(sizeText).option(
                        Case($(size -> size.endsWith(GB)), tryParseFileSize(sizeText, GB, 1024 * 1024 * 1024)),
                        Case($(size -> size.endsWith(MB)), tryParseFileSize(sizeText, MB, 1024 * 1024)),
                        Case($(size -> size.endsWith(KB)), tryParseFileSize(sizeText, KB, 1024)),
                        Case($(size -> size.endsWith(B)), tryParseFileSize(sizeText, B, 1))
                )).getOrNull();
    }

    private Long tryParseFileSize(String sizeText, String unit, int multiplyBy) {
        return Try.of(() -> Double.parseDouble(sizeText.replace(unit, EMPTY)))
                .map(decimalSize -> decimalSize * multiplyBy)
                .map(Double::longValue).getOrNull();
    }

    private Integer getLengthInSeconds(Element element) {
        return Option.of(element.selectFirst(TIME_ICON_CLASS))
                .map(Element::parent)
                .map(Element::text)
                .map(time -> Try.of(() -> LocalTime.parse(time, ISO_LOCAL_TIME).toSecondOfDay()).getOrNull())
                .getOrNull();
    }

    private Item.Thumbnail getThumbnail(Element linkElement) {
        Element thumbElement = linkElement.selectFirst(THUMB_DIV_CLASS).selectFirst(IMG);
        String src = thumbElement.attr(SRC);
        String width = thumbElement.attr(WIDTH);
        String height = thumbElement.attr(HEIGHT);
        String count = thumbElement.attr(DATA_COUNT_ATTR);
        return new Item.Thumbnail(src, width, height, count);
    }

}
