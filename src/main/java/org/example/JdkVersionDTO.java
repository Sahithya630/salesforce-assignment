package org.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JdkVersionDTO implements Comparable<JdkVersionDTO>{

    private int id;
    private ArrayList<Integer> java_version;
    private ArrayList<Integer> jdk_version;
    private ArrayList<Integer> zulu_version;
    private String name;
    private String url;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<Integer> getJava_version() {
        return java_version;
    }

    public void setJava_version(ArrayList<Integer> java_version) {
        this.java_version = java_version;
    }

    public ArrayList<Integer> getJdk_version() {
        return jdk_version;
    }

    public void setJdk_version(ArrayList<Integer> jdk_version) {
        this.jdk_version = jdk_version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ArrayList<Integer> getZulu_version() {
        return zulu_version;
    }

    public void setZulu_version(ArrayList<Integer> zulu_version) {
        this.zulu_version = zulu_version;
    }

    @Override
    public int compareTo(JdkVersionDTO o) {
        if(this.getJava_version().get(0)>o.getJava_version().get(0)){
            return 1;
        } else if ((this.getJava_version().get(0)<o.getJava_version().get(0))){
            return -1;
        } else {
            if(this.getJava_version().get(1)>o.getJava_version().get(1)){
                return 1;
            } else if ((this.getJava_version().get(1)<o.getJava_version().get(1))){
                return -1;
            } else {
                if(this.getJava_version().get(2)>o.getJava_version().get(2)){
                    return 1;
                } else if ((this.getJava_version().get(2)<o.getJava_version().get(2))){
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    }
}
