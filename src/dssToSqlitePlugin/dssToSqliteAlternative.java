/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dssToSqlitePlugin;
import com.rma.io.RmaFile;
import hec2.model.DataLocation;
import hec2.plugin.model.ComputeOptions;
import hec2.plugin.selfcontained.SelfContainedPluginAlt;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import hec2.wat.client.WatFrame;
/**
 *
 * @author WatPowerUser
 */
public class dssToSqliteAlternative extends SelfContainedPluginAlt{
    private List<DataLocation> _dataLocations = new ArrayList<>();
    private static final String DocumentRoot = "DSSTOSQLITE";
    private static final String AlternativeNameAttribute = "Name";
    private static final String AlternativeDescriptionAttribute = "Desc";
    private ComputeOptions _computeOptions;
    public dssToSqliteAlternative(){
        super();
        _dataLocations = new ArrayList<>();
    }
    public dssToSqliteAlternative(String name){
        this();
        setName(name);
    }
    @Override
    public boolean saveData(RmaFile file){
        if(file!=null){
            Element root = new Element(DocumentRoot);
            root.setAttribute(AlternativeNameAttribute,getName());
            root.setAttribute(AlternativeDescriptionAttribute,getDescription());
            if(_dataLocations!=null){
                saveDataLocations(root,_dataLocations);
            }
            Document doc = new Document(root);
            return writeXMLFile(doc,file);
        }
        return false;
    }
    @Override
    protected boolean loadDocument(org.jdom.Document dcmnt) {
        if(dcmnt!=null){
            org.jdom.Element ele = dcmnt.getRootElement();
            if(ele==null){
                System.out.println("No root element on the provided XML document.");
                return false;   
            }
            if(ele.getName().equals(DocumentRoot)){
                setName(ele.getAttributeValue(AlternativeNameAttribute));
                setDescription(ele.getAttributeValue(AlternativeDescriptionAttribute));
            }else{
                System.out.println("XML document root was imporoperly named.");
                return false;
            }
            if(_dataLocations==null){
                _dataLocations = new ArrayList<>();
            }
            _dataLocations.clear();
            loadDataLocations(ele, _dataLocations);
            setModified(false);
            return true;
        }else{
            System.out.println("XML document was null.");
            return false;
        }
    }
    public List<DataLocation> getOutputDataLocations(){
       //construct output data locations 
	return defaultDataLocations();
    }
    public List<DataLocation> getInputDataLocations(){
        //construct input data locations.
	return defaultDataLocations();
    }
    private List<DataLocation> defaultDataLocations(){
        return _dataLocations;
    }
    public void setComputeOptions(ComputeOptions opts){
        _computeOptions = opts;
    }
    @Override
    public boolean isComputable() {
        return true;
    }
    @Override
    public boolean compute() {
        if(_computeOptions instanceof hec2.wat.model.ComputeOptions){
            boolean returnValue = true;
            hec2.wat.model.ComputeOptions wco = (hec2.wat.model.ComputeOptions)_computeOptions;
            WatFrame fr = hec2.wat.WAT.getWatFrame();
            String inputPath = wco.getDssFilename();//THIS IS A DSS FILE!
            String outputPath = convertDssToSqlite.changeExtensionAndName(wco.getDssFilename(),"db", "_sqlite");
            //this should only happen once per lifecycle at the end...
            //only perform conversion on final event - how does this work with "Run Event" logic?
            if(wco.getEventList().size()!= wco.getCurrentEventNumber()){
                return returnValue;
            }
            //must be an FRA compute to convert a lifecycle DSS file. 
            if(!wco.isFrmCompute()){
                fr.addMessage("Asked to run in a non FRA compute failing disgracefully...");
                return false;
            }
            return convertDssToSqlite.convert(inputPath, outputPath);
        }
        //theoretically, this should mean it is a CWMS compute. 
        return false;
    }

    @Override
    public boolean cancelCompute() {
        return false;
    }
    @Override
    public String getLogFile() {
        return null;
    }
    @Override
    public int getModelCount() {
        return 1;
    }

}
