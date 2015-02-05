package ro.croco.integration.dms.toolkit.jcr;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.nodetype.PropertyDefinitionTemplate;

/**
 * Created by danielp on 6/30/14.
 */
public class JcrRegisterCustomNodetypes {


    private static boolean checkType(NodeTypeManager ntm, String name) {
        try {
            ntm.getNodeType(name);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static void addNtResourceMixinType(Session session) throws RepositoryException {
        NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();

//        if (checkType(nodeTypeManager, "mix:fileAttributes")) {
//            //unregister previous definition
//            nodeTypeManager.unregisterNodeType("mix:fileAttributes");
//        }
        boolean allowUpdate = false;
        if (!checkType(nodeTypeManager, "mix:fileAttributes") || (allowUpdate && checkType(nodeTypeManager, "mix:fileAttributes"))) {
            //create nodetype
            NodeTypeTemplate ntt = nodeTypeManager.createNodeTypeTemplate();
            ntt.setName("mix:fileAttributes");
            ntt.setMixin(true);   //structural
            //fill with properties
            {
                PropertyDefinitionTemplate pdt = nodeTypeManager.createPropertyDefinitionTemplate();
                pdt.setName("fileName");
                pdt.setRequiredType(PropertyType.STRING);
                pdt.setMandatory(false);
                ntt.getPropertyDefinitionTemplates().add(pdt);
            }

            //add properties used by FrontOffice
            //frontUserName
            {
                PropertyDefinitionTemplate pdt = nodeTypeManager.createPropertyDefinitionTemplate();
                pdt.setName("frontUserName");
                pdt.setRequiredType(PropertyType.STRING);
                pdt.setMandatory(false);
                ntt.getPropertyDefinitionTemplates().add(pdt);
            }
            //documentType
            {
                PropertyDefinitionTemplate pdt = nodeTypeManager.createPropertyDefinitionTemplate();
                pdt.setName("documentType");
                pdt.setRequiredType(PropertyType.STRING);
                pdt.setMandatory(false);
                ntt.getPropertyDefinitionTemplates().add(pdt);
            }
            //documentKey
            {
                PropertyDefinitionTemplate pdt = nodeTypeManager.createPropertyDefinitionTemplate();
                pdt.setName("documentKey");
                pdt.setRequiredType(PropertyType.STRING);
                pdt.setMandatory(false);
                ntt.getPropertyDefinitionTemplates().add(pdt);
            }

            //register nodetype
            nodeTypeManager.registerNodeType(ntt, allowUpdate);
            session.save();
        }

        // create a child node defined by the mixin without specifying the
        // node type

    }


}
