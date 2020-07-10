/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dssToSqlitePlugin;
import com.rma.io.RmaFile;
import hec.TimeSeriesStorage;
import hec.collections.TimeSeriesCollection;
import hec.heclib.dss.DSSPathname;
import hec.timeseries.BlockedRegularIntervalTimeSeries;
import hec.timeseries.TimeSeries;
import hec.timeseries.TimeSeriesIdentifier;
import hec.timeseries.storage.BlockedStorage;
import hec2.model.DataLocation;
import hec2.plugin.model.ComputeOptions;
import hec2.plugin.selfcontained.SelfContainedPluginAlt;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import hec2.wat.client.WatFrame;
import java.util.logging.Level;
import java.util.logging.Logger;
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
            String outputPath = changeExtensionAndName(wco.getDssFilename(),"db", "_sqlite");//need to change the name too "
            //this should only happen once per lifecycle at the end...
            if(wco.getYearsInLifeCycle()== wco.getCurrentEventNumber()){
                //success! do it now (or wait and nothing bad will happen either...)
            }else{
                fr.addMessage("This is not the last event " + wco.getCurrentEventNumber());
                return returnValue;
            }
            if(wco.isFrmCompute()){
                //stochastic
                //in this case, the compute should keep continuing... 
                
            }else{
                fr.addMessage("Asked to run in a non FRA compute failing disgracefully...");
                return false;
            }
            try {
                //convert the lifecycle dss file to a sqlite file!
                hec.JdbcTimeSeriesDatabase dbase = new hec.JdbcTimeSeriesDatabase(outputPath, hec.JdbcTimeSeriesDatabase.CREATION_MODE.OPEN_EXISTING_UPDATE);
                DSSPathname path = new DSSPathname(inputPath);
                //determine relavent information from dss//
                
                
                
                TimeSeriesIdentifier ts_id = new TimeSeriesIdentifier("THISISMYNAME",java.time.Duration.ofMinutes(1),java.time.Duration.ofMinutes(15),"UNITS");//name,interval,duration,units
                TimeSeries ts = new BlockedRegularIntervalTimeSeries(ts_id);
//                    DSSIdentifier dssId = new DSSIdentifier(incomingPath, originalPDC.fullName);
//                    String[] collPaths = DssFileManagerImpl.getDssFileManager().getCollectionList(dssId);
//                    colSize = collPaths == null ? 0 : collPaths.length;
//                    for (int i = 0; i < colSize; i++) {
//                        dssId.setDSSPath(collPaths[i]);
//                        PairedDataContainer Pdc = DssFileManagerImpl.getDssFileManager().readPairedDataContainer(dssId);
//                        path.setCollectionSequence(collectionNumber);
//                        collectionNumber++;
//                        Pdc.fileName = originalPDC.fileName;
//                        Pdc.fullName = path.getPathname();
//                        DssFileManagerImpl.getDssFileManager().write(Pdc);
//                    }
                
            } catch (Exception ex) {
                Logger.getLogger(dssToSqliteAlternative.class.getName()).log(Level.SEVERE, null, ex);
            }

            return returnValue;
        }
        //theoretically, this could mean it is a CWMS compute. 
        return false;
    }
    private String changeExtensionAndName(String f, String newExtension, String additionalNameText) {
        int i = f.lastIndexOf('.');
        String oldPathandName = f.substring(0,i);
        return oldPathandName + additionalNameText + '.' + newExtension;
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
