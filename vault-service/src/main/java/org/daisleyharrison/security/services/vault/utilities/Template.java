package org.daisleyharrison.security.services.vault.utilities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

public class Template {
    private static final String CONTENT_TEMPLATE_NODE_NAME = ":content";
    private static final String BINARY_CONTENT_TEMPLATE_NODE_NAME = ":binaryContent";
    private static final String ACTION_TEMPLATE_NODE_NAME = ":action";
    private static final String DEFAULT_SECURE_FILE_SUFFIX = ".sec";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public enum Action {
        KEEP, CLEAN, REPLACE, REMOVE
    }

    private JsonNode templateNode;

    private String secureFileSuffix = DEFAULT_SECURE_FILE_SUFFIX;

    public String getSecureFileSuffix() {
        return secureFileSuffix;
    }

    public void setSecureFileSuffix(String secureFileSuffix) {
        this.secureFileSuffix = secureFileSuffix;
    }

    private static Action getNodeAction(JsonNode node, Action defaultAction) {
        JsonNode resetNode = node.path(ACTION_TEMPLATE_NODE_NAME);
        return resetNode.isMissingNode() ? defaultAction : Action.valueOf(resetNode.asText().toUpperCase());
    }

    private static final OpenOption[] OVERRIDE = new OpenOption[] { StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING };

    private String fileToFieldName(File file) {
        String fileName = file.toPath().getFileName().toString();
        if (fileName.endsWith(secureFileSuffix)) {
            return fileName.substring(0, fileName.length() - secureFileSuffix.length());
        } else {
            return fileName;
        }
    }
    private boolean isFileNode(JsonNode node){
        return node.has(CONTENT_TEMPLATE_NODE_NAME) || node.has(BINARY_CONTENT_TEMPLATE_NODE_NAME);
    }
    private void applyFileNode(Path path, String name, JsonNode node, Action action, boolean exists)
            throws IOException {
        if (action == Action.REPLACE || !exists) {
            JsonNode contentNode = node.get(CONTENT_TEMPLATE_NODE_NAME);
            if (contentNode == null) {
                contentNode = node.get(BINARY_CONTENT_TEMPLATE_NODE_NAME);
                if (contentNode == null) {
                    throw new IllegalArgumentException("No content at node " + name);
                }
                // binary content in the form of base64 encoded text
                byte[] content = Base64.getDecoder().decode(contentNode.asText());
                Files.write(path, content, OVERRIDE);
            } else {
                // String content(either straght text or json)
                String content;
                if (contentNode.isContainerNode()) {
                    content = objectMapper.writeValueAsString(contentNode);
                } else {
                    content = contentNode.asText();
                }
                Files.writeString(path, content, OVERRIDE);
            }
        }
    }

    private void applyDirectoryNode(Path path, String name, JsonNode node, Action action, boolean exists)
            throws IOException {
        if (action != Action.KEEP) {
            if (exists) {
                for (File file : path.toFile().listFiles()) {
                    String fieldName = fileToFieldName(file);
                    boolean isNotInTemplate = node.path(fieldName).isMissingNode();
                    if (isNotInTemplate) {
                        if (file.isFile()) {
                            file.delete();
                        } else {
                            // recursively delete the directory
                            Files.walk(file.toPath()).sorted(Comparator.reverseOrder()).map(Path::toFile)
                                    .forEach(File::delete);
                        }
                    }
                }
            }
        }
        Files.createDirectories(path);
    }

    private void applyTemplateNode(Path root, String name, JsonNode node, Action action) throws IOException {
        if (node.isContainerNode()) {
            Path path = name == null ? root : root.resolve(name);
            boolean exists = Files.exists(path);
            action = action == Action.REMOVE ? Action.REMOVE : getNodeAction(node, action);
            if (isFileNode(node)) {
                // this is a file node with content
                applyFileNode(path, name, node, action, exists);
            } else {
                applyDirectoryNode(path, name, node, action, exists);

                for (Iterator<Map.Entry<String, JsonNode>> fields = node.fields(); fields.hasNext();) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    String fieldName = field.getKey();
                    JsonNode fieldNode = field.getValue();
                    if (!fieldName.startsWith(":")) {
                        if(fieldNode.isContainerNode()) {
                            applyTemplateNode(path, fieldName, fieldNode, action);
                        }
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Unexpected template node " + name);
        }
    }

    public Template(InputStream templateStream) throws IOException {
        templateNode = objectMapper.readTree(templateStream);
    }

    public void apply(Path root, Action action) throws IOException {
        applyTemplateNode(root, null, templateNode, action);
    }

}