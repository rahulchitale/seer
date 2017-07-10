package com.lowLevel;

import org.apache.log4j.PatternLayout;

public class OverloadingMethodsForCSVLogs extends PatternLayout {
	public String getHeader() {  
        return "datetime,endpoint, status, avg_response_time_sec" ; 
    }  

}
