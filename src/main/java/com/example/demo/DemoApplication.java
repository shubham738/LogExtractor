package com.example.demo;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

	public static String[] getDates(String filepath){
		String s[]=new String[2];
		BufferedReader reader=null;
		try {
            reader = new BufferedReader(new FileReader(filepath));
            String next, line = reader.readLine();
            for (boolean first = true, last = (line == null); !last; first = false, line = next) {
                last = ((next = reader.readLine()) == null);

                if (first) {
					s[0]=line.substring(0,line.indexOf("Z")+1);
                } else if (last) {s[1]=line.substring(0,line.indexOf("Z")+1);
                } else {
                }
            }
		} catch(Exception ex){ex.printStackTrace();}
		 finally {
            if (reader != null) try { reader.close(); } catch (Exception logOrIgnore) {}
        }
		return s;
	}

    public static TreeSet<String> addFileInSet(Date initialTime, Date finaltime,String path){
        TreeSet<String> treeSet = new TreeSet<>();
		String bytes = null;
		String filepath=path+"\\";
        try {
            bytes = new String(Files.readAllBytes(Paths.get("C:\\Users\\Namita\\Downloads\\demo\\demo\\LogFileNames.txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert bytes != null;
        String[] logLines = bytes.split("\\r?\\n");
		//System.out.println("logLines: "+logLines.length);
        int j=-1;
        for (int i = 0; i<logLines.length; i++) {
			String[] ll = logLines[i].split(" ");
			filepath=path+"\\"+ll[0];
			String[] dates=getDates(filepath);
			Date stime = translateToISO(dates[0]);
			Date etime = translateToISO(dates[1]);
		     if(stime.compareTo(initialTime)<=0 && etime.compareTo(initialTime)>=0 
			   || stime.compareTo(initialTime)>0 && etime.compareTo(finaltime)>=0 
			   || stime.compareTo(initialTime)>0 && etime.compareTo(finaltime)<0){
                treeSet.add(ll[0]);
			}else if(stime.compareTo(initialTime)<=0 && etime.compareTo(initialTime)<=0){continue;}
			else{j = i+1;break;}
        }
        return treeSet;
    }


    public static Integer binary_search(int s,int top,MappedByteBuffer buffer,Date fromTime) {
        int mid;
        while (s < top) {
            mid = s + (top - s) / 2;
            if (readTime(mid, buffer, fromTime) >= 0)
                top = mid;
            else
                s = mid + 1;
        }
        StringBuilder str = new StringBuilder();
        for(int i=s; i<buffer.limit(); i++){
            byte read = buffer.get(i);
            char res = (char)read;
            str.append(res);
            if(res == 'Z') {
                if(str.length()==27)
                    break;
            }
            if(res=='\n') {
                return i+1;
            }
        }
        return s;
    }

	public static Integer readTime(Integer start, MappedByteBuffer buffer, Date fromTime){
        StringBuilder str = new StringBuilder();
        for(Integer i=start; i<buffer.limit(); i++){
            byte read = buffer.get(i);
            char res = (char)read;
            str.append(res);
            if(res == 'Z') {
                if(str.length()==25)
                    break;
            }
            if(res=='\n') {
                str.setLength(0);
            }
        }

        Date matchFounDate = translateToISO(str.toString());
        return matchFounDate.compareTo(fromTime);
    }


    public static Date translateToISO(String stringdate){
        TemporalAccessor temporalAccessor = DateTimeFormatter.ISO_INSTANT.parse(stringdate);
        Instant instant = Instant.from(temporalAccessor);
        return Date.from(instant);
    }

    public static void main(String[] args) {
		String fromTime = null, toTime = null, path = null;
		for(int i = 0; i<args.length; i++) {
            switch (args[i]) {
                case "-f":
                    fromTime = args[i + 1];
                    break;
                case "-t":
                    toTime = args[i + 1];
                    break;
                case "-i":
                    path = args[i + 1];
                    break;
            }
		}

        Date initialTime = translateToISO(fromTime);
        Date finaltime = translateToISO(toTime);
        if(initialTime.compareTo(finaltime)>=0){				
            System.out.println("NOTE: Final Time should be greater then Initial Time");
            return;
        }
        try {
            TreeSet<String> logsFileList = addFileInSet(initialTime, finaltime,path);
            for (String file : logsFileList) {
                RandomAccessFile aFile = new RandomAccessFile(path +"\\"+ file, "r");
                FileChannel chnl = aFile.getChannel();
                long size = chnl.size();
                long i = 170000000, j = 0;
                MappedByteBuffer buffer;
                if (file.equals(logsFileList.first())) {
                    while (i < size) {
                        buffer = chnl.map(FileChannel.MapMode.READ_ONLY, j, 170000000);
                        int v1 = readTime(0, buffer, translateToISO(fromTime));
                        if (v1 >= 0) {
                            break;
                        }
                        j = i;
                        if (size - i >= 170000000) {
                            i += 170000000;
                        } else {
                            i = size;
                            break;
                        }
                    }
                    long initialIndex;
                    if (i == size) {
                        buffer = chnl.map(FileChannel.MapMode.READ_ONLY, j, i - j);
                        initialIndex = binary_search(0, buffer.limit(), buffer, translateToISO(fromTime));
                        buffer = chnl.map(FileChannel.MapMode.READ_ONLY, initialIndex + j, i - (initialIndex + j));
                    } else if (j != 0) {					
                        long pos = j - 170000000;
                        buffer = chnl.map(FileChannel.MapMode.READ_ONLY, pos, 170000000);
                        initialIndex = binary_search(0, 170000000, buffer, translateToISO(fromTime));
                        buffer = chnl.map(FileChannel.MapMode.READ_ONLY, initialIndex + pos, 170000000);
                    } else {					
                        buffer = chnl.map(FileChannel.MapMode.READ_ONLY, 0, size);
                    }
                } else {					
                    buffer = chnl.map(FileChannel.MapMode.READ_ONLY, 0, size);
                }					
                StringBuilder str = new StringBuilder();
                Date t1;int errorCode=0;
                for (int x = 0; x < buffer.limit(); x++) {
                    byte read = buffer.get(x);
                    char res = (char) read;
					str.append(res);
                    if (res == 'Z') {
						t1 = translateToISO(str.toString());
                        if (t1.compareTo(finaltime) > 0 ) {
                            break;
						}
						if(t1.compareTo(initialTime)<0){
							errorCode=1; continue;
						}
                    }
                    if (res == '\n'  ) {
						if(errorCode==0){
						System.out.print(str);}
						errorCode=0;
                        str.setLength(0);
                    }
                }
            }
        }
        catch (IOException e){
            System.out.println(e.getMessage());
            System.exit(0);
        }
      
    }

	
}
