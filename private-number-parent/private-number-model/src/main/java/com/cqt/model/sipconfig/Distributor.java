package com.cqt.model.sipconfig;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @author linshiqiang
 * @since 2022-12-02 13:55
 * dis组配置
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class Distributor {

    @JsonProperty("configuration")
    private Configuration configuration;

    @Data
    public static class Configuration {

        @JsonProperty("@name")
        private String name;

        @JsonProperty("@description")
        private String description;

        @JsonProperty("lists")
        private Lists lists;
    }

    @Data
    public static class Lists {

        @JsonProperty("list")
        List<NodeList> nodeList;
    }

    @Data
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public static class NodeList {

        @JsonProperty("@total-weight")
        private String totalWeight;

        @JsonProperty("@name")
        private String name;

        @JsonProperty("node")
        private List<Node> node;
    }

    @Data
    public static class Node {

        @JsonProperty("@name")
        private String name;

        @JsonProperty("@weight")
        private String weight;
    }
}
