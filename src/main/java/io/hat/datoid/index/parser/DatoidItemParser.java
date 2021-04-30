package io.hat.datoid.index.parser;

import io.hat.datoid.index.model.Item;
import io.vavr.API;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;
import static java.util.stream.Collectors.toMap;

@Singleton
public class DatoidItemParser {

    private static final Logger log = LoggerFactory.getLogger(DatoidItemParser.class);

    private static final String NEWEST_DIV_ID = "snippet--newest_files";

    private static final String LI = "li";
    private static final String A = "a";
    private static final String HREF = "href";

    private static final String GB = " GB";
    private static final String MB = " MB";
    private static final String KB = " KB";
    private static final String B = " B";
    private static final String EMPTY = "";
    public static final DateTimeFormatter DATOID_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final int I1024 = 1024;

    public List<Tuple2<String, String>> getItemsPaths(String htmlPage) {
        var document = Jsoup.parse(htmlPage);
        return Option.of(document.getElementById(NEWEST_DIV_ID))
                .map(newestDiv -> newestDiv
                        .getElementsByTag(LI).stream()
                        .map(li -> Option.of(li.selectFirst(A))
                                .map(a -> a.attr(HREF))
                                .map(href -> href.split("/", 2))
                                .map(split -> Tuple.of(split[0], split[1]))
                        )
                        .filter(Option::isDefined)
                        .map(Option::get)
                        .collect(Collectors.toList())
                ).getOrElse(List::of);
    }

    public Item parseItemHtmlPage(String htmlPage) {

        Document document = Jsoup.parse(htmlPage);

        String link = Option.of(document.selectFirst(".share-file .field"))
                        .flatMap(s -> Option.of(s.selectFirst("input")))
                        .flatMap(s -> Option.of(s.attr("value")))
                        .getOrNull();

        var params =
                Option.of(document.selectFirst(".parameters"))
                .map(table -> table.select("tr"))
                .map(rows ->
                    rows.stream()
                        .collect(toMap(
                            r -> r.selectFirst("th").text(),
                            r -> r.selectFirst("td").text()
                        ))
                ).getOrElse(Map.of());

        return new Item(
                link,
                getStringParam("Název souboru:", params),
                getSizeInKilobytes("Velikost:", params),
                getDateParam("Datum nahrání:", params),
                getStringParam("Typ souboru:", params),
                getResolution("Rozlišení:", params),
                getLengthInSeconds("Délka:", params),
                getIntParam("Fps:", params),
                getStringParam("Audio kodek:", params)
                );
    }

    private Item.Resolution getResolution(String key, Map<String, String> params) {
        return Option.of(params.get(key))
                .map(res -> res.split("x", 2))
                .flatMap(res -> Try.of(() -> new Item.Resolution(Integer.parseInt(res[0]), Integer.parseInt(res[1]))).toOption())
                .getOrNull();
    }

    private LocalDate getDateParam(String key, Map<String, String> params) {
        return Option.of(params.get(key))
                .flatMap(date -> Try.of(() -> LocalDate.parse(date, DATOID_DATE_FORMAT)).toOption())
                .getOrNull();
    }

    private Integer getIntParam(String key, Map<String, String> params) {
        return Option.of(params.get(key))
                .flatMap(strint -> Try.of(() -> Integer.parseInt(strint)).toOption())
                .getOrNull();
    }

    private String getStringParam(String key, Map<String, String> params) {
        return params.get(key);
    }

    private Long getSizeInKilobytes(String key, Map<String, String> params) {
        return Option.of(params.get(key))
                .flatMap(sizeText -> API.Match(sizeText).option(
                        Case($(size -> size.endsWith(GB)), tryParseFileSize(sizeText, GB, I1024 * I1024 * I1024)),
                        Case($(size -> size.endsWith(MB)), tryParseFileSize(sizeText, MB, I1024 * I1024)),
                        Case($(size -> size.endsWith(KB)), tryParseFileSize(sizeText, KB, I1024)),
                        Case($(size -> size.endsWith(B)), tryParseFileSize(sizeText, B, 1))
                )).getOrNull();
    }

    private Long tryParseFileSize(String sizeText, String unit, int multiplyBy) {
        return Try.of(() -> Double.parseDouble(sizeText.replace(unit, EMPTY)))
                .map(decimalSize -> decimalSize * multiplyBy)
                .map(Double::longValue).getOrNull();
    }

    private Integer getLengthInSeconds(String key, Map<String, String> params) {
        return Option.of(params.get(key))
                .flatMap(time -> Try.of(() -> LocalTime.parse(time, ISO_LOCAL_TIME).toSecondOfDay()).toOption())
                .getOrNull();
    }

}
