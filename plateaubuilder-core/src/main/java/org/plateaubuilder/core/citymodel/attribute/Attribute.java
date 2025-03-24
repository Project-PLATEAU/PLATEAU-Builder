package org.plateaubuilder.core.citymodel.attribute;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Attribute {
    private String namespace;

    private String name;

    private String minOccurs;

    private String maxOccurs;

    private Type type;

    private Type actualType;

    private Attribute parent;

    private List<Attribute> children = new ArrayList<>();

    public Attribute(String namespace, String name, Type type) {
        this(namespace, name, null, null, type, null);
    }

    public Attribute(String namespace, String name, Type type, Type actualType) {
        this(namespace, name, null, null, type, actualType);
    }

    public Attribute(String namespace, String name, String minOccurs, String maxOccurs, Type type) {
        this(namespace, name, minOccurs, maxOccurs, type, null);
    }

    public Attribute(String namespace, String name, String minOccurs, String maxOccurs, Type type, Type actualType) {
        this.namespace = namespace;
        this.name = name;
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs;
        this.type = type;
        this.actualType = actualType;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public String getMinOccursString() {
        return minOccurs;
    }

    public String getMaxOccursString() {
        return maxOccurs;
    }

    public Type getType() {
        return type;
    }

    public Type getActualType() {
        return actualType;
    }

    public Type getActualTypeOrType() {
        return actualType != null ? actualType : type;
    }

    public List<Attribute> getChildren() {
        return children;
    }

    public boolean isMaxOccursUnbounded() {
        return maxOccurs != null && maxOccurs.equals("unbounded");
    }

    public int getMinOccurs() {
        return minOccurs != null && !minOccurs.isEmpty() ? Integer.parseInt(minOccurs) : 1;
    }

    public int getMaxOccurs() {
        return maxOccurs != null && !maxOccurs.isEmpty() ? Integer.parseInt(maxOccurs) : 1;
    }

    public void setMinOccurs(String minOccurs) {
        this.minOccurs = minOccurs;
    }

    public void setMaxOccurs(String maxOccurs) {
        this.maxOccurs = maxOccurs;
    }

    public String getFullName() {
        var sb = new StringBuilder();
        if (parent != null) {
            sb.append(parent.getFullName()).append("_");
        }
        sb.append(String.format("%s:%s", namespace, name));
        return sb.toString();
    }

    public List<String> getLeafFullNames() {
        var fullNames = new ArrayList<String>();
        if (children.isEmpty()) {
            fullNames.add(getFullName());
        } else {
            for (var child : children) {
                fullNames.addAll(child.getLeafFullNames());
            }
        }

        return fullNames;
    }

    public List<Attribute> getLeaves() {
        var attributes = new ArrayList<Attribute>();
        if (children.isEmpty()) {
            attributes.add(this);
        } else {
            for (var child : children) {
                attributes.addAll(child.getLeaves());
            }
        }

        return attributes;
    }

    public void addChild(Attribute child) {
        this.children.add(child);
        child.parent = this;
    }
}
