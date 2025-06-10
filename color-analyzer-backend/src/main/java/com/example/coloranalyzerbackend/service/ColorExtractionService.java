package com.example.coloranalyzerbackend.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.Base64;

@Service
public class ColorExtractionService {

    // Extreme Simplification: Only hex, no concatenation, no flags.
    private static final Pattern COLOR_PATTERN = Pattern.compile(
        "\\b(?<hex>#(?:[0-9a-fA-F]{6}|[0-9a-fA-F]{3}))\\b"
    );

    private static final Map<String, String> NAMED_COLORS_HEX = new HashMap<>();
    static {
        NAMED_COLORS_HEX.put("ALICEBLUE", "#F0F8FF");
        NAMED_COLORS_HEX.put("ANTIQUEWHITE", "#FAEBD7");
        NAMED_COLORS_HEX.put("AQUA", "#00FFFF");
        NAMED_COLORS_HEX.put("AQUAMARINE", "#7FFFD4");
        NAMED_COLORS_HEX.put("AZURE", "#F0FFFF");
        NAMED_COLORS_HEX.put("BEIGE", "#F5F5DC");
        NAMED_COLORS_HEX.put("BISQUE", "#FFE4C4");
        NAMED_COLORS_HEX.put("BLACK", "#000000");
        NAMED_COLORS_HEX.put("BLANCHEDALMOND", "#FFEBCD");
        NAMED_COLORS_HEX.put("BLUE", "#0000FF");
        NAMED_COLORS_HEX.put("BLUEVIOLET", "#8A2BE2");
        NAMED_COLORS_HEX.put("BROWN", "#A52A2A");
        NAMED_COLORS_HEX.put("BURLYWOOD", "#DEB887");
        NAMED_COLORS_HEX.put("CADETBLUE", "#5F9EA0");
        NAMED_COLORS_HEX.put("CHARTREUSE", "#7FFF00");
        NAMED_COLORS_HEX.put("CHOCOLATE", "#D2691E");
        NAMED_COLORS_HEX.put("CORAL", "#FF7F50");
        NAMED_COLORS_HEX.put("CORNFLOWERBLUE", "#6495ED");
        NAMED_COLORS_HEX.put("CORNSILK", "#FFF8DC");
        NAMED_COLORS_HEX.put("CRIMSON", "#DC143C");
        NAMED_COLORS_HEX.put("CYAN", "#00FFFF");
        NAMED_COLORS_HEX.put("DARKBLUE", "#00008B");
        NAMED_COLORS_HEX.put("DARKCYAN", "#008B8B");
        NAMED_COLORS_HEX.put("DARKGOLDENROD", "#B8860B");
        NAMED_COLORS_HEX.put("DARKGRAY", "#A9A9A9");
        NAMED_COLORS_HEX.put("DARKGREY", "#A9A9A9");
        NAMED_COLORS_HEX.put("DARKGREEN", "#006400");
        NAMED_COLORS_HEX.put("DARKKHAKI", "#BDB76B");
        NAMED_COLORS_HEX.put("DARKMAGENTA", "#8B008B");
        NAMED_COLORS_HEX.put("DARKOLIVEGREEN", "#556B2F");
        NAMED_COLORS_HEX.put("DARKORANGE", "#FF8C00");
        NAMED_COLORS_HEX.put("DARKORCHID", "#9932CC");
        NAMED_COLORS_HEX.put("DARKRED", "#8B0000");
        NAMED_COLORS_HEX.put("DARKSALMON", "#E9967A");
        NAMED_COLORS_HEX.put("DARKSEAGREEN", "#8FBC8F");
        NAMED_COLORS_HEX.put("DARKSLATEBLUE", "#483D8B");
        NAMED_COLORS_HEX.put("DARKSLATEGRAY", "#2F4F4F");
        NAMED_COLORS_HEX.put("DARKSLATEGREY", "#2F4F4F");
        NAMED_COLORS_HEX.put("DARKTURQUOISE", "#00CED1");
        NAMED_COLORS_HEX.put("DARKVIOLET", "#9400D3");
        NAMED_COLORS_HEX.put("DEEPPINK", "#FF1493");
        NAMED_COLORS_HEX.put("DEEPSKYBLUE", "#00BFFF");
        NAMED_COLORS_HEX.put("DIMGRAY", "#696969");
        NAMED_COLORS_HEX.put("DIMGREY", "#696969");
        NAMED_COLORS_HEX.put("DODGERBLUE", "#1E90FF");
        NAMED_COLORS_HEX.put("FIREBRICK", "#B22222");
        NAMED_COLORS_HEX.put("FLORALWHITE", "#FFFAF0");
        NAMED_COLORS_HEX.put("FORESTGREEN", "#228B22");
        NAMED_COLORS_HEX.put("FUCHSIA", "#FF00FF");
        NAMED_COLORS_HEX.put("GAINSBORO", "#DCDCDC");
        NAMED_COLORS_HEX.put("GHOSTWHITE", "#F8F8FF");
        NAMED_COLORS_HEX.put("GOLD", "#FFD700");
        NAMED_COLORS_HEX.put("GOLDENROD", "#DAA520");
        NAMED_COLORS_HEX.put("GRAY", "#808080");
        NAMED_COLORS_HEX.put("GREY", "#808080");
        NAMED_COLORS_HEX.put("GREEN", "#008000");
        NAMED_COLORS_HEX.put("GREENYELLOW", "#ADFF2F");
        NAMED_COLORS_HEX.put("HONEYDEW", "#F0FFF0");
        NAMED_COLORS_HEX.put("HOTPINK", "#FF69B4");
        NAMED_COLORS_HEX.put("INDIANRED", "#CD5C5C");
        NAMED_COLORS_HEX.put("INDIGO", "#4B0082");
        NAMED_COLORS_HEX.put("IVORY", "#FFFFF0");
        NAMED_COLORS_HEX.put("KHAKI", "#F0E68C");
        NAMED_COLORS_HEX.put("LAVENDER", "#E6E6FA");
        NAMED_COLORS_HEX.put("LAVENDERBLUSH", "#FFF0F5");
        NAMED_COLORS_HEX.put("LAWNGREEN", "#7CFC00");
        NAMED_COLORS_HEX.put("LEMONCHIFFON", "#FFFACD");
        NAMED_COLORS_HEX.put("LIGHTBLUE", "#ADD8E6");
        NAMED_COLORS_HEX.put("LIGHTCORAL", "#F08080");
        NAMED_COLORS_HEX.put("LIGHTCYAN", "#E0FFFF");
        NAMED_COLORS_HEX.put("LIGHTGOLDENRODYELLOW", "#FAFAD2");
        NAMED_COLORS_HEX.put("LIGHTGRAY", "#D3D3D3");
        NAMED_COLORS_HEX.put("LIGHTGREY", "#D3D3D3");
        NAMED_COLORS_HEX.put("LIGHTGREEN", "#90EE90");
        NAMED_COLORS_HEX.put("LIGHTPINK", "#FFB6C1");
        NAMED_COLORS_HEX.put("LIGHTSALMON", "#FFA07A");
        NAMED_COLORS_HEX.put("LIGHTSEAGREEN", "#20B2AA");
        NAMED_COLORS_HEX.put("LIGHTSKYBLUE", "#87CEFA");
        NAMED_COLORS_HEX.put("LIGHTSLATEGRAY", "#778899");
        NAMED_COLORS_HEX.put("LIGHTSLATEGREY", "#778899");
        NAMED_COLORS_HEX.put("LIGHTSTEELBLUE", "#B0C4DE");
        NAMED_COLORS_HEX.put("LIGHTYELLOW", "#FFFFE0");
        NAMED_COLORS_HEX.put("LIME", "#00FF00");
        NAMED_COLORS_HEX.put("LIMEGREEN", "#32CD32");
        NAMED_COLORS_HEX.put("LINEN", "#FAF0E6");
        NAMED_COLORS_HEX.put("MAGENTA", "#FF00FF");
        NAMED_COLORS_HEX.put("MAROON", "#800000");
        NAMED_COLORS_HEX.put("MEDIUMAQUAMARINE", "#66CDAA");
        NAMED_COLORS_HEX.put("MEDIUMBLUE", "#0000CD");
        NAMED_COLORS_HEX.put("MEDIUMORCHID", "#BA55D3");
        NAMED_COLORS_HEX.put("MEDIUMPURPLE", "#9370DB");
        NAMED_COLORS_HEX.put("MEDIUMSEAGREEN", "#3CB371");
        NAMED_COLORS_HEX.put("MEDIUMSLATEBLUE", "#7B68EE");
        NAMED_COLORS_HEX.put("MEDIUMSPRINGGREEN", "#00FA9A");
        NAMED_COLORS_HEX.put("MEDIUMTURQUOISE", "#48D1CC");
        NAMED_COLORS_HEX.put("MEDIUMVIOLETRED", "#C71585");
        NAMED_COLORS_HEX.put("MIDNIGHTBLUE", "#191970");
        NAMED_COLORS_HEX.put("MINTCREAM", "#F5FFFA");
        NAMED_COLORS_HEX.put("MISTYROSE", "#FFE4E1");
        NAMED_COLORS_HEX.put("MOCCASIN", "#FFE4B5");
        NAMED_COLORS_HEX.put("NAVAJOWHITE", "#FFDEAD");
        NAMED_COLORS_HEX.put("NAVY", "#000080");
        NAMED_COLORS_HEX.put("OLDLACE", "#FDF5E6");
        NAMED_COLORS_HEX.put("OLIVE", "#808000");
        NAMED_COLORS_HEX.put("OLIVEDRAB", "#6B8E23");
        NAMED_COLORS_HEX.put("ORANGE", "#FFA500");
        NAMED_COLORS_HEX.put("ORANGERED", "#FF4500");
        NAMED_COLORS_HEX.put("ORCHID", "#DA70D6");
        NAMED_COLORS_HEX.put("PALEGOLDENROD", "#EEE8AA");
        NAMED_COLORS_HEX.put("PALEGREEN", "#98FB98");
        NAMED_COLORS_HEX.put("PALETURQUOISE", "#AFEEEE");
        NAMED_COLORS_HEX.put("PALEVIOLETRED", "#DB7093");
        NAMED_COLORS_HEX.put("PAPAYAWHIP", "#FFEFD5");
        NAMED_COLORS_HEX.put("PEACHPUFF", "#FFDAB9");
        NAMED_COLORS_HEX.put("PERU", "#CD853F");
        NAMED_COLORS_HEX.put("PINK", "#FFC0CB");
        NAMED_COLORS_HEX.put("PLUM", "#DDA0DD");
        NAMED_COLORS_HEX.put("POWDERBLUE", "#B0E0E6");
        NAMED_COLORS_HEX.put("PURPLE", "#800080");
        NAMED_COLORS_HEX.put("REBECCAPURPLE", "#663399");
        NAMED_COLORS_HEX.put("RED", "#FF0000");
        NAMED_COLORS_HEX.put("ROSYBROWN", "#BC8F8F");
        NAMED_COLORS_HEX.put("ROYALBLUE", "#4169E1");
        NAMED_COLORS_HEX.put("SADDLEBROWN", "#8B4513");
        NAMED_COLORS_HEX.put("SALMON", "#FA8072");
        NAMED_COLORS_HEX.put("SANDYBROWN", "#F4A460");
        NAMED_COLORS_HEX.put("SEAGREEN", "#2E8B57");
        NAMED_COLORS_HEX.put("SEASHELL", "#FFF5EE");
        NAMED_COLORS_HEX.put("SIENNA", "#A0522D");
        NAMED_COLORS_HEX.put("SILVER", "#C0C0C0");
        NAMED_COLORS_HEX.put("SKYBLUE", "#87CEEB");
        NAMED_COLORS_HEX.put("SLATEBLUE", "#6A5ACD");
        NAMED_COLORS_HEX.put("SLATEGRAY", "#708090");
        NAMED_COLORS_HEX.put("SLATEGREY", "#708090");
        NAMED_COLORS_HEX.put("SNOW", "#FFFAFA");
        NAMED_COLORS_HEX.put("SPRINGGREEN", "#00FF7F");
        NAMED_COLORS_HEX.put("STEELBLUE", "#4682B4");
        NAMED_COLORS_HEX.put("TAN", "#D2B48C");
        NAMED_COLORS_HEX.put("TEAL", "#008080");
        NAMED_COLORS_HEX.put("THISTLE", "#D8BFD8");
        NAMED_COLORS_HEX.put("TOMATO", "#FF6347");
        NAMED_COLORS_HEX.put("TURQUOISE", "#40E0D0");
        NAMED_COLORS_HEX.put("VIOLET", "#EE82EE");
        NAMED_COLORS_HEX.put("WHEAT", "#F5DEB3");
        NAMED_COLORS_HEX.put("WHITE", "#FFFFFF");
        NAMED_COLORS_HEX.put("WHITESMOKE", "#F5F5F5");
        NAMED_COLORS_HEX.put("YELLOW", "#FFFF00");
        NAMED_COLORS_HEX.put("YELLOWGREEN", "#9ACD32");
    }

    private DocumentBuilderFactory createDocumentBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // XXE Protection
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setExpandEntityReferences(false);
        return factory;
    }


    public Map<String, Integer> extractColorsFromUrl(String urlString) throws IOException {
        Map<String, Integer> colorFrequencies = new HashMap<>();
        Map<String, String> cssVariables = new HashMap<>(); // Placeholder for future CSS variable parsing

        Document doc = Jsoup.connect(urlString).get();

        // 1. Inline styles
        Elements styledElements = doc.select("[style]");
        for (Element element : styledElements) {
            extractColorsFromString(element.attr("style"), colorFrequencies, cssVariables);
        }

        // 2. <style> tags
        Elements styleTags = doc.select("style");
        for (Element styleTag : styleTags) {
            extractColorsFromString(styleTag.html(), colorFrequencies, cssVariables);
        }

        // 3. Linked CSS files
        Elements cssLinks = doc.select("link[rel=stylesheet][href]");
        for (Element link : cssLinks) {
            String cssUrl = link.absUrl("href");
            if (cssUrl != null && !cssUrl.isEmpty()) {
                try {
                    String cssContent = fetchContent(cssUrl, false);
                    extractColorsFromString(cssContent, colorFrequencies, cssVariables);
                } catch (IOException e) {
                    System.err.println("Error fetching CSS from " + cssUrl + ": " + e.getMessage());
                }
            }
        }

        // 4. SVG images (linked and inline)
        // Linked SVGs via <img> tags
        Elements imgTags = doc.select("img");
        for (Element imgTag : imgTags) {
            String imgSrc = imgTag.absUrl("src");
            if (imgSrc != null && (imgSrc.toLowerCase().endsWith(".svg") || imgSrc.startsWith("data:image/svg+xml"))) {
                try {
                    String svgContent;
                    if (imgSrc.startsWith("data:image/svg+xml")) {
                        String base64Data = imgSrc.substring(imgSrc.indexOf(",") + 1);
                        svgContent = new String(Base64.getDecoder().decode(base64Data));
                    } else {
                         svgContent = fetchContent(imgSrc, true); // true to ignore content type for SVGs
                    }
                    if (svgContent != null) {
                         extractColorsFromSvgContent(svgContent, colorFrequencies, cssVariables);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing SVG from <img> " + imgSrc + ": " + e.getMessage());
                }
            }
        }

        // Inline SVGs
        Elements svgTags = doc.select("svg");
        for (Element svgTag : svgTags) {
            try {
                extractColorsFromSvgContent(svgTag.outerHtml(), colorFrequencies, cssVariables);
            } catch (Exception e) {
                System.err.println("Error processing inline SVG: " + e.getMessage());
            }
        }


        return colorFrequencies;
    }

    private String fetchContent(String urlString, boolean ignoreContentType) throws IOException {
        try {
            // Basic validation and handling for relative protocols
            URI uri = new URI(urlString);
            if (uri.getScheme() == null) {
                if (urlString.startsWith("//")) {
                    urlString = "http:" + urlString; // Or https, default to http for now
                } else {
                    // Potentially a relative path, Jsoup's absUrl should handle it, but good to be aware
                }
            }
             // For SVGs, we might need to ignore content type to get raw text
            if (ignoreContentType) {
                return Jsoup.connect(urlString).ignoreContentType(true).execute().body();
            } else {
                return Jsoup.connect(urlString).execute().body();
            }
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL syntax: " + urlString, e);
        } catch (IllegalArgumentException e) {
            throw new IOException("URL cannot be null or empty", e);
        }
    }

    public void extractColorsFromSvgContent(String svgContent, Map<String, Integer> colorFrequencies, Map<String, String> cssVariables) {
        try {
            DocumentBuilderFactory factory = createDocumentBuilderFactory();
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document svgDoc = builder.parse(new InputSource(new StringReader(svgContent)));
            extractColorsFromSvgNode(svgDoc.getDocumentElement(), colorFrequencies, cssVariables);
        } catch (Exception e) {
            System.err.println("Error parsing SVG content: " + e.getMessage());
            // Potentially log or handle more gracefully
        }
    }

    private void extractColorsFromSvgNode(Node node, Map<String, Integer> colorFrequencies, Map<String, String> cssVariables) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            NamedNodeMap attributes = node.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                String attrName = attribute.getNodeName().toLowerCase();
                // Common SVG attributes that can contain colors
                if (attrName.equals("fill") || attrName.equals("stroke") || attrName.equals("color") || attrName.equals("stop-color") || attrName.equals("flood-color") || attrName.equals("lighting-color")) {
                    extractColorsFromString(attribute.getNodeValue(), colorFrequencies, cssVariables);
                } else if (attrName.equals("style")) {
                    extractColorsFromString(attribute.getNodeValue(), colorFrequencies, cssVariables);
                }
            }
        }

        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            extractColorsFromSvgNode(children.item(i), colorFrequencies, cssVariables);
        }
    }


    public void extractColorsFromString(String text, Map<String, Integer> colorFrequencies, Map<String, String> cssVariables) {
        if (text == null || text.isEmpty()) {
            return;
        }
        Matcher matcher = COLOR_PATTERN.matcher(text);
        while (matcher.find()) {
            String normalizedColor = normalizeColor(matcher);
            if (normalizedColor != null) {
                colorFrequencies.put(normalizedColor, colorFrequencies.getOrDefault(normalizedColor, 0) + 1);
            }
        }
    }

    private String normalizeColor(Matcher matcher) {
        String hex = matcher.group("hex");
        // All other groups are removed for this test

        if (hex != null) {
            return normalizeHex(hex.toUpperCase());
        }
        // else if (matcher.group("rgb") != null) { // rgb() - This group is removed for now
        //     return String.format("#%02X%02X%02X",
        //             parseColorComponent(matcher.group("r_rgb")), // These inner groups are also removed
        //             parseColorComponent(matcher.group("g_rgb")),
        //             parseColorComponent(matcher.group("b_rgb")));
        // } else if (matcher.group("rgba") != null) { // rgba()
        //     int r = parseColorComponent(matcher.group("r_rgba"));
        //     int g = parseColorComponent(matcher.group("g_rgba"));
        //     int b = parseColorComponent(matcher.group("b_rgba"));
        //     // Alpha is ignored for hex normalization, but could be handled if needed
        //     return String.format("#%02X%02X%02X", r, g, b);
        // } else if (hslH != null || hslaH != null) { // hsl() or hsla()
        //     int h = Integer.parseInt(hslH != null ? matcher.group("h_hsl") : matcher.group("h_hsla"));
        //     int s = Integer.parseInt((hslH != null ? matcher.group("s_hsl") : matcher.group("s_hsla")).replace("%", ""));
        //     int l = Integer.parseInt((hslH != null ? matcher.group("l_hsl") : matcher.group("l_hsla")).replace("%", ""));
        //     // Alpha from hsla is ignored for hex normalization
        //     return hslToHex(h, s, l);
        // } else if (named != null) {
        //     return NAMED_COLORS_HEX.getOrDefault(named.toUpperCase(), null);
        // }
        return null;
    }

    private String normalizeHex(String hex) {
        // Simplified to only handle #RGB and #RRGGBB as per simplified regex
        if (hex.length() == 4) { // #RGB
            return String.format("#%C%C%C%C%C%C", hex.charAt(1), hex.charAt(1), hex.charAt(2), hex.charAt(2), hex.charAt(3), hex.charAt(3));
        }
        // else if (hex.length() == 5) { // #RGBA - removed from simplified regex
        //      return String.format("#%C%C%C%C%C%C", hex.charAt(1), hex.charAt(1), hex.charAt(2), hex.charAt(2), hex.charAt(3), hex.charAt(3)); // Alpha ignored
        // } else if (hex.length() == 9) { // #RRGGBBAA - removed from simplified regex
        //     return hex.substring(0, 7); // Alpha ignored
        // }
        return hex; // #RRGGBB
    }

    private int parseColorComponent(String component) {
        if (component.endsWith("%")) {
            return (int) (Integer.parseInt(component.substring(0, component.length() - 1)) * 2.55);
        }
        return Integer.parseInt(component);
    }

    private String hslToHex(int h, int sPercent, int lPercent) {
        float s = sPercent / 100.0f;
        float l = lPercent / 100.0f;

        float c = (1 - Math.abs(2 * l - 1)) * s;
        float x = c * (1 - Math.abs((h / 60.0f) % 2 - 1));
        float m = l - c / 2;

        float rPrime, gPrime, bPrime;

        if (h < 60) { rPrime = c; gPrime = x; bPrime = 0; }
        else if (h < 120) { rPrime = x; gPrime = c; bPrime = 0; }
        else if (h < 180) { rPrime = 0; gPrime = c; bPrime = x; }
        else if (h < 240) { rPrime = 0; gPrime = x; bPrime = c; }
        else if (h < 300) { rPrime = x; gPrime = 0; bPrime = c; }
        else { rPrime = c; gPrime = 0; bPrime = x; }

        int r = (int) ((rPrime + m) * 255);
        int g = (int) ((gPrime + m) * 255);
        int b = (int) ((bPrime + m) * 255);

        return String.format("#%02X%02X%02X", r, g, b);
    }

    public List<Map<String, Integer>> getTopNColors(Map<String, Integer> colorFrequencies, int n) {
        return colorFrequencies.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(n > 0 ? n : colorFrequencies.size())
                .map(entry -> Map.of(entry.getKey(), entry.getValue())) // Convert each Entry to a single-entry Map
                .collect(Collectors.toList());
    }
}
