package fr.upmc.ta.customizationserver;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.gmt.modisco.infra.browser.custom.MetamodelView;
import org.eclipse.gmt.modisco.infra.browser.custom.core.CustomizationsCatalog;
import org.eclipse.gmt.modisco.infra.browser.uicore.internal.customization.CustomizationEngine;

/**
 * Gère les services REST de customizations
 * @author Charles DUFOUR, Nicolas RIGNAULT
 *
 */
public class RestApplication extends Application {
	
	/**
	 * Liste des classes (nécessaire pour Resteasy)
	 */
	private Set<Class<?>> classes;
	
	/**
	 * Liste des singletons (nécessaire pour Resteasy)
	 */
	private Set<Object> singletons;
	
	/**
	 * CustomizationEngine de modisco
	 */
	private CustomizationEngine customizationEngine;
	
	/**
	 * Gestionnaire de queries (communique avec Modisco)
	 */
	private QueryManager queryManager;
	
	public final String errorCustomizationEngine = "Error: customization Engine is null!";
	
	/**
	 * Chemin par défaut vers le datastore
	 */
	public final String datastorePath = "/home/nicolas/Facets4GWT/Library.editor/war/WEB-INF/appengine-generated/local_db.bin";
	
	/**
	 * Application Id par défaut de l'éditeur EMF
	 */
	public final String applicationId = "library-editor";
	
	/**
	 * Version de l'application par défaut de l'éditeur EMF
	 */
	public final String applicationVersion = "1";
	
	/**
	 * Clé par défaut de l'éditeur EMF
	 */
	public final String modelKey = "org.eclipse.emf.ecore.resource";
	
	/**
	 * Initialise les services REST
	 */
	public RestApplication()
	{
		
		try
		{
			this.customizationEngine = getCustomizationEngineInstance();
			this.queryManager = new QueryManager(this);
			
			classes = new HashSet<Class<?>>();
			singletons = new HashSet<Object>();
			classes.add(QueryManager.class);
			singletons.add(queryManager);
			LoadModel();
			
			
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Change le fichier de customization sélectionné
	 * @param name, le nom du nouveau fichier
	 * @return si le changement est effectué
	 */
	public boolean selectCustomization(String name)
	{
		if (customizationEngine == null)
			return false;
		
		CustomizationsCatalog catalog = new CustomizationsCatalog();
		for(MetamodelView customization : catalog.getAllCustomizations())
		{
			if (customization.getName().equals(name))
			{
				customizationEngine.clear();
				customizationEngine.registerCustomization(customization);
				customizationEngine.loadCustomizations();
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Singleton qui initialise le moteur de customizations de Modisco
	 * @return le moteur de customization
	 */
	public CustomizationEngine getCustomizationEngineInstance()
	{
		if (customizationEngine != null)
			return customizationEngine;
		
		CustomizationsCatalog catalog = new CustomizationsCatalog();
		Iterator<MetamodelView> iterator = catalog.getAllCustomizations().iterator();
		
		CustomizationEngine customizationEngine = new CustomizationEngine();
		
		if (!iterator.hasNext())
			return customizationEngine;
		
		MetamodelView metamodelView = iterator.next();
		if (metamodelView == null)
			return null;
		
		customizationEngine.registerCustomization(metamodelView);
		customizationEngine.loadCustomizations();
		return customizationEngine;
	}
	
	/**
	 * Charge le modèle dans la mémoire
	 */
	public void LoadModel()
	{
		if (queryManager == null)
			return;
		
		Resource resource = DataStoreUtil.getResourceFrom(
				datastorePath, 
				applicationId, 
				applicationVersion, 
				modelKey);
		queryManager.setModel(resource);
	}
	
	@Override
	public Set<Class<?>> getClasses() {
		return classes;
	}
	
	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}
	
	public QueryManager getQuerymanager() {
		return this.queryManager;
	}
}
