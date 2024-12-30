package org.example;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        String inputFilePath = "C:\\Users\\SKTelecom\\Desktop\\skorea-municipalities-2018-geo.json";  // 원본 JSON 파일 경로
        String outputFilePath = "C:\\Users\\SKTelecom\\Desktop\\skorea-municipalities-2018-decoded-geo.json";  /// 변환된 JSON 파일 경로

        try {
            // JSON 파일 읽기
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(new File(inputFilePath));

            // 유니코드 문자열 변환
            JsonNode transformedNode = decodeUnicode(rootNode);

            // 결과를 파일에 저장
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputFilePath), transformedNode);

            System.out.println("유니코드 변환 완료: " + outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // JSON 데이터를 탐색하며 유니코드 문자열 변환
    private static JsonNode decodeUnicode(JsonNode node) {
        if (node.isTextual()) {
            // 유니코드 문자열을 디코딩
            return decodeUnicodeString(node.asText());
        } else if (node.isObject()) {
            // 객체의 모든 필드를 변환
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                objectNode.set(entry.getKey(), decodeUnicode(entry.getValue()));
            }
            return objectNode;
        } else if (node.isArray()) {
            // 배열의 모든 요소를 변환
            ArrayNode arrayNode = (ArrayNode) node;
            for (int i = 0; i < arrayNode.size(); i++) {
                arrayNode.set(i, decodeUnicode(arrayNode.get(i)));
            }
            return arrayNode;
        }

        // 다른 경우 변경 없이 반환
        return node;
    }

    // 유니코드 문자열 디코딩
    private static JsonNode decodeUnicodeString(String text) {
        Pattern unicodePattern = Pattern.compile("\\\\u([0-9a-fA-F]{4})");
        Matcher matcher = unicodePattern.matcher(text);

        StringBuffer decodedString = new StringBuffer();
        while (matcher.find()) {
            int unicodeValue = Integer.parseInt(matcher.group(1), 16);
            matcher.appendReplacement(decodedString, Character.toString((char) unicodeValue));
        }
        matcher.appendTail(decodedString);

        return new ObjectMapper().valueToTree(decodedString.toString());
    }
}