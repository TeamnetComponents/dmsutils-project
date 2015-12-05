package ro.croco.integration.dms.commons;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import ro.croco.integration.dms.commons.exceptions.TemplateEngineValueException;
import ro.croco.integration.dms.toolkit.MetadataService;

import java.io.StringWriter;
import java.util.*;

/**
 * Created by Lucian.Dragomir on 12/16/2014.
 */

public abstract class TemplateEngine {
    private static Map<TemplateEngineType, TemplateEngine> templateInstances = new HashMap<TemplateEngineType, TemplateEngine>();


    public static enum TemplateEngineType {
        BASIC(TemplateEngineImpl_Basic.class),
        VELOCITY(TemplateEngineImpl_Velocity.class);

        private Class clazz;

        TemplateEngineType(Class clazz) {
            this.clazz = clazz;
        }

        public Class getEngineClass() {
            return clazz;
        }

        private static TemplateEngineType DEFAULT = TemplateEngineType.BASIC;

        public static TemplateEngineType fromValue(String v) {
            if (v == null) {
                v = TemplateEngineType.DEFAULT.name();
            }
            for (TemplateEngineType c : TemplateEngineType.values()) {
                if (c.name().equalsIgnoreCase(v)) {
                    return c;
                }
            }
            throw new IllegalArgumentException(v);
        }

        public static TemplateEngineType getDefault() {
            return DEFAULT;
        }
    }


    public abstract String getValueFromTemplate(String template, Map<String, Object> properties);

    public String getValueFromTemplate(String template, Map<String, Object> properties, boolean throwIfReplaceIncomplete) {
        String content = null;
        content = getValueFromTemplate(template, properties);
        if (throwIfReplaceIncomplete && hasPlaceholders(content)) {
            String errMessage = null;
            //errMessage = "The calculated value still contains placeholders.";
            errMessage = "The calculated value still contains the following placeholders [" + getPlaceholders(template, ",") + "]";
            throw new TemplateEngineValueException(errMessage);
        }
        return content;
    }

    public String getValueFromTemplate(String template, String... itemList) {
        return getValueFromTemplate(template, itemList, false);
    }

    public String getValueFromTemplate(String template, String[] itemList, boolean throwIfReplaceIncomplete) {
        Map<String, Object> itemMap = new HashMap<String, Object>();
        for (int index = 0; index < itemList.length / 2; index++) {
            itemMap.put(itemList[2 * index], itemList[2 * index + 1]);
        }
        return getValueFromTemplate(template, itemMap, throwIfReplaceIncomplete);
    }

    public final String getValueFromTemplate(String template, MetadataService.MetadataProperties properties) {
        return getValueFromTemplate(template, properties.getAsMap(), false);
    }

    public final String getValueFromTemplate(String template, MetadataService.MetadataProperties properties, boolean throwIfReplaceIncomplete) {
        return getValueFromTemplate(template, properties.getAsMap(), throwIfReplaceIncomplete);
    }


    public String getValueFromTemplate(String template, Properties properties) {
        return getValueFromTemplate(template, toMap(properties), false);
    }

    public String getValueFromTemplate(String template, Properties properties, boolean throwIfReplaceIncomplete) {
        return getValueFromTemplate(template, toMap(properties), throwIfReplaceIncomplete);
    }

    public String escapeVariable(String variableName) {
        return "${" + variableName + "}";
    }

    protected static Map<String, Object> toMap(Properties properties) {
        Map context = new HashMap();
        for (Object propertyKey : properties.keySet()) {
            context.put(propertyKey, properties.get(propertyKey));
        }
        return context;
    }


    public boolean hasPlaceholders(String content) {
        boolean containFlag = false;
        if (content != null) {
            int iStart = content.indexOf("${");
            if (iStart >= 0) {
                int iEnd = content.indexOf("}", iStart);
                if (iStart >= 0 && iEnd > iStart) {
                    containFlag = true;
                }
            }
        }
        return containFlag;
    }

    public List<String> getPlaceholders(String template) {
        List<String> placeholderList = new ArrayList<String>();
        int iStart;
        int iEnd;
        int iLastPos = 0;

        while (iLastPos >= 0) {
            iStart = template.indexOf("${", iLastPos);
            iEnd = template.indexOf("}", iStart);
            if (iStart >= 0 && iEnd > iStart) {
                String placeholder = template.substring(iStart + 2, iEnd);
                if (!placeholderList.contains(placeholder)) {
                    placeholderList.add(placeholder);
                }
                iLastPos = iEnd + 1;
            } else {
                iLastPos = -1;
            }
        }
        return placeholderList;
    }

    public String getPlaceholders(String template, String delimiter) {
        String list = "";
        List<String> placeholderList = getPlaceholders(template);
        for (String placeholder : placeholderList) {
            list = list + (list.length() > 0 ? delimiter : "") + placeholder;
        }
        return list;
    }

    public static TemplateEngine getInstance() {
        return getInstance(TemplateEngineType.DEFAULT);
    }

    public static TemplateEngine getInstance(TemplateEngineType templateEngineType) {
        if (!templateInstances.containsKey(templateEngineType)) {
            synchronized (TemplateEngine.class) {
                if (!templateInstances.containsKey(templateEngineType)) {
                    templateInstances.put(templateEngineType, newInstance(templateEngineType));
                }
            }
        }
        return templateInstances.get(templateEngineType);
    }

    public static TemplateEngine newInstance(TemplateEngineType templateEngineType) {
        TemplateEngine templateEngine = null;
        try {
            templateEngine = (TemplateEngine) templateEngineType.getEngineClass().newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return templateEngine;
    }

    //------------------------------------------------------------------------------------------------------------------
    // BASIC IMPLeEMENTATION
    public static class TemplateEngineImpl_Basic extends TemplateEngine {

        @Override
        public String getValueFromTemplate(String template, Map<String, Object> properties) {
            if (template == null) {
                return null;
            }
            String replaced = template;
            for (String key : properties.keySet()) {
                replaced = replaced.replace(escapeVariable(key), properties.get(key).toString());
            }
            return replaced;
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    // VELOCITY IMPLEMENTATION
    public static class TemplateEngineImpl_Velocity extends TemplateEngine {

        public TemplateEngineImpl_Velocity() {
            Velocity.init();
        }

        @Override
        public String getValueFromTemplate(String template, Properties properties) {
            return getValueFromTemplate(template, new VelocityContext(properties));
        }

        @Override
        public String getValueFromTemplate(String template, Map<String, Object> properties) {
            return getValueFromTemplate(template, new VelocityContext(properties));
        }

        private String getValueFromTemplate(String template, VelocityContext properties) {
            //template = "args = #foreach ($arg in $args) $arg #end";
            //avem buba la Velocity ca nu suporta variabile formate decat din
            //alphabetic (a .. z, A .. Z)
            //numeric (0 .. 9)
            //hyphen ("-")
            //underscore ("_")
            if (template == null) {
                return null;
            }
            properties = fixVelocityContext(properties);
            StringWriter writer = new StringWriter();

            //TODO - trebuie verificat ca este threadsafe
            synchronized (this) {
                Velocity.evaluate(properties, writer, "LOG", template);
            }
            return String.valueOf(writer.getBuffer());
        }

        private VelocityContext fixVelocityContext(VelocityContext properties) {
            for (Object propertyName : properties.getKeys()) {
                String propertyNameCurrent = (String) propertyName;
                String propertyNameChecked = propertyNameCurrent.replaceAll("[^a-zA-Z0-9\\-]", "_");
                if (!propertyNameChecked.equals(propertyNameCurrent)) {
                    properties.put(propertyNameChecked, properties.get(propertyNameCurrent));
                    properties.remove(propertyNameCurrent);
                }
            }
            return properties;
        }
    }
}
