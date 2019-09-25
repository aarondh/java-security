package org.daisleyharrison.security.samples.spring.microservices.shared.utilities;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.w3c.dom.*;

public class XmlHelper {
    public static interface NodeFilter extends Function<Node, Boolean> {
    }

    public static class NodeIterator<T extends Node> implements Iterator<T>, Iterable<T> {

        private Node node;
        private Class<T> nodeType;
        private Predicate<Node> filter;
        private T next;

        public NodeIterator(Node node, Class<T> nodeType, Predicate<Node> filter) {
            this.node = node;
            this.nodeType = nodeType;
            this.filter = filter;
        }

        public NodeIterator(Node node, Class<T> nodeType) {
            this.node = node;
            this.nodeType = nodeType;
            this.filter = null;
        }

        @Override
        public boolean hasNext() {
            if (next == null) {
                while (node != null) {
                    if ((filter == null || filter.test(node)) && nodeType.isInstance(node)) {
                        next = nodeType.cast(node);
                        node = node.getNextSibling();
                        break;
                    }
                    node = node.getNextSibling();
                }
            }
            return next != null;
        }

        @Override
        public T next() {
            T result = next;
            next = null;
            return result;
        }

        @Override
        public Iterator<T> iterator() {
            return this;
        }

        public Stream<T> stream() {
            return StreamSupport.stream(this.spliterator(), false);
        }

    }

    public static class NodeFilters {
        public static Predicate<Node> nodeType(short nodeType) {
            return new Predicate<Node>() {

                @Override
                public boolean test(Node node) {
                    return node.getNodeType() == nodeType;
                }

            };
        }

        public static Predicate<Node> element(String tagName) {
            return new Predicate<Node>() {

                @Override
                public boolean test(Node node) {
                    return node.getNodeType() == Node.ELEMENT_NODE
                            && (tagName == null || tagName.equals(node.getNodeName()));
                }

            };
        }

        public static Predicate<Node> element() {
            return element(null);
        }

        public static Predicate<Node> attribute(String name) {
            return new Predicate<Node>() {

                @Override
                public boolean test(Node node) {
                    return node.getNodeType() == Node.ATTRIBUTE_NODE
                            && (name == null || name.equals(node.getNodeName()));
                }

            };
        }

        public static Predicate<Node> attribute() {
            return attribute(null);
        }

        public static Predicate<Node> text() {
            return nodeType(Node.TEXT_NODE);
        }

        public static Predicate<Node> processingInstruction() {
            return nodeType(Node.PROCESSING_INSTRUCTION_NODE);
        }

        public static Predicate<Node> comment() {
            return nodeType(Node.COMMENT_NODE);
        }

        public static Predicate<Node> cdataSection() {
            return nodeType(Node.CDATA_SECTION_NODE);
        }
    }

    public static Iterator<Element> iterate(Node node, String tagName) {
        return new NodeIterator<>(node == null ? null : node.getFirstChild(), Element.class,
                NodeFilters.element(tagName));
    }

    public static Iterator<Element> interate(Node node, String tagName, String childTagName) {
        return iterate(firstChildOf(node, tagName), childTagName);
    }

    public static Iterable<Element> iterable(Node node, String tagName) {
        return new NodeIterator<>(node == null ? null : node.getFirstChild(), Element.class,
                NodeFilters.element(tagName));
    }

    public static Iterable<Element> iterable(Node node, String tagName, String childTagName) {
        return iterable(firstChildOf(node, tagName), childTagName);
    }

    public static Stream<Element> stream(Node node, String tagName) {
        return ((NodeIterator<Element>)iterable(node,tagName)).stream();
    }

    public static Stream<Element> stream(Node node, String tagName, String childTagName) {
        return ((NodeIterator<Element>)iterable(node,tagName,childTagName)).stream();
    }

    public static <T extends Node> T firstChildOf(Node node, Class<T> nodeType, Predicate<Node> filter) {
        NodeIterator<T> iterator = new NodeIterator<>(node == null ? null : node.getFirstChild(), nodeType, filter);
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    public static String textFromFirstChildOf(Element element, String tagName) {
        Node node = firstChildOf(element, Element.class, NodeFilters.element(tagName));
        if (node == null) {
            return null;
        }
        return node.getTextContent();
    }

    private static void innerHTML(Node node, StringBuilder result) {
        Node child = node == null ? null : node.getFirstChild();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                String tagName = child.getNodeName();
                result.append("<");
                result.append(tagName);
                NamedNodeMap attributes = child.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    Node attribute = attributes.item(i);
                    result.append(" ");
                    result.append(attribute.toString());
                }
                result.append(">");
                innerHTML(child, result);
                result.append("</");
                result.append(tagName);
                result.append(">");
            } else if (child.getNodeType() == Node.ATTRIBUTE_NODE) {
            } else {
                result.append(child.getNodeValue());
            }
            child = child.getNextSibling();
        }
    }

    public static Element firstChildOf(Node node, String tagName) {
        return firstChildOf(node, Element.class, NodeFilters.element(tagName));
    }

    public static String htmlOf(Node node) {
        StringBuilder result = new StringBuilder();
        innerHTML(node, result);
        return result.toString();
    }

    public static String htmlFromFirstChildOf(Element element, String tagName) {
        Iterator<Element> iterator = new NodeIterator<>(element == null ? null : element.getFirstChild(), Element.class,
                NodeFilters.element(tagName));
        if (iterator.hasNext()) {
            StringBuilder result = new StringBuilder();
            innerHTML(iterator.next(), result);
            return result.toString();
        }
        return null;
    }

    public static List<String> textForEachChildOf(Element element, String tagName) {
        NodeIterator<Element> iterator = new NodeIterator<>(element == null ? null : element.getFirstChild(),
                Element.class, NodeFilters.element(tagName));
        return iterator.stream().map(node -> node.getTextContent()).collect(Collectors.toList());
    }

    public static List<String> textForEachChildOfChild(Element element, String tagName, String childTagName) {
        Element child = firstChildOf(element, Element.class, NodeFilters.element(tagName));
        if (child != null) {
            return textForEachChildOf(child, childTagName);
        }
        return Collections.<String>emptyList();
    }
}