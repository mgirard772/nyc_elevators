import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderAddressComponent;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.LatLng;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import eu.bitm.NominatimReverseGeocoding.NominatimReverseGeocodingJAPI;

public class Elevators 
{
	public static int ZIP_CODE_INDEX = 8;
	public static int ELEVATOR_TYPE_INDEX = 10;
	public static int BOROUGH_INDEX = 9;
	public static int LAT_INDEX = 27;
	public static int LONG_INDEX = 28;
	public static String[] ELEVATOR_TYPES = new String[]{	"Dumbwaiter (D)", 
															"Escalator (E)", 
															"Freight (F)", 
															"Handicap Lift (H)", 
															"Manlift (M)", 
															"Passenger Elevator (P)",
															"Private Elevator (T)",
															"Public Elevator (L)",
															"Sidewalk (S)"};
	//Returns an initialized hash table for data collection
	public static Hashtable<String, Integer> initHash(){
		Hashtable<String, Integer> hash = new Hashtable<String, Integer>();
		for(int i=0; i<ELEVATOR_TYPES.length; i++) hash.put(ELEVATOR_TYPES[i], 0);
		return hash;
	}
	
	//Returns a zip code (in String format) for a given pair of latitude and longitude
	public static String getZipCode(Double latitude, Double longitude)
	{
		String zipcode = null;
		try
		{
			LatLng latLng = new LatLng();
	        latLng.setLat(BigDecimal.valueOf(latitude));
	        latLng.setLng(BigDecimal.valueOf(longitude));
	        final Geocoder geocoder = new Geocoder();
	        GeocoderRequest geocoderRequest = new GeocoderRequestBuilder().setLocation(latLng).getGeocoderRequest();
	        GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);
	        List<GeocoderResult> results = geocoderResponse.getResults();
	        List<GeocoderAddressComponent> geList= results.get(0).getAddressComponents();
	        for(int i=0; i<geList.size(); i++)
	        {
	        	String temp = geList.get(i).getLongName();
	        	if(temp.matches("^[0-9]{5}(?:-[0-9]{4})?$"))
	        	{
	        		zipcode = temp;
	        		break;
	        	}
	        }
	        	
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
		System.out.println(zipcode);
		return zipcode;
	}
	
	public static void countByZip(List<String []> list, String zip, Hashtable<String, Integer> hash){
		for(int i = 0; i<list.size(); i++){
			String[] entry = list.get(i);
			if(entry[Elevators.ZIP_CODE_INDEX].equals(zip)) hash.put(entry[ELEVATOR_TYPE_INDEX], hash.get(entry[ELEVATOR_TYPE_INDEX])+1);
		}
	}
	
	public static void printResults(Hashtable<String, Integer> hash){
		int grandTotal = 0;
		for(int i = 0; i<ELEVATOR_TYPES.length; i++){
			System.out.println(ELEVATOR_TYPES[i] + ": " + hash.get(ELEVATOR_TYPES[i]));
			grandTotal+=hash.get(ELEVATOR_TYPES[i]);
		}
		System.out.println("Grand Total: " + grandTotal);
	}
	
	//Resolves zip codes for entries without zip codes using latitude and longitude data
	public static void resolveZipCodes(List<String[]> data)
	{
	    //NominatimReverseGeocodingJAPI nominatim1 = new NominatimReverseGeocodingJAPI();
		int resolved = 0;
	    String[] entry;
	    for(int i = 0; i<data.size(); i++)
	    {
	    	entry = data.get(i);
	    	if(entry[ZIP_CODE_INDEX].equals("0"))
	    	{
	    		try{
	    			//Address temp = nominatim1.getAdress(Double.valueOf(entry[LAT_INDEX]), Double.valueOf(entry[LONG_INDEX]));
	    			//entry[ZIP_CODE_INDEX] = temp.getPostcode();
		    		//System.out.println(temp.getPostcode());
	    			entry[ZIP_CODE_INDEX] = getZipCode(Double.valueOf(entry[LAT_INDEX]), Double.valueOf(entry[LONG_INDEX]));
	    			if(entry[ZIP_CODE_INDEX] != null) resolved++;
	    		}
	    		catch (Exception e){
	    			System.out.println("Missing coordinates at index " + i);
	    		}		
	    	}
	    }
	    System.out.println(resolved+"zip codes resolved.");
	}
	
	public static List<String[]> readData(String file) throws Exception
	{
		CSVReader reader = new CSVReader(new FileReader(file));
		List<String[]> data = reader.readAll();
		reader.close();
		System.out.println(data.size() + " entries read.");
		return data;
	}
	
	public static void trimZips(List<String []> data)
	{
	    int lines = data.size();
	    int zips = 0;
	    
	    for(int i = 1; i < lines; i++)
	    {
	    	String[] entry = data.get(i);
	    	if(entry[ZIP_CODE_INDEX].length() > 5)
	    	{
	    		zips++;
	    		entry[ZIP_CODE_INDEX] = entry[ZIP_CODE_INDEX].substring(0, 5); //Convert to 5-digit zip code
	    	}
	    }
	    System.out.println(zips + " zip codes trimmed.");
	}
	
	public static void saveData(List<String []> data, String filename)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		    CSVWriter resultwriter = new CSVWriter(writer, ',');
		    resultwriter.writeAll(data);
		    resultwriter.close();
		}
		catch (Exception e)
		{
			System.out.println("Error saving "+filename+".");
		}
	}
	
	public static void storeResults(LinkedList<String[]> results, String zip, Hashtable<String, Integer> hash)
	{
		LinkedList<String> temp = new LinkedList<String>();
		temp.add(zip);
		int total = 0;
		for(int i=0; i<hash.size(); i++)
		{
			int value = hash.get(ELEVATOR_TYPES[i]);
			temp.add(Integer.toString(value));
			total+=value;
		}
		temp.add(Integer.toString(total));
		String[] temp2 = temp.toArray(new String[temp.size()]);
		results.add(temp2);
	}
	
	public static void cleanData(List<String[]> data)
	{
		resolveZipCodes(data);
	    trimZips(data);
	    saveData(data, "elevators_resolved1.csv");
	}

	public static void main(String[] args) throws Exception
	{
		//PrintWriter writer = new PrintWriter("zipcodes.txt", "UTF-8");
		//writer.close();
		
		//Read data csv
	    List<String[]> data = readData("elevators_resolved.csv");
	    LinkedList<String[]> results = new LinkedList<String[]>();
	    LinkedList<String> headers = new LinkedList<String>();
	    
	    //Prepare result header information
	    headers.add("Zip Code");
	    headers.addAll(Arrays.asList(ELEVATOR_TYPES));
	    headers.add("Totals");
	    String[] temp = headers.toArray(new String[headers.size()]);
	    results.add(temp);
	    
	    //cleanData(data);
		
	    //Read in zip code queries and store & print results
	    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	    while(true)
	    {
	    	System.out.print("Enter zip code ('q' exits): ");
	    	String zip = br.readLine();
	    	if(zip.equals("q")) break;
	    	Hashtable<String, Integer> hash = initHash();
		    countByZip(data, zip, hash);
		    storeResults(results, zip, hash);
		    printResults(hash);
	    }
	    
	    //Save results
	    saveData(results, "results.csv");
	}
}
