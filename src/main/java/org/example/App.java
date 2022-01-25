package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Hello world!
 *
 *
 */
public class App 
{
    private static final String JAVA_VENDOR = "java.vendor";
    private static final String JAVA_VERSION = "java.version";
    public static void main(String[] args) throws IOException, InterruptedException {

        String vendor = System.getProperties().getProperty("java.vendor");
        String javaSpecificVersion = System.getProperties().getProperty("java.specification.version");
        String javaVersionString = System.getProperties().getProperty("java.version");
        String osName = System.getProperties().getProperty("os.name");
        ArrayList<Integer> userJavaVersion = new ArrayList<>();

        // For java 8 and below java version will be in format 1.8.0_201
        // From java 9 onwards java version is in format 9.0.1
        if(javaSpecificVersion.startsWith("1.")){
            javaSpecificVersion = javaSpecificVersion.substring(2);
            userJavaVersion.add(Integer.parseInt(javaVersionString.substring(2,3)));
            userJavaVersion.add(Integer.parseInt(javaVersionString.substring(4,5)));
            if(javaVersionString.contains("_") && StringUtils.isNotEmpty(javaVersionString.substring(6))){
                userJavaVersion.add(Integer.parseInt(javaVersionString.substring(6)));
            } else {
                userJavaVersion.add(0);
            }
        } else{
           String[] splits = javaVersionString.split("\\.");
            userJavaVersion.add(Integer.parseInt(splits[0]));
            if(splits.length>1) {
                userJavaVersion.add(Integer.parseInt(splits[1]));
            } else {
                userJavaVersion.add(0);
            }
            if(splits.length>2) {
                userJavaVersion.add(Integer.parseInt(splits[2]));
            } else{
                userJavaVersion.add(0);
            }
        }
        Integer javaVersion = Integer.parseInt(javaSpecificVersion);

        if(!vendor.contains("Oracle") && vendor.contains("Azul") && javaVersion>=8){
            System.out.println("System meets the requirements of jdk version");
            return;
        }

        if(javaVersion<8){
            javaVersion = 8;
        }

        List<JdkVersionDTO> javaVersions = getJavaVersions(javaVersion, osName);
        Collections.sort(javaVersions);
        // Collecting all objects matching users java version
        List<JdkVersionDTO> requiredJdkVersions = new ArrayList<>();
        for(JdkVersionDTO jdkVersionDTO: javaVersions){
            if(compareTwoVersions(jdkVersionDTO.getJava_version(), userJavaVersion)){
                requiredJdkVersions.add(jdkVersionDTO);
            }
        }
        // Each java version is having multiple zulu version hence sorting in desecnding order and collecting all urls for latest version
        requiredJdkVersions.sort(getZuluVersionComparator());
        ArrayList<Integer> requiredZuluVersion = requiredJdkVersions.get(0).getZulu_version();
        ArrayList<String> urlsTobeSuggestedToUser = new ArrayList<>();
        for(JdkVersionDTO jdkVersionDTO: requiredJdkVersions){
            if(isZuluVersionsEqual(requiredZuluVersion, jdkVersionDTO.getZulu_version())){
                urlsTobeSuggestedToUser.add(jdkVersionDTO.getUrl());
            } else {
                break;
            }
        }
        System.out.println("Your system contains jdk from vendor which is not suggested. Please download the jdk from given urls and install: ");
        for(String url: urlsTobeSuggestedToUser){
            System.out.println(url);
        }
    }

    private static boolean isZuluVersionsEqual(ArrayList<Integer> requiredZuluVersion, ArrayList<Integer> zulu_version) {
        if(requiredZuluVersion.get(0)==zulu_version.get(0) && requiredZuluVersion.get(1)==zulu_version.get(1) && requiredZuluVersion.get(2)==zulu_version.get(2) && requiredZuluVersion.get(3)==zulu_version.get(3)){
            return true;
        }
        return false;
    }

    private static Comparator<JdkVersionDTO> getZuluVersionComparator() {
        return new Comparator<JdkVersionDTO>() {
            @Override
            public int compare(JdkVersionDTO o1, JdkVersionDTO o2) {
                if(o1.getZulu_version().get(0)>o2.getZulu_version().get(0)){
                    return -1;
                } else if ((o1.getZulu_version().get(0)<o2.getZulu_version().get(0))){
                    return 1;
                } else {
                    if(o1.getZulu_version().get(1)>o2.getZulu_version().get(1)){
                        return -1;
                    } else if ((o1.getZulu_version().get(1)<o2.getZulu_version().get(1))){
                        return 1;
                    } else {
                        if(o1.getZulu_version().get(2)>o2.getZulu_version().get(2)){
                            return -1;
                        } else if ((o1.getZulu_version().get(2)<o2.getZulu_version().get(2))){
                            return 1;
                        } else {
                            if(o1.getZulu_version().get(3)>o2.getZulu_version().get(3)){
                                return -1;
                            } else if ((o1.getZulu_version().get(3)<o2.getZulu_version().get(3))){
                                return 1;
                            } else {
                                return 0;
                            }
                        }
                    }
                }
            }
        };
    }

    private static boolean compareTwoVersions(ArrayList<Integer> jdk_version, ArrayList<Integer> userJdkVersion) {
        if(jdk_version.get(0)==userJdkVersion.get(0) && jdk_version.get(1)==userJdkVersion.get(1) && jdk_version.get(2)==userJdkVersion.get(2)){
            return true;
        }
        return false;
    }

    private static List<JdkVersionDTO> getJavaVersions(Integer javaVersion, String osName) {
        ObjectMapper mapper = new ObjectMapper();
        try (CloseableHttpClient client = HttpClients.createDefault()) {

            HttpGet request = new HttpGet("https://api.azul.com/zulu/download/community/v1.0/bundles");
            URI uri = new URIBuilder(request.getURI())
                    .addParameter("os", getOsName(osName))
                    .addParameter("bundle_type", "jdk")
                    .addParameter("java_version", javaVersion.toString())
                    .addParameter("release_status", "ga")
                    //.addParameter("ext", "zip")
                    .build();
            ((HttpRequestBase) request).setURI(uri);
            List<JdkVersionDTO> response = client.execute(request, httpResponse ->
                    mapper.readValue(httpResponse.getEntity().getContent(), new TypeReference<List<JdkVersionDTO>>(){}));

            return response;
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getOsName(String osName) {
        if(osName.toLowerCase(Locale.ROOT).contains("mac")){
            return "macos";
        }
        if(osName.toLowerCase().contains("windows")){
            return "windows";
        }
        if(osName.toLowerCase().contains("linux")){
            return "linux";
        }
        return null;
    }

}


