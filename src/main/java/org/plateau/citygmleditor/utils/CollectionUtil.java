package org.plateau.citygmleditor.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.Node;


public class CollectionUtil {
    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

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
}
