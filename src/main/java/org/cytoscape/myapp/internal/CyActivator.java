package org.cytoscape.myapp.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.cytoscape.app.CyAppAdapter;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.osgi.framework.BundleContext;

import org.cytoscape.application.swing.CyNodeViewContextMenuFactory;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.TaskManager;

import org.cytoscape.service.util.CyServiceRegistrar;

import org.cytoscape.session.CyNetworkNaming;

public class CyActivator extends AbstractCyActivator {

    private static CyAppAdapter appAdapter;
    private static HashMap<CyNode, Set<String>> nodeProps = new HashMap<>(); //  a node and props for that node
    private static HashMap<String, String> propProps = new HashMap<>(); // prop name to prop ID

    public CyActivator() {
        super();
    }

    @Override
    public void start(BundleContext bc) throws Exception {

        this.appAdapter = getService(bc, CyAppAdapter.class);
        CyApplicationManager cyApplicationManager = getService(bc, CyApplicationManager.class);
        CySwingApplication cyDesktopService = getService(bc, CySwingApplication.class);
        CyServiceRegistrar cyServiceRegistrar = getService(bc, CyServiceRegistrar.class);
        CyNetworkManager cyNetworkManagerServiceRef = getService(bc, CyNetworkManager.class);
        CyNetworkNaming cyNetworkNamingServiceRef = getService(bc, CyNetworkNaming.class);
        CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc, CyNetworkFactory.class);
        TaskManager taskManager = getService(bc, TaskManager.class);
        CyEventHelper eventHelper = getService(bc, CyEventHelper.class);

        // Toolbar menu
        MenuAction action = new MenuAction(cyApplicationManager, cyNetworkManagerServiceRef, "WikiData");
        Properties properties = new Properties();
        registerAllServices(bc, action, properties);

        // Right click menu (node). Query
        CyNodeViewContextMenuFactory myNodeViewContextMenuFactory = new NodeTemplateQueryContextMenuFactory(cyNetworkManagerServiceRef,
                cyNetworkFactoryServiceRef, taskManager, cyApplicationManager, eventHelper);
        Properties myNodeViewContextMenuFactoryProps = new Properties();
        myNodeViewContextMenuFactoryProps.put("preferredMenu", "WikiData");
        myNodeViewContextMenuFactoryProps.setProperty("title", "wikidata title");
        registerAllServices(bc, myNodeViewContextMenuFactory, myNodeViewContextMenuFactoryProps);

        // Right click menu (node). Lookup
        CyNodeViewContextMenuFactory lookupNodeViewContextMenuFactory = new NodeLookupContextMenuFactory(cyNetworkManagerServiceRef,
                cyNetworkFactoryServiceRef, taskManager, cyApplicationManager, eventHelper);
        Properties lookupNodeViewContextMenuFactoryProps = new Properties();
        lookupNodeViewContextMenuFactoryProps.put("preferredMenu", "WikiData");
        lookupNodeViewContextMenuFactoryProps.setProperty("title", "wikidata title");
        registerAllServices(bc, lookupNodeViewContextMenuFactory, lookupNodeViewContextMenuFactoryProps);

        // Right click menu (node). Props
        CyNodeViewContextMenuFactory propNodeViewContextMenuFactory = new NodePropQueryContextMenuFactory(cyNetworkManagerServiceRef,
                cyNetworkFactoryServiceRef, taskManager, cyApplicationManager, eventHelper);
        Properties propNodeViewContextMenuFactoryProps = new Properties();
        propNodeViewContextMenuFactoryProps.put("preferredMenu", "WikiData");
        propNodeViewContextMenuFactoryProps.setProperty("title", "wikidata title");
        registerAllServices(bc, propNodeViewContextMenuFactory, propNodeViewContextMenuFactoryProps);

        // set node visual styles
        SetVisualStyleTask setVisualStyleTask = new SetVisualStyleTask();
        taskManager.execute(setVisualStyleTask.createTaskIterator());

        System.out.println("Started Up");
    }

    public static CyAppAdapter getCyAppAdapter() {
        return appAdapter;
    }

    public static void setNodeProps(CyNode node, Map<String, String> props) {
        Set<String> put = nodeProps.put(node, props.keySet());
        // need to also store the prop label -> prop XXX mapping
        for (String key : props.keySet()) {
            propProps.putIfAbsent(key, props.get(key).replaceFirst("http://www.wikidata.org/prop/", "").replaceFirst("direct/", ""));
        }
        System.out.println("nodeProps: " + nodeProps);
        System.out.println("propProps: " + propProps);
    }

    public static Set<String> getNodeProps(CyNode node) {
        return nodeProps.getOrDefault(node, new HashSet<>());
    }

    public static String getPropID(String prop) {
        return propProps.get(prop);
    }
}

/*
Q511968 cdk2
Q21296321 tryptophanase
Q1147372 cdk1
Q908221 indoleamine

doesnt work: Q416356

*/