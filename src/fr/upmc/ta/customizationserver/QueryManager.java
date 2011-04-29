package fr.upmc.ta.customizationserver;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.gmt.modisco.infra.browser.custom.MetamodelView;
import org.eclipse.gmt.modisco.infra.browser.custom.core.CustomizationsCatalog;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * Services de customizations des queries proposé via un WebService Rest
 * @author Charles DUFOUR, Nicolas RIGNAULT
 *
 */
@Path("querymanager")
public class QueryManager {
	
	/**
	 * Classe de l'application rest
	 */
	private final RestApplication restApplication;
	
	/**
	 * Copie du modèle généré côté GWT
	 */
	private Resource model;
	
	/**
	 * Constructeur
	 * @param restApplication information de l'application
	 */
	public QueryManager(RestApplication restApplication)
	{
		this.restApplication = restApplication;
		//model = EcoreFactory.eINSTANCE.createEObject();
	}
	
	/**
	 * Renvoie la chaîne correcte de l'uri
	 * @param src l'uri dont on a echappé les caractères
	 * @return la chaîne correcte
	 */
	private String unescape(String src)
	{
	    src = src.replaceAll("\\$", "/"); //Encodage $
//	    src = src.replaceAll("%3A", ":");
//	    src = src.replaceAll("%40", "@");
//	    src = src.replaceAll("%23", "#");
	    src = src.replace(model.getURI().toString()+"#", "");
	    return src;
	}
	
	/**
	 * Recharge le modèle et renvoie le EObject associé à l'URI
	 * @param uri l'uri de l'EObject
	 * @return le EObject
	 */
	private EObject prepareCustomization(String uri)
	{
		restApplication.LoadModel();

		EObject eObject = model.getEObject(unescape(uri));
		return eObject;
	}
	
	/**
	 * Header XML des réponses des services
	 * @return header XML
	 */
	private StringBuilder xmlHeader()
	{
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		strBuilder.append("\n");
		strBuilder.append("<response>");
		return strBuilder;
	}
	
	/**
	 * Fin de la chaîne XML renvoyée en tant que réponse
	 * @param strBuilder le stringbuilder avec les données actuelles du XML
	 * @return le XML complet de la réponse du service
	 */
	private String xmlFooter(StringBuilder strBuilder)
	{
		strBuilder.append("\n");
		strBuilder.append("</response>");
		return strBuilder.toString();
	}
	
	/**
	 * Renvoie les noms des fichiers de customisations trouvés
	 * @return information
	 */
	@GET
    @Path("/info/customizationcount")
	@Produces(MediaType.TEXT_HTML)
	public String getInfoCustomizationcount()
	{
		
		String message = "<html><head><title>CustomizationCount</title></head><body>";
		if (restApplication.getCustomizationEngineInstance() == null)
		{
			message += "<div>" + restApplication.errorCustomizationEngine + "</div>";
			message += "</body></html>";
			return message;
		}
		
		CustomizationsCatalog catalog = new CustomizationsCatalog();
		int nb = catalog.getAllCustomizations().size();
		if (nb < 1)
			message += "<div>No customization file found</div>";
		else if (nb == 1)
			message += "<div>1 customization file found</div>";
		else
			message += "<div>"+nb + " customization file found</div>";
		
		for (MetamodelView customizationFile : catalog.getAllCustomizations()) {
			message += "<div>";
			message += customizationFile.getName() + " - "+ customizationFile.getMetamodelURI();
			message += "</div>";
		}
		
		message += "</body></html>";
		
		return message;
	}
	
	
	/**
	 * Permet de changer le fichier de customization sélectionné
	 * @return information
	 */
//	@PUT
//  @Path("/info/changecustomizations/{name}")
//	@Produces("text/plain")
//	public Response getInfoCustomizationSelected(@PathParam("name") String name)
//	{
//		String message = "<html><head><title>ChangeCustomization</title></head><body>";
//		if (restApplication.getCustomizationEngineInstance() == null)
//		{
//			message += "<div>" + restApplication.errorCustomizationEngine + "</div>";
//			message += "</body></html>";
//			return Response.status(HttpResponseCodes.SC_OK).entity(message).build();
//		}
//		
//		
//		if (restApplication.selectCustomization(name))
//			message += "<div>Customization file changed to "+name+"</div>";
//		else
//			message += "<div>No customization file name " + name + " in workspace</div>";
//		message += "</body></html>";
//		return Response.status(HttpResponseCodes.SC_OK).entity(message).build();
//	}
	
	/**
	 * Renvoie la customisation de la couleur associée à l'élément du modèle
	 * @param eObject
	 * @return la couleur customisée
	 */
	@GET
	@Path("/getTypeColor/{eObjectUri}")
	@Produces(MediaType.APPLICATION_XML)
	public String getTypeColor(@PathParam("eObjectUri") String eObjectUri) {
		StringBuilder strBuilder = xmlHeader();
		strBuilder.append("<color isNull=");
		try
		{
			EObject eObject = prepareCustomization(eObjectUri);
			Color resultColor = restApplication.getCustomizationEngineInstance().getTypeColor(eObject);
			if (resultColor == null)
				throw new NullPointerException();

			strBuilder.append("'false'");
			strBuilder.append(">");
			strBuilder.append(resultColor);
			
		}catch(NullPointerException e)
		{
			strBuilder.append("'true'");
			strBuilder.append(">");
		}
		strBuilder.append("</color>");
		return xmlFooter(strBuilder);
	}

	/**
	 * Renvoie la customisation de la couleur associée à un attribut de l'élément du modèle
	 * @param name
	 * @param context
	 * @return la couleur customisée
	 */
	@GET
	@Path("/getAttributeColor/{name}_{contextUri}")
	@Produces(MediaType.APPLICATION_XML)
	public String getAttributeColor(@PathParam("name") String name, @PathParam("contextUri") String contextUri) {
		StringBuilder strBuilder = xmlHeader();
		
		
		strBuilder.append("<color isNull=");
		try
		{
			EObject eObject = prepareCustomization(contextUri);
			String resultColor = restApplication.getCustomizationEngineInstance()
			.getAttributeColor(eObject.eClass(), name, eObject).toString();
			
			if (resultColor == null)
				throw new NullPointerException();
			
			strBuilder.append("'false'");
			strBuilder.append(">");
			strBuilder.append(resultColor);
		}catch(NullPointerException e)
		{
			strBuilder.append("'true'");
			strBuilder.append(">");
		}
		strBuilder.append("</color>");
		return xmlFooter(strBuilder);
	}
	
	/**
	 * Renvoie la customisation du Label associée à une référence de l'élément du modèle
	 * @param name
	 * @param context
	 * @return la couleur customisée
	 */
	@GET
	@Path("/getReferenceColor/{name}_{contextUri}")
	@Produces(MediaType.APPLICATION_XML)
	public String getReferenceColor(@PathParam("name") String name, @PathParam("contextUri") String contextUri) {
		StringBuilder strBuilder = xmlHeader();
		
		
		strBuilder.append("<color isNull=");
		try
		{
			EObject eObject = prepareCustomization(contextUri);
			String resultColor = restApplication.getCustomizationEngineInstance()
			.getReferenceColor(EcoreFactory.eINSTANCE.eClass(), name, eObject).toString();
			
			if (resultColor == null)
				throw new NullPointerException();

			strBuilder.append("'false'");
			strBuilder.append(">");
			strBuilder.append(resultColor);
		}catch(NullPointerException e)
		{
			strBuilder.append("'true'");
			strBuilder.append(">");
		}
		strBuilder.append("</color>");
	
		return xmlFooter(strBuilder);
	}
	
	
	/**
	 * Renvoie la customisation du Label associé à l'élément du modèle
	 * @param eObject
	 * @return le label customisé
	 */
	@GET
	@Path("/getTypeLabel/{eObjectUri}")
	@Produces(MediaType.APPLICATION_XML)
	public String getTypeLabel(@PathParam("eObjectUri") String eObjectUri) {
		StringBuilder strBuilder = xmlHeader();
		strBuilder.append("<label isNull=");
		try
		{
			EObject eObject = prepareCustomization(eObjectUri);
			String result = restApplication.getCustomizationEngineInstance().getTypeLabel(eObject);
			if (result == null)
				throw new NullPointerException();

			strBuilder.append("'false'");
			strBuilder.append(">");
			strBuilder.append(result);
			
		}catch(NullPointerException e)
		{
			strBuilder.append("'true'");
			strBuilder.append(">");
		}
		strBuilder.append("</label>");
		return xmlFooter(strBuilder);
	}

	/**
	 * Renvoie la customisation du Label associée à un attribut de l'élément du modèle
	 * @param name
	 * @param context
	 * @return le label customisé
	 */
	@GET
	@Path("/getAttributeLabel/{name}_{contextUri}")
	@Produces(MediaType.APPLICATION_XML)
	public String getAttributeLabel(@PathParam("name") String name, @PathParam("contextUri") String contextUri) {
		StringBuilder strBuilder = xmlHeader();
		
		
		strBuilder.append("<label isNull=");
		try
		{
			EObject eObject = prepareCustomization(contextUri);
			String result = restApplication.getCustomizationEngineInstance()
			.getAttributeLabel(eObject.eClass(), name, eObject);
			
			if (result == null)
				throw new NullPointerException();
			
			strBuilder.append("'false'");
			strBuilder.append(">");
			strBuilder.append(result);
		}catch(NullPointerException e)
		{
			strBuilder.append("'true'");
			strBuilder.append(">");
		}
		strBuilder.append("</label>");
		return xmlFooter(strBuilder);
	}
	
	/**
	 * Renvoie la customisation du Label associée à une référence de l'élément du modèle
	 * @param name
	 * @param context
	 * @return le label customisé
	 */
	@GET
	@Path("/getReferenceLabel/{name}_{contextUri}")
	@Produces(MediaType.APPLICATION_XML)
	public String getReferenceLabel(@PathParam("name") String name, @PathParam("contextUri") String contextUri) {
		StringBuilder strBuilder = xmlHeader();
		
		
		strBuilder.append("<label isNull=");
		try
		{
			EObject eObject = prepareCustomization(contextUri);
			String result = restApplication.getCustomizationEngineInstance()
			.getReferenceLabel(EcoreFactory.eINSTANCE.eClass(), name, eObject);
			
			if (result == null)
				throw new NullPointerException();
			
			strBuilder.append("'false'");
			strBuilder.append(">");
			strBuilder.append(result);
		}catch(NullPointerException e)
		{
			strBuilder.append("'true'");
			strBuilder.append(">");
		}
		strBuilder.append("</label>");
	
		return xmlFooter(strBuilder);
	}
//	@PUT
//	@Path("/isOnline")
//	public String isOnline()
//	{
//		return "true";
//	}

	public void setModel(Resource resource)
	{
		this.model = resource;
	}
	
	public Resource getModel()
	{
		return this.model;
	}
}
