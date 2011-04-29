package fr.upmc.ta.customizationserver;



import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.BinaryResourceImpl;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

/**
 * 
 * @author Charles Dufour, Nicolas Rignault
 * Permet de manipuler le datastore généré par l'éditeur EMF
 */
public class DataStoreUtil {
	
	/**
	 * Renvoie la ressource contenant le modèle
	 * @param path le chemin vers le datastore
	 * @param appId l'id de l'application
	 * @param version la version de l'application
	 * @param key la clé utilisée pour stocker le modèle
	 * @return une ressource
	 */
	public static Resource getResourceFrom(String path, String appId, String version, String key)
	{
		LocalDatastoreServiceTestConfig config = new LocalDatastoreServiceTestConfig();
		config.setBackingStoreLocation(path);
		config.setNoStorage(false);
		config.setStoreDelayMs(1);
		LocalServiceTestHelper helper = new LocalServiceTestHelper(config);
		helper.setEnvAppId(appId);
		helper.setEnvVersionId(version);
		helper.setUp();
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

		Query query = new Query(key);
		PreparedQuery preparedQuery = ds.prepare(query);
		
		for (Entity entity : preparedQuery.asIterable())
		{
			byte[] bytes = DataStoreUtil.getBytes(entity, ds);
			ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
			Resource resource = new BinaryResourceImpl();
			try {
				resource.load(stream, Collections.EMPTY_MAP);
				org.eclipse.emf.common.util.URI uri = org.eclipse.emf.common.util.URI.createURI((String)entity.getProperty("uri"));
				resource.setURI(uri);
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
			return resource;
		}
		return null;
	}
	
	/**
	 * Renvoie un tableau de bytes de l'entité associée au datastore
	 * @param entity l'entité
	 * @param datastoreService le service associé au datastore
	 * @return un tableau de bytes
	 */
	public static byte[] getBytes(Entity entity, DatastoreService datastoreService)
	  {
	    if (entity.hasProperty("content"))
	    {
	      Blob blob = (Blob)entity.getProperty("content");
	      return blob.getBytes();
	    }
	    else
	    {
	      Query contentBlobsQuery = new Query("ChildBlob", entity.getKey());
	      PreparedQuery preparedContentBlobsQuery = datastoreService.prepare(contentBlobsQuery);
	      Map<Long, byte[]> contents = new TreeMap<Long, byte[]>();
	      int length = 0;
	      for (Entity contentBlobEntity : preparedContentBlobsQuery.asIterable())
	      {
	        byte[] childBytes = ((Blob)contentBlobEntity.getProperty("value")).getBytes();
	        contents.put((Long)contentBlobEntity.getProperty("index"), childBytes);
	        length += childBytes.length;
	      }
	      byte[] bytes = new byte[length];
	      int offset = 0;
	      for (byte[] childBytes : contents.values())
	      {
	        System.arraycopy(childBytes, 0, bytes, offset, childBytes.length);
	        offset += childBytes.length;
	      }
	      return bytes;
	    }
	  }
}
