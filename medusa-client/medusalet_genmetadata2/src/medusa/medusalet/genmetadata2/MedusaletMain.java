/**
 * 'Generate Metadata Medusalet'
 *
 * - Providing Network Statistics.
 *
 * @modified : Apr. 1st 2012
 * @author   : Xing Xu (xingx@enl.usc.edu)
 **/

package medusa.medusalet.genmetadata2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import android.database.sqlite.SQLiteCursor;
import android.media.ExifInterface;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;
import medusa.mobile.client.MedusaStorageManager;
import medusa.mobile.client.MedusaStorageTextFileAdapter;
import medusa.mobile.client.MedusaTransformFFmpegAdapter2;
import medusa.mobile.client.MedusaUtil;
import medusa.mobile.client.MedusaletBase;
import medusa.mobile.client.MedusaletCBBase;
import medusa.mobile.client.G;

public class MedusaletMain extends MedusaletBase 
{
	private final String TAG = "MedusaGenerateMetadata";
	private final String TAG_LIU = "_____________________";
	private final String CAMERA_DIR = "/DCIM/";
    private final String RECORD_NAME = "MedusaCam_data/MetadataPhoto.txt";
    
	ArrayList<String> resultData;
	
	private long beforeSeconds = 600;   //10 minutes for default setting
		
	private String getFilePath() {
		return G.PATH_SDCARD + G.getDirectoryPathByTag("genmetadata") + MedusaStorageManager.generateTimestampFilename(TAG);
	}
	
	private String genMetadata(String file)
	{
		Random r = new Random();
		return new Integer(r.nextInt(5)).toString();
	}
	
    MedusaletCBBase cbReg = new MedusaletCBBase() { 
    	public void cbPostProcess(Object data, String msg) {
    		if (((String)data).contains(TAG) == true) {
	    		MedusaStorageManager.requestServiceSQLite(TAG, cbGet_Output, "medusadata.db"
	    				, "select path,uid from mediameta where path='" + data + "'");
	    		
	    		MedusaUtil.log(TAG, "* new file [" + data + "] has been created.");
    		}
    	}
    };
    
    MedusaletCBBase cbGet_Output = new MedusaletCBBase() {
    	public void cbPostProcess(Object data, String msg) 
    	{
    		if (data != null) {
        		SQLiteCursor cr = (SQLiteCursor)data;
        		
        		/* send raw video files */
        		if (cr.moveToFirst()) {
        			do {
        				String uid = cr.getString(1 /* column index */);
        				reportUid(uid);
        			} while(cr.moveToNext());
        			
        			cr.close();
        			quitThisMedusalet();
        		}
        		else {
        			MedusaUtil.log(TAG, "! QueryRes: may not have any data.");
        		}
    		}
    		else {
    			MedusaUtil.log(TAG, "! QueryRes: the query has been executed, but data == null ?!");
    		}
    	}
    };
    
    /** Created by Xiaochen 0331 */
    private String getMetadata(String din) {
        /*
            Medusa:         "C2DM_ID", UID, "CEDD(img)", lat, lng, Create_time, File_size
            Cam (meta):     "file_name", cars, faces, blur, "sceneTag, AngleOfView, Light, ACC(3), Mag(3), Bearing(3), Loc&Acc(3)"
         */
    	String [] dataList = din.split(" ");
    	if (dataList.length < 1) {  // file name is at the first
    		Log.i(TAG_LIU, "metadata has too few items! din: " + din);
    		return "error";
    	}
    	
    	String metadata = "\"" + dataList[0] + "\",";		// item 0: file name
    	for (int i = 1; i < dataList.length; i++) {	
    		if (i == 4)                                	// "indoor/outdoor tag"
    			dataList[i] = "\"" + dataList[i] + "\",";	
    		else {                                    	// other metadata
    			if (i == dataList.length - 1)				// last item
    				dataList[i] = dataList[i];
    			else										// float with one value
    				dataList[i] = dataList[i] + ",";
    		}
    		metadata += dataList[i];
    	}
    	Log.i(TAG_LIU, "metadata: " + metadata);
    	return metadata;
    }
	    
    // mod by Xiaochen
    MedusaletCBBase cbGet = new MedusaletCBBase() {
    	public void cbPostProcess(Object data, String msg) 
    	{
    		if (data != null) {
        		SQLiteCursor cr = (SQLiteCursor)data;
        		/* send raw video files */
        		if (cr.moveToFirst()) {
        			do {
        				String uid = cr.getString(0);
        				String type = cr.getString(1);
        				String file = cr.getString(2);
        				String time = cr.getString(3);
        				String lat = cr.getString(5);
        				String lng = cr.getString(4);
        				String size = cr.getString(6);
        				MedusaUtil.log(TAG, "file name: "+file);
        				Log.i(TAG_LIU, "file name: " + file);
        				
        				/*
        				 * get photo metadata
        				 */

        				if (type.equals("image")) {
        					String imageName = file.split("/")[file.split("/").length - 1];
        					String imagePath = Environment.getExternalStorageDirectory() + CAMERA_DIR + RECORD_NAME;
                            /*
                             Medusa:        "C2DM_ID", UID, "CEDD(img)", lat, lng, Create_time, File_size,
                             Cam (meta):    "file_name", cars, faces, blur, "sceneTag, AngleOfView, Light, ACC(3), Mag(3), Bearing(3), Loc&Acc(3)"
                             */
        					String dat = "\"" + G.C2DM_ID + "\"," + 
        								uid + 
        								",\"" + MedusaTransformFFmpegAdapter2.ImgFeature(file) + "\"," + 
        								lat + "," + lng + "," + time + ',' + size + ',' + 
        								getMetadata(readFile(imagePath, imageName));
        					Log.i(TAG_LIU, "dat: " + dat);
        					resultData.add(dat);
        				}

        				/*
        				 * get video metadata - no videos at present
        				 */
                        /*
        				if(type.equals("video")) {
        					String dat = "\"" + G.C2DM_ID + "\"," + 
        								uid + 
        								",\"" + "video_cedd" + "\"," +  // MedusaTransformFFmpegAdapter2.FrameFeature(file, 10)
        								lat+","+lng+"," + time+','+size;
        					Log.i(TAG_LIU, "video metadat upload: " + dat);
        					resultData.add(dat);
        				}
                        */
        			} while(cr.moveToNext());
        			
        			cr.close();
        			MedusaUtil.log(TAG, "file path: "+getFilePath());
        			MedusaStorageTextFileAdapter.write(getFilePath(), resultData, true);
        		}
        		else {
        			resultData.add("none");
        			MedusaStorageTextFileAdapter.write(getFilePath(), resultData, true);
        			MedusaUtil.log(TAG, "! QueryRes: may not have any data.");
        		}
    		}
    		else {
    			MedusaUtil.log(TAG, "! QueryRes: the query has been executed, but data == null ?!");
    		}
    	}
    };
    
	@Override
	public boolean init() 
	{	
		/* Mandatory Operations */
		cbGet.setRunner(runnerInstance);
		cbReg.setRunner(runnerInstance);
		cbGet_Output.setRunner(runnerInstance);
				
		resultData = new ArrayList<String>();
		
		/* Parsing arguments: <config> tag */
		String timeCode = this.getConfigParams("-t");
		if (timeCode != null) beforeSeconds = Integer.parseInt(timeCode);
		
		MedusaUtil.log(TAG, "* time code =" + timeCode);
		
		return true;
	}
	
    @Override
    public boolean run() {
    	MedusaUtil.log(TAG, "* Started.");
    	
    	String start_uid = "";
    	
		String[] input_keys = this.getConfigInputKeys();    	
    	if (input_keys.length > 0) {
			for (int i = 0 ; i < input_keys.length; i++) {
				String content = this.getConfigInputData(input_keys[i]);
				MedusaUtil.log(TAG, "* requested data tag=" + input_keys[i] + " uids= " + content);
				
				start_uid = content;
				break;
			}
		}
    	
    	Time now = new Time();
    	now.setToNow();
    	
		MedusaUtil.log(TAG, "'" + start_uid + "'");
    	//String statement = "select uid,type,path,mtime from mediameta where mtime >=" + Long.toString(earliest) + " and type='image'";
    	String statement = "select uid,type,path,mtime,lat,lng,fsize from mediameta where (type='video' or type='image') and uid>=" + start_uid;
    	MedusaStorageManager.requestServiceSubscribe(TAG, cbReg, "text");
		MedusaStorageManager.requestServiceSQLite(TAG, cbGet, "medusadata.db"
				, statement);
    	MedusaUtil.log(TAG, "* Request process is done. (" + statement + ")");
    	
        return true;
    }

    @Override
    public void exit() {
    	super.exit();
    	
    	MedusaStorageManager.requestServiceUnsubscribe(TAG, cbReg, "text");
    }
    
    // read file: line by line
    // added by Xiaochen
    public String readFile(String filePath, String photoName){
        File tempFile = new File(filePath);
        BufferedReader reader;
        String funcResult = "";
        if (tempFile.exists()){
            try{
                reader = new BufferedReader(new FileReader(tempFile));
                String tempLine;
                while ((tempLine = reader.readLine()) != null) {
                    // Do something here...
                    if(tempLine.contains(photoName)) {
                    	funcResult = tempLine;
                    	break;
                    }
                }
                reader.close();
            } catch (FileNotFoundException e) {
                Log.i(TAG, "file not found!");
                e.printStackTrace();
            } catch (IOException e) {
                Log.i(TAG,"File reading error!");
                e.printStackTrace();
            }
        }
        else{
            Log.i(TAG, "file not found!");
        }
        return funcResult;
    }
}