/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dssToSqlitePlugin;

import com.rma.io.DssFileManagerImpl;
import hec.heclib.dss.CondensedReference;
import hec.heclib.dss.DSSPathname;
import hec.heclib.dss.HecDSSDataAttributes;
import hec.heclib.util.HecTime;
import hec.io.DSSIdentifier;
import hec.io.TimeSeriesContainer;
import hec.timeseries.BlockedRegularIntervalTimeSeries;
import hec.timeseries.TimeSeries;
import hec.timeseries.TimeSeriesIdentifier;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Q0HECWPL
 */
public class convertDssToSqlite {
    public static boolean convert(String inputPath, String outputPath){
        boolean returnValue = true;
        try {
            //convert the lifecycle dss file to a sqlite file
            try (hec.JdbcTimeSeriesDatabase dbase = new hec.JdbcTimeSeriesDatabase(outputPath, hec.JdbcTimeSeriesDatabase.CREATION_MODE.CREATE_NEW_OR_OPEN_EXISTING_UPDATE);){
                Vector<CondensedReference> paths = DssFileManagerImpl.getDssFileManager().getCondensedCatalog(inputPath);
                for(CondensedReference ref : paths){
                    String[] subPaths = ref.getPathnameList();
                    for(String sP : subPaths){
                        DSSPathname subPath = new DSSPathname(sP);
                        DSSIdentifier eventDss = new DSSIdentifier(inputPath,subPath.getPathname());
                        int type = DssFileManagerImpl.getDssFileManager().getRecordType(eventDss);
                        if((HecDSSDataAttributes.REGULAR_TIME_SERIES<=type && type < HecDSSDataAttributes.PAIRED)){
                            boolean exist = DssFileManagerImpl.getDssFileManager().exists(eventDss);
                            TimeSeriesContainer eventTsc = null;
                            if (!exist )
                            {
                                try
                                {
                                    Thread.sleep(1000);//hey wait a second...
                                }
                                catch (InterruptedException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                            eventTsc = DssFileManagerImpl.getDssFileManager().readTS(eventDss, true);
                            if ( eventTsc != null )
                            {
                                exist = eventTsc.numberValues > 0;
                            }
                            if(exist){
                                //copy over to sqlite
                                TimeSeriesIdentifier ts_id = new TimeSeriesIdentifier(eventTsc.fullName,java.time.Duration.ofMinutes(eventTsc.interval),java.time.Duration.ofMinutes(eventTsc.interval),eventTsc.units);//name,interval,duration,units
                                TimeSeries ts = new BlockedRegularIntervalTimeSeries(ts_id);
                                HecTime t = new HecTime();
                                for(int i = 0; i<eventTsc.numberValues; i++){
                                    t.set(eventTsc.times[i]);
                                    ZonedDateTime zdt = ZonedDateTime.of(t.year(),t.month(),t.day(),t.hour()-1,t.minute(),t.minute(),t.second(),ZoneId.of("GMT-08:00"));
                                    ts.addRow(zdt, eventTsc.values[i]);
                                }
                                dbase.write(ts);
                            }
                        }
                    }
                } 
            }
        } catch (Exception ex) {
            returnValue = false;
            Logger.getLogger(dssToSqliteAlternative.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnValue;
    }
    public static String changeExtensionAndName(String f, String newExtension, String additionalNameText) {
        int i = f.lastIndexOf('.');
        String oldPathandName = f.substring(0,i);
        return oldPathandName + additionalNameText + '.' + newExtension;
    }
}
