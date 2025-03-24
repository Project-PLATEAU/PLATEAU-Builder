package org.plateaubuilder.validation;

import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CollectionUtil {

    /**
     * Update error map with parent node and error child node
     * @param errorMap existing error map
     * @param parentNode parent node for key
     * @param errorChildNode error child node for value
     */
    public static <T> void updateErrorMap(Map<Node, Set<T>> errorMap, Node parentNode, T errorChildNode) {
        Set<T> errorList = errorMap.getOrDefault(parentNode, new HashSet<>());
        errorList.add(errorChildNode);
        errorMap.put(parentNode, errorList);
    }

    public static <K, V> void updateErrorMap(Map<Node, Map<K, V>> errorMap, Node parentNode, K errorChildNode, V errorChildNodeValue) {
        Map<K, V> errorList = errorMap.getOrDefault(parentNode, new HashMap<>());
        errorList.put(errorChildNode, errorChildNodeValue);
        errorMap.put(parentNode, errorList);
    }
}
