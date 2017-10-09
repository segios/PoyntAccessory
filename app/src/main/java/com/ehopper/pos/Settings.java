package com.ehopper.pos;

/**
 * Created by SStarikov on 1/19/2016.
 */
public class Settings {
    private String url;

    public Settings() {
    }

    public Settings(String boot_url) {
        url = boot_url;

//        , String dynatrace_url
//        dynaTraceUrl = dynatrace_url;
    }

    public String getUrl(){
        return url;
    }

    public void setUrl(String url){
        this.url = url;
    }

//    private String dynaTraceUrl;

    //public String getDynaTraceUrl(){
//        return dynaTraceUrl;
//    }

//    public void setDynaTraceUrl(String dynaTraceUrl){
//        this.dynaTraceUrl = dynaTraceUrl;
//    }

}
